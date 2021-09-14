package lime.ui.notifications;

import lime.core.events.impl.Event2D;

import java.util.ArrayList;

public class NotificationManager {
    private final ArrayList<Notification> notifications;

    public NotificationManager() {
        this.notifications = new ArrayList<>();
    }

    public void addNotification(String title, String description, Notification.Type type) {
        this.notifications.add(new Notification(title, description, type));
    }

    public void addNotification(String title, String description, int time, Notification.Type type) {
        this.notifications.add(new Notification(title, description, time, type));
    }

    public void drawNotifications(Event2D e) {
        int i = 0;
        for (Notification notification : notifications) {
            notification.drawNotification(e.getScaledResolution().getScaledWidth() - notification.getWidth() - 4, e.getScaledResolution().getScaledHeight() - notification.getHeight() - (i * (notification.getHeight() + 4) + 4));
            ++i;
        }

        notifications.removeIf(Notification::isDone);
    }
}
