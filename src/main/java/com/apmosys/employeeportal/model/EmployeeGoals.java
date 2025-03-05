
package com.apmosys.employeeportal.model;

import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "employee_goals")
@Getter
@Setter
public class EmployeeGoals {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long goalId;

 
    private Long empId;

   
    private String assignedBy;

    
    private String goalTitle;

    @Enumerated(EnumType.STRING)
    private GoalProgress goalProgress;

    @Enumerated(EnumType.STRING)
    private ReviewStatus reviewStatus;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expectedCompletionDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate actualCompletionDate;

    @CreatedDate
    @Column(updatable = false)
    private LocalDate createdDate;

    @LastModifiedDate
    private LocalDate updatedDate;
    
    
    public enum GoalProgress {
        NOT_STARTED, IN_PROGRESS, COMPLETED, ON_HOLD
    }

    public enum ReviewStatus {
        PENDING, APPROVED, REJECTED, UNDER_REVIEW
    }

	
}

