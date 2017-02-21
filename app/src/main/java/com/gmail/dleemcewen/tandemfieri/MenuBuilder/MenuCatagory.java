package com.gmail.dleemcewen.tandemfieri.MenuBuilder;

import java.util.ArrayList;

/**
 * Created by jfly1_000 on 2/13/2017.
 */

public class MenuCatagory extends MenuCompenet {

    private ArrayList<MenuItem> subItems;
    private ArrayList<MenuCatagory> subCategories;



    public MenuCatagory(){

    }

    public MenuCatagory(String name){
        this.name=name;
        subItems= new ArrayList<>();
        subCategories=new ArrayList<>();
    }

    public ArrayList<MenuItem> getSubItems() {
        return subItems;
    }

    public void setSubItems(ArrayList<MenuItem> subItems) {
        this.subItems = subItems;
    }

    public ArrayList<MenuCatagory> getSubCategories() {
        if(subCategories!=null)return subCategories;
        subCategories= new ArrayList<>();
        return  subCategories;
    }

    public void setSubCategories(ArrayList<MenuCatagory> subCategories) {
        this.subCategories = subCategories;
    }
}
