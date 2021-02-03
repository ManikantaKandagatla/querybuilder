package com.rzp.querybuilder.service;

import com.rzp.querybuilder.model.MetaCollectionMaster;

import java.io.IOException;
import java.util.Map;

public interface DescribeService {
    Map<String, Object> describeObject(String object);
}
