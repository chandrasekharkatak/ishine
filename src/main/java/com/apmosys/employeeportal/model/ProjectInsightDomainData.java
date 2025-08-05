package com.apmosys.employeeportal.model;

import javax.persistence.*;
import javax.persistence.Table;
import javax.persistence.CascadeType;
import javax.persistence.Entity;

import java.util.*;

import org.hibernate.annotations.*;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
@Entity
@Table(name = "project_insight_domain_data")
public class ProjectInsightDomainData {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;

  private String type;

  private Boolean isActive = true;

  private Long createdBy;

  private String isApproved = "pending";

  // only for domains
  private Long approvedBy;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_id")
  @JsonIgnore
  private ProjectInsightDomainData parent;

  @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ProjectInsightDomainData> children = new ArrayList<>();

  @CreationTimestamp
  private LocalDateTime createdOn;

  @UpdateTimestamp
  private LocalDateTime updatedAt;
}