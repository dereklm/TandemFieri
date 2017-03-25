package com.gmail.dleemcewen.tandemfieri.Filters;

import java.util.ArrayList;
import java.util.List;

/**
 * SubscriberFilter defines one or more filters that can be specified by the subscriber to ensure
 * that the subscriber only receives the appropriate messages
 */

public class SubscriberFilter {
    private String field;
    private List<Object> values;

    /**
     * Default constructor
     */
    public SubscriberFilter() {
        values = new ArrayList<>();
    }

    /**
     * Optional constructor
     * @param field identifies the name of the field that contains the values to be matched
     * @param values indicates the values that must match in order for the filter to be valid
     */
    public SubscriberFilter(String field, List<Object> values) {
        this.field = field;
        this.values = values;
    }

    /**
     * getField returns the name of the field that contains the values to be matched
     * @return name of the field that contains the values to be matched
     */
    public String getField() {
        return field;
    }

    /**
     * setField sets the field name that will contain the values to be matched
     * @param field indicates the name of the field
     */
    public void setField(String field) {
        this.field = field;
    }

    /**
     * getValues returns the list of values that will be matched
     * @return list of object values to match
     */
    public List<Object> getValues() {
        return values;
    }

    /**
     * setValues sets the list of values that will be matched
     * @param values indicates the list of values that will be matched
     */
    public void setValues(List<Object> values) {
        this.values = values;
    }
}
