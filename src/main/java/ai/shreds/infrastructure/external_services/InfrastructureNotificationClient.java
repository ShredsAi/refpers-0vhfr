package ai.shreds.infrastructure.external_services;

import ai.shreds.application.ports.ApplicationNotificationOutputPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ai.shreds.infrastructure.exceptions.InfrastructureExternalServiceException;

@Service
public class InfrastructureNotificationClient implements ApplicationNotificationOutputPort {

    private final InfrastructureEmailServiceClient emailServiceClient;
    private final InfrastructureSmsGatewayClient smsGatewayClient;

    @Autowired
    public InfrastructureNotificationClient(InfrastructureEmailServiceClient emailServiceClient,
                                             InfrastructureSmsGatewayClient smsGatewayClient) {
        this.emailServiceClient = emailServiceClient;
        this.smsGatewayClient = smsGatewayClient;
    }

    @Override
    public void sendEmail(String to, String subject, String body) {
        try {
            emailServiceClient.sendEmail(to, subject, body);
        } catch (Exception ex) {
            throw new InfrastructureExternalServiceException(
                "Failed to send email: " + ex.getMessage(),
                "EmailServiceClient",
                0
            );
        }
    }

    @Override
    public void sendSms(String phoneNumber, String message) {
        try {
            smsGatewayClient.sendSms(phoneNumber, message);
        } catch (Exception ex) {
            throw new InfrastructureExternalServiceException(
                "Failed to send SMS: " + ex.getMessage(),
                "SmsGatewayClient",
                0
            );
        }
    }
}
