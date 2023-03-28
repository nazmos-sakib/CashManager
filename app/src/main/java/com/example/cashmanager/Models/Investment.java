package com.example.cashmanager.Models;

public class Investment {
    private int _id;
    private int user_id;
    private int transaction_id;
    private String name;
    private double amount;
    private String init_date;
    private String finish_date;
    private double monthly_roi;

    public Investment(int _id, int user_id, int transaction_id, String name, double amount, String init_date, String finish_date, double monthly_roi) {
        this._id = _id;
        this.user_id = user_id;
        this.transaction_id = transaction_id;
        this.name = name;
        this.amount = amount;
        this.init_date = init_date;
        this.finish_date = finish_date;
        this.monthly_roi = monthly_roi;
    }

    public Investment() {
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

    public int getTransaction_id() {
        return transaction_id;
    }

    public void setTransaction_id(int transaction_id) {
        this.transaction_id = transaction_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getInit_date() {
        return init_date;
    }

    public void setInit_date(String init_date) {
        this.init_date = init_date;
    }

    public String getFinish_date() {
        return finish_date;
    }

    public void setFinish_date(String finish_date) {
        this.finish_date = finish_date;
    }

    public double getMonthly_roi() {
        return monthly_roi;
    }

    public void setMonthly_roi(double monthly_roi) {
        this.monthly_roi = monthly_roi;
    }


    @Override
    public String toString() {
        return "Investment{" +
                "_id=" + _id +
                ", user_id=" + user_id +
                ", transaction_id=" + transaction_id +
                ", name='" + name + '\'' +
                ", amount=" + amount +
                ", init_date='" + init_date + '\'' +
                ", finish_date='" + finish_date + '\'' +
                ", monthly_roi=" + monthly_roi +
                '}';
    }
}
