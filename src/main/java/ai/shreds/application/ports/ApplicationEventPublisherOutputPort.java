package ai.shreds.application.ports;

public interface ApplicationEventPublisherOutputPort {

    /**
     * Publish a generic application event for other shreds to consume.
     * @param event the event payload
     */
    void publishEvent(Object event);
}
