package com.apmosys.employeeportal.mongodb.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParentCountDTO {

    private String parentId;
    private long count;
    private long distinctGroupCount;
    public ParentCountDTO(String parentId, long count) {
        this.parentId = parentId;
        this.count = count;
    }
   

}
