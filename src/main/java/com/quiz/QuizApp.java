package com.quiz;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.List;

public class QuizApp extends Application {

    private AIService aiService = new AIService();
    private int currentQuestionIndex = 0;
    private int score = 0;
    private List<String[]> questions;
    
    private Label questionLabel = new Label();
    private Button opt1 = new Button();
    private Button opt2 = new Button();
    private Button opt3 = new Button();
    private Label scoreLabel = new Label("Score: 0");
    private Label statusLabel = new Label("Topic: Java");

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("AI-Enhanced Quiz Platform ⚡");

        // UI Setup
        Button javaBtn = createTopicButton("Java");
        Button pythonBtn = createTopicButton("Python");
        Button mathBtn = createTopicButton("Quantum Physics"); // Fun AI Test

        HBox topicMenu = new HBox(10, javaBtn, pythonBtn, mathBtn);
        topicMenu.setAlignment(Pos.CENTER);

        VBox layout = new VBox(20);
        layout.setPadding(new Insets(30));
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #1e1e24;");

        Label header = new Label("🤖 AI Quiz Gen");
        header.setStyle("-fx-text-fill: #6366f1; -fx-font-size: 32px; -fx-font-weight: bold;");

        statusLabel.setStyle("-fx-text-fill: #a3a3a3; -fx-font-size: 14px;");
        scoreLabel.setStyle("-fx-text-fill: #22c55e; -fx-font-size: 18px; -fx-font-weight: bold;");

        questionLabel.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-wrap-text: true;");
        questionLabel.setWrapText(true);
        questionLabel.setMaxWidth(500);
        questionLabel.setAlignment(Pos.CENTER);
        questionLabel.setText("Select a topic to start generating questions with AI!");

        styleAnswerButton(opt1);
        styleAnswerButton(opt2);
        styleAnswerButton(opt3);
        
        // Hide options initially
        opt1.setVisible(false); opt2.setVisible(false); opt3.setVisible(false);

        layout.getChildren().addAll(header, statusLabel, topicMenu, scoreLabel, questionLabel, opt1, opt2, opt3);
        
        Scene scene = new Scene(layout, 600, 650);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private Button createTopicButton(String topic) {
        Button btn = new Button(topic);
        btn.setStyle("-fx-background-color: #6366f1; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        btn.setOnAction(e -> loadTopic(topic));
        return btn;
    }

    private void styleAnswerButton(Button btn) {
        btn.setMinWidth(300);
        btn.setMinHeight(50);
        btn.setStyle("-fx-background-color: #2a2a35; -fx-text-fill: white; -fx-font-size: 16px; -fx-border-color: #6366f1; -fx-border-radius: 10;");
        btn.setOnAction(e -> handleAnswer(btn.getText()));
    }

    // 🔴 BACKGROUND THREADING FOR AI CALL
    private void loadTopic(String topic) {
        questionLabel.setText("🤖 Asking Gemini AI to generate " + topic + " questions...\nPlease wait...");
        opt1.setVisible(false); opt2.setVisible(false); opt3.setVisible(false);
        scoreLabel.setText("Score: 0");
        score = 0;
        currentQuestionIndex = 0;

        new Thread(() -> {
            List<String[]> fetchedQuestions = aiService.generateQuestions(topic);
            
            javafx.application.Platform.runLater(() -> {
                questions = fetchedQuestions;
                if (!questions.isEmpty() && !questions.get(0)[0].contains("Error")) {
                    opt1.setVisible(true); opt2.setVisible(true); opt3.setVisible(true);
                    opt1.setDisable(false); opt2.setDisable(false); opt3.setDisable(false);
                    statusLabel.setText("Current Topic: " + topic);
                    loadQuestion();
                } else {
                    questionLabel.setText("⚠️ Failed to generate questions. Check API Key.");
                }
            });
        }).start();
    }

    private void loadQuestion() {
        if (currentQuestionIndex < questions.size()) {
            String[] q = questions.get(currentQuestionIndex);
            questionLabel.setText(q[0]);
            opt1.setText(q[1]);
            opt2.setText(q[2]);
            opt3.setText(q[3]);
        } else {
            questionLabel.setText("🎉 Quiz Completed! Final Score: " + score + "/" + questions.size());
            opt1.setDisable(true); opt2.setDisable(true); opt3.setDisable(true);
        }
    }

    private void handleAnswer(String selected) {
        String correctAnswer = questions.get(currentQuestionIndex)[4];
        if (selected.equals(correctAnswer)) score++;
        scoreLabel.setText("Score: " + score);
        currentQuestionIndex++;
        loadQuestion();
    }
}