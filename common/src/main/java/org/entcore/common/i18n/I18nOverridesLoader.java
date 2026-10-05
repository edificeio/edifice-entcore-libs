package org.entcore.common.i18n;

import fr.wseduc.webutils.I18n;
import fr.wseduc.webutils.I18nOverrides;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.shareddata.LocalMap;
import org.entcore.broker.api.dto.BaseResponseDTO;
import org.entcore.broker.api.dto.i18n.ApplicationI18nOverridesDTO;
import org.entcore.broker.api.dto.i18n.DomainI18nOverridesDTO;
import org.entcore.broker.api.dto.i18n.I18nOverrideValuesDTO;
import org.entcore.broker.api.dto.i18n.I18nOverridesChangedDTO;
import org.entcore.broker.api.dto.i18n.TenantI18nOverridesDTO;
import org.entcore.broker.api.utils.BrokerProxyUtils;
import org.entcore.broker.proxy.I18nOverridesBrokerListener;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Loads the translation overrides of applications into an {@link I18n}, then reloads them each
 * time they change:
 * <ul>
 *     <li>when the tenant service notifies it (broker subject "tenant.i18n.overrides.changed");</li>
 *     <li>when asked programmatically with {@link #requestReload(Vertx, Collection)}, i.e. a
 *     message published on {@value #RELOAD_ADDRESS}, or with {@link #reload()}.</li>
 * </ul>
 * Reloads are debounced, so that a burst of changes triggers a single fetch. Until they could be
 * fetched, the overrides are retried periodically; a failed reload keeps the previous overrides.
 */
public class I18nOverridesLoader {

    /** Event bus address of the reload requests: {"applications": [...]}, all of them when empty. */
    public static final String RELOAD_ADDRESS = "i18n.overrides.reload";

    private static final Logger log = LoggerFactory.getLogger(I18nOverridesLoader.class);
    private static final long RETRY_DELAY = 30_000L;
    private static final long DEBOUNCE_DELAY = 1_000L;
    private static final String SHARED_MAP = "i18n.overrides";
    private static final String BROKER_LISTENER_REGISTERED = "brokerListenerRegistered";

    private final Vertx vertx;
    private final I18nOverridesSupplier supplier;
    private final I18n target;
    private final long retryDelay;
    private final long debounceDelay;
    private Set<String> applications;
    private MessageConsumer<JsonObject> consumer;
    private long reloadTimer = -1;
    private long retryTimer = -1;
    /** Number of the latest load: the results of older ones arriving late are dropped. */
    private int latestLoad;

    public I18nOverridesLoader(Vertx vertx, I18nOverridesSupplier supplier, I18n target,
                               Collection<String> applications) {
        this(vertx, supplier, target, applications, RETRY_DELAY, DEBOUNCE_DELAY);
    }

    I18nOverridesLoader(Vertx vertx, I18nOverridesSupplier supplier, I18n target, Collection<String> applications,
                        long retryDelay, long debounceDelay) {
        this.vertx = vertx;
        this.supplier = supplier;
        this.target = target;
        this.applications = normalize(applications);
        this.retryDelay = retryDelay;
        this.debounceDelay = debounceDelay;
    }

    /**
     * Creates the loader described by the "i18nOverridesSupplier" object of a module's
     * configuration (see {@link I18nOverridesSupplierFactory}) and starts it.
     *
     * @param target       the I18n the overrides are loaded into
     * @param applications the applications whose overrides are loaded, e.g. "workspace"
     * @return the started loader, null when the platform has no overrides ("static" mode)
     */
    public static Future<I18nOverridesLoader> start(Vertx vertx, JsonObject config, I18n target,
                                                    Collection<String> applications) {
        return I18nOverridesSupplierFactory.createI18nOverridesSupplier(vertx, config).map(supplier -> {
            if (supplier instanceof StaticI18nOverridesSupplier) {
                return null;
            }
            final I18nOverridesLoader loader = new I18nOverridesLoader(vertx, supplier, target, applications);
            loader.start();
            return loader;
        });
    }

    /**
     * Asks every loader concerned, in every instance of the platform, to reload its overrides.
     *
     * @param applications the applications whose overrides changed, all of them when empty
     */
    public static void requestReload(Vertx vertx, Collection<String> applications) {
        vertx.eventBus().publish(RELOAD_ADDRESS, new JsonObject().put("applications",
                new JsonArray(applications == null ? Collections.emptyList() : new java.util.ArrayList<>(applications))));
    }

    /** Loads the overrides, then listens to their changes. */
    public synchronized Future<Void> start() {
        if (consumer == null) {
            consumer = vertx.eventBus().consumer(RELOAD_ADDRESS, message -> {
                if (concerns(message.body())) {
                    reload();
                }
            });
            registerBrokerListenerOnce(vertx);
        }
        return load();
    }

    public synchronized void stop() {
        if (consumer != null) {
            consumer.unregister();
            consumer = null;
        }
        cancelTimers();
    }

    /** Reloads the overrides soon, once for a burst of calls. */
    public synchronized void reload() {
        if (reloadTimer != -1) {
            vertx.cancelTimer(reloadTimer);
        }
        reloadTimer = vertx.setTimer(debounceDelay, timerId -> {
            synchronized (this) {
                reloadTimer = -1;
            }
            load();
        });
    }

    /** Changes the applications whose overrides are loaded, reloading them if they changed. */
    public synchronized void setApplications(Collection<String> applications) {
        final Set<String> normalized = normalize(applications);
        if (!normalized.equals(this.applications)) {
            this.applications = normalized;
            reload();
        }
    }

    public synchronized Set<String> getApplications() {
        return applications;
    }

    /** Fetches the overrides now; retried later on failure. */
    public Future<Void> load() {
        final int load;
        final Set<String> loadedApplications;
        synchronized (this) {
            load = ++latestLoad;
            loadedApplications = applications;
        }
        return supplier.getOverrides(loadedApplications)
                .<Void>map(overrides -> {
                    synchronized (this) {
                        if (load == latestLoad) {
                            target.setOverrides(toOverrides(overrides));
                            if (retryTimer != -1) {
                                vertx.cancelTimer(retryTimer);
                                retryTimer = -1;
                            }
                            log.info("Loaded the translation overrides of " + loadedApplications);
                        }
                    }
                    return null;
                })
                .onFailure(err -> scheduleRetry(load, loadedApplications, err));
    }

    private synchronized void scheduleRetry(int load, Set<String> failedApplications, Throwable err) {
        if (load != latestLoad || retryTimer != -1 || consumer == null) {
            return;
        }
        log.warn("Failed to load the translation overrides of " + failedApplications + ", retrying in "
                + retryDelay + " ms: " + err.getMessage());
        retryTimer = vertx.setTimer(retryDelay, timerId -> {
            synchronized (this) {
                retryTimer = -1;
            }
            load();
        });
    }

    private synchronized boolean concerns(JsonObject reloadRequest) {
        final JsonArray requested = reloadRequest == null ? null : reloadRequest.getJsonArray("applications");
        if (requested == null || requested.isEmpty()) {
            return true;
        }
        for (Object application : requested) {
            if (application instanceof String && applications.contains(((String) application).toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private void cancelTimers() {
        if (reloadTimer != -1) {
            vertx.cancelTimer(reloadTimer);
            reloadTimer = -1;
        }
        if (retryTimer != -1) {
            vertx.cancelTimer(retryTimer);
            retryTimer = -1;
        }
    }

    /**
     * Merges the overrides of the applications into one snapshot, in their order. Applications are
     * expected to override distinct keys: an overlap is logged.
     */
    static I18nOverrides toOverrides(List<ApplicationI18nOverridesDTO> overrides) {
        final I18nOverrides.Builder builder = I18nOverrides.builder();
        final Map<String, String> applicationByKey = new HashMap<>();
        for (ApplicationI18nOverridesDTO application : overrides) {
            if (application.getTenants() != null) {
                for (TenantI18nOverridesDTO tenant : application.getTenants()) {
                    for (I18nOverrideValuesDTO values : nonNull(tenant.getValues())) {
                        checkOverlap(applicationByKey, application.getApplication(), "tenant " + tenant.getTenantId(), values);
                        builder.addTenantOverrides(tenant.getTenantId(), values.getLanguage(), values.getTheme(),
                                toJson(values.getTranslations()));
                    }
                }
            }
            if (application.getDomains() != null) {
                for (DomainI18nOverridesDTO domain : application.getDomains()) {
                    for (I18nOverrideValuesDTO values : nonNull(domain.getValues())) {
                        checkOverlap(applicationByKey, application.getApplication(), "domain " + domain.getDomain(), values);
                        builder.addDomainOverrides(domain.getDomain(), values.getLanguage(), values.getTheme(),
                                toJson(values.getTranslations()));
                    }
                }
            }
        }
        return builder.build();
    }

    private static void checkOverlap(Map<String, String> applicationByKey, String application, String scope,
                                     I18nOverrideValuesDTO values) {
        if (values.getTranslations() == null) {
            return;
        }
        for (String key : values.getTranslations().keySet()) {
            final String overriddenKey = scope + "|" + values.getLanguage() + "|" + values.getTheme() + "|" + key;
            final String previous = applicationByKey.put(overriddenKey, application);
            if (previous != null && !previous.equals(application)) {
                log.warn("Applications " + previous + " and " + application + " both override " + key + " for "
                        + scope + ": using the one of " + application);
            }
        }
    }

    private static JsonObject toJson(Map<String, String> translations) {
        return translations == null ? new JsonObject() : new JsonObject(new HashMap<String, Object>(translations));
    }

    private static <T> List<T> nonNull(List<T> list) {
        return list == null ? Collections.<T>emptyList() : list;
    }

    private static Set<String> normalize(Collection<String> applications) {
        final Set<String> normalized = new LinkedHashSet<>();
        if (applications != null) {
            for (String application : applications) {
                if (application != null && !application.trim().isEmpty()) {
                    normalized.add(application.trim().toLowerCase(Locale.ROOT));
                }
            }
        }
        return Collections.unmodifiableSet(normalized);
    }

    /**
     * Relays the changes notified by the tenant service to every loader. Registered once per Vert.x
     * instance: the broker delivers each notification to a single consumer of the instance, which
     * publishes it to all loaders of the platform.
     */
    private static void registerBrokerListenerOnce(Vertx vertx) {
        final LocalMap<String, Boolean> registrations = vertx.sharedData().getLocalMap(SHARED_MAP);
        if (registrations.putIfAbsent(BROKER_LISTENER_REGISTERED, true) == null) {
            BrokerProxyUtils.addBrokerProxy(new ChangesListener(vertx), vertx);
        }
    }

    public static class ChangesListener implements I18nOverridesBrokerListener {
        private final Vertx vertx;

        ChangesListener(Vertx vertx) {
            this.vertx = vertx;
        }

        @Override
        public Future<BaseResponseDTO> onI18nOverridesChanged(I18nOverridesChangedDTO notification) {
            requestReload(vertx, notification == null ? null : notification.getApplications());
            return Future.succeededFuture(new BaseResponseDTO(true, null));
        }
    }
}
