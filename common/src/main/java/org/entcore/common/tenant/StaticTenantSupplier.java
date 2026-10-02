package org.entcore.common.tenant;

import io.vertx.core.Future;

import java.util.Collection;

/** For platforms without tenants: no user ever belongs to one. */
public class StaticTenantSupplier implements TenantSupplier {
    @Override
    public Future<String> getTenantId(String userId, Collection<String> structureIds, String hostname) {
        return Future.succeededFuture("");
    }
}
