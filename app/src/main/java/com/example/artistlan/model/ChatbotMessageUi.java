package com.example.artistlan.model;

import com.example.artistlan.Conector.model.ChatbotActionDTO;

import java.util.ArrayList;
import java.util.List;

public class ChatbotMessageUi {

    private String text;
    private boolean fromUser;
    private String intent;
    private String source;
    private List<String> quickReplies;
    private List<ChatbotActionDTO> actions;
    private boolean loading;

    public ChatbotMessageUi() {
        this.quickReplies = new ArrayList<>();
        this.actions = new ArrayList<>();
    }

    public ChatbotMessageUi(String text, boolean fromUser, String intent, String source,
                            List<String> quickReplies, List<ChatbotActionDTO> actions,
                            boolean loading) {
        this.text = text;
        this.fromUser = fromUser;
        this.intent = intent;
        this.source = source;
        this.quickReplies = quickReplies != null ? quickReplies : new ArrayList<>();
        this.actions = actions != null ? actions : new ArrayList<>();
        this.loading = loading;
    }

    public static ChatbotMessageUi user(String text) {
        return new ChatbotMessageUi(text, true, null, null, null, null, false);
    }

    public static ChatbotMessageUi bot(String text, String intent, String source,
                                       List<String> quickReplies, List<ChatbotActionDTO> actions) {
        return new ChatbotMessageUi(text, false, intent, source, quickReplies, actions, false);
    }

    public static ChatbotMessageUi loading(String text) {
        return new ChatbotMessageUi(text, false, null, null, null, null, true);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isFromUser() {
        return fromUser;
    }

    public void setFromUser(boolean fromUser) {
        this.fromUser = fromUser;
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

    public List<String> getQuickReplies() {
        return quickReplies;
    }

    public void setQuickReplies(List<String> quickReplies) {
        this.quickReplies = quickReplies != null ? quickReplies : new ArrayList<>();
    }

    public List<ChatbotActionDTO> getActions() {
        return actions;
    }

    public void setActions(List<ChatbotActionDTO> actions) {
        this.actions = actions != null ? actions : new ArrayList<>();
    }

    public boolean isLoading() {
        return loading;
    }

    public void setLoading(boolean loading) {
        this.loading = loading;
    }
}
