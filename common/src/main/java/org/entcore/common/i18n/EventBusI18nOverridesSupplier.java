package org.entcore.common.i18n;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.entcore.broker.api.dto.i18n.ApplicationI18nOverridesDTO;
import org.entcore.broker.api.dto.i18n.GetI18nOverridesResponseDTO;
import org.entcore.common.configuration.EventBusConfigurationSupplierConfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Asks the translation overrides over the event bus, the same way as
 * {@link org.entcore.common.configuration.EventBusConfigurationSupplier} does for configuration.
 */
public class EventBusI18nOverridesSupplier implements I18nOverridesSupplier {
    private static final Logger log = LoggerFactory.getLogger(EventBusI18nOverridesSupplier.class);
    private final EventBus eb;
    private final EventBusConfigurationSupplierConfiguration conf;
    private final DeliveryOptions deliveryOptions;

    public EventBusI18nOverridesSupplier(final Vertx vertx, final EventBusConfigurationSupplierConfiguration conf,
                                         final long timeout) {
        this.eb = vertx.eventBus();
        this.conf = conf;
        this.deliveryOptions = new DeliveryOptions().setSendTimeout(timeout);
    }

    @Override
    public Future<List<ApplicationI18nOverridesDTO>> getOverrides(Set<String> applications) {
        final Promise<List<ApplicationI18nOverridesDTO>> promise = Promise.promise();
        this.eb.request(conf.getAddress(), new JsonObject()
                        .put(conf.getRoutingFieldName(), conf.getRoutingValue())
                        .put(conf.getBodyFieldName(), new JsonObject()
                                .put("applications", new JsonArray(new ArrayList<>(applications))).encode()),
                        deliveryOptions)
                .onSuccess(message -> {
                    final GetI18nOverridesResponseDTO response;
                    try {
                        response = Json.decodeValue((String) message.body(), GetI18nOverridesResponseDTO.class);
                    } catch (Exception e) {
                        log.error("Invalid translation overrides of " + applications + ": " + message.body(), e);
                        promise.fail(e);
                        return;
                    }
                    if (response.isSuccess()) {
                        promise.complete(response.getApplications() == null
                                ? Collections.emptyList() : response.getApplications());
                    } else {
                        log.error("Failed to get the translation overrides of " + applications + ": " + response.getErrorMsg());
                        promise.fail("Failed to get translation overrides: " + response.getErrorMsg());
                    }
                })
                .onFailure(err -> {
                    log.error("Failed to get the translation overrides of " + applications, err);
                    promise.fail(err);
                });
        return promise.future();
    }
}
