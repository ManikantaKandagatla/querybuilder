package com.rzp.querybuilder.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Column {
    private String fieldDbName;
    private String objectDbName;
    private DataType dataType;
    private boolean hasLookup;
    private LookupDetail lookupObject;
    private boolean reportable;
}
