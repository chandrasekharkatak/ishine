package com.apmosys.employeeportal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParentCount {
	private String id;
	private long count;

	public void setId(Object id) {
        if (id instanceof String) {
            this.id = (String) id;
        } else if (id != null) {
            this.id = id.toString();
        }
    }
}