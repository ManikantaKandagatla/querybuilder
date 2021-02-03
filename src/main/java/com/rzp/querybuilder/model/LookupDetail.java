package com.rzp.querybuilder.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties
public class LookupDetail {
    private String fieldDbName;
    private String objectName;
    private String lookupName;
}
