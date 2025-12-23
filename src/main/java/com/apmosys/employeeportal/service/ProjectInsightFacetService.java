package com.apmosys.employeeportal.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.Exception.BadRequestException;
import com.apmosys.employeeportal.Exception.ResourceNotFoundException;
import com.apmosys.employeeportal.dto.ProjectInsightFacetCategoryDTO;
import com.apmosys.employeeportal.dto.ProjectInsightFacetValueDTO;
import com.apmosys.employeeportal.model.ProjectInsightFacetCategory;
import com.apmosys.employeeportal.repository.ProjectInsightFacetCategoryRepository;
import com.apmosys.employeeportal.repository.ProjectInsightFacetValueRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;
import com.apmosys.employeeportal.utility.ValidationUtility;

@Service
public class ProjectInsightFacetService {

    @Autowired
    private ProjectInsightFacetCategoryRepository projectInsightFacetCategoryRepository;

    @Autowired
    private ProjectInsightFacetValueRepository projectInsightFacetValueRepository;

    public List<ProjectInsightFacetCategory> getAllProjectInsightFacetCategory() {
        try {
            return projectInsightFacetCategoryRepository.findAll();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public ProjectInsightFacetCategoryDTO getProjectInsightCategoryById(Long id) {
        try {
            if (id == null) {
                throw new BadRequestException("Facet Category Id cannot be null.");
            }
            ProjectInsightFacetCategoryDTO projectInsightFacetCategoryDTO = projectInsightFacetCategoryRepository
                    .getProjectInsightFacetCategoryById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Facet Category Not Found."));

            List<ProjectInsightFacetValueDTO> projectInsightFacetValueDTOList = projectInsightFacetValueRepository
                    .findProjectInsightFacetValueByFacetCategoryId(projectInsightFacetCategoryDTO.getFacetCategoryId());

            projectInsightFacetCategoryDTO.setProjectInsightFacetValueDTOList(projectInsightFacetValueDTOList);

            return projectInsightFacetCategoryDTO;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public ServiceResponse saveProjectInsightFacetCategory(
            ProjectInsightFacetCategoryDTO projectInsightFacetCategoryDTO) {
        try {
            if (projectInsightFacetCategoryDTO == null) {
                throw new BadRequestException("Request Body Cannot be null.");
            }
            if (!ValidationUtility.isStringNotNullOrEmpty(projectInsightFacetCategoryDTO.getCategoryName())) {
                throw new BadRequestException("Facet Category Name cannot be null.");
            }

            boolean exists = projectInsightFacetCategoryRepository
                    .existsByCategoryNameIgnoreCase(projectInsightFacetCategoryDTO.getCategoryName().trim());
            if (exists) {
                throw new BadRequestException("Facet Category with name '"
                        + projectInsightFacetCategoryDTO.getCategoryName() + "' already exists.");
            }

            return null;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public ServiceResponse deleteProjectInsightCategoryById(Long id) {
        try {

            return null;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public ServiceResponse saveProjectInsightFacetCategoryList(List<ProjectInsightFacetCategoryDTO> projectInsightFacetCategoryDTOList) {
        ServiceResponse serviceResponse = new ServiceResponse();
        try {
            if (!ValidationUtility.isListNotNullOrEmpty(projectInsightFacetCategoryDTOList)) {
                serviceResponse.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
                serviceResponse.setServiceResponse("Facets List cannot be Null or Empty.");
            }

            List<String> dtoNames = projectInsightFacetCategoryDTOList.stream()
                    .filter(dto -> dto.getFacetCategoryId() == null)
                    .map(obj -> obj.getCategoryName().trim())
                    .collect(Collectors.toList());

            List<String> loweredCaseName = dtoNames.stream().map(String::toLowerCase).collect(Collectors.toList());

            List<String> existingNames = projectInsightFacetCategoryRepository
                    .findCategoryNameByCategoryNameInIgnoreCase(loweredCaseName);

            List<ProjectInsightFacetCategory> newCategories = dtoNames.stream()
                    .filter(name -> existingNames.stream().noneMatch(e -> e.equalsIgnoreCase(name)))
                    .map(name -> {
                        ProjectInsightFacetCategory category = new ProjectInsightFacetCategory();
                        category.setCategoryName(name);
                        return category;
                    })
                    .collect(Collectors.toList());

            List<ProjectInsightFacetCategory> tempList = projectInsightFacetCategoryRepository.saveAll(newCategories);
            serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            serviceResponse.setServiceResponse(tempList);
        } catch (Exception e) {
            e.printStackTrace();
            serviceResponse.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
            serviceResponse.setServiceResponse("Something Went Wrong.");
            serviceResponse.setServiceError(e.getMessage());
        }
        return serviceResponse;
    }

}
