package org.example.model;

public class ChatMessage {
    private int id;
    private String senderName;
    private String senderAvatar;
    private String message;
    private boolean isMyMessage; // Để xác định tin nhắn của mình hay người khác

    public ChatMessage(int id, String senderName, String senderAvatar, String message, boolean isMyMessage) {
        this.id = id;
        this.senderName = senderName;
        this.senderAvatar = senderAvatar;
        this.message = message;
        this.isMyMessage = isMyMessage;
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    public String getSenderAvatar() { return senderAvatar; }
    public void setSenderAvatar(String senderAvatar) { this.senderAvatar = senderAvatar; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public boolean isMyMessage() { return isMyMessage; }
    public void setIsMyMessage(boolean isMyMessage) { this.isMyMessage = isMyMessage; }
}