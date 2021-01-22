package com.rzp.querybuilder.model.query;

import lombok.Getter;

@Getter
public enum Operator {
    EQ("equals", "="),
    NEQ("not equals", "<>"),
    GTE("greater than or equals", ">="),
    GT("greater than ", ">"),
    LT("less than ", "<"),
    LTE("less than or equals", "<="),
    LIKE("like", "like"),
    ILIKE("ignore case like", "ilike"),
    IN("in ", "in");

    private String description;
    private String sqlOperator;
    Operator(String description, String sqlOperator) {
        this.description = description;
        this.sqlOperator = sqlOperator;
    }
}
