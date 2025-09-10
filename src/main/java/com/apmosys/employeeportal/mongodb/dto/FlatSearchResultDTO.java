package com.apmosys.employeeportal.mongodb.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FlatSearchResultDTO {

    private String objectId;
    private String type;
    private Long occurrenceCount;
    private String prefixPath;
    private Long distinctGroupCount;

    public FlatSearchResultDTO(String objectId, String type, Long occurrenceCount, String prefixPath) {
        this.objectId = objectId;
        this.type = type;
        this.occurrenceCount = occurrenceCount;
        this.prefixPath = prefixPath;
    }

}
