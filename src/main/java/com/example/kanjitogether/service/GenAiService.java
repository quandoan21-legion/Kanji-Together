package com.example.kanjitogether.service;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.kanjitogether.dto.KanjiStory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import com.google.genai.types.Schema;
import com.google.genai.types.Type;

@Service
public class GenAiService {

    private static final Content SYSTEM_INSTRUCTION = Content.fromParts(
        Part.fromText("""
            You write vivid, bite-sized Kanji mnemonic stories.
            Users provide one or more Kanji characters (sometimes repeating the same symbol).
            Produce a JSON array where each element has kanji, meaning, and story fields that match the schema exactly.
            Keep every story under four sentences and describe visuals that tie back to each Kanji's strokes.
            Preserve the original order of the Kanji characters.
            """
        ));

    private static final Schema KANJI_STORY_ARRAY_SCHEMA = buildKanjiStoryArraySchema();

    private final Client client;
    private final ObjectMapper objectMapper;
    private final GenerateContentConfig kanjiStoryConfig;

    public GenAiService(Client client, ObjectMapper objectMapper) {
        this.client = client;
        this.objectMapper = objectMapper;
        this.kanjiStoryConfig = GenerateContentConfig.builder()
            .systemInstruction(SYSTEM_INSTRUCTION)
            .responseMimeType("application/json")
            .responseSchema(KANJI_STORY_ARRAY_SCHEMA)
            .maxOutputTokens(400)
            .build();
    }

    public List<KanjiStory> generateStories(String model, String kanjiSequence) {
        String trimmedPrompt = kanjiSequence.trim();
        String modelPrompt = """
            Generate structured stories for this Kanji sequence. Keep duplicates and order intact.
            Kanji sequence: %s
            """.formatted(trimmedPrompt);

        GenerateContentResponse response =
            client.models.generateContent(model, modelPrompt, kanjiStoryConfig);
        String json = response.text();
        if (json == null || json.isBlank()) {
            throw new IllegalStateException("Model returned empty Kanji stories.");
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<KanjiStory>>() {});
        } catch (IOException ex) {
            throw new IllegalStateException("Model returned invalid Kanji stories JSON.", ex);
        }
    }

    private static Schema buildKanjiStoryArraySchema() {
        Map<String, Schema> properties = new LinkedHashMap<>();
        properties.put("kanji",
            Schema.builder()
                .description("The exact Kanji character that the story explains.")
                .type(new Type(Type.Known.STRING))
                .minLength(1L)
                .maxLength(3L)
                .build());
        properties.put("meaning",
            Schema.builder()
                .description("Plain-language keyword that summarizes the Kanji meaning.")
                .type(new Type(Type.Known.STRING))
                .minLength(1L)
                .build());
        properties.put("story",
            Schema.builder()
                .description("A concise mnemonic story referencing the Kanji's visual components.")
                .type(new Type(Type.Known.STRING))
                .minLength(20L)
                .build());

        Schema storySchema = Schema.builder()
            .title("KanjiStory")
            .description("JSON structure for a short Kanji mnemonic story.")
            .type(new Type(Type.Known.OBJECT))
            .properties(properties)
            .required(List.of("kanji", "meaning", "story"))
            .build();

        return Schema.builder()
            .description("Array of Kanji stories honoring the order of provided characters.")
            .type(new Type(Type.Known.ARRAY))
            .items(storySchema)
            .minItems(1L)
            .build();
    }
}
