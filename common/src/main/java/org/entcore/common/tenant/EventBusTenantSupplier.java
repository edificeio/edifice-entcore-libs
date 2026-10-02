package org.entcore.common.tenant;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.entcore.common.configuration.EventBusConfigurationSupplierConfiguration;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Asks the tenant of a user over the event bus, the same way as
 * {@link org.entcore.common.configuration.EventBusConfigurationSupplier} does for configuration.
 */
public class EventBusTenantSupplier implements TenantSupplier {
    private static final Logger log = LoggerFactory.getLogger(EventBusTenantSupplier.class);
    private final EventBus eb;
    private final EventBusConfigurationSupplierConfiguration conf;

    public EventBusTenantSupplier(final Vertx vertx, final EventBusConfigurationSupplierConfiguration conf) {
        this.eb = vertx.eventBus();
        this.conf = conf;
    }

    @Override
    public Future<String> getTenantId(String userId, Collection<String> structureIds, String hostname) {
        final Promise<String> promise = Promise.promise();
        this.eb.request(conf.getAddress(), new JsonObject()
                        .put(conf.getRoutingFieldName(), conf.getRoutingValue())
                        .put(conf.getBodyFieldName(), new JsonObject()
                                .put("userId", userId)
                                .put("structureIds", new JsonArray(structureIds == null ? new ArrayList<>() : new ArrayList<>(structureIds)))
                                .put("hostname", hostname).encode())
                )
                .onSuccess(message -> {
                    final JsonObject body = new JsonObject((String) message.body());
                    if (body.getBoolean("success", false)) {
                        promise.complete(body.getString("tenantId", ""));
                    } else {
                        log.error("Failed to get tenant of user " + userId + ":" + body);
                        promise.fail("Failed to get tenant: " + body.getString("errorMsg", body.getString("message")));
                    }
                })
                .onFailure(err -> {
                    log.error("Failed to get tenant of user " + userId, err);
                    promise.fail(err);
                });
        return promise.future();
    }
}
