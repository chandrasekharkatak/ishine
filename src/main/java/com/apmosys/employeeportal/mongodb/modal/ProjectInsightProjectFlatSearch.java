package com.apmosys.employeeportal.mongodb.modal;

import javax.persistence.Id;
import java.util.*;

import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document
(collection = "project_insight_project_flat_search")
public class ProjectInsightProjectFlatSearch {

    @Id
	private String id;

    private String parentId;

    private String flatSearchableText;

    private String type;

    private String prefixPath = "";

    private List<String> parentIds = new ArrayList<>();
    
}