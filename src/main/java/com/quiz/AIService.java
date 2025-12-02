package com.quiz;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;

public class AIService {

    // ⚠️ SECURITY NOTE: Do not commit your actual API Key!
    // Instructions: Get a free key from https://aistudio.google.com/ and paste it below to run locally.
    private static final String API_KEY = "PASTE_YOUR_API_KEY_HERE";
    // ✅ FIX: Switched to 'gemini-2.5-flash' because 1.5 has been shut down.
    private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + API_KEY;

    public List<String[]> generateQuestions(String topic) {
        System.out.println("🤖 CONNECTING TO GOOGLE AI (" + topic + ")...");
        List<String[]> questions = new ArrayList<>();

        try {
            // 1. JSON Payload
            String prompt = "Generate 5 multiple choice questions about " + topic + ". " +
                            "Format strictly like this: Question|Option1|Option2|Option3|CorrectAnswer. " +
                            "Do not add markdown. Do not add numbering.";

            String jsonBody = "{"
                    + "\"contents\": [{"
                    + "\"parts\": [{\"text\": \"" + prompt + "\"}]"
                    + "}]"
                    + "}";

            // 2. Build Request
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GEMINI_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            // 3. Send Request
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // 🔴 DEBUG PRINT
            if (response.statusCode() != 200) {
                System.out.println("❌ GOOGLE ERROR: " + response.statusCode());
                System.out.println(response.body());
            }

            // 4. Parse JSON
            if (response.statusCode() == 200) {
                JSONObject jsonResponse = new JSONObject(response.body());
                String rawText = jsonResponse.getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text");

                String[] lines = rawText.split("\n");
                for (String line : lines) {
                    if (line.contains("|")) {
                        String[] parts = line.split("\\|");
                        if (parts.length >= 5) {
                            questions.add(new String[]{
                                parts[0].trim(), parts[1].trim(), parts[2].trim(), parts[3].trim(), parts[4].trim()
                            });
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // BACKUP MODE: If API fails, load these so the app doesn't crash
        if (questions.isEmpty()) {
            System.out.println("⚠️ Switching to Offline Mode.");
            if(topic.equalsIgnoreCase("Java")) {
                questions.add(new String[]{"Size of int?", "4 bytes", "8", "2", "4 bytes"});
                questions.add(new String[]{"Inheritance keyword?", "extends", "implements", "super", "extends"});
            } else {
                questions.add(new String[]{"Value of Pi?", "3.14", "3.41", "3.12", "3.14"});
                questions.add(new String[]{"Speed of light?", "3x10^8", "300", "Infinite", "3x10^8"});
            }
        }

        return questions;
    }
}