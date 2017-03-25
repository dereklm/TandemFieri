package com.gmail.dleemcewen.tandemfieri.menubuilder;

import com.gmail.dleemcewen.tandemfieri.Entities.Restaurant;

import java.util.Stack;

/**
 * Created by jfly1_000 on 2/28/2017.
 */

public class MenuIterator {

    private static MenuIterator singilton;

    private MenuCompenet currentComponent;

    private Restaurant restaurant;

    private Stack<MenuCompenet> stack;

    private ItemOption currentOption = null;

    private OptionSelection currentSelection = null;

    private MenuIterator(){
        stack = new Stack<>();
    }

    public static MenuIterator create(){
        if (singilton!=null) return singilton;
        singilton= new MenuIterator();
        return singilton;
    }

    public MenuCompenet getCurrentComponent(){
        return currentComponent;
    }

    public void setCurrentComponent(MenuCompenet com){
        stack.add(currentComponent);
        currentComponent=com;
    }

    public void setRestaurant(Restaurant r) throws Exception {
        if(!stack.isEmpty()) throw new Exception("you are overidding data by setting this resurant");
        restaurant = r;
        this.currentComponent = restaurant.getMenu();
    }

    public void backtrack(){
        if(!stack.isEmpty())currentComponent = stack.pop();
        else currentComponent = null;
        currentOption=null;
        currentSelection=null;
    }

    public Restaurant getRestaurant(){
        return restaurant;
    }

    public ItemOption getCurrentOption() {
        return currentOption;
    }

    public void setCurrentOption(ItemOption currentOption) {
        this.currentOption = currentOption;
        currentSelection=null;
    }

    public OptionSelection getCurrentSelection() {
        return currentSelection;
    }

    public void setCurrentSelection(OptionSelection currentSelection) {
        this.currentSelection = currentSelection;
    }
}
