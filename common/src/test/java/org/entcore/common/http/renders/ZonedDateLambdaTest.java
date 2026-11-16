package org.entcore.common.http.renders;

import com.samskivert.mustache.Mustache;
import org.junit.Test;

import java.time.Instant;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class ZonedDateLambdaTest {

    // 16:46 in Paris (CEST), 04:46 in Tahiti
    private static final long DATE = Instant.parse("2026-10-22T14:46:00Z").toEpochMilli();
    private static final String BROWSER_ACCEPT_LANGUAGE = "fr-FR,fr;q=0.9,en-US;q=0.8,en;q=0.7";

    private static String render(String acceptLanguage, String timezonePreference) {
        final Map<String, Object> ctx = new HashMap<>();
        ctx.put("date", DATE);
        ctx.put("datetime", new ZonedDateLambda(acceptLanguage));
        if (timezonePreference != null) {
            ctx.put(ZonedDateLambda.TIMEZONE_PARAM, timezonePreference);
        }
        return Mustache.compiler().compile("{{#datetime}}{{date}}{{/datetime}}").execute(ctx);
    }

    @Test
    public void testNoPreference_DefaultsToParis() {
        assertTrue(render("fr", null).contains("16:46"));
    }

    @Test
    public void testUserPreference_UsesUserTimezone() {
        assertTrue(render("fr", "{\"timezone\":\"Pacific/Tahiti\",\"managedBy\":\"STRUCTURE\"}").contains("04:46"));
    }

    @Test
    public void testInvalidPreference_DefaultsToParis() {
        assertTrue(render("fr", "{\"timezone\":\"Mars/Olympus\"}").contains("16:46"));
        assertTrue(render("fr", "not json").contains("16:46"));
    }

    @Test
    public void testNoZoneLabel() {
        final String date = render("fr", null);
        assertFalse(date.contains("UTC"));
        assertFalse(date.contains("CEST"));
    }

    @Test
    public void testRawAcceptLanguageHeader_FormatsInFrench() {
        final String date = render(BROWSER_ACCEPT_LANGUAGE, null);
        assertTrue(date.contains("octobre"));
        assertFalse(date.contains("PM"));
    }

    @Test
    public void testInvalidTimestamp() {
        final Map<String, Object> ctx = new HashMap<>();
        ctx.put("date", "abc");
        ctx.put("datetime", new ZonedDateLambda("fr"));
        assertEquals("DATE INVALIDE", Mustache.compiler().compile("{{#datetime}}{{date}}{{/datetime}}").execute(ctx));
    }

    @Test
    public void testResolveTimezone_NonMapContext() {
        assertEquals(ZoneId.of("Europe/Paris"), ZonedDateLambda.resolveTimezone(new Object()));
    }
}
