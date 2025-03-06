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
 
    private String approvedBy;
    
    private String department;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt; 
    
    @UpdateTimestamp
    private LocalDateTime updatedAt; 

}

