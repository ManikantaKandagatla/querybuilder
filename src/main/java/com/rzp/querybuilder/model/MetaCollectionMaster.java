package com.rzp.querybuilder.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MetaCollectionMaster {
    private String objectName;
    private DataStoreType dataStoreType;
    private String objectGroupType;
    private List<Column> columns;
}
