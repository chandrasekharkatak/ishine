package com.apmosys.employeeportal.model;

import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity
public class Proficiency {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long proficiencyId;

    private String proficiencyName;

    private String imageUrl;

}
