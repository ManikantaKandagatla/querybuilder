package com.rzp.querybuilder.model;

import lombok.Getter;

@Getter
public enum DataStoreType {
    MYSQL("mysql");

    private String name;
    DataStoreType(String name)
    {
        this.name = name;
    }
}
