package org.entcore.common.configuration;

import io.vertx.core.Future;
import io.vertx.core.http.HttpServerRequest;
import org.entcore.common.user.UserInfos;

import java.util.Map;

public interface ConfigurationSupplier {
    default Future<String> getConfigurationString(String key, UserInfos userInfos, HttpServerRequest request) {
        return getConfigurationStrings(new String[]{key}, userInfos, request).map(configMap -> configMap.get(key));
    }
    Future<Map<String, String>> getConfigurationStrings(final String[] keys, final UserInfos userInfos, final HttpServerRequest request);
    default Future<Void> clearCache(final String... keys) {return Future.succeededFuture();}
}
