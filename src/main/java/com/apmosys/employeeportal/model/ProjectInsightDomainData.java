package com.apmosys.employeeportal.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Index;
import javax.persistence.CascadeType;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.JoinColumn;
import javax.persistence.Column;
import javax.persistence.PrePersist;


import java.util.*;



import java.time.LocalDateTime;

import com.apmosys.employeeportal.listener.ProjectInsightDomainDataListener;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
@Entity
// @EntityListeners(ProjectInsightDomainDataListener.class)
@Table(name = "project_insight_domain_data", 
  indexes = {
        @Index(name = "name", columnList = "name")
    }
)
public class ProjectInsightDomainData {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true, name="name")
  private String name;

  private String type;

  @Column(name="is_active")
  private Boolean isActive = true;

  @Column(name="created_by")
  private Long createdBy;

  @Column(name="is_approved")
  private String isApproved = "pending";

  @Column(name="approved_by")
  private Long approvedBy;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_id")
  @JsonIgnore
  private ProjectInsightDomainData parent;

  @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ProjectInsightDomainData> children = new ArrayList<>();

  @CreationTimestamp
  @Column(name = "created_on")
  private LocalDateTime createdOn;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "domaincolor_code")
	private String domaincolorCode;

  @PrePersist
  public void assignRandomColor() {
      if ("domain".equalsIgnoreCase(this.type) && 
          (this.domaincolorCode == null || this.domaincolorCode.isEmpty())) {
          
          Random random = new Random();
          int randomColor = random.nextInt(0xFFFFFF + 1);
          this.domaincolorCode = String.format("#%06X", randomColor);
      }
  }


}