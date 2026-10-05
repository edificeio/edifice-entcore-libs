package org.entcore.common.i18n;

import io.vertx.core.Future;
import org.entcore.broker.api.dto.i18n.ApplicationI18nOverridesDTO;

import java.util.List;
import java.util.Set;

/**
 * Provides the translations overriding those of the i18n files of applications, per tenant and per
 * domain (see {@link I18nOverridesSupplierFactory}).
 */
public interface I18nOverridesSupplier {

    /**
     * @param applications applications whose overrides are wanted, e.g. "workspace"
     * @return the overrides of the applications having any
     */
    Future<List<ApplicationI18nOverridesDTO>> getOverrides(Set<String> applications);
}
