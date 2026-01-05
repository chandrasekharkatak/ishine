package com.apmosys.employeeportal.dto;

import lombok.Data;

@Data
public class ClientIdAndName {

    private Integer clientId;
    private String clientName;

    public ClientIdAndName(Integer clientId, String clientName) {
        this.clientId = clientId;
        this.clientName = clientName;
    }

}