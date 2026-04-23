package com.apmosys.employeeportal.model;

import javax.persistence.*;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@Table(name = "custom_query_roles")
public class CustomQueryRoles {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "query_id")
    private Long queryId;

    @Column(name = "role_id")
    private Long roleId;
}
