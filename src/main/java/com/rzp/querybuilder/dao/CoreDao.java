package com.rzp.querybuilder.dao;

import java.util.List;
import java.util.Map;

public interface CoreDao {
    List<Map<String, Object>> getData(String sql, Map<String, Object> params);
}
