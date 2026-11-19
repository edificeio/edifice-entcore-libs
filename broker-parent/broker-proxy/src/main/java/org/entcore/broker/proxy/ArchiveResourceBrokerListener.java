package org.entcore.broker.proxy;

import io.vertx.core.Future;
import org.entcore.broker.api.BrokerListener;
import org.entcore.broker.api.dto.archive.ExportResourcesRequestDTO;
import org.entcore.broker.api.dto.archive.ExportResourcesResponseDTO;
import org.entcore.broker.api.dto.archive.ImportResourcesRequestDTO;
import org.entcore.broker.api.dto.archive.ImportResourcesResponseDTO;
import org.entcore.broker.api.dto.archive.ListResourceIdsRequestDTO;
import org.entcore.broker.api.dto.archive.ListResourceIdsResponseDTO;
import org.entcore.broker.api.dto.archive.ListSubresourcesRequestDTO;

/**
 * Methods exposes by each applications to expose reading/writing of a user's
 * own resources and attachments
 */
public interface ArchiveResourceBrokerListener {

  @BrokerListener(subject = "archive.resources.list.{application}", proxy = true)
  Future<ListResourceIdsResponseDTO> listResourceIds(ListResourceIdsRequestDTO request);

  /**
   * Lists an app's sub-resources (e.g. blog's posts), scoped to {@code request.getParentIds()} -
   * an app with no sub-resources just returns an empty, cursor-less response.
   */
  @BrokerListener(subject = "archive.resources.subresources.list.{application}", proxy = true)
  Future<ListResourceIdsResponseDTO> listSubresources(ListSubresourcesRequestDTO request);

  @BrokerListener(subject = "archive.resources.export.{application}", proxy = true)
  Future<ExportResourcesResponseDTO> exportResources(ExportResourcesRequestDTO request);

  @BrokerListener(subject = "archive.resources.import.{application}", proxy = true)
  Future<ImportResourcesResponseDTO> importResources(ImportResourcesRequestDTO request);
}
