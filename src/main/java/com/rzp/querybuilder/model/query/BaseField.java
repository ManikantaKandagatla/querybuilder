package com.rzp.querybuilder.model.query;

import com.rzp.querybuilder.model.DataType;
import com.rzp.querybuilder.model.FieldPath;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BaseField {
    private String fieldDbName;
    private String objectDbName;
    private DataType dataType;
    private boolean hasLookup;
    private FieldPath fieldPath;
    private FieldType fieldType;
    private String fieldAlias;
    private String tableAlas;
    private AggregateFunction aggregateFunction;
    private SortOrder sortOrder;
}
