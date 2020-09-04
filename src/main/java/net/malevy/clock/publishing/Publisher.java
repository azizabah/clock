package net.malevy.clock.publishing;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.cloudevents.json.Json;
import io.cloudevents.v1.CloudEventImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
public class Publisher {

    private static Logger logger = LoggerFactory.getLogger(Publisher.class);
    private final Source source;

    public Publisher(Source source) {
        this.source = source;
    }

    public void publish(CloudEventImpl<Object> cloudEvent) throws JsonProcessingException {

        String serializedValue = Json.encode(cloudEvent);

        Message<String> message = MessageBuilder
                .withPayload(serializedValue)
                .setHeader("contentType", MediaType.APPLICATION_JSON_VALUE)
                .build();

        try {
            logger.debug("publishing...");
            this.source.temporalEvents().send(message);
        } catch (Exception e) {
            logger.error("an error was encountered while publishing", e);
            // just logging the error
        }
    }

}
