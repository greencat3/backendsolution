package com.example.venn.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LoadEventOutput(String id, @JsonProperty("customer_id") String customerId, Boolean accepted) {
}
