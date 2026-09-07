package org.entcore.broker.client;

import fr.wseduc.webutils.metrics.HealthCheckProbe;
import fr.wseduc.webutils.metrics.HealthCheckProbeResult;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.entcore.broker.api.dto.NATSResponseDTO;
import org.entcore.broker.api.utils.BrokerProxyUtils;
import org.entcore.broker.listener.ProbeListener;
import org.entcore.broker.listener.ProbeResponseDTO;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static io.vertx.core.Future.succeededFuture;

public class NATSProbe implements HealthCheckProbe {
    private static final Logger log = LoggerFactory.getLogger(NATSProbe.class);
    private Vertx vertx;
    @Override
    public Future<Void> init(Vertx vertx, JsonObject jsonObject) {
        this.vertx = vertx;
        BrokerProxyUtils.addBrokerProxy(new ProbeListener(vertx), vertx);
        return succeededFuture();
    }

    @Override
    public Future<HealthCheckProbeResult> probe() {
        final Promise<HealthCheckProbeResult> promise = Promise.promise();
        vertx.eventBus().request("vertx.hck", new JsonObject().put("data", UUID.randomUUID().toString()).encode().getBytes(StandardCharsets.UTF_8), new DeliveryOptions().setLocalOnly(true), reply -> {
            try {
                if (reply.succeeded()) {
                    final NATSResponseDTO response = Json.decodeValue(reply.result().body().toString(), NATSResponseDTO.class);
                    if (StringUtils.isBlank(response.getErr())) {
                        promise.complete(new HealthCheckProbeResult(getName(), true, null));
                    } else {
                        promise.complete(new HealthCheckProbeResult(getName(), false, new JsonObject().put("error", "Invalid response")));
                    }
                } else {
                    log.error("Error while probing NATS", reply.cause());
                    promise.complete(new HealthCheckProbeResult(getName(), false, new JsonObject().put("error", reply.cause().getMessage())));
                }
            } catch (Exception e) {
                log.error("Error while probing NATS", e);
                promise.complete(new HealthCheckProbeResult(getName(), false, new JsonObject().put("error", e.getMessage())));
            }
        });
        return promise.future();
    }

    @Override
    public String getName() {
        return "nats";
    }

    @Override
    public Vertx getVertx() {
        return vertx;
    }
}
