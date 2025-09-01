package com.apmosys.employeeportal.listener;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.apmosys.employeeportal.model.ProjectInsightDomainData;
import com.apmosys.employeeportal.model.ProjectInsightDomainDataFlatSearch;
import com.apmosys.employeeportal.repository.ProjectInsightDomainDataFlatSearchRepository;

@Component
public class ProjectInsightDomainDataListener {

    private static ProjectInsightDomainDataFlatSearchRepository flatRepo;

    @Autowired
    public void setFlatRepo(ProjectInsightDomainDataFlatSearchRepository repo) {
        flatRepo = repo;
    }

    @PostPersist
    @PostUpdate
    public void afterSave(ProjectInsightDomainData entity) {
        if (entity.getId() == null) {
            return; // avoid null id error
        }

        String flatSearch = buildFlatSearch(entity);

        ProjectInsightDomainDataFlatSearch flat =
                flatRepo.findByDomainId(entity.getId())
                        .orElse(new ProjectInsightDomainDataFlatSearch());

        flat.setDomainId(entity.getId());
        flat.setFlatSearch(flatSearch);

        flatRepo.save(flat);

        System.out.println("Flat search saved for domainId " 
                + entity.getId() + " => " + flatSearch);
    }

    private String buildFlatSearch(ProjectInsightDomainData entity) {
        List<String> names = new ArrayList<>();
        ProjectInsightDomainData current = entity;
        while (current != null) {
            names.add(0, current.getName());
            current = current.getParent();
        }
        return String.join(":", names) + ":";
    }
}
