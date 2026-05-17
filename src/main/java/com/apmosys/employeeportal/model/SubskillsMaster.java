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
@Table(name = "subskills_master")
@Getter
@Setter
public class SubskillsMaster {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "subskill_id")
	private Integer subskillId;

	@Column(name = "skill_id", nullable = false)
	private Integer skillId;

	/** Read-only join for list/filter; writes use {@link #skillId}. */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "skill_id", referencedColumnName = "skill_id", insertable = false, updatable = false)
	private SkillsMaster skill;

	@Column(name = "subskill_name", nullable = false)
	private String subskillName;

	@Column(name = "is_active")
	private Boolean isActive;

	@Column(name = "created_at", insertable = false, updatable = false)
	private Timestamp createdAt;

	@Column(name = "updated_at", insertable = false, updatable = false)
	private Timestamp updatedAt;
}
