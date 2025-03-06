
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
@Setter
@Getter
public class EmployeeGoals {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long goalId;

 
    private Long empId;

   
    private Long assignedBy;

    private Long templateId;
    
    private String goalTitle;

    private String goalProgress;
    
    private String goalStatus;
   

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expectedCompletionDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate actualCompletionDate;

    @CreatedDate
    @Column(updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate createdDate;    
    


	
}

