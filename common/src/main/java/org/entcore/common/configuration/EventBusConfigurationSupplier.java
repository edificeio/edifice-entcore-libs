package org.entcore.common.configuration;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.entcore.common.user.UserInfos;

import java.util.Arrays;
import java.util.Map;

public class EventBusConfigurationSupplier implements ConfigurationSupplier {
    private static final Logger log = LoggerFactory.getLogger(EventBusConfigurationSupplier.class);
    private final EventBus eb;
    private final EventBusConfigurationSupplierConfiguration conf;

    public EventBusConfigurationSupplier(final Vertx vertx,
                                         final EventBusConfigurationSupplierConfiguration conf) {
        this.eb = vertx.eventBus();
        this.conf = conf;
    }


    @Override
    public Future<Map<String, String>> getConfigurationStrings(String[] keys, UserInfos userInfos, HttpServerRequest request) {
        final Promise<Map<String, String>> promise = Promise.promise();
        this.eb.request(conf.getAddress(), new JsonObject()
                        .put(conf.getRoutingFieldName(), conf.getRoutingValue())
                        .put(conf.getBodyFieldName(), new JsonObject()
                            .put("keys", keys)
                            .put("userInfos", userInfos == null ? null : Json.encode(userInfos))
                            .put("request", request == null ? null : Json.encode(request)))
                )
                .onSuccess(message -> {
                    final JsonObject body = (JsonObject) message.body();
                    if(body.getBoolean("success", false)) {
                        final Map<String, String> configMap = new java.util.HashMap<>();
                        for (String key : keys) {
                            if (body.containsKey(key)) {
                                configMap.put(key, body.getString(key));
                            }
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
        return promise.future();
    }
}
