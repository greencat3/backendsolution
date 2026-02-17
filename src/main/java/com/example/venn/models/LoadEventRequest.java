package com.example.venn.models;

import java.time.Instant;

public record LoadEventRequest(Long id, Long customerId, Double loadAmount, Instant time) {
}
