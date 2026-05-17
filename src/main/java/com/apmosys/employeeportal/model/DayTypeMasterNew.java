package com.apmosys.employeeportal.model;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "day_type_master_new")
public class DayTypeMasterNew {

	@Id
    @Column(name = "day_type_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer dayTypeId;

    @Column(name = "day_type")
    private String dayType;
    
    @Column(name="is_active")
    private Boolean isActive;
    
    @Column(name = "is_working_day")
    private Boolean isWorkingDay;
    
    
    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", insertable = false, updatable = false)
	private Timestamp createdOn;
    
    private Long createdBy;
}
