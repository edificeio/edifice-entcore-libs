package org.entcore.common.events.impl;

import io.vertx.core.json.JsonObject;
import org.entcore.common.user.UserInfos;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class DeviceInfoTest {

    private static final String DEVICE = new JsonObject()
            .put("osName", "iOS").put("osVersion", "18.7").put("deviceName", "iPhone 14 Pro").encode();

    @Test
    public void toSessionValue_keepsDeviceInfoOnly() {
        final JsonObject body = new JsonObject().put("module", "Timeline").put("appName", "appe")
                .put("osName", "iOS").put("osVersion", "18.7").put("deviceName", "iPhone 14 Pro");

        assertEquals(DEVICE, DeviceInfo.toSessionValue(body));
    }

    @Test
    public void toSessionValue_ignoresBlankValues() {
        final JsonObject body = new JsonObject().put("osName", "Android").put("osVersion", 14).put("deviceName", " ")
                .put("deviceType", "unknown");

        assertEquals(new JsonObject().put("osName", "Android").put("osVersion", "14").encode(), DeviceInfo.toSessionValue(body));
    }

    @Test
    public void toSessionValue_withoutDeviceInfo_isNull() {
        assertNull(DeviceInfo.toSessionValue(new JsonObject().put("module", "Timeline")));
        assertNull(DeviceInfo.toSessionValue(null));
    }

    @Test
    public void fillFromSession_fillsMissingOrUnknownFieldsOnly() {
        final JsonObject event = new JsonObject().put("deviceType", "mobile")
                .put("osName", "unknown").put("osVersion", "17.0");

        DeviceInfo.fillFromSession(event, userWithDevice(DEVICE));

        assertEquals("iOS", event.getString("osName"));
        assertEquals("17.0", event.getString("osVersion"));
        assertEquals("iPhone 14 Pro", event.getString("deviceName"));
        assertEquals("mobile", event.getString("deviceType"));
    }

    @Test
    public void deviceType_isStoredAndFilledWhenUnknown_butNeverOverridden() {
        final String device = DeviceInfo.toSessionValue(new JsonObject().put("deviceType", "tablet"));
        assertEquals(new JsonObject().put("deviceType", "tablet").encode(), device);

        final JsonObject unknown = new JsonObject().put("deviceType", "Unknown");
        DeviceInfo.fillFromSession(unknown, userWithDevice(device));
        assertEquals("tablet", unknown.getString("deviceType"));

        final JsonObject mobile = new JsonObject().put("deviceType", "mobile");
        DeviceInfo.fillFromSession(mobile, userWithDevice(device));
        assertEquals("mobile", mobile.getString("deviceType"));
    }

    @Test
    public void fillFromSession_withoutDeviceInSession_isUnchanged() {
        final JsonObject event = new JsonObject().put("osName", "Unknown");

        DeviceInfo.fillFromSession(event, userWithDevice(null));
        DeviceInfo.fillFromSession(event, null);
        DeviceInfo.fillFromSession(event, userWithDevice("not json"));

        assertEquals(new JsonObject().put("osName", "Unknown"), event);
    }

    private static UserInfos userWithDevice(String device) {
        final UserInfos user = new UserInfos();
        user.setUserId("user");
        final Map<String, Object> cache = new HashMap<>();
        if (device != null) {
            cache.put(DeviceInfo.SESSION_ATTRIBUTE, device);
        }
        user.setCache(cache);
        return user;
    }
}
