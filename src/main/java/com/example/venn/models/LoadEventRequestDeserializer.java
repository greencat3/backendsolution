package com.example.venn.models;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.deser.std.StdDeserializer;

import java.time.Instant;

public class LoadEventRequestDeserializer extends StdDeserializer<LoadEventRequest> {

    public LoadEventRequestDeserializer() {
        super(LoadEventRequest.class);
    }

    @Override
    public LoadEventRequest deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
        JsonNode node = ctxt.readTree(p);
        Long id = Long.parseLong(node.get("id").asText());
        Long customerId = Long.parseLong(node.get("customer_id").asText());
        String loadAmountString = node.get("load_amount").asText();
        Double loadAmount = Double.parseDouble(loadAmountString.replace("$", ""));
        String timeString = node.get("time").asText();
        Instant time = Instant.parse(timeString);
        LoadEventRequest result = new LoadEventRequest(id, customerId, loadAmount, time);
        return result;
    }
}
