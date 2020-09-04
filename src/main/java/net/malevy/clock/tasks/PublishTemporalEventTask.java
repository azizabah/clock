package net.malevy.clock.tasks;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.cloudevents.v1.CloudEventBuilder;
import io.cloudevents.v1.CloudEventImpl;
import net.malevy.clock.publishing.Publisher;
import net.malevy.clock.time.DateTimeSupplier;
import net.malevy.clock.time.TemporalEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.http.MediaType;
import org.springframework.integration.leader.event.OnGrantedEvent;
import org.springframework.integration.leader.event.OnRevokedEvent;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.UUID;

@Component
public class PublishTemporalEventTask {

    private static final Logger logger = LoggerFactory.getLogger(PublishTemporalEventTask.class);
    private final Publisher publisher;
    private final DateTimeSupplier dateTimeSupplier;
    private boolean canPublish = false;

    @Autowired
    public PublishTemporalEventTask(Publisher publisher, DateTimeSupplier dateTimeSupplier) {

        this.publisher = publisher;
        this.dateTimeSupplier = dateTimeSupplier;
        this.disablePublishing();
        logger.info("PublishTemporalEventTask has been created");
    }

    @Scheduled(cron = "${schedule}")
    public void publish() throws JsonProcessingException {

        if (!canPublish) {
            logger.debug("temporal event publishing disabled for this node");
            return;
        }

        final ZonedDateTime moment = this.dateTimeSupplier.now();
        final TemporalEvent eventToPublish = new TemporalEvent(moment);

        CloudEventImpl<Object> event = CloudEventBuilder.builder()
                .withId(UUID.randomUUID().toString())
                .withSource(URI.create("urn:clock"))
                .withType("clock.temporal-event")
                .withDataContentType(MediaType.APPLICATION_JSON.toString())
                .withTime(moment)
                .withData(eventToPublish)
                .build();

        this.publisher.publish(event);
        logger.debug("temporal event published");
    }

    public void enablePublishing() {
        logger.info("publishing has been enabled");
        this.canPublish = true;
    }
    public void disablePublishing() {
        logger.warn("publishing has been disabled");
        this.canPublish = false;
    }

    @EventListener
    public void onLeadershipGranted(OnGrantedEvent grantedEvent) {
        this.enablePublishing();
    }

    @EventListener
    public void onLeadershipRevoked(OnRevokedEvent revokedEvent) {
        this.disablePublishing();
    }

}
