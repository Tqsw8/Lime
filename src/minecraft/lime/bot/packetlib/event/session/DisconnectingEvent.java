package lime.bot.packetlib.event.session;

import lime.bot.packetlib.Session;

/**
 * Called when the session is about to disconnect.
 */
public class DisconnectingEvent implements SessionEvent {
    private Session session;
    private String reason;
    private Throwable cause;

    /**
     * Creates a new DisconnectingEvent instance.
     *
     * @param session Session being disconnected.
     * @param reason  Reason for the session to disconnect.
     */
    public DisconnectingEvent(Session session, String reason) {
        this(session, reason, null);
    }

    /**
     * Creates a new DisconnectingEvent instance.
     *
     * @param session Session being disconnected.
     * @param reason  Reason for the session to disconnect.
     * @param cause   Throwable that caused the disconnect.
     */
    public DisconnectingEvent(Session session, String reason, Throwable cause) {
        this.session = session;
        this.reason = reason;
        this.cause = cause;
    }

    /**
     * Gets the session involved in this event.
     *
     * @return The event's session.
     */
    public Session getSession() {
        return this.session;
    }

    /**
     * Gets the reason given for the session disconnecting.
     *
     * @return The event's reason.
     */
    public String getReason() {
        return this.reason;
    }

    /**
     * Gets the Throwable responsible for the session disconnecting.
     *
     * @return The Throwable responsible for the disconnect, or null if the disconnect was not caused by a Throwable.
     */
    public Throwable getCause() {
        return this.cause;
    }

    @Override
    public void call(SessionListener listener) {
        listener.disconnecting(this);
    }
}
