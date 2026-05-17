package com.apmosys.employeeportal.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "skill_subdomain_master")
@Getter
@Setter
public class SkillSubdomainMaster {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "subdomain_id")
	private Integer subdomainId;

	@Column(name = "domain_id", nullable = false)
	private Integer domainId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "domain_id", referencedColumnName = "domain_id", insertable = false, updatable = false)
	private SkillDomainMaster domain;

	@Column(name = "subdomain_name", nullable = false, length = 255)
	private String subdomainName;

	@Column(name = "is_active")
	private Boolean isActive;
}
