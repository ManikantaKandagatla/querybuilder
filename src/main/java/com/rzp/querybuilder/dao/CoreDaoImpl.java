package com.rzp.querybuilder.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class CoreDaoImpl implements CoreDao {

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public List<Map<String, Object>> getData(String sql, Map<String, Object> params) {
        return namedParameterJdbcTemplate.queryForList(sql, params);
    }
}
