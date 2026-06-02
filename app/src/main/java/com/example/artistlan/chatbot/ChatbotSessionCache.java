package com.example.artistlan.chatbot;

import com.example.artistlan.Conector.model.ChatbotActionDTO;
import com.example.artistlan.model.ChatbotMessageUi;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ChatbotSessionCache {

    private static final Object LOCK = new Object();
    private static String sessionId;
    private static final List<ChatbotMessageUi> messages = new ArrayList<>();

    private ChatbotSessionCache() {
    }

    public static String getOrCreateSessionId() {
        synchronized (LOCK) {
            if (sessionId == null || sessionId.trim().isEmpty()) {
                sessionId = UUID.randomUUID().toString();
            }
            return sessionId;
        }
    }

    public static boolean hasMessages() {
        synchronized (LOCK) {
            return !messages.isEmpty();
        }
    }

    public static List<ChatbotMessageUi> getMessagesSnapshot() {
        synchronized (LOCK) {
            List<ChatbotMessageUi> snapshot = new ArrayList<>(messages.size());
            for (ChatbotMessageUi message : messages) {
                ChatbotMessageUi copy = copyMessage(message);
                if (copy != null) {
                    snapshot.add(copy);
                }
            }
            return snapshot;
        }
    }

    public static void addMessage(ChatbotMessageUi message) {
        ChatbotMessageUi copy = copyMessage(message);
        if (copy == null) {
            return;
        }
        synchronized (LOCK) {
            messages.add(copy);
        }
    }

    public static void clear() {
        synchronized (LOCK) {
            sessionId = null;
            messages.clear();
        }
    }

    private static ChatbotMessageUi copyMessage(ChatbotMessageUi message) {
        if (message == null || message.isLoading()) {
            return null;
        }
        return new ChatbotMessageUi(
                message.getText(),
                message.isFromUser(),
                message.getIntent(),
                message.getSource(),
                copyQuickReplies(message.getQuickReplies()),
                copyActions(message.getActions()),
                false
        );
    }

    private static List<String> copyQuickReplies(List<String> quickReplies) {
        return quickReplies == null ? new ArrayList<>() : new ArrayList<>(quickReplies);
    }

    private static List<ChatbotActionDTO> copyActions(List<ChatbotActionDTO> actions) {
        List<ChatbotActionDTO> copy = new ArrayList<>();
        if (actions == null) {
            return copy;
        }
        for (ChatbotActionDTO action : actions) {
            if (action == null) {
                continue;
            }
            copy.add(new ChatbotActionDTO(action.getLabel(), action.getType()));
        }
        return copy;
    }
}
