package com.apmosys.employeeportal.model;

import java.sql.Timestamp;

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
@Table(name = "skills_master")
@Getter
@Setter
public class SkillsMaster {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "skill_id")
	private Integer skillId;

	@Column(name = "skill_name", nullable = false, length = 120)
	private String skillName;

	@Column(name = "category_id", nullable = false)
	private Integer categoryId;

	/** Read-only join for list/filter; writes use {@link #categoryId}. */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "category_id", referencedColumnName = "category_id", insertable = false, updatable = false)
	private SkillCategoryMaster skillCategory;

	@Column(name = "skill_type")
	private String skillType;

	@Column(name = "is_active")
	private Boolean isActive;

	@Column(name = "department_id", nullable = false)
	private Long departmentId;

	/** Read-only join for list/filter; writes use {@link #departmentId}. */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "department_id", insertable = false, updatable = false)
	private Department department;

	@Column(name = "created_at", insertable = false, updatable = false)
	private Timestamp createdAt;

	@Column(name = "updated_at", insertable = false, updatable = false)
	private Timestamp updatedAt;
}
