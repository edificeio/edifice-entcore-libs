package org.entcore.common.i18n;

import fr.wseduc.webutils.I18n;
import fr.wseduc.webutils.I18nOverrides;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.entcore.broker.api.dto.i18n.ApplicationI18nOverridesDTO;
import org.entcore.broker.api.dto.i18n.DomainI18nOverridesDTO;
import org.entcore.broker.api.dto.i18n.I18nOverrideValuesDTO;
import org.entcore.broker.api.dto.i18n.TenantI18nOverridesDTO;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@RunWith(VertxUnitRunner.class)
public class I18nOverridesLoaderTest {

    private Vertx vertx;

    @Before
    public void setUp() {
        vertx = Vertx.vertx();
    }

    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    private static List<ApplicationI18nOverridesDTO> overridesOf(String application, String text) {
        return Collections.singletonList(new ApplicationI18nOverridesDTO(application,
                Collections.singletonList(new TenantI18nOverridesDTO("t1", Collections.singletonList(
                        new I18nOverrideValuesDTO("fr", null, Collections.singletonMap("k", text + " tenant"))))),
                Collections.singletonList(new DomainI18nOverridesDTO("ent.example.org", Collections.singletonList(
                        new I18nOverrideValuesDTO(null, "panda", Collections.singletonMap("k", text + " domain")))))));
    }

    @Test
    public void overridesOfApplicationsAreMergedPerTenantAndDomain(TestContext context) {
        final I18nOverrides overrides = I18nOverridesLoader.toOverrides(Arrays.asList(
                overridesOf("app", "app").get(0),
                new ApplicationI18nOverridesDTO("other", null, Collections.singletonList(new DomainI18nOverridesDTO(
                        "ent.example.org", Collections.singletonList(new I18nOverrideValuesDTO(null, "panda",
                        Collections.singletonMap("other.k", "other domain"))))))));

        context.assertEquals("app tenant", overrides.find("k", "t1", "ent.example.org", "panda", Locale.FRENCH));
        context.assertEquals("app domain", overrides.find("k", null, "ent.example.org", "panda", Locale.ENGLISH));
        context.assertEquals("other domain", overrides.find("other.k", "t1", "ent.example.org", "panda", Locale.FRENCH));
        context.assertNull(overrides.find("k", null, "ent.example.org", null, Locale.ENGLISH));
    }

    @Test
    public void theEventBusSupplierAsksTheConfiguredAddress(TestContext context) {
        final JsonObject[] received = new JsonObject[1];
        vertx.eventBus().<JsonObject>consumer("test.bridge", message -> {
            received[0] = message.body();
            message.reply(new JsonObject().put("success", true).put("applications", new JsonArray()
                    .add(new JsonObject().put("application", "app").put("unknownField", 1)
                            .put("domains", new JsonArray().add(new JsonObject().put("domain", "default-domain")
                                    .put("values", new JsonArray().add(new JsonObject().put("language", "fr")
                                            .put("translations", new JsonObject().put("k", "v")))))))).encode());
        });
        final JsonObject config = new JsonObject().put("i18nOverridesSupplier", new JsonObject()
                .put("mode", "event-bus").put("address", "test.bridge")
                .put("routingFieldName", "subject").put("bodyFieldName", "body"));

        I18nOverridesSupplierFactory.createI18nOverridesSupplier(vertx, config)
                .compose(supplier -> supplier.getOverrides(Collections.singleton("app")))
                .onComplete(context.asyncAssertSuccess(overrides -> {
                    context.assertEquals("tenant.i18n.overrides.get", received[0].getString("subject"));
                    context.assertEquals(new JsonObject().put("applications", new JsonArray().add("app")),
                            new JsonObject(received[0].getString("body")));
                    context.assertEquals(1, overrides.size());
                    context.assertEquals("v", overrides.get(0).getDomains().get(0).getValues().get(0)
                            .getTranslations().get("k"));
                }));
    }

    @Test
    public void theEventBusSupplierFailsWhenTheTenantServiceDoes(TestContext context) {
        vertx.eventBus().<JsonObject>consumer("test.bridge", message -> message.reply(
                new JsonObject().put("success", false).put("errorMsg", "boom").encode()));
        final JsonObject config = new JsonObject().put("i18nOverridesSupplier", new JsonObject()
                .put("mode", "event-bus").put("address", "test.bridge")
                .put("routingFieldName", "subject").put("bodyFieldName", "body"));

        I18nOverridesSupplierFactory.createI18nOverridesSupplier(vertx, config)
                .compose(supplier -> supplier.getOverrides(Collections.singleton("app")))
                .onComplete(context.asyncAssertFailure(err -> context.assertTrue(err.getMessage().contains("boom"))));
    }

    @Test
    public void withoutConfigurationThereIsNoLoader(TestContext context) {
        I18nOverridesLoader.start(vertx, new JsonObject(), new I18n(), Collections.singleton("app"))
                .onComplete(context.asyncAssertSuccess(context::assertNull));
    }

    @Test
    public void overridesAreReloadedOnceForABurstOfChangesConcerningTheirApplications(TestContext context) {
        final AtomicInteger calls = new AtomicInteger();
        final I18n target = new I18n();
        final I18nOverridesLoader loader = new I18nOverridesLoader(vertx,
                applications -> Future.succeededFuture(overridesOf("app", "v" + calls.incrementAndGet())),
                target, Collections.singleton("APP"), 100, 100);
        final Async async = context.async();

        loader.start().onComplete(context.asyncAssertSuccess(v -> {
            context.assertEquals("v1 tenant", target.translate("k", "ent.example.org", "t1", null, Locale.FRENCH));
            I18nOverridesLoader.requestReload(vertx, Collections.singleton("other"));
            vertx.setTimer(300, t1 -> {
                context.assertEquals(1, calls.get(), "changes of other applications are ignored");
                I18nOverridesLoader.requestReload(vertx, Collections.singleton("app"));
                I18nOverridesLoader.requestReload(vertx, Collections.<String>emptyList());
                // As notified by the tenant service through the broker
                vertx.eventBus().request("tenant.i18n.overrides.changed",
                        "{\"applications\": [\"app\"]}".getBytes(StandardCharsets.UTF_8));
                vertx.setTimer(500, t2 -> {
                    context.assertEquals(2, calls.get(), "a burst of changes triggers a single reload");
                    context.assertEquals("v2 tenant", target.translate("k", "ent.example.org", "t1", null, Locale.FRENCH));
                    loader.stop();
                    async.complete();
                });
            });
        }));
    }

    @Test
    public void failedLoadsAreRetriedAndKeepThePreviousOverrides(TestContext context) {
        final AtomicInteger calls = new AtomicInteger();
        final I18n target = new I18n();
        final Set<String> fetched = new HashSet<>();
        final I18nOverridesLoader loader = new I18nOverridesLoader(vertx, applications -> {
            fetched.addAll(applications);
            return calls.incrementAndGet() == 2
                    ? Future.failedFuture("tenant service unavailable")
                    : Future.succeededFuture(overridesOf("app", "v" + calls.get()));
        }, target, Collections.singleton("app"), 100, 50);
        final Async async = context.async();

        loader.start().onComplete(context.asyncAssertSuccess(v -> {
            loader.setApplications(Arrays.asList("app", "timeline"));
            vertx.setTimer(80, t1 -> {
                context.assertEquals(2, calls.get(), "a change of applications triggers a reload");
                context.assertEquals("v1 tenant", target.translate("k", "ent.example.org", "t1", null, Locale.FRENCH),
                        "a failed reload keeps the previous overrides");
                vertx.setTimer(200, t2 -> {
                    context.assertEquals(3, calls.get(), "a failed load is retried");
                    context.assertEquals("v3 tenant", target.translate("k", "ent.example.org", "t1", null, Locale.FRENCH));
                    context.assertEquals(new HashSet<>(Arrays.asList("app", "timeline")), fetched);
                    loader.stop();
                    async.complete();
                });
            });
        }));
    }
}
