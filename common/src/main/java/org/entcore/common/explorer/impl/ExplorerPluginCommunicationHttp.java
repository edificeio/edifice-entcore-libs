package org.entcore.common.explorer.impl;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.entcore.common.explorer.ExplorerMessage;
import org.entcore.common.explorer.IExplorerPluginCommunication;
import org.entcore.common.explorer.IExplorerPluginMetricsRecorder;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static io.vertx.core.Future.succeededFuture;

public class ExplorerPluginCommunicationHttp implements IExplorerPluginCommunication {
    private final Logger log = LoggerFactory.getLogger(ExplorerPluginCommunicationHttp.class);
    private final Vertx vertx;
    private final String ebAddress;
    private final EventBus eventBus;
    private final long requestTimeout;
    private final IExplorerPluginMetricsRecorder metricsRecorder;
    private final boolean isEnabled;

    public ExplorerPluginCommunicationHttp(final Vertx vertx,
                                           final String ebAddress,
                                           final long requestTimeout,
                                           final IExplorerPluginMetricsRecorder metricsRecorder,
                                           final boolean isEnabled) {
        this.vertx = vertx;
        this.ebAddress = ebAddress;
        this.isEnabled = isEnabled;
        this.eventBus = vertx().eventBus();
        this.requestTimeout = requestTimeout;
        this.metricsRecorder = metricsRecorder;
    }

    @Override
    public Vertx vertx() {
        return vertx;
    }

    @Override
    public Future<Void> waitPending() {
        return Future.failedFuture(new IllegalStateException("ExplorerPluginCommunicationHttp.waitPending.notImplemented"));
    }

    @Override
    public Future<Void> pushMessage(ExplorerMessage message) {
        return pushMessage(newArrayList(message));
    }

    @Override
    public Future<Void> pushMessage(List<ExplorerMessage> messages) {
        if(!isEnabled) {
            return succeededFuture();
        }
        final Promise<Void> promise = Promise.promise();
        this.eventBus.request(ebAddress, Json.encode(messages), new DeliveryOptions().setSendTimeout(this.requestTimeout))
        .onComplete(response -> {
            if(response.succeeded()) {
                final Object rawBody = response.result().body();
                if (rawBody instanceof JsonObject) {
                    final JsonObject body = (JsonObject) rawBody;
                    if ("ok".equalsIgnoreCase(body.getString("status", "error"))) {
                        promise.complete();
                        metricsRecorder.onSendMessageSuccess(messages.size());
                    } else {
                        metricsRecorder.onSendMessageFailure(messages.size());
                        log.warn("Error while calling EUR : " + rawBody);
                        promise.tryFail(body.getString("message"));
                    }
                } else {
                    metricsRecorder.onSendMessageFailure(messages.size());
                    log.warn("Unknown error while calling EUR : " + rawBody);
                    promise.tryFail("unknown.error");
                }
            } else {
                metricsRecorder.onSendMessageFailure(messages.size());
                log.warn("Unknown error while calling EUR", response.cause());
                promise.tryFail(response.cause());
            }
        });
        return promise.future();
    }
}
