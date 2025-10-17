package com.example.app;

public class ReceiverModel {
    private String receiver;
    private String group;
    private String colour;

    public ReceiverModel(String receiver, String group, String colour) {
        this.receiver = receiver;
        this.group = group;
        this.colour = colour;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getColour() {
        return colour;
    }

    public void setColour(String colour) {
        this.colour = colour;
    }
}
