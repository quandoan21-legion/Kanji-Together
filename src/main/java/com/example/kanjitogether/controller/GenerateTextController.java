package com.example.kanjitogether.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.kanjitogether.dto.GenerateTextRequest;
import com.example.kanjitogether.dto.GenerateTextResponse;
import com.example.kanjitogether.service.GenAiService;

@RestController
@RequestMapping("/api/genai")
public class GenerateTextController {

    private static final String DEFAULT_MODEL = "gemini-2.5-flash";

    private final GenAiService genAiService;

    public GenerateTextController(GenAiService genAiService) {
        this.genAiService = genAiService;
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generate(@RequestBody GenerateTextRequest request) {
        if (request == null || request.getPrompt() == null || request.getPrompt().isBlank()) {
            return ResponseEntity.badRequest().body("Prompt is required.");
        }

        String model = request.resolveModel(DEFAULT_MODEL);
        try {
            var stories = genAiService.generateStories(model, request.getPrompt());
            return ResponseEntity.ok(new GenerateTextResponse(model, stories));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to generate response: " + ex.getMessage());
        }
    }
}
