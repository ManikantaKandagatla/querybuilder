package com.rzp.querybuilder.controller;

import com.rzp.querybuilder.dao.CoreDao;
import com.rzp.querybuilder.model.MetaCollectionMaster;
import com.rzp.querybuilder.model.query.QueryMaster;
import com.rzp.querybuilder.service.DescribeService;
import com.rzp.querybuilder.util.SQLTransformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class QueryController {

    @Autowired
    private CoreDao coreDao;

    @Autowired
    private DescribeService describeService;


    @RequestMapping(method = RequestMethod.POST, path = "/getData")
    public List<Map<String, Object>> getQueryData(@RequestBody QueryMaster reportConfig) {
        SQLTransformer sqlTransformer = new SQLTransformer(reportConfig);
        String query = sqlTransformer.build().toString();
        return coreDao.getData(query, new HashMap<>());
    }

    @RequestMapping(method = RequestMethod.POST,path = "/buildQuery")
    public Map<String, Object> getQuery(@RequestBody QueryMaster reportConfig)
    {
        SQLTransformer sqlTransformer = new SQLTransformer(reportConfig);
        String query = sqlTransformer.build().toString();
        Map<String, Object> response = new HashMap<>();
        response.put("raw_sql", query);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST, path = "/executeSQL")
    public ResponseEntity executeQuery(@RequestBody Map<String, Object> query) {
        return new ResponseEntity<>(coreDao.getData(query.get("sql").toString(), new HashMap<>()), HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/describe/{objectName}")
    public ResponseEntity describeObject(@PathVariable String objectName) {
        return new ResponseEntity<>(describeService.describeObject(objectName), HttpStatus.OK);
    }
}
