package net.malevy.clock.time;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

public interface DateTimeSupplier {

    ZonedDateTime now();

}

