package com.apmosys.employeeportal.model;

import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.data.annotation.CreatedDate;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "goal_remarks")
@Setter
@Getter
public class GoalRemarks {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "goal_id")
    private EmployeeGoals employeeGoal;
    
    private Long remarkBy;
    private String remarkByName;
    private String remarkText;
    private Long empId;
    private LocalDate createdDate;
    
    // Getters and setters
}