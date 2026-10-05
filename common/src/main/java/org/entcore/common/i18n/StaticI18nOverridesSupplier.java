package org.entcore.common.i18n;

import io.vertx.core.Future;
import org.entcore.broker.api.dto.i18n.ApplicationI18nOverridesDTO;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/** For platforms without translation overrides: only the i18n files are used. */
public class StaticI18nOverridesSupplier implements I18nOverridesSupplier {

    @Override
    public Future<List<ApplicationI18nOverridesDTO>> getOverrides(Set<String> applications) {
        return Future.succeededFuture(Collections.emptyList());
    }
}
