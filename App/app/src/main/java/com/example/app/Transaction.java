package com.example.app;

public class Transaction {
    private String receiver;
    private String date;
    private String time;
    private double amount;
    private String ID;

    public Transaction(String sender, String body, String date, String time) {
        receiver = "XXX";
        this.date = date;
        this.time = time;
        amount = 0.0;
        ID = "XXX";
        if (!sender.equals("AX-MAHABK-S")) {
            return;
        }

        try {
            int amountStart = body.indexOf("Rs.");
            int amountEnd = body.indexOf(" for UPI ");
            int receiverStart = body.indexOf("payment to");
            int receiverEnd = body.indexOf(" on ", receiverStart+11);

            if (amountStart == -1 || amountEnd == -1 || receiverStart == -1 || receiverEnd == -1) {
                return;
            }

            String amountString = body.substring(amountStart + 3, amountEnd).trim();
            amountString = amountString.replace(",", "");
            amount = Double.parseDouble(amountString);

            receiver = body.substring(receiverStart + 11, receiverEnd).trim();

            if (receiver.isEmpty() || amount <= 0) {
                return;
            }

            ID = date + "-" + time;

        } catch (Exception e) {
            ID = "XXX";
            receiver = "XXX";
            amount = 0.0;
        }
    }

    public String getReceiver() { return receiver; }
    public String getTime() { return time; }
    public String getDate() { return date; }
    public double getAmount() { return amount; }
    public String getID() { return ID; }
}
