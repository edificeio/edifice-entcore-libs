package org.entcore.common.tenant;

import io.vertx.core.Future;

import java.util.Collection;

/**
 * Finds the tenant a user belongs to, without its callers knowing where tenants come from (see
 * {@link TenantSupplierFactory}).
 */
public interface TenantSupplier {
    /**
     * @param userId       id of the user
     * @param structureIds ids of the structures the user is attached to
     * @param hostname     host the user reached the platform at, used when its structures belong to
     *                     no tenant (may be null)
     * @return the id of the tenant of the user, an empty string when it has none
     */
    Future<String> getTenantId(String userId, Collection<String> structureIds, String hostname);
}
