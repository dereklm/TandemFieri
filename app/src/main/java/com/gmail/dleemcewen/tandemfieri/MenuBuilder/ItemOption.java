package com.gmail.dleemcewen.tandemfieri.MenuBuilder;

import java.util.ArrayList;

/**
 * Created by jfly1_000 on 2/13/2017.
 */
public class ItemOption {
    private String OptionName;
    private ArrayList<OptionSelection> selections;
    private String Description;
    private boolean required;
    private boolean andRelationship; //determines if the relations ship of the items is mutually exclusive or not ie: you cannot have both ranch and ceaser for your dressing

    public String getOptionName() {
        return OptionName;
    }

    public void setOptionName(String optionName) {
        OptionName = optionName;
    }

    public ArrayList<OptionSelection> getSelections() {
        return selections;
    }

    public void setSelections(ArrayList<OptionSelection> selections) {
        this.selections = selections;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }
}
