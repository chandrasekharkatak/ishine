package com.apmosys.employeeportal.mongodb.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FacetSearchResultDTO {
    private String parentId;
    private String type;

    public FacetSearchResultDTO(String parentId, String type) {
        this.parentId = parentId;
        this.type = type;
    }

}
