package com.example.managedevices;

/**
 * Gồm ID và Tên nhà máy:
 * id: Factory_ID;
 * name: Name_Factory;
 */

public class Factory {
    private Integer id = 0;
    private String Name = "";

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }
}