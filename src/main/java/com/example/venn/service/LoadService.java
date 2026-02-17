package com.example.venn.service;

import com.example.venn.models.LoadEvent;
import com.example.venn.models.LoadEventRequest;
import com.example.venn.repository.LoadRepository;
import com.example.venn.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;

@Service
public class LoadService {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoadService.class);

    private final LoadRepository loadRepository;
    private final File outputFile;

    public LoadService(@Autowired LoadRepository loadRepository, @Autowired File outputFile) {
        this.outputFile = outputFile;
        this.loadRepository = loadRepository;
    }

    //max of 3 load events per day
    private Boolean isDailyNumberOfDailyLoadEventsViolated(LoadEventRequest loadEvent) {
        var customerId = loadEvent.customerId();
        var eventTimestamp = loadEvent.time();
        var timerangeDays = 1;
        LocalDateTime timestamp = eventTimestamp.atZone(ZoneOffset.UTC).toLocalDateTime();
        var startOfDayTimestamp = timestamp.toLocalDate().atStartOfDay();
        var startTime = startOfDayTimestamp.toInstant(ZoneOffset.UTC);
        var endTime = startOfDayTimestamp.plusDays(timerangeDays).toInstant(ZoneOffset.UTC);
        var foundEvents = loadRepository.findEventsByCustomerIdAndTimerange(customerId, true, startTime, endTime);
        var currentNumEvents = foundEvents.size();
        var limitAmount = 3;
        var totalEvents = currentNumEvents + 1;
        if (totalEvents <= limitAmount) {
            return false;
        } else {
            return true;
        }
    }

    //max of $20000 per week
    private Boolean isWeeklyLimitViolated(LoadEventRequest loadEvent) {
        var loadAmount = loadEvent.loadAmount();
        var customerId = loadEvent.customerId();
        var eventTimestamp = loadEvent.time();
        LocalDateTime timestamp = eventTimestamp.atZone(ZoneOffset.UTC).toLocalDateTime();
        var startOfDayTimestamp = timestamp.toLocalDate().atStartOfDay();
        var startOfWeek = startOfDayTimestamp
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .with(LocalTime.MIN);
        var startTime = startOfWeek.toInstant(ZoneOffset.UTC);
        var endTime = timestamp.toInstant(ZoneOffset.UTC);
        var foundEvents = loadRepository.findEventsByCustomerIdAndTimerange(customerId, true, startTime, endTime);
        var currentSum = foundEvents.stream().mapToDouble(LoadEvent::loadAmount).sum();
        var limitAmount = 20000.0;
        var totalSum = currentSum + loadAmount;
        if (totalSum <= limitAmount) {
            return false;
        } else {
            return true;
        }
    }

    //max of $5000 per day
    private Boolean isDailyLimitViolated(LoadEventRequest loadEvent) {
        var loadAmount = loadEvent.loadAmount();
        var customerId = loadEvent.customerId();
        var eventTimestamp = loadEvent.time();
        var timerangeDays = 1;
        LocalDateTime timestamp = eventTimestamp.atZone(ZoneOffset.UTC).toLocalDateTime();
        var startOfDayTimestamp = timestamp.toLocalDate().atStartOfDay();
        var startTime = startOfDayTimestamp.toInstant(ZoneOffset.UTC);
        var endTime = startOfDayTimestamp.plusDays(timerangeDays).toInstant(ZoneOffset.UTC);
        var foundEvents = loadRepository.findEventsByCustomerIdAndTimerange(customerId, true, startTime, endTime);
        var currentSum = foundEvents.stream().mapToDouble(LoadEvent::loadAmount).sum();
        var limitAmount = 5000.0;
        var totalSum = currentSum + loadAmount;
        if (totalSum <= limitAmount) {
            return false;
        } else {
            return true;
        }
    }

    private Boolean isLoadEventProcessed(LoadEventRequest loadEvent) {
        var foundLoadEvent = loadRepository.findEventsByLoadIdAndCustomerId(loadEvent.id(), loadEvent.customerId());
        if (!foundLoadEvent.isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    private void persistEventClassification(LoadEventRequest inputEvent, Boolean accepted, String statusMessage) throws IOException {
        var loadEvent = new LoadEvent(null, inputEvent.id(),
                inputEvent.customerId(), inputEvent.loadAmount(), inputEvent.time(), accepted, statusMessage);
        loadRepository.save(loadEvent);
        Utils.writeToOutputFile(loadEvent, accepted, outputFile);
        LOGGER.info("Load id: {} Customer id: {} Message: {}", loadEvent.loadId(), loadEvent.customerId(), statusMessage);
    }

    public void processLoadEvent(LoadEventRequest inputEvent) throws IOException {
        LOGGER.debug("Load id: {} Customer id: {} Message: Processing load event", inputEvent.id(), inputEvent.customerId());
        Boolean isLoadEventProcessed = isLoadEventProcessed(inputEvent);
        if (isLoadEventProcessed) {
            LOGGER.info("Load id: {} Customer id: {} Message: Already processed load event", inputEvent.id(), inputEvent.customerId());
            return;
        }
        Boolean isDailyLimitViolated = isDailyLimitViolated(inputEvent);
        if (isDailyLimitViolated) {
            var statusMessage = "daily limit violated";
            persistEventClassification(inputEvent, false, statusMessage);
            return;
        }
        Boolean isWeeklyLimitViolated = isWeeklyLimitViolated(inputEvent);
        if (isWeeklyLimitViolated) {
            var statusMessage = "weekly limit violated";
            persistEventClassification(inputEvent, false, statusMessage);
            return;
        }
        Boolean isDailyNumberOfLoadEventsLimitViolated = isDailyNumberOfDailyLoadEventsViolated(inputEvent);
        if (isDailyNumberOfLoadEventsLimitViolated) {
            var statusMessage = "daily number of events limit violated";
            persistEventClassification(inputEvent, false, statusMessage);
            return;
        }
        var statusMessage = "successful";
        persistEventClassification(inputEvent, true, statusMessage);
    }

}
