package org.entcore.broker.proxy;

import org.entcore.broker.api.BrokerListener;
import org.entcore.broker.api.dto.antivirus.ScanAntivirusRequestDTO;
import org.entcore.broker.api.dto.antivirus.ScanAntivirusResponseDTO;

import io.vertx.core.Future;

public interface AntivirusBrokerListener {
  @BrokerListener(subject = "antivirus.scan", proxy = true)
  Future<ScanAntivirusResponseDTO> scan(ScanAntivirusRequestDTO request);
}
