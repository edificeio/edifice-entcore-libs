package org.entcore.common.configuration;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.entcore.common.user.UserInfos;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import static io.vertx.core.Future.failedFuture;
import static io.vertx.core.Future.succeededFuture;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class EventBusConfigurationSupplier implements ConfigurationSupplier {
    private static final Logger log = LoggerFactory.getLogger(EventBusConfigurationSupplier.class);
    private final EventBus eb;
    private final EventBusConfigurationSupplierConfiguration conf;
    private final Map<String, String> propertiesCacheByTenant = new HashMap<>();

    public EventBusConfigurationSupplier(final Vertx vertx,
                                         final EventBusConfigurationSupplierConfiguration conf) {
        this.eb = vertx.eventBus();
        this.conf = conf;
    }


    @Override
    public Future<Void> clearCache(String... keys) {
        this.propertiesCacheByTenant.clear();
        return succeededFuture();
    }

    @Override
    public Future<Map<String, String>> getConfigurationStrings(String[] keys, UserInfos userInfos, HttpServerRequest request) {
        final Promise<Map<String, String>> promise = Promise.promise();
        final JsonObject ebRequest = new JsonObject()
                .put("keys", new JsonArray(Arrays.asList(keys)));
        getTenantId(userInfos, request)
        .onComplete(res -> {
            if(res.succeeded()) {
                ebRequest.put("tenantId", res.result());
            } else {
                ebRequest.put("domain", getHostnameFromRequest(request).orElse(""));
            }
            this.eb.request(conf.getAddress(), new JsonObject()
                    .put(conf.getRoutingFieldName(), conf.getRoutingValue())
                    .put(conf.getBodyFieldName(), ebRequest.encode()))
            .onSuccess(message -> {
                final JsonObject body = new JsonObject((String) message.body());
                if (body.getBoolean("success", false)) {
                    final Map<String, String> configMap = new HashMap<>();
                    final JsonArray receivedProperties = body.getJsonArray("properties");
                    for (Object rawProp : receivedProperties) {
                        final JsonObject val = (JsonObject) rawProp;
                        final String key = val.getString("key");
                        final String value;
                        final long ttl;
                        value = val.getString("value");
                        ttl = val.getLong("ttl", -1L);
                        configMap.put(key, value);
                        propertiesCacheByTenant.put(key, value);
                    }
                    promise.complete(configMap);
                } else {
                    log.error("Failed to get configuration of keys " + Arrays.toString(keys) + ":" + body);
                    promise.fail("Failed to get configuration");
                }
            })
            .onFailure(err -> {
                log.error("Failed to get configuration of keys " + Arrays.toString(keys), err);
                promise.fail(err);
            });

        });
        return promise.future();
    }

    private Future<String> getTenantId(final UserInfos userInfos, final HttpServerRequest request) {
        String tenantId = request.getHeader("X-Tenant-Id");
        if(isNotBlank(tenantId)) {
            return succeededFuture(tenantId);
        }
        if(userInfos != null) {
            tenantId = userInfos.getTenantId();
        }
        if(isNotBlank(tenantId)) {
            return succeededFuture(tenantId);
        }
        return failedFuture("not.found");
    }

    private Optional<String> getHostnameFromRequest(final HttpServerRequest request) {
        String val = request.getHeader("Host");
        if(isNotBlank(val)) {
            return Optional.of(val);
        }
        val = request.getHeader("Referer");
        if(isBlank(val)) {
            val = request.getHeader("Origin");
        }
        return Optional.ofNullable(val)
                .map(r -> {
                    try {
                        return new URL(r);
                    } catch (MalformedURLException e) {
                        return null;
                    }
                })
                .map(URL::getHost);
    }
}
