package com.example.kanjitogether.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.kanjitogether.dto.KanjiStory;
import com.example.kanjitogether.service.GenAiService;

@ExtendWith(MockitoExtension.class)
class GenerateTextControllerTest {

    @Mock
    private GenAiService genAiService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new GenerateTextController(genAiService)).build();
    }

    @Test
    void generateReturnsResponse() throws Exception {
        List<KanjiStory> stories =
            List.of(new KanjiStory("月", "moon", "A rabbit pounds mochi on the glowing moon."));
        when(genAiService.generateStories(eq("gemini-2.5-flash"), eq("Explain AI")))
            .thenReturn(stories);

        mockMvc.perform(post("/api/genai/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"prompt":"Explain AI"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.model").value("gemini-2.5-flash"))
            .andExpect(jsonPath("$.kanjiStories[0].kanji").value("月"))
            .andExpect(jsonPath("$.kanjiStories[0].meaning").value("moon"))
            .andExpect(jsonPath("$.kanjiStories[0].story").value("A rabbit pounds mochi on the glowing moon."));
    }

    @Test
    void generateUsesProvidedModel() throws Exception {
        List<KanjiStory> stories =
            List.of(new KanjiStory("火", "fire", "Two sparks meet and twist into a campfire."));
        when(genAiService.generateStories(eq("custom-model"), eq("Hi")))
            .thenReturn(stories);

        mockMvc.perform(post("/api/genai/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"model":"custom-model","prompt":"Hi"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.model").value("custom-model"))
            .andExpect(jsonPath("$.kanjiStories[0].story").value("Two sparks meet and twist into a campfire."));
    }

    @Test
    void generateValidatesPrompt() throws Exception {
        mockMvc.perform(post("/api/genai/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"model":"anything"}
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("Prompt is required."));
    }

    @Test
    void generateHandlesErrors() throws Exception {
        when(genAiService.generateStories(eq("gemini-2.5-flash"), eq("Explain AI")))
            .thenThrow(new RuntimeException("Boom"));

        mockMvc.perform(post("/api/genai/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"prompt":"Explain AI"}
                    """))
            .andExpect(status().isInternalServerError())
            .andExpect(content().string("Failed to generate response: Boom"));
    }
}
