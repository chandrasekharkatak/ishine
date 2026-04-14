package com.apmosys.employeeportal.model;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.data.annotation.CreatedDate;
import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "employee_goals")
@Setter
@Getter
public class EmployeeGoals {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long goalId;
 
    private Long empId;
   
    private String assignedBy;
    private Long templateId;
    
    private String goalTitle;
    
    private String description;
    private Long goalProgress;
    
    private String goalStatus;
   
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expectedCompletionDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate actualCompletionDate;
    
    @CreatedDate
    @Column(updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate createdDate;    
    
    private String quarter;
    
    private Long quarterId;
    
    // Define one-to-many relationship with GoalRemarks
    @OneToMany(mappedBy = "employeeGoal", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<GoalRemarks> remarks = new ArrayList<>();
}