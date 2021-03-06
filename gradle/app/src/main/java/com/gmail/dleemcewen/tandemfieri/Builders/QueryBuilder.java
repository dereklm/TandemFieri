package com.gmail.dleemcewen.tandemfieri.Builders;

import com.gmail.dleemcewen.tandemfieri.Constants.QueryConstants;
import com.gmail.dleemcewen.tandemfieri.Query.ParsedQuery;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

import java.util.Arrays;
import java.util.List;

/**
 * QueryBuilder builds firebase queries from SQL-like query snippets
 */

public class QueryBuilder {
    /**
     * build builds a new query from the provided querystring
     * @param dataContext indicates the dataContext
     * @param queryString identifies the querystring to parse.
     *                    Querystrings are either in the form of "<field> = <value>" or "<field> between <value1> and <value2>"
     * @return the parsed query
     */
    public static ParsedQuery build(DatabaseReference dataContext, String queryString) {
        ParsedQuery parsedQuery = new ParsedQuery();

        if (queryString != null && !queryString.trim().equals("")) {
            List<String> splitQueryString = Arrays.asList(queryString.split(" "));
            if (splitQueryString.size() > 2) {
                //use assignment to determine what kind of query to build
                //TODO: refactor this to use strategies instead of a conditional
                if (splitQueryString.get(1).toLowerCase().trim().equals("between")) {
                    //set the assignment values
                    String assignmentValue1 = splitQueryString.get(2);
                    String assignmentValue2 = splitQueryString.get(5);

                    parsedQuery.setQuery(buildRangeQuery(dataContext, assignmentValue1, assignmentValue2, splitQueryString.get(0)));
                    parsedQuery.setField(splitQueryString.get(0));
                    parsedQuery.values.add(assignmentValue1);
                    parsedQuery.values.add(assignmentValue2);
                    parsedQuery.setQueryType(QueryConstants.QueryType.RANGE);
                } else {
                    //Check to make sure this is an equals query
                    switch (splitQueryString.get(1))
                    {
                        case "!=":
                            parsedQuery.setQueryType(QueryConstants.QueryType.NOTEQUALS);
                            break;
                        default:
                            parsedQuery.setQueryType(QueryConstants.QueryType.EQUALS);
                            break;
                    }

                    //set the assignment value
                    String assignmentValue = splitQueryString.get(2);

                    parsedQuery.setQuery(buildEqualsQuery(dataContext, assignmentValue, splitQueryString.get(0)));
                    parsedQuery.setField(splitQueryString.get(0));
                    parsedQuery.values.add(assignmentValue.replace("'", ""));
                }
            }
        }

         return parsedQuery;
    }

    /**
     * buildEqualsQuery builds the appropriate query that defines that the data at the provided childnodes
     * should be equal to the provided equalsValue
     * @param dataContext indicates the data context
     * @param equalsValue indicates the value to equal
     * @param childNode indicates the child node that identifies the location
     *                         of the desired data
     * @return query that can be executed by firebase to find desired records
     */
    private static Query buildEqualsQuery(DatabaseReference dataContext, Object equalsValue, String childNode) {
        Query query = buildQuery(dataContext, childNode);

        if (equalsValue != null && !equalsValue.toString().equals("")) {
            if (equalsValue.toString().startsWith("'") && equalsValue.toString().endsWith("'")) {
                query = query.equalTo(equalsValue.toString().replace("'", ""));
            } else {
                query = query.equalTo(Integer.valueOf(equalsValue.toString()));
            }
        }

        return query;
    }

    /**
     * buildRangeQuery builds the appropriate query that defines that the data at the provided childnodes
     * should be bounded by the provided startingRangeValue and the provided endingRangeValue
     * @param dataContext indicates the data context
     * @param startingRangeValue indicates the starting range value (optional)
     * @param endingRangeValue indicates the starting range value (optional)
     * @param childNode indicates the child node that identifies the location
     *                         of the desired data
     * @return query that can be executed by firebase to find desired records
     */
    private static Query buildRangeQuery(DatabaseReference dataContext, String startingRangeValue, String endingRangeValue, String childNode) {
        Query query = buildQuery(dataContext, childNode);

        if (!startingRangeValue.equals("")) {
            query = query.startAt(startingRangeValue);
        }

        if (!endingRangeValue.equals("")) {
            query = query.endAt(endingRangeValue);
        }

        return query;
    }

    /**
     * buildQuery builds the appropriate query based on the provided childNodes and value
     * @param dataContext indicates the data context
     * @param childNode indicates the child node to query (optional)
     * @return query that can be executed by firebase to find desired records
     */
    private static Query buildQuery(DatabaseReference dataContext, String childNode) {
        Query query;

        if (childNode != null && !childNode.equals("")) {
            query = dataContext.orderByChild(childNode);
        } else {
            query = dataContext.orderByKey();
        }

        return query;
    }
}
