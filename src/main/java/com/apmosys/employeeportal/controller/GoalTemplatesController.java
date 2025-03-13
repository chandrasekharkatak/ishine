package com.apmosys.employeeportal.controller;

import com.apmosys.employeeportal.dto.GoalTemplatesDto;

import com.apmosys.employeeportal.service.GoalTemplateService;
import com.apmosys.employeeportal.utility.ServiceResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/goal-templates")
public class GoalTemplatesController {
	
	@Autowired
	 private GoalTemplateService goalTemplatesService;

    @PostMapping
    public ServiceResponse createGoalTemplate(@RequestBody GoalTemplatesDto goalTemplatesDto) {
        return goalTemplatesService.createGoalTemplate(goalTemplatesDto);
    }
    @GetMapping("/{id}")
    public ServiceResponse getGoalTemplateById(@PathVariable Long id) {
        return goalTemplatesService.getGoalTemplateById(id);
    }

    @GetMapping
    public ServiceResponse getAllGoalTemplates() {
        return goalTemplatesService.getAllGoalTemplates();
    }

    @GetMapping("/department/{departmentId}")
    public ServiceResponse getGoalTemplatesByDepartmentId(@PathVariable Long departmentId) {
        return goalTemplatesService.getGoalTemplatesByDepartmentId(departmentId);
    }
    @GetMapping("/department/name/{departmentName}")
    public ServiceResponse getGoalTemplatesByDepartmentName(@PathVariable String departmentName) {
        return goalTemplatesService.getGoalTemplatesByDepartmentName(departmentName);
    }
    @PutMapping("/{id}")
    public GoalTemplatesDto updateGoalTemplate(@PathVariable Long id, @RequestBody GoalTemplatesDto goalTemplatesDto) {
        return goalTemplatesService.updateGoalTemplate1(id, goalTemplatesDto);
    }
    
    @DeleteMapping("/{id}")
    public ServiceResponse deleteGoalTemplate(@PathVariable Long id) {
        return goalTemplatesService.deleteGoalTemplate(id);
    }
    
}