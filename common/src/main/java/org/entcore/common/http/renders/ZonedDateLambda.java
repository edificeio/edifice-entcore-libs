/* Copyright © "Open Digital Education", 2026
 *
 * This program is published by "Open Digital Education".
 * You must indicate the name of the software and the company in any production /contribution
 * using the software and indicate on the home page of the software industry in question,
 * "powered by Open Digital Education" with a reference to the website: https://opendigitaleducation.com/.
 *
 * This program is free software, licensed under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation, version 3 of the License.
 *
 * You can redistribute this application and/or modify it since you respect the terms of the GNU Affero General Public License.
 * If you modify the source code and then use this modified source code in your creation, you must make available the source code of your modifications.
 *
 * You should have received a copy of the GNU Affero General Public License along with the software.
 * If not, please see : <http://www.gnu.org/licenses/>. Full compliance requires reading the terms of this license and following its directives.

 *
 */

package org.entcore.common.http.renders;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import fr.wseduc.webutils.I18n;
import io.vertx.core.json.Json;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.entcore.common.user.dto.TimezonePreference;

import java.io.IOException;
import java.io.Writer;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Map;

/**
 * {{#datetime}} lambda for emails: formats a timestamp (in ms) in the recipient's timezone, without any zone label.
 * The timezone is read from the {@link #TIMEZONE_PARAM} template parameter, which holds the recipient's timezone
 * preference as stored in uac.timezone. Falls back to {@link #DEFAULT_TIMEZONE} when absent or invalid.
 */
public class ZonedDateLambda implements Mustache.Lambda {
	public static final String TIMEZONE_PARAM = "timezone";
	public static final ZoneId DEFAULT_TIMEZONE = ZoneId.of("Europe/Paris");
	private static final Logger log = LoggerFactory.getLogger(ZonedDateLambda.class);

	private final DateTimeFormatter formatter;

	public ZonedDateLambda(String acceptLanguage) {
		this.formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL, FormatStyle.SHORT)
				.withLocale(I18n.getLocale(acceptLanguage));
	}

	@Override
	public void execute(Template.Fragment frag, Writer out) throws IOException {
		final String timestamp = frag.execute();
		try {
			final Instant instant = Instant.ofEpochMilli(Long.parseLong(timestamp.trim()));
			out.write(formatter.format(instant.atZone(resolveTimezone(frag.context()))));
		} catch (NumberFormatException e) {
			out.write("DATE INVALIDE");
		}
	}

	static ZoneId resolveTimezone(Object context) {
		if (context instanceof Map) {
			final Object preference = ((Map<?, ?>) context).get(TIMEZONE_PARAM);
			if (preference instanceof String) {
				try {
					return ZoneId.of(Json.decodeValue((String) preference, TimezonePreference.class).getTimezone());
				} catch (Exception e) {
					log.warn("[ZonedDateLambda] Invalid timezone preference: " + preference);
				}
			}
		}
		return DEFAULT_TIMEZONE;
	}
}
