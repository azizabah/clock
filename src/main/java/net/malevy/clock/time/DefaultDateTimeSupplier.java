package net.malevy.clock.time;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * returns the current date and time in UTC
 */
@Service
public class DefaultDateTimeSupplier implements DateTimeSupplier {
    @Override
    public ZonedDateTime now() {
        return ZonedDateTime.now(ZoneId.of("UTC"));
    }
}
