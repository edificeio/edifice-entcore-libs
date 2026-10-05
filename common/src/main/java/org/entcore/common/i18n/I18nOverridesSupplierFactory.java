package org.entcore.common.i18n;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.entcore.common.configuration.EventBusConfigurationSupplierConfiguration;

/**
 * Creates the {@link I18nOverridesSupplier} described by the "i18nOverridesSupplier" object of a
 * module's configuration, whose "mode" is either "static" (the default: no override) or "event-bus",
 * with the same fields as for "configurationSupplier" (see
 * {@link org.entcore.common.configuration.ConfigurationSupplierFactory}), "routingValue" defaulting
 * to {@value #DEFAULT_ROUTING_VALUE}, plus an optional "timeout" in milliseconds.
 */
public class I18nOverridesSupplierFactory {

    public static final String DEFAULT_ROUTING_VALUE = "tenant.i18n.overrides.get";
    private static final long DEFAULT_TIMEOUT = 10_000L;

    public static Future<I18nOverridesSupplier> createI18nOverridesSupplier(final Vertx vertx,
                                                                           final JsonObject appConfiguration) {
        final JsonObject supplierConfig = appConfiguration.getJsonObject("i18nOverridesSupplier");
        if (supplierConfig == null || "static".equalsIgnoreCase(supplierConfig.getString("mode"))) {
            return Future.succeededFuture(new StaticI18nOverridesSupplier());
        } else if ("event-bus".equalsIgnoreCase(supplierConfig.getString("mode"))) {
            final EventBusConfigurationSupplierConfiguration supplierConfiguration =
                    new EventBusConfigurationSupplierConfiguration(
                            supplierConfig.getString("routingFieldName"),
                            supplierConfig.getString("routingValue", DEFAULT_ROUTING_VALUE),
                            supplierConfig.getString("bodyFieldName"),
                            supplierConfig.getString("address")
                    );
            return Future.succeededFuture(new EventBusI18nOverridesSupplier(vertx, supplierConfiguration,
                    supplierConfig.getLong("timeout", DEFAULT_TIMEOUT)));
        } else {
            return Future.failedFuture("Unknown I18nOverridesSupplier type");
        }
    }
}
