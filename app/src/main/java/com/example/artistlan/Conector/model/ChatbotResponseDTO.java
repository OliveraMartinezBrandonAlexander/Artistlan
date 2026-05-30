package com.example.artistlan.Conector.model;

import java.util.List;

public class ChatbotResponseDTO {

    private String reply;
    private String intent;
    private String source;
    private Double confidence;
    private List<String> quickReplies;
    private List<ChatbotActionDTO> actions;

    public ChatbotResponseDTO() {
    }

    public ChatbotResponseDTO(String reply, String intent, String source, Double confidence,
                              List<String> quickReplies, List<ChatbotActionDTO> actions) {
        this.reply = reply;
        this.intent = intent;
        this.source = source;
        this.confidence = confidence;
        this.quickReplies = quickReplies;
        this.actions = actions;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public String getIntent() {
        return intent;
    }

    public void setIntent(String intent) {
        this.intent = intent;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    public List<String> getQuickReplies() {
        return quickReplies;
    }

    public void setQuickReplies(List<String> quickReplies) {
        this.quickReplies = quickReplies;
    }

    public List<ChatbotActionDTO> getActions() {
        return actions;
    }

    public void setActions(List<ChatbotActionDTO> actions) {
        this.actions = actions;
    }
}
