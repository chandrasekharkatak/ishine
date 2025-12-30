package com.apmosys.employeeportal.model;

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
}
