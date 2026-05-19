package com.apmosys.employeeportal.model;

import javax.persistence.*;

import lombok.*;

@Data
@Entity
@Table(name = "delivery_mode")
public class DeliveryMode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String deliveryMode;
    
}