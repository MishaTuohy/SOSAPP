package com.michael.helper;

public class NotificationsModel {
    private String location;
    private String gLocation;
    private String status;
    private String responder;

    public NotificationsModel() {
    }

    public NotificationsModel(String status) {
        this.gLocation = "";
        this.location = "";
        this.status = status;
    }

    public String getResponder() {
        return responder;
    }

    public void setResponder(String responder) {
        this.responder = responder;
    }

    public String getgLocation() {
        return gLocation;
    }

    public void setgLocation(String gLocation) {
        this.gLocation = gLocation;
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