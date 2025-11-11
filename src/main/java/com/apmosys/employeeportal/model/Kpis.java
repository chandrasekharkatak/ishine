package com.apmosys.employeeportal.model;

import lombok.*;
import javax.persistence.*;

@Entity
@Table(name = "kpis")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Kpis {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String description;
    
    private String review;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kpi_id")  
    private Kpi kpi;
    
}