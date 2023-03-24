package com.example.cashmanager.Models;

public class Shopping {
    private int _id;
    private int user_id;
    private int item_id;
    private int transaction_id;
    private double price;
    private String description;
    private String date;

    public Shopping(int _id, int user_id, int item_id, int transaction_id, double price, String description, String date) {
        this._id = _id;
        this.user_id = user_id;
        this.item_id = item_id;
        this.transaction_id = transaction_id;
        this.price = price;
        this.description = description;
        this.date = date;
    }

    public Shopping() {
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public int getItem_id() {
        return item_id;
    }

    public void setItem_id(int item_id) {
        this.item_id = item_id;
    }

    public int getTransaction_id() {
        return transaction_id;
    }

    public void setTransaction_id(int transaction_id) {
        this.transaction_id = transaction_id;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "Shopping{" +
                "_id=" + _id +
                ", user_id=" + user_id +
                ", item_id=" + item_id +
                ", transaction_id=" + transaction_id +
                ", price=" + price +
                ", description='" + description + '\'' +
                ", date='" + date + '\'' +
                '}';
    }
}
