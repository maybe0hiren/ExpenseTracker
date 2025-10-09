package com.example.app;

public class Transaction {
    private String receiver;
    private String date;
    private String time;
    private double amount;
    private String ID;
    public Transaction(String sender, String body, String  date, String time) {
        if(!sender.equals("AX-MAHABK-S")){
            amount = 0;
            receiver = "XXX";
            this.date = "XXX";
            this.time = "XXX";
            ID = "XXX";
            return;
        }
        amount = Double.parseDouble(body.substring(body.indexOf("Rs.") + 4, body.indexOf(" for UPI ")).trim());
        receiver = body.substring(body.indexOf("payment to") + 11, body.indexOf(" on ")).trim();
        this.date = date;
        this.time = time;
        ID = date.concat("-").concat(time);
    }
    public String getReceiver(){ return receiver; }
    public String getTime(){ return time; }
    public String getDate(){ return date; }
    public double getAmount(){ return amount; }
    public String getID() { return ID; }

}
