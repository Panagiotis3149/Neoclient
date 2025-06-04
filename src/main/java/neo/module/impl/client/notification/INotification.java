package neo.module.impl.client.notification;

import neo.module.impl.client.Notifications;

public interface INotification {

    void render(Notifications.Notification notification);
}