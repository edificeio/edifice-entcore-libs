package org.entcore.broker.listener;

import io.vertx.core.Vertx;
import org.entcore.broker.api.BrokerListener;

import java.util.UUID;

public class ProbeListener {
    public ProbeListener(final Vertx vertx) {

    }
    @BrokerListener(subject = "vertx.hck", proxy = true)
    public ProbeResponseDTO probe(final ProbeRequestDTO request) {
        return new ProbeResponseDTO(true, request.getData() + " -" +UUID.randomUUID().toString());
    }
}
