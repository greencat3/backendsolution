package com.example.venn.config;

import com.example.venn.service.LoadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;

import static com.example.venn.utils.Utils.readLoadEventsFromFile;

@Configuration
public class ProcessInputConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessInputConfig.class);
    private final LoadService loadService;
    private final File inputFile;
    private final File outputFile;

    public ProcessInputConfig(@Autowired LoadService loadService, @Autowired File inputFile, @Autowired File outputFile) {
        this.inputFile = inputFile;
        this.outputFile = outputFile;
        this.loadService = loadService;
    }

    @Bean
    public ApplicationRunner runAfterStartup() {
        LOGGER.info("Starting to process input load events");
        return args -> {
            if (outputFile.exists()) {
                outputFile.delete();
            }
            var loadEvents = readLoadEventsFromFile(inputFile);
            loadEvents.stream().forEach(loadEvent -> {
                try {
                    loadService.processLoadEvent(loadEvent);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        };
    }
}
