package com.apmosys.employeeportal.model;
import javax.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    private Long quarterId;
    private Long departmentId;
  
    private String name;
    private String description;
   
   

    @OneToMany(mappedBy = "kpi", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Kpis> kpis = new ArrayList<>();

    private String approvedBy;
    private String createdBy;

    private String department;
    private Long managerRating;
    private String managerRemark;
   
    private String employeeRole;
    public void addKpis(Kpis kpisItem) {
        kpis.add(kpisItem);
        kpisItem.setKpi(this);
    }

    public void removeKpis(Kpis kpisItem) {
        kpis.remove(kpisItem);
        kpisItem.setKpi(null);
    }
}