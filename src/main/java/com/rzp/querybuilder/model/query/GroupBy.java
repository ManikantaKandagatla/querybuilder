package com.rzp.querybuilder.model.query;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GroupBy {
    private List<BaseField> fields;
}
