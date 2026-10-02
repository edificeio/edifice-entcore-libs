package org.entcore.common.tenant;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.entcore.common.configuration.EventBusConfigurationSupplierConfiguration;

/**
 * Creates the {@link TenantSupplier} described by the "tenantSupplier" object of a module's
 * configuration, whose "mode" is either "static" (the default) or "event-bus" (with the same
 * fields as for "configurationSupplier", see
 * {@link org.entcore.common.configuration.ConfigurationSupplierFactory}).
 */
public class TenantSupplierFactory {
    public static Future<TenantSupplier> createTenantSupplier(final Vertx vertx, final JsonObject appConfiguration) {
        final JsonObject tenantSupplierConfig = appConfiguration.getJsonObject("tenantSupplier");
        if (tenantSupplierConfig == null || "static".equalsIgnoreCase(tenantSupplierConfig.getString("mode"))) {
            return Future.succeededFuture(new StaticTenantSupplier());
        } else if ("event-bus".equalsIgnoreCase(tenantSupplierConfig.getString("mode"))) {
            final EventBusConfigurationSupplierConfiguration tenantSupplierConfiguration =
                    new EventBusConfigurationSupplierConfiguration(
                            tenantSupplierConfig.getString("routingFieldName"),
                            tenantSupplierConfig.getString("routingValue"),
                            tenantSupplierConfig.getString("bodyFieldName"),
                            tenantSupplierConfig.getString("address")
                    );
            return Future.succeededFuture(new EventBusTenantSupplier(vertx, tenantSupplierConfiguration));
        } else {
            return Future.failedFuture("Unknown TenantSupplier type");
        }
    }
}
