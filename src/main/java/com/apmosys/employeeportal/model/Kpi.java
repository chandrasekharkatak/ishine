package com.apmosys.employeeportal.model;


import javax.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "kpi_kra")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Kpi {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    

    
    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GoalType type;

    @Enumerated(EnumType.STRING)
    private GoalStatus status; 

    private String assignedBy;

    @Column(nullable = false)
    private String createdBy; 

    private String updatedBy; 
    

    private String approvedBy;
    
    private String department;

    private String rejectedBy;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt; 
    @UpdateTimestamp
    private LocalDateTime updatedAt; 

}

