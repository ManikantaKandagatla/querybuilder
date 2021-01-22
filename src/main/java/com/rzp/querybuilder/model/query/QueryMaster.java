package com.rzp.querybuilder.model.query;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QueryMaster {
    private Select select;
    private FilterConfig filterConfig;
    private GroupBy groupBy;
    private OrderBy orderBy;
    private Having having;
    private String baseObject;
    private int limit;
    private int offset;
}
