package com.apmosys.employeeportal.model;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class EmployeeClientSideIdMapId implements Serializable {

    @Column(name = "emp_id")
    private Long empId;

    @Column(name = "client_side_id")
    private String clientSideId;

    @Column(name = "project_id")
    private Long projectId;  
    
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EmployeeClientSideIdMapId)) return false;
        EmployeeClientSideIdMapId that = (EmployeeClientSideIdMapId) o;
        return Objects.equals(empId, that.empId)
                && Objects.equals(clientSideId, that.clientSideId)
                && Objects.equals(projectId, that.projectId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(empId, clientSideId, projectId);
    }
}