package com.example.app;

public class GroupSpending {
    private String groupName;
    private double totalSpending;
    private String color;

    public GroupSpending(String groupName, double totalSpending, String color) {
        this.groupName = groupName;
        this.totalSpending = totalSpending;
        this.color = color;
    }

    public String getGroupName() {
        return groupName;
    }

    public double getTotalSpending() {
        return totalSpending;
    }

    public String getColor() {
        return color;
    }

    public void addSpending(double amount) {
        this.totalSpending += amount;
    }
}
