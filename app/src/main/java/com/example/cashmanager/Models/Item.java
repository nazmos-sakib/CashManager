package com.example.cashmanager.Models;

public class Item {
    private int _id;
    private String name;
    private String image_url;
    private String description;

    public Item(int _id, String name, String image_url, String description) {
        this._id = _id;
        this.name = name;
        this.image_url = image_url;
        this.description = description;
    }

    public Item() {
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Item{" +
                "_id=" + _id +
                ", name='" + name + '\'' +
                ", image_url='" + image_url + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
