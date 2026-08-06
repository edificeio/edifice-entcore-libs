package org.entcore.common.notification.push;

import io.vertx.core.Future;
import org.entcore.common.notification.push.impl.SqlPushNotifService;

/**
 * Queues push notifications for the push-manager module, which dequeues and sends them to FCM.
 * Nothing here talks to FCM: a queued notification is a promise of a send, not a send.
 */
public interface PushNotifService {

    /** Queues a new notification. */
    Future<Void> create(PushNotifBuilder pushNotif);

    /**
     * Appends a timeline notification to the recap already queued for a user, in a single statement:
     * the id joins {@code notification_ids}, the {@code count} message param is bumped, and the body
     * is rendered again from {@code bodyTemplate} against that new count.
     *
     * <p>The counter belongs to this method, the wording to the caller: {@code bodyTemplate} is the
     * body already translated and truncated, in which {@code countPlaceholder} stands for the count.
     *
     * @return {@code true} when a recap was updated, {@code false} when there was none to update —
     * none queued, or push-manager already reserved the one queued — and the caller has to queue a
     * new recap instead.
     */
    Future<Boolean> appendToPendingRecap(String userId, String notifType, String notificationId,
                                         String bodyTemplate, String countPlaceholder);

    /** Queue held in the push-manager schema of the platform database. */
    static PushNotifService createDefault() {
        return new SqlPushNotifService();
    }
}
