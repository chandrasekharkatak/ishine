package com.apmosys.employeeportal.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class SubServiceDataDTO {

  private Long id;

  private String name;
  private String type;

  private List<SubServiceDataDTO> subServices = new ArrayList<>();

}
