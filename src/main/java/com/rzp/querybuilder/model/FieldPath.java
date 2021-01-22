package com.rzp.querybuilder.model;

import com.rzp.querybuilder.model.query.BaseField;
import com.rzp.querybuilder.model.query.JoinType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FieldPath {

    private BaseField left;
    private BaseField right;
    private FieldPath fieldPath;
    private JoinType joinType;
    private String lookupName;
    private String lookupId;
}
