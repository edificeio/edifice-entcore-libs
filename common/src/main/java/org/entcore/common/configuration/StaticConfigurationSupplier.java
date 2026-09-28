package org.entcore.common.configuration;

import io.vertx.core.Future;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import org.entcore.common.user.UserInfos;

import java.util.Map;

public class StaticConfigurationSupplier implements ConfigurationSupplier {
    private final JsonObject configuration;

    public StaticConfigurationSupplier(JsonObject configuration) {
        this.configuration = configuration;
    }


    @Override
    public Future<Map<String, String>> getConfigurationStrings(String[] keys, UserInfos userInfos, HttpServerRequest request) {
        final Map<String, String> configMap = new java.util.HashMap<>();
        for (String key : keys) {
            if(configuration.containsKey(key)) {
                configMap.put(key, configuration.getString(key));
            }
        }
        return Future.succeededFuture(configMap);
    }
}
