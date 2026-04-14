package com.apmosys.employeeportal.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Index;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import lombok.Data;

@Data
@Entity
@Table(name = "project_insight_domain_data_flat_search", 
    indexes = {
        @Index(name = "domain_flat_search_idx", columnList = "flat_search"),
        @Index(name = "domain_id_flat_search_idx", columnList = "domain_id")
    }
)
public class ProjectInsightDomainDataFlatSearch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "flat_search")
    private String flatSearch;

    @Column(name = "domain_id")
    private Long domainId;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

}