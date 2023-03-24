package com.example.cashmanager.Models;

public class Transaction {
    private int _id;
    private double amount;
    private String date;
    private String type;
    private int user_id;
    private String recipient;
    private String description;

    public Transaction(int _id, double amount, String date, String type, int user_id, String recipient, String description) {
        this._id = _id;
        this.amount = amount;
        this.date = date;
        this.type = type;
        this.user_id = user_id;
        this.recipient = recipient;
        this.description = description;
    }

    public Transaction() {
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "_id=" + _id +
                ", amount=" + amount +
                ", date='" + date + '\'' +
                ", type='" + type + '\'' +
                ", user_id=" + user_id +
                ", recipient='" + recipient + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
