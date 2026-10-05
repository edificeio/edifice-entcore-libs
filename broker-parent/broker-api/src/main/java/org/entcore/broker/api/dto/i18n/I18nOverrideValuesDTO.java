package org.entcore.broker.api.dto.i18n;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/** Overridden translations for a language and/or a theme. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class I18nOverrideValuesDTO {

    /** The language they apply to (e.g. "fr"), null for every language */
    private final String language;
    /** The theme they apply to (e.g. "panda"), null for every theme */
    private final String theme;
    /** The translated text of each overridden key */
    private final Map<String, String> translations;

    @JsonCreator
    public I18nOverrideValuesDTO(@JsonProperty("language") String language,
                                 @JsonProperty("theme") String theme,
                                 @JsonProperty("translations") Map<String, String> translations) {
        this.language = language;
        this.theme = theme;
        this.translations = translations;
    }

    public String getLanguage() {
        return language;
    }

    public String getTheme() {
        return theme;
    }

    public Map<String, String> getTranslations() {
        return translations;
    }
}
