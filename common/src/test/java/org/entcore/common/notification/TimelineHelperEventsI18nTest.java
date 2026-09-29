package org.entcore.common.notification;

import static org.entcore.common.notification.TimelineHelper.parseEventsI18n;
import static org.junit.Assert.assertEquals;

import io.vertx.core.json.JsonObject;
import org.junit.Test;

public class TimelineHelperEventsI18nTest {

    @Test
    public void testEmpty() {
        assertEquals(new JsonObject(), parseEventsI18n(null));
        assertEquals(new JsonObject(), parseEventsI18n(""));
    }

    @Test
    public void testCurrentFormat() {
        assertEquals(new JsonObject().put("a", "A").put("b", "B"),
                parseEventsI18n("{\"a\":\"A\",\"b\":\"B\"}"));
    }

    @Test
    public void testLegacyFormat() {
        assertEquals(new JsonObject().put("a", "A").put("b", "B"),
                parseEventsI18n("\"a\":\"A\",\"b\":\"B\","));
    }

    @Test
    public void testMixedFormatWithOneLegacyAppend() {
        assertEquals(new JsonObject().put("a", "A").put("k", "K"),
                parseEventsI18n("{\"a\":\"A\"}\"k\":\"K\","));
    }

    @Test
    public void testMixedFormatWithSeveralLegacyAppends() {
        assertEquals(new JsonObject().put("a", "A").put("k", "K").put("l", "L"),
                parseEventsI18n("{\"a\":\"A\"}\"k\":\"K\",\"l\":\"L\",\"k\":\"K\","));
    }

    @Test
    public void testMixedFormatWithMustacheBraces() {
        assertEquals(new JsonObject()
                        .put("a", "{{username}} a publié {{resourceName}}")
                        .put("k", "{{#nested}}}{{/nested}}"),
                parseEventsI18n("{\"a\":\"{{username}} a publié {{resourceName}}\"}\"k\":\"{{#nested}}}{{/nested}}\","));
    }

    @Test
    public void testMixedFormatLegacyKeyWins() {
        assertEquals(new JsonObject().put("a", "new"),
                parseEventsI18n("{\"a\":\"old\"}\"a\":\"new\","));
    }

    @Test
    public void testMixedFormatWithUnreadableTailKeepsHead() {
        assertEquals(new JsonObject().put("a", "A"),
                parseEventsI18n("{\"a\":\"A\"}\"k\":,"));
    }

    @Test
    public void testUnreadableValues() {
        assertEquals(new JsonObject(), parseEventsI18n("{{\"a\":\"A\"}"));
        assertEquals(new JsonObject(), parseEventsI18n("{\"a\":\"A\""));
        assertEquals(new JsonObject(), parseEventsI18n("\"a\":,"));
    }
}
