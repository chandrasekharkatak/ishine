package com.apmosys.employeeportal.dto;

import java.util.*;

import lombok.Data;

@Data
public class DomainInfo {
    private String color;
    private Set<String> data = new HashSet<>();

    public DomainInfo(String color) {
        this.color = color;
    }
}
