package com.example.venn.models;

import org.springframework.data.annotation.Id;

import java.time.Instant;

public record LoadEvent(@Id Long id, Long loadId, Long customerId, Double loadAmount, Instant time, Boolean accepted,
                        String status) {
}
