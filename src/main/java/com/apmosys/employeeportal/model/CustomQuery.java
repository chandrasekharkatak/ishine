package com.apmosys.employeeportal.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@Table(name = "custom_queries")
public class CustomQuery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "query_id")
    private Long queryId;

    @Column(name = "query_name", columnDefinition = "TEXT")
    private String queryName;

    @Column(name = "query_sql", columnDefinition = "TEXT")
    private String querySql;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    private int status;

    @Column(name = "created_by")
    private Long createdBy;
}
