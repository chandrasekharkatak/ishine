package com.apmosys.employeeportal.utility;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.meilisearch.sdk.Client;
import com.meilisearch.sdk.SearchRequest;

@Service
public class SearchUtils {

    private final Client meiliSearchClient;
    private final ObjectMapper objectMapper;

    @Autowired
    public SearchUtils(Client meiliSearchClient, ObjectMapper objectMapper) {
        this.meiliSearchClient = meiliSearchClient;
        this.objectMapper = objectMapper;
    }

    public void addData(String indexName, List<Map<String, Object>> data) {
        try {
            String json = objectMapper.writeValueAsString(data);
            meiliSearchClient.index(indexName).addDocuments(json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public Object searchData(String indexName, String query) {
        try {
            SearchRequest searchRequest = new SearchRequest(query)
            .setAttributesToSearchOn(new String[]{"tag"})
            .setLimit(10);

            return meiliSearchClient.index(indexName).search(searchRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
}