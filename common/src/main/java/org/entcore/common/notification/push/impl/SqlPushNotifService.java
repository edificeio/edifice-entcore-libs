package org.entcore.common.notification.push.impl;

import fr.wseduc.webutils.Either;
import io.vertx.core.Future;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.entcore.common.notification.push.PushNotifBuilder;
import org.entcore.common.notification.push.PushNotifStatus;
import org.entcore.common.notification.push.PushNotifService;
import org.entcore.common.sql.Sql;
import org.entcore.common.sql.SqlResult;

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
 * <p>That verticle binds every parameter as text but integers, so the queries carry the casts the
 * client would otherwise do.
 */
public class SqlPushNotifService implements PushNotifService {

    private static final String DEFAULT_TABLE = "push_manager.push_notifs";

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
    public Future<Boolean> appendToPendingRecap(final String userId, final String notifType,
            final String notificationId, final String bodyTemplate, final String countPlaceholder) {
        // Every expression of a SET list reads the row as it stood before the update, so the param and
        // the body are both rendered against the same count.
        final String nextCount = "(coalesce((message_params->>'count')::int, 1) + 1)";
        final String query = "UPDATE " + tableName +
                " SET notification_ids = array_append(coalesce(notification_ids, '{}'::text[]), ?::text)," +
                " message_params = jsonb_set(coalesce(message_params, '{}'::jsonb), '{count}'," +
                " to_jsonb(" + nextCount + "))," +
                // The envelope PushNotifBuilder.withMessage wraps the fcm payload in.
                " message = jsonb_set(message, '{message,notification,body}'," +
                " to_jsonb(replace(?, ?, " + nextCount + "::text)))" +
                // The oldest pending recap of that user, locked so a concurrent append waits for it.
                // attempt_at is stamped by push-manager when it reserves a notification for a send:
                // one it already holds is left alone, and the caller queues a new recap instead.
                " WHERE (id, created_date) = (SELECT id, created_date FROM " + tableName +
                " WHERE user_id = ?::uuid AND notif_type = ? AND status = ? AND attempt_at IS NULL" +
                " ORDER BY created_date LIMIT 1 FOR UPDATE)";
        final JsonArray values = new JsonArray()
                .add(notificationId)
                .add(bodyTemplate)
                .add(countPlaceholder)
                .add(UUID.fromString(userId).toString())
                .add(notifType)
                .add(PushNotifStatus.PENDING.getCode());
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

    private static <T> Future<T> toFuture(final Either<String, T> result) {
        return result.isRight() ? Future.succeededFuture(result.right().getValue())
                : Future.failedFuture(result.left().getValue());
    }
}
