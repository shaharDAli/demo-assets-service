package com.demo.assetservice.events.controller;

import com.demo.assetservice.events.dto.PingRequest;
import com.demo.assetservice.events.producer.PingEventProducer;
import com.demo.assetservice.events.mapper.PingResponse;
import com.demo.shared.events.dto.PingEvent;

import java.time.Instant;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/kafka")
public class KafkaTestController {

    private final PingEventProducer pingEventProducer;

    public KafkaTestController(PingEventProducer pingEventProducer) {
        this.pingEventProducer = pingEventProducer;
    }

    @PostMapping("/ping")
    public ResponseEntity<PingResponse> ping(
            @RequestBody(required = false) PingRequest request,
            @RequestParam(required = false) String message) {

        String eventId = (request != null && request.id() != null && !request.id().isBlank())
                ? request.id()
                : UUID.randomUUID().toString();

        String eventMessage = (request != null && request.message() != null && !request.message().isBlank())
                ? request.message()
                : (message != null && !message.isBlank() ? message : "Health check ping");

        String sourceService = (request != null && request.sourceService() != null && !request.sourceService().isBlank())
                ? request.sourceService()
                : "payment-gateway";

        Instant timestamp = (request != null && request.timestamp() != null && request.timestamp() > 0)
                ? Instant.ofEpochMilli(request.timestamp())
                : Instant.now();

        PingEvent pingEvent = pingEventProducer.sendPing(eventId, eventMessage, sourceService, timestamp);
        return ResponseEntity.ok(PingResponse.from(pingEvent));
    }
}
