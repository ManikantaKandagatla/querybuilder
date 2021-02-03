package com.rzp.querybuilder.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rzp.querybuilder.model.MetaCollectionMaster;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class DescribeServiceImpl implements DescribeService {

    private static final String BASE_PATH = "metadata/";

    @Override
    public Map<String, Object> describeObject(String object) {
        Map<String, Object> map = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String fileName = BASE_PATH + object + ".json";
            MetaCollectionMaster collectionMaster = objectMapper.readValue(new File(getClass().getClassLoader().getResource(fileName).getFile()), MetaCollectionMaster.class);
            map = new HashMap<>();
            map.put(object, collectionMaster);
        } catch (Exception e) {
            map.put(object, "error");
        }
        return map;
    }
}
