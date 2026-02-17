package com.example.venn.utils;

import com.example.venn.models.LoadEvent;
import com.example.venn.models.LoadEventOutput;
import com.example.venn.models.LoadEventRequest;
import com.example.venn.models.LoadEventRequestDeserializer;
import com.example.venn.service.LoadService;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;


import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

public class Utils {

    private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);

    public static List<LoadEventRequest> readLoadEventsFromFile(File inputFile) throws IOException {
        var input = FileUtils.readFileToString(inputFile, StandardCharsets.UTF_8);
        var lineList = input.lines().toList();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(LoadEventRequest.class, new LoadEventRequestDeserializer());
        var objectMapper = JsonMapper.builder()
                .addModule(module)
                .build();
        var result = lineList.stream().map(line -> {
                    try {
                        return objectMapper.readValue(line, LoadEventRequest.class);
                    } catch (Exception e) {
                        LOGGER.warn("Skipping invalid line: {}", line, e);
                        return null;
                    }
                }).filter(Objects::nonNull)
                .toList();
        return result;
    }

    public static void writeToOutputFile(LoadEvent loadEvent, Boolean isAccepted, File outputFile) throws IOException {
        LoadEventOutput loadEventOutput = new LoadEventOutput(loadEvent.loadId().toString(), loadEvent.customerId().toString(), isAccepted);
        var objectMapper = new ObjectMapper();
        var output = objectMapper.writeValueAsString(loadEventOutput) + System.lineSeparator();
        FileUtils.writeStringToFile(outputFile, output, StandardCharsets.UTF_8, true);
    }
}
