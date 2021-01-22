package com.rzp.querybuilder.model.query;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FilterCriteria {
    private BaseField filterField;
    private FilterValue filterValue;
    private Operator operator;
}
