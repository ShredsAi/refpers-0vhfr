package ai.shreds.application.ports;

/**
 * Output port for notification services (email and SMS).
 */
public interface ApplicationNotificationOutputPort {

    /**
     * Send email notification.
     * @param to recipient email address
     * @param subject email subject
     * @param body email body content
     */
    void sendEmail(String to, String subject, String body);

    /**
     * Send SMS notification.
     * @param phoneNumber recipient phone number
     * @param message SMS message content
     */
    void sendSms(String phoneNumber, String message);
}