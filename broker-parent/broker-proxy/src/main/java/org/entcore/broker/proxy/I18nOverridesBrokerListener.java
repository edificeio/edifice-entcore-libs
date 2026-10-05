package org.entcore.broker.proxy;

import io.vertx.core.Future;
import org.entcore.broker.api.BrokerListener;
import org.entcore.broker.api.dto.BaseResponseDTO;
import org.entcore.broker.api.dto.i18n.I18nOverridesChangedDTO;

/**
 * Listens to the changes of the translation overrides published by the tenant service, for every
 * instance of the platform to reload them.
 */
public interface I18nOverridesBrokerListener {

    /**
     * Called once a change of the translation overrides of applications is committed.
     *
     * @param notification the applications concerned, empty for all of them
     */
    @BrokerListener(subject = "tenant.i18n.overrides.changed", proxy = true, broadcast = true)
    Future<BaseResponseDTO> onI18nOverridesChanged(I18nOverridesChangedDTO notification);
}
