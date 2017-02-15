package com.gmail.dleemcewen.tandemfieri.MenuBuilder;

import java.io.Serializable;

/**
 * Created by jfly1_000 on 2/13/2017.
 */

public abstract class MenuCompenet implements Serializable{

    protected String name;

    public String getName() {
        return name;
    }

    public void setName(String name){
        this.name=name;
    }

}
