package net.malevy.clock.publishing;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.cloudevents.v1.CloudEventBuilder;
import io.cloudevents.v1.CloudEventImpl;
import net.malevy.clock.ClockApplication;
import net.malevy.clock.time.TemporalEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.cloud.stream.test.binder.MessageCollectorAutoConfiguration;
import org.springframework.http.MediaType;
import org.springframework.messaging.Message;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("test")
@SpringBootTest(
        properties = {
                "spring.cloud.stream.bindings.temporalEvents.binder=test"
        },

        // I really don't understand why I have to explicitly pull in the auto-config for the MessageCollector
        classes = {ClockApplication.class, MessageCollectorAutoConfiguration.class}
)
@DirtiesContext
public class PublisherIT {

    @Autowired
    private Source source;

    @Autowired
    private MessageCollector collector;

    private Message<String> actualPublishedEvent;

    @Before
    @SuppressWarnings("unchecked")
    public void whenAnEventIsPublished() throws JsonProcessingException {
        ZonedDateTime moment = ZonedDateTime.of(2017, 12, 27, 17, 05, 0, 0, ZoneId.of("UTC"));
        final TemporalEvent tevent = new TemporalEvent(moment);
        final Publisher publisher = new Publisher(source);

        CloudEventImpl<Object> event = CloudEventBuilder.builder()
                .withId(UUID.randomUUID().toString())
                .withSource(URI.create("urn:clock"))
                .withType("clock.temporal-event")
                .withDataContentType(MediaType.APPLICATION_JSON.toString())
                .withTime(moment)
                .withData(tevent)
                .build();

        publisher.publish(event);

        actualPublishedEvent = (Message<String>) collector.forChannel(source.temporalEvents()).poll();
    }

    @Test
    public void thenAnEventResults() {
        assertThat(actualPublishedEvent, notNullValue());
    }

}
