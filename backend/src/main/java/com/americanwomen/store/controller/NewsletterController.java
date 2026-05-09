package com.americanwomen.store.controller;

import com.americanwomen.store.dto.MessageResponse;
import com.americanwomen.store.dto.NewsletterSubscriptionRequest;
import com.americanwomen.store.service.NewsletterService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/newsletter")
@CrossOrigin(origins = "*")
public class NewsletterController {
    private final NewsletterService newsletterService;

    public NewsletterController(NewsletterService newsletterService) {
        this.newsletterService = newsletterService;
    }

    @PostMapping("/subscribe")
    public ResponseEntity<MessageResponse> subscribe(@Valid @RequestBody NewsletterSubscriptionRequest request) {
        newsletterService.subscribe(request);
        return ResponseEntity.ok(new MessageResponse("Successfully subscribed to our newsletter! Welcome to the community."));
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getSubscriberCount() {
        return ResponseEntity.ok(newsletterService.getSubscriberCount());
    }
}

