package com.apmosys.employeeportal.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KpisDTO {
    private Long id;
    private String description;
    private Float response;
}
