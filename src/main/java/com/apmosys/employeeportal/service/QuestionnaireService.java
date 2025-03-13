package com.apmosys.employeeportal.service;

import com.apmosys.employeeportal.dto.QuestionnaireDTO;
import com.apmosys.employeeportal.model.Questionnaire;
import com.apmosys.employeeportal.repository.QuestionnaireRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuestionnaireService {

    @Autowired
    private QuestionnaireRepository questionnaireRepository;

    public Questionnaire createQuestionnaireTemplate(QuestionnaireDTO dto) {
        Questionnaire questionnaire = new Questionnaire();
        questionnaire.setQuestionTitle(dto.getQuestionTitle());
//        questionnaire.setQuestionDescription(dto.getQuestionDescription());
        questionnaire.setCreatedBy(dto.getCreatedBy());
        questionnaire.setQuarterId(dto.getQuarterId());
        return questionnaireRepository.save(questionnaire);
    }

   
    public List<Questionnaire> getAllQuestionnaires() {
        return questionnaireRepository.findAll();
    }

  
    public Questionnaire getQuestionnaireById(Long id) {
        return questionnaireRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Questionnaire not found"));
    }

   
    public List<Questionnaire> getQuestionsByQuarter(Long quarterId) {
        return questionnaireRepository.findByQuarterId(quarterId);
    }

    public Questionnaire updateQuestionnaire(Long id, QuestionnaireDTO dto) {
        Questionnaire questionnaire = getQuestionnaireById(id);
        questionnaire.setQuestionTitle(dto.getQuestionTitle());
        questionnaire.setQuestionDescription(dto.getQuestionDescription());
        questionnaire.setCreatedBy(dto.getCreatedBy());
        questionnaire.setQuarterId(dto.getQuarterId());
        return questionnaireRepository.save(questionnaire);
    }

  
    public void deleteQuestionnaire(Long id) {
        questionnaireRepository.deleteById(id);
    }
}
