package com.gmail.dleemcewen.tandemfieri.MenuBuilder;

import java.util.ArrayList;

/**
 * Created by jfly1_000 on 2/13/2017.
 */

public class MenuCatagory extends MenuCompenet {

    private ArrayList<MenuCompenet> subItems;


    public ArrayList<MenuCompenet> getSubItems() {
        return subItems;
    }

    public void setSubItems(ArrayList<MenuCompenet> subItems) {
        this.subItems = subItems;
    }

    public MenuCatagory(String name){
        this.name=name;
        subItems= new ArrayList<>();
    }
}
