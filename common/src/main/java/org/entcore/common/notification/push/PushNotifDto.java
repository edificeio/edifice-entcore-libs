package org.entcore.common.notification.push;

import io.vertx.core.json.JsonObject;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * A push notification queued for the push-manager module, as stored in {@code push_manager.push_notifs}.
 *
 * <p>{@code attempts} and {@code attemptAt} belong to the push-manager sending loop: they are readable
 * here but cannot be written through {@link PushNotifBuilder}.
 */
public class PushNotifDto {

    private UUID id;
    private Instant createdDate;
    private String userId;
    private ScheduleType scheduled;
    private String notifType;
    private String notifSubType;
    private JsonObject message;
    private JsonObject messageParams;
    private List<String> notificationIds = Collections.emptyList();
    private Status status;
    private Instant scheduleAt;
    private Instant attemptAt;
    private int attempts;

    public UUID getId() {
        return id;
    }

    public PushNotifDto setId(UUID id) {
        this.id = id;
        return this;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public PushNotifDto setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
        return this;
    }

    public String getUserId() {
        return userId;
    }

    public PushNotifDto setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public ScheduleType getScheduled() {
        return scheduled;
    }

    public PushNotifDto setScheduled(ScheduleType scheduled) {
        this.scheduled = scheduled;
        return this;
    }

    public String getNotifType() {
        return notifType;
    }

    public PushNotifDto setNotifType(String notifType) {
        this.notifType = notifType;
        return this;
    }

    public String getNotifSubType() {
        return notifSubType;
    }

    public PushNotifDto setNotifSubType(String notifSubType) {
        this.notifSubType = notifSubType;
        return this;
    }

    /**
     * The FCM payload, wrapped in the {@code message} envelope read by push-manager
     * ({@code {"message": {"notification": ..., "data": ...}}}).
     */
    public JsonObject getMessage() {
        return message;
    }

    public PushNotifDto setMessage(JsonObject message) {
        this.message = message;
        return this;
    }

    public JsonObject getMessageParams() {
        return messageParams;
    }

    public PushNotifDto setMessageParams(JsonObject messageParams) {
        this.messageParams = messageParams;
        return this;
    }

    /** Never null, empty when the column is null. */
    public List<String> getNotificationIds() {
        return notificationIds;
    }

    public PushNotifDto setNotificationIds(List<String> notificationIds) {
        this.notificationIds = notificationIds == null ? Collections.<String>emptyList() : notificationIds;
        return this;
    }

    public Status getStatus() {
        return status;
    }

    public PushNotifDto setStatus(Status status) {
        this.status = status;
        return this;
    }

    public Instant getScheduleAt() {
        return scheduleAt;
    }

    public PushNotifDto setScheduleAt(Instant scheduleAt) {
        this.scheduleAt = scheduleAt;
        return this;
    }

    /** Set by push-manager when it reserves the notification for a sending attempt. */
    public Instant getAttemptAt() {
        return attemptAt;
    }

    public PushNotifDto setAttemptAt(Instant attemptAt) {
        this.attemptAt = attemptAt;
        return this;
    }

    /** Number of sending attempts already counted by push-manager. */
    public int getAttempts() {
        return attempts;
    }

    public PushNotifDto setAttempts(int attempts) {
        this.attempts = attempts;
        return this;
    }

    /**
     * Delivery status of a queued notification. The codes are the ordinals persisted by
     * push-manager's {@code PushNotifEntity.Status}: both enums must keep the same order.
     */
    public enum Status {
        PENDING(0), SUCCESS(1), ERROR(2);

        private final int code;

        Status(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static Status fromCode(Integer code) {
            if (code == null) {
                return null;
            }
            for (final Status status : values()) {
                if (status.code == code) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Unknown push notif status: " + code);
        }
    }

    /** Values of the postgres {@code schedule_type} enum backing the {@code scheduled} column. */
    public enum ScheduleType {
        IMMEDIATE, AT_DATE
    }
}
