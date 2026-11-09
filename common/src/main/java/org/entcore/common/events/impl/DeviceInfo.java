package org.entcore.common.events.impl;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.entcore.common.user.UserInfos;
import org.entcore.common.user.UserUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Device info of a session (osName, osVersion, deviceType, deviceName).
 * It is kept in the session to fill the events of this session when the request does not carry it
 * (e.g. the mobile app only sends it on mobile ACCESS events).
 */
public final class DeviceInfo {

	/** Session attribute holding the device info (JSON encoded) */
	public static final String SESSION_ATTRIBUTE = "deviceInfo";
	private static final List<String> FIELDS = Arrays.asList("osName", "osVersion", "deviceType", "deviceName");
	private static final String UNKNOWN = "Unknown";
	private static final Logger log = LoggerFactory.getLogger(DeviceInfo.class);

	private DeviceInfo() {
	}

	/**
	 * @param body payload holding the device info
	 * @return the device info found in the payload, JSON encoded, or null if there is none
	 */
	public static String toSessionValue(JsonObject body) {
		if (body == null) {
			return null;
		}
		final JsonObject device = new JsonObject();
		for (String field : FIELDS) {
			final String value = body.getString(field);
			if (StringUtils.isNotBlank(value) && !UNKNOWN.equalsIgnoreCase(value)) {
				device.put(field, value);
			}
		}
		return device.isEmpty() ? null : device.encode();
	}

	/**
	 * Stores the device info of the payload in the session of the request, only if it changed.
	 * @return a future completed when the device info is stored or when there is nothing to store
	 */
	public static Future<Void> storeInSession(EventBus eb, HttpServerRequest request, UserInfos user, JsonObject body) {
		final String value = toSessionValue(body);
		if (value == null || value.equals(user.getAttribute(SESSION_ATTRIBUTE))) {
			return Future.succeededFuture();
		}
		final String sessionId = UserUtils.getSessionIdOrTokenId(request).orElse(null);
		if (sessionId == null) {
			return Future.succeededFuture();
		}
		final Promise<Void> promise = Promise.promise();
		UserUtils.addSessionAttributeOnId(eb, sessionId, SESSION_ATTRIBUTE, value, stored -> {
			if (Boolean.TRUE.equals(stored)) {
				promise.complete();
			} else {
				promise.fail("Could not store device info in session of user " + user.getUserId());
			}
		});
		return promise.future().onFailure(th -> log.warn(th.getMessage()));
	}

	/**
	 * Fills the device fields that are missing or unknown with the device info stored in the user session.
	 * Does nothing if the session has no device info.
	 */
	public static void fillFromSession(JsonObject event, UserInfos user) {
		final Object value = user != null ? user.getAttribute(SESSION_ATTRIBUTE) : null;
		if (!(value instanceof String)) {
			return;
		}
		final JsonObject device;
		try {
			device = new JsonObject((String) value);
		} catch (Exception e) {
			log.warn("Invalid device info in session of user " + user.getUserId());
			return;
		}
		for (String field : FIELDS) {
			final String current = event.getString(field);
			final String fromSession = device.getString(field);
			if ((StringUtils.isBlank(current) || UNKNOWN.equalsIgnoreCase(current)) && StringUtils.isNotBlank(fromSession)) {
				event.put(field, fromSession);
			}
		}
	}
}
