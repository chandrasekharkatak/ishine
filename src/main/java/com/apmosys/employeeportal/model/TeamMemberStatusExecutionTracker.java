package com.apmosys.employeeportal.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.apmosys.employeeportal.enums.TeamMemberStatusExecutionStatus;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "team_member_status_execution_tracker")
@Getter
@Setter
public class TeamMemberStatusExecutionTracker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tracker_id")
    private Long trackerId;

    @Column(name = "last_executed_date")
    private LocalDate lastExecutedDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "last_executed_status")
    private TeamMemberStatusExecutionStatus lastExecutedStatus;

    @Column(name = "updated_on")
    private LocalDateTime updatedOn;
}
