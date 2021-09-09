package com.michael.sender;

// This is the data model for the sender

public class Info {
    private String location;
    private String status;
    private String responder;

    public Info() {
    }

    public Info(String status) {
        this.location = "";
        this.status = status;
    }

    public String getResponder() {
        return responder;
    }

    public void setResponder(String responder) {
        this.responder = responder;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}