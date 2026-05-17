package com.apmosys.employeeportal.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.mongodb.modal.ProjectInsightFormDetails;
import com.apmosys.employeeportal.mongodb.modal.ProjectInsightQuestionDetails;
import com.apmosys.employeeportal.mongodb.modal.ProjectInsightResponseDetails;
import com.apmosys.employeeportal.mongodb.repository.ProjectInsightFormDetailsRepository;
import com.apmosys.employeeportal.mongodb.repository.ProjectInsightQuestionDetailsRepository;
import com.apmosys.employeeportal.mongodb.repository.ProjectInsightResponseDetailsRepository;

@Service
public class ProjectInsightProjectFlatSearchService {

    @Autowired
    private ProjectInsightResponseDetailsRepository projectInsightResponseDetailsRepository;

    @Autowired
    private ProjectInsightQuestionDetailsRepository projectInsightQuestionDetailsRepository;

    @Autowired
	private ProjectInsightFormDetailsRepository projectInsightFormDetailsRepository ;

    @Autowired
	private ProjectInsightFlatSearchDetailsService projectInsightFlatSearchDetailsService;

    public void syncAllDataWithFlatSearch() {
        try {
            List<ProjectInsightQuestionDetails> questionDetailsList = projectInsightQuestionDetailsRepository.findAll();
            projectInsightQuestionDetailsRepository.saveAll(questionDetailsList);

            List<ProjectInsightResponseDetails> responseDetailsList = projectInsightResponseDetailsRepository.findAll();
            projectInsightResponseDetailsRepository.saveAll(responseDetailsList);

            List<ProjectInsightFormDetails>  formDetailsList = projectInsightFormDetailsRepository.findAll();
            for(ProjectInsightFormDetails projectInsightFormDetails : formDetailsList ){
                projectInsightFlatSearchDetailsService.saveProjectInsightFlatSearchDetailsProjectOrGroup(projectInsightFormDetails);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
