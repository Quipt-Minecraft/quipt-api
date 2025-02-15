package com.quiptmc.api.feedback;

public record Feedback(Result result, String message) {


    public String json() {
        return "{\"result\":\"" + result + "\",\"message\":\"" + message + "\"}";
    }

    public String raw() {
        return result + ": " + message;
    }

    public String xml(){
        return "<feedback><result>" + result + "</result><message>" + message + "</message></feedback>";
    }

    public enum Result {
        SUCCESS,
        FAILURE
    }
}
