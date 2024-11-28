package com.example.al_doodler.model;
// This file has some functions for creating the tool bar. Just some getter and setter functions.
public class ToolsItem {

    private int icon;
    private String name;

    public ToolsItem(int icon, String name){
        this.icon = icon;
        this.name = name;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
