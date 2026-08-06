package org.entcore.common.notification.push.impl;

import fr.wseduc.webutils.Either;
import io.vertx.core.Future;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.entcore.common.notification.push.PushNotifBuilder;
import org.entcore.common.notification.push.PushNotifDto;
import org.entcore.common.notification.push.PushNotifService;
import org.entcore.common.sql.Sql;
import org.entcore.common.sql.SqlResult;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Push notification queue held in the push-manager schema of the platform database, reached through
 * the platform sql verticle.
 *
 * <p>That verticle binds every parameter as text but integers, and renders every result as text as
 * well, so the queries carry the casts and the conversions the client would otherwise do.
 */
public class SqlPushNotifService implements PushNotifService {

    private static final String DEFAULT_TABLE = "push_manager.push_notifs";
    /** Columns the sql verticle hands back as text and {@link SqlResult} parses back into json. */
    private static final JsonArray JSONB_FIELDS = new JsonArray()
            .add("message").add("message_params").add("notification_ids");
    private static final String COLUMNS =
            "id, user_id, scheduled, notif_type, notif_sub_type, message, message_params, status, attempts, " +
            // A postgres array comes back as (index, value) pairs, json keeps it flat.
            "to_json(notification_ids) AS notification_ids, " +
            // Timestamps come back formatted in the timezone of the JVM and without any offset, epoch
            // microseconds leave nothing to interpret and keep the precision postgres stores, which
            // created_date needs to match again when it comes back as the key of an update.
            "(extract(epoch from created_date) * 1000000)::bigint AS created_date, " +
            "(extract(epoch from schedule_at) * 1000000)::bigint AS schedule_at, " +
            "(extract(epoch from attempt_at) * 1000000)::bigint AS attempt_at";

    private final Sql sql;
    private final String tableName;
    /** Cast to append to the placeholder of a column, for the types the sql verticle cannot bind. */
    private final Map<String, String> writeCasts;

    public SqlPushNotifService() {
        this(Sql.getInstance(), DEFAULT_TABLE);
    }

    public SqlPushNotifService(final Sql sql, final String tableName) {
        this.sql = sql;
        this.tableName = tableName;
        final int schemaEnd = tableName.lastIndexOf('.');
        final String schema = schemaEnd < 0 ? "" : tableName.substring(0, schemaEnd + 1);
        final Map<String, String> casts = new HashMap<>();
        casts.put("user_id", "::uuid");
        casts.put("scheduled", "::" + schema + "schedule_type");
        casts.put("message", "::jsonb");
        casts.put("message_params", "::jsonb");
        casts.put("notification_ids", "::text[]");
        casts.put("schedule_at", "::timestamptz");
        this.writeCasts = Collections.unmodifiableMap(casts);
    }

    @Override
    public Future<List<PushNotifDto>> findPending(final String userId, final String notifType) {
        final String query = "SELECT " + COLUMNS + " FROM " + tableName +
                " WHERE user_id = ?::uuid AND notif_type = ? AND status = ? ORDER BY created_date";
        final JsonArray values = new JsonArray()
                .add(UUID.fromString(userId).toString())
                .add(notifType)
                .add(PushNotifDto.Status.PENDING.getCode());
        return sql.prepared(query, values, new DeliveryOptions()).compose(message -> {
            message.body().put("jsonb_fields", JSONB_FIELDS.copy());
            return toFuture(SqlResult.validResult(message)).map(SqlPushNotifService::toDtos);
        });
    }

    @Override
    public Future<Void> create(final PushNotifBuilder pushNotif) {
        final List<String> names = new ArrayList<>();
        final List<String> placeholders = new ArrayList<>();
        final JsonArray values = new JsonArray()
                .add(pushNotif.getId().toString())
                .add(pushNotif.getCreatedDate().toString());
        names.add("id");
        placeholders.add("?::uuid");
        names.add("created_date");
        placeholders.add("?::timestamptz");
        for (final Map.Entry<String, Object> column : pushNotif.getColumns().entrySet()) {
            names.add(column.getKey());
            placeholders.add(placeholder(column.getKey()));
            values.add(toSqlValue(column.getValue()));
        }
        final String query = "INSERT INTO " + tableName + " (" + String.join(", ", names) + ")" +
                " VALUES (" + String.join(", ", placeholders) + ")";
        return sql.prepared(query, values, new DeliveryOptions())
                .compose(message -> toFuture(SqlResult.validRowsResult(message)))
                .mapEmpty();
    }

    @Override
    public Future<Boolean> update(final PushNotifBuilder pushNotif) {
        if (pushNotif.getColumns().isEmpty()) {
            return Future.failedFuture("push.notif.update.without.column");
        }
        final List<String> assignments = new ArrayList<>();
        final JsonArray values = new JsonArray();
        for (final Map.Entry<String, Object> column : pushNotif.getColumns().entrySet()) {
            assignments.add(column.getKey() + " = " + placeholder(column.getKey()));
            values.add(toSqlValue(column.getValue()));
        }
        values.add(pushNotif.getId().toString())
                .add(PushNotifDto.Status.PENDING.getCode());
        // attempt_at is stamped by push-manager when it reserves the notification for a send:
        // a notification it already holds is left alone, and the caller queues another one.
        final String query = "UPDATE " + tableName + " SET " + String.join(", ", assignments) +
                " WHERE id = ?::uuid AND status = ? AND attempt_at IS NULL";
        return sql.prepared(query, values, new DeliveryOptions())
                .compose(message -> toFuture(SqlResult.validRowsResult(message)))
                .map(updated -> updated.getLong("rows", 0L) > 0);
    }

    private String placeholder(final String column) {
        final String cast = writeCasts.get(column);
        return cast == null ? "?" : "?" + cast;
    }

    /** The sql verticle binds integers as integers and calls {@code toString()} on anything else. */
    private static Object toSqlValue(final Object value) {
        if (value instanceof Integer) {
            return value;
        }
        if (value instanceof JsonObject) {
            return ((JsonObject) value).encode();
        }
        if (value instanceof List) {
            return toArrayLiteral((List<?>) value);
        }
        return value.toString();
    }

    /** Postgres array literal, {@code {"first","second"}}. */
    private static String toArrayLiteral(final List<?> values) {
        final List<String> elements = new ArrayList<>(values.size());
        for (final Object value : values) {
            if(value != null) {
                elements.add('"' + value.toString().replace("\\", "\\\\").replace("\"", "\\\"") + '"');
            }
        }
        return "{" + String.join(",", elements) + "}";
    }

    private static List<PushNotifDto> toDtos(final JsonArray rows) {
        final List<PushNotifDto> pushNotifs = new ArrayList<>(rows.size());
        for (int i = 0; i < rows.size(); i++) {
            pushNotifs.add(toDto(rows.getJsonObject(i)));
        }
        return pushNotifs;
    }

    private static PushNotifDto toDto(final JsonObject row) {
        final String scheduled = row.getString("scheduled");
        final JsonArray notificationIds = row.getJsonArray("notification_ids");
        return new PushNotifDto()
                .setId(UUID.fromString(row.getString("id")))
                .setCreatedDate(toInstant(row.getLong("created_date")))
                .setUserId(row.getString("user_id"))
                .setScheduled(scheduled == null ? null : PushNotifDto.ScheduleType.valueOf(scheduled))
                .setNotifType(row.getString("notif_type"))
                .setNotifSubType(row.getString("notif_sub_type"))
                .setMessage(row.getJsonObject("message"))
                .setMessageParams(row.getJsonObject("message_params"))
                .setNotificationIds(toStrings(notificationIds))
                .setStatus(PushNotifDto.Status.fromCode(row.getInteger("status")))
                .setScheduleAt(toInstant(row.getLong("schedule_at")))
                .setAttemptAt(toInstant(row.getLong("attempt_at")))
                .setAttempts(row.getInteger("attempts", 0));
    }

    private static List<String> toStrings(final JsonArray values) {
        if (values == null) {
            return Collections.emptyList();
        }
        final List<String> strings = new ArrayList<>(values.size());
        for (int i = 0; i < values.size(); i++) {
            strings.add(values.getString(i));
        }
        return strings;
    }

    private static Instant toInstant(final Long epochMicro) {
        return epochMicro == null ? null
                : Instant.ofEpochSecond(Math.floorDiv(epochMicro, 1000000L),
                        Math.floorMod(epochMicro, 1000000L) * 1000L);
    }

    private static <T> Future<T> toFuture(final Either<String, T> result) {
        return result.isRight() ? Future.succeededFuture(result.right().getValue())
                : Future.failedFuture(result.left().getValue());
    }
}
