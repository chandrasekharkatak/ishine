package com.apmosys.employeeportal.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class ServiceDataDTO {

  private Long id;

  private String type;
  private String name;

  private List<SubServiceDataDTO> subServices = new ArrayList<>();

}
