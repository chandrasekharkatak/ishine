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

/**
 * Business feature under a skill-matrix domain.
 * <ul>
 * <li>{@code subdomainId == null} — feature belongs directly to the domain.</li>
 * <li>{@code subdomainId != null} — feature belongs to that sub domain (must match {@code domainId}).</li>
 * </ul>
 */
@Entity
@Table(name = "skill_domain_feature_master")
@Getter
@Setter
public class SkillDomainFeatureMaster {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "feature_id")
	private Integer featureId;

	@Column(name = "domain_id", nullable = false)
	private Integer domainId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "domain_id", referencedColumnName = "domain_id", insertable = false, updatable = false)
	private SkillDomainMaster domain;

	/** When null, the feature is scoped to the domain only. */
	@Column(name = "subdomain_id")
	private Integer subdomainId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "subdomain_id", referencedColumnName = "subdomain_id", insertable = false, updatable = false)
	private SkillSubdomainMaster subdomain;

	@Column(name = "feature_name", nullable = false, length = 255)
	private String featureName;

	@Column(name = "is_active")
	private Boolean isActive;
}
