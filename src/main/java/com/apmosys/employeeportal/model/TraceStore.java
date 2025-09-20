package com.apmosys.employeeportal.model;

import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Data;

@Entity
@Data
public class TraceStore {

	@Id
    private String traceId;
}
