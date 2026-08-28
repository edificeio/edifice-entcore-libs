package org.entcore.common.notification.push;

/**
 * Delivery status of a queued push notification. The codes are the ordinals persisted by
 * push-manager's {@code PushNotifEntity.Status}: both enums must keep the same order.
 */
public enum PushNotifStatus {
    PENDING(0), SUCCESS(1), ERROR(2);

    private final int code;

    PushNotifStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
