package com.apmosys.employeeportal.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@Table(
	    name = "role_details",
	    uniqueConstraints = {
	        @UniqueConstraint(
	            name = "uk_role_dept_exp",
	            columnNames = {"role", "department", "experience"}
	        )
	    }
	)
public class RoleDetails {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long roleId;
	
	 @Column(nullable = false)
	private String role;
	 
	 @Column(nullable = false)
	private String department;
	 
	 @Column(nullable = false)
	private String experience;

}
