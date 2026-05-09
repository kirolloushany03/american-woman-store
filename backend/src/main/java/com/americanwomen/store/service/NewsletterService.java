package com.americanwomen.store.service;

import com.americanwomen.store.dto.NewsletterSubscriptionRequest;
import com.americanwomen.store.entity.NewsletterSubscriber;
import com.americanwomen.store.repository.NewsletterSubscriberRepository;
import com.americanwomen.store.ocl.OclValidationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class NewsletterService {
    private final NewsletterSubscriberRepository subscriberRepository;
    private final OclValidationService oclValidationService;

    public NewsletterService(NewsletterSubscriberRepository subscriberRepository, OclValidationService oclValidationService) {
        this.subscriberRepository = subscriberRepository;
        this.oclValidationService = oclValidationService;
    }

    @Transactional
    public NewsletterSubscriber subscribe(NewsletterSubscriptionRequest request) {
        // Check if email already exists
        return subscriberRepository.findByEmail(request.getEmail())
                .map(existing -> {
                    // If exists but inactive, reactivate
                    if (!existing.getActive()) {
                        existing.setActive(true);
                        existing.setSubscribedAt(LocalDateTime.now());
                        existing.setUnsubscribedAt(null);
                        return subscriberRepository.save(existing);
                    }
                    // If already active, return existing
                    return existing;
                })
                .orElseGet(() -> {
                    // Create new subscriber
                    NewsletterSubscriber subscriber = new NewsletterSubscriber();
                    subscriber.setEmail(request.getEmail());
                    subscriber.setFirstName(request.getFirstName());
                    subscriber.setLastName(request.getLastName());
                    subscriber.setActive(true);
                    subscriber.setSubscribedAt(LocalDateTime.now());
                    
                    // OCL validation before saving
                    oclValidationService.validateNewsletterSubscriber(subscriber);
                    
                    return subscriberRepository.save(subscriber);
                });
    }

    public long getSubscriberCount() {
        return subscriberRepository.countByActiveTrue();
    }
}

