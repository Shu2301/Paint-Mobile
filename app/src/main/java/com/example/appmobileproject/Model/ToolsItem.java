package com.example.appmobileproject.Model;

public class ToolsItem {
    private int icone;
    private String name;

    public ToolsItem(int icone, String name) {
        this.icone = icone;
        this.name = name;
    }

    public void setIcone(int icone) {
        this.icone = icone;
    }

    public String getName() {
        return name;
    }

    public int getIcone() {
        return 0;
    }
}
