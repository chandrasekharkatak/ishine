package com.apmosys.employeeportal.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class InActivePoDTO {
  private String totalEmpPerProjectTypeLast7days;
  private String totalEmpPerProjectTypeLast30days;
  private String totalEmpPerProjectTypeLast90days;
  private String totalEmpPerProjectTypeLast180days;
  private String totalEmpPerProjectTypeLast1Year;
  private String totalEmpPerProjectTypeTotal;
  private Integer days;
  private String tabName;
  private String billableType;
  private String poProjectType;
  private List<Long> deptId;
  
  
}
