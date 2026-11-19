package org.entcore.broker.proxy;

import io.vertx.core.Future;
import org.entcore.broker.api.BrokerListener;
import org.entcore.broker.api.dto.archive.FinalizeManifestRequestDTO;
import org.entcore.broker.api.dto.archive.FinalizeManifestResponseDTO;
import org.entcore.broker.api.dto.archive.VerifySignatureRequestDTO;
import org.entcore.broker.api.dto.archive.VerifySignatureResponseDTO;

/**
 * Manifest and signature operations exposed by archive module (not app-specific, unlike
 * {@link ArchiveResourceBrokerListener})
 */
public interface ArchiveBrokerListener {

  @BrokerListener(subject = "archive.manifest.finalize", proxy = true)
  Future<FinalizeManifestResponseDTO> finalizeManifest(FinalizeManifestRequestDTO request);

  @BrokerListener(subject = "archive.signature.verify", proxy = true)
  Future<VerifySignatureResponseDTO> verifySignature(VerifySignatureRequestDTO request);
}
