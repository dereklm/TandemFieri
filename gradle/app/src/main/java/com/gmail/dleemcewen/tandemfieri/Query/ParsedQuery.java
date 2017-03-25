package com.gmail.dleemcewen.tandemfieri.Query;

import com.gmail.dleemcewen.tandemfieri.Constants.QueryConstants;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.List;

/**
 * ParsedQuery contains the query information that was parsed from a provided query string
 */

public class ParsedQuery {
    private String field;
    private QueryConstants.QueryType queryType;
    private Query query;
    public List<Object> values;

    /**
     * Default constructor
     */
    public ParsedQuery() {
        values = new ArrayList<>();
    }

    /**
     * getField returns the field the query is acting on
     * @return query field
     */
    public String getField() {
        return field;
    }

    /**
     * setField sets the field the query is acting on
     * @param field indicates the field the query is acting on
     */
    public void setField(String field) {
        this.field = field;
    }

    /**
     * getQueryType returns the type of the query
     * @return query type
     */
    public QueryConstants.QueryType getQueryType() {
        return queryType;
    }

    /**
     * setQueryType sets the type of the query
     * @param queryType indicates the type of the query
     */
    public void setQueryType(QueryConstants.QueryType queryType) {
        this.queryType = queryType;
    }

    /**
     * getQuery returns the firebase query that was built from the query string
     * @return firebase query built from the query string
     */
    public Query getQuery() {
        return query;
    }

    /**
     * setQuery sets the firebase query that was built from the query string
     * @param query indicates the firebase query to set
     */
    public void setQuery(Query query) {
        this.query = query;
    }
}
