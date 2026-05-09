package com.americanwomen.store.controller;

import com.americanwomen.store.dto.ContactRequest;
import com.americanwomen.store.dto.MessageResponse;
import com.americanwomen.store.service.ContactService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contact")
@CrossOrigin(origins = "*")
public class ContactController {
    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @PostMapping
    public ResponseEntity<MessageResponse> submitContact(@Valid @RequestBody ContactRequest request) {
        contactService.saveContactMessage(request);
        return ResponseEntity.ok(new MessageResponse("Message sent successfully"));
    }
}

