package org.entcore.common.notification.push;

import io.vertx.core.json.JsonObject;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Builds the payload of a push notification to queue for push-manager, keyed by
 * {@code (id, createdDate)}.
 *
 * <p>A {@code null} value never sets a column: the builder carries what has to be written, not the
 * whole row. Setters take caller-friendly types, getters expose the values as they are handed to
 * the storage.
 */
public class PushNotifBuilder {

    private final UUID id;
    private final OffsetDateTime createdDate;
    private final Map<String, Object> columns = new LinkedHashMap<>();

    private PushNotifBuilder(UUID id, Instant createdDate) {
        this.id = id;
        this.createdDate = OffsetDateTime.ofInstant(createdDate, ZoneOffset.UTC);
    }

    /** A notification to queue, with a freshly generated key. */
    public static PushNotifBuilder create() {
        return new PushNotifBuilder(UUID.randomUUID(), Instant.now());
    }

    /**
     * @param userId ENT user id, which the storage holds as a UUID
     * @throws IllegalArgumentException if the id is not a UUID
     */
    public PushNotifBuilder withUserId(String userId) {
        return put("user_id", userId == null ? null : UUID.fromString(userId).toString());
    }

    /** To be sent on the next push-manager pass. */
    public PushNotifBuilder immediate() {
        return put("scheduled", PushNotifScheduleType.IMMEDIATE.name());
    }

    /**
     * To be held until {@code sendAt}, for instance until the end of a quiet hours window.
     *
     */
    public PushNotifBuilder scheduledAt(Instant sendAt) {
        if(sendAt == null) {
            return this;
        }
        put("scheduled", PushNotifScheduleType.AT_DATE.name());
        return put("schedule_at", OffsetDateTime.ofInstant(sendAt, ZoneOffset.UTC));
    }

    public PushNotifBuilder withNotifType(String notifType) {
        return put("notif_type", notifType);
    }

    public PushNotifBuilder withNotifSubType(String notifSubType) {
        return put("notif_sub_type", notifSubType);
    }

    /**
     * The FCM payload — {@code notification}, {@code data}… — as built for a send. The builder wraps
     * it in the {@code message} envelope push-manager unwraps before adding the device token.
     */
    public PushNotifBuilder withMessage(JsonObject fcmMessage) {
        return put("message", fcmMessage == null ? null : new JsonObject().put("message", fcmMessage));
    }

    public PushNotifBuilder withMessageParams(JsonObject messageParams) {
        return put("message_params", messageParams);
    }

    /** Timeline notifications this push notification stands for. */
    public PushNotifBuilder withNotificationIds(List<String> notificationIds) {
        return put("notification_ids", notificationIds == null ? null : new ArrayList<String>(notificationIds));
    }

    public PushNotifBuilder withStatus(PushNotifStatus status) {
        return put("status", status == null ? null : status.getCode());
    }

    public UUID getId() {
        return id;
    }

    public OffsetDateTime getCreatedDate() {
        return createdDate;
    }

    /** The columns to write, excluding the {@code (id, createdDate)} key. */
    public Map<String, Object> getColumns() {
        return columns;
    }

    private PushNotifBuilder put(String column, Object value) {
        if (value != null) {
            columns.put(column, value);
        }
        return this;
    }
}
