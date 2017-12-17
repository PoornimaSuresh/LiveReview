package com.mobile.project.livereview.entity;

/**
 * Created by PS-Student on 11/25/17.
 */

public class ChatBubble {
    private String content;
    private boolean myMessage;

    public ChatBubble(String content, boolean myMessage) {
        this.content = content;
        this.myMessage = myMessage;
    }

    public String getContent() {
        return content;
    }

    public boolean myMessage() {
        return myMessage;
    }

    //public boolean isMyMessage() { return myMessage; }
}

