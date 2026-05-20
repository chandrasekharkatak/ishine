package com.apmosys.employeeportal.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "po_session_access_log")
@Getter
@Setter
public class PoSessionAccessLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "emp_id", nullable = false)
    private Long empId;

    @Column(name = "deep_link", nullable = false, length = 2000)
    private String deepLink;

    @Column(name = "action", nullable = false, length = 50)
    private String action;

    @Column(name = "session_key", length = 512)
    private String sessionKey;

    @Column(name = "accessed_at", nullable = false)
    private LocalDateTime accessedAt;
}
