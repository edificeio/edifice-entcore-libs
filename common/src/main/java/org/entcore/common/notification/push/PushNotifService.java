package org.entcore.common.notification.push;

import io.vertx.core.Future;
import org.entcore.common.notification.push.impl.SqlPushNotifService;

import java.util.List;

/**
 * Queues push notifications for the push-manager module, which dequeues and sends them to FCM.
 * Nothing here talks to FCM: a queued notification is a promise of a send, not a send.
 */
public interface PushNotifService {

    /**
     * Notifications of a user still waiting to be sent for a given type, oldest first. Meant to be
     * called before writing, when a new notification should be merged into an already queued one
     * rather than queued next to it.
     */
    Future<List<PushNotifDto>> findPending(String userId, String notifType);

    /** Queues a new notification. */
    Future<Void> create(PushNotifBuilder pushNotif);

    /**
     * Rewrites the columns set on the builder, provided the notification is still pending.
     *
     * @return {@code true} when the notification was rewritten, {@code false} when it left the
     * pending state in the meantime — push-manager has sent it or given up on it, and the caller
     * has to queue a new one instead.
     */
    Future<Boolean> update(PushNotifBuilder pushNotif);

    /** Queue held in the push-manager schema of the platform database. */
    static PushNotifService createDefault() {
        return new SqlPushNotifService();
    }
}
