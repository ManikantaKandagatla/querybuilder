package com.rzp.querybuilder.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Column {
    private String fieldDbName;
    private String displayName;
    private DataType dataType;
    private boolean hasLookup;
    private List<LookupDetail> lookupObject;
    private boolean reportable;
}
