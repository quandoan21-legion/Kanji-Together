package com.example.kanjitogether.dto;

import java.util.List;

public class GenerateTextResponse {

    private final String model;
    private final List<KanjiStory> kanjiStories;

    public GenerateTextResponse(String model, List<KanjiStory> kanjiStories) {
        this.model = model;
        this.kanjiStories = kanjiStories;
    }

    public String getModel() {
        return model;
    }

    public List<KanjiStory> getKanjiStories() {
        return kanjiStories;
    }
}
