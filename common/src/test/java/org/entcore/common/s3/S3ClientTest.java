package org.entcore.common.s3;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class S3ClientTest {

	/**
	 * A key holding characters to encode. {@code URLEncoder} used to write the space as {@code +}, storing a
	 * literally wrong key, and to disagree with the server on {@code ~} — SignatureDoesNotMatch.
	 */
	@Test
	public void encodeUrlPathKeepsSeparatorsAndPercentEncodesTheRest() {
		assertEquals("dossier%20priv%C3%A9/rapport%20%28v2%29.pdf",
				S3Client.encodeUrlPath("dossier privé/rapport (v2).pdf"));
		assertEquals("a%20b.pdf", S3Client.encodeUrlPath("a b.pdf"));
		assertEquals("~", S3Client.encodeUrlPath("~"));
	}

	/**
	 * Why pre-encoding in {@code writeFromFileSystem(s3Path, fsPath)} was a bug: {@code MultipartUpload}
	 * encodes the key it is handed, so a key encoded upstream too came out with every {@code %} doubled.
	 */
	@Test
	public void encodeUrlPathIsNotIdempotent() {
		assertEquals("a%2520b.pdf", S3Client.encodeUrlPath(S3Client.encodeUrlPath("a b.pdf")));
	}

	/**
	 * Every key the client builds is {@code getPath()} output — two hex levels and a UUID — on which encoding
	 * is a no-op. That is what makes encoding the key of every request URI harmless for the objects already
	 * stored, where the client used to encode on some routes only.
	 */
	@Test
	public void encodeUrlPathIsANoOpOnAGeneratedKey() {
		final String key = S3Client.getPath("bd06ba32-1234-4c8f-9e4f-2f3b1a0d5c77");
		assertEquals("77/5c/bd06ba32-1234-4c8f-9e4f-2f3b1a0d5c77", key);
		assertEquals(key, S3Client.encodeUrlPath(key));
	}

	@Test
	public void decodePathRestoresTheKey() {
		final String key = "dossier privé/rapport (v2).pdf";
		assertEquals(key, S3Client.decodePath(S3Client.encodeUrlPath(key)));
	}

}
