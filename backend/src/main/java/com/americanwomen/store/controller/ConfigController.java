package com.americanwomen.store.controller;

import com.americanwomen.store.dto.ConfigResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/config")
@CrossOrigin(origins = "*")
public class ConfigController {
    @Value("${server.port:8090}")
    private int serverPort;

    @GetMapping
    public ResponseEntity<ConfigResponse> getConfig() {
        String apiBase = "http://localhost:" + serverPort + "/api";
        return ResponseEntity.ok(new ConfigResponse(apiBase));
    }
}

