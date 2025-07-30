package com.apmosys.employeeportal.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class DomainDataDTO {

  private Long id;

  private String name;
  private String type;

  private List<SubDomainDataDTO> subDomains = new ArrayList<>();

  private List<ServiceDataDTO> services = new ArrayList<>();

}
