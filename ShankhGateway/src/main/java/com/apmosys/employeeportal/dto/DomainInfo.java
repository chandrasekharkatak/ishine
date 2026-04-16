package com.apmosys.employeeportal.dto;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DomainInfo {
    private String color;
    private Set<String> data = new HashSet<>();
    private Set<String> totalClients;
    private String totalQuestions;

    public DomainInfo(String color) {
        this.color = color;
    }

    public void setData(Object input) {
        if (input == null) return;

        if (input instanceof String) {
            this.data.add((String) input);

        } else if (input instanceof Collection) {
            for (Object item : (Collection<?>) input) {
                if (item instanceof String) {
                    this.data.add((String) item);
                } else {
                    throw new IllegalArgumentException(
                        "Invalid element type: " + item.getClass().getName()
                    );
                }
            }

        } else if (input instanceof String[]) {
            this.data.addAll(Arrays.asList((String[]) input));

        } else {
            throw new IllegalArgumentException(
                "Unsupported data type: " + input.getClass().getName()
            );
        }
    }
}
