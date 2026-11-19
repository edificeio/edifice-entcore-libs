package org.entcore.common.archive;

/**
 * Shared naming convention for archive.
 * <p>
 * Example layout for a job:
 * <pre>
 * job42/
 *   Manifest.json
 *   archive.signature
 *   Blog/
 *     post_My post_abc123
 *     Documents/
 *       image_def456.png
 * </pre>
 */
public final class ArchiveConstants {

  public static final String DOCUMENTS_FOLDER = "Documents";
  public static final String MANIFEST_NAME = "Manifest.json";
  public static final String SIGNATURE_NAME = "archive.signature";

  private ArchiveConstants() {
  }
}
