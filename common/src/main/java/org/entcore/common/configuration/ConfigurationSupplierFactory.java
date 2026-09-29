package org.entcore.common.configuration;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

public class ConfigurationSupplierFactory {
    public static Future<ConfigurationSupplier> createConfigurationSupplier(final Vertx vertx, final JsonObject appConfiguration) {
        final JsonObject configurationSupplierConfig = appConfiguration.getJsonObject("configurationSupplier");
        if (configurationSupplierConfig == null || "static".equalsIgnoreCase(configurationSupplierConfig.getString("mode"))) {
            return Future.succeededFuture(new StaticConfigurationSupplier(appConfiguration));
        } else if ("event-bus".equalsIgnoreCase(configurationSupplierConfig.getString("mode"))) {
            final EventBusConfigurationSupplierConfiguration configurationSupplierConfiguration =
                    new EventBusConfigurationSupplierConfiguration(
                            configurationSupplierConfig.getString("routingFieldName"),
                            configurationSupplierConfig.getString("routingValue"),
                            configurationSupplierConfig.getString("bodyFieldName"),
                            configurationSupplierConfig.getString("address")
                    );
            return Future.succeededFuture(new EventBusConfigurationSupplier(vertx, configurationSupplierConfiguration));
        } else {
            return Future.failedFuture("Unknown ConfigurationSupplier type");
        }
    }
}
