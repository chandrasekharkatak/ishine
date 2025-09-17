package com.apmosys.employeeportal.service;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.apmosys.employeeportal.Exception.BadRequestException;
import com.apmosys.employeeportal.dto.PageDTO;
import com.apmosys.employeeportal.model.ProjectInsightFacetCategory;
import com.apmosys.employeeportal.mongodb.dto.ProjectInsightQuestionLibraryEntryRequest;
import com.apmosys.employeeportal.mongodb.modal.ProjectInsightQuestionDetails;
import com.apmosys.employeeportal.mongodb.modal.ProjectInsightQuestionLibraryEntry;
import com.apmosys.employeeportal.mongodb.repository.ProjectInsightQuestionLibraryEntryRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.ProjectInsightFacetCategoryRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;
import com.apmosys.employeeportal.utility.ValidationUtility;

@Service
public class ProjectInsightQuestionLibraryService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ProjectInsightQuestionLibraryEntryRepository projectInsightQuestionLibraryEntryRepository;

    @Autowired
    private ProjectInsightFacetCategoryRepository projectInsightFacetCategoryRepository;

    @Autowired
    private DepartmentService departmentService;

    public Page<ProjectInsightQuestionLibraryEntry> getAllProjectInsightQuestionEntriesByDepartment(PageDTO pageDTO) {
        try {
            Query query = new Query();
            // Match if deptIds contains any of the given IDs
            query.addCriteria(Criteria.where("deptIds").in(pageDTO.getFilterIdList()));

            // Sorting
            Sort sort = pageDTO.getSortDirection().equalsIgnoreCase("desc")
                    ? Sort.by(pageDTO.getSortColumn()).descending()
                    : Sort.by(pageDTO.getSortColumn()).ascending();

            // Pagination
            int pageIndex = Math.max(pageDTO.getPage() - 1, 0); // Convert to 0-based
            Pageable pageable = PageRequest.of(pageIndex, pageDTO.getSize(), sort);
            query.with(pageable);

            long total = mongoTemplate.count(Query.of(query).limit(-1).skip(-1),
                    ProjectInsightQuestionLibraryEntry.class);

            List<ProjectInsightQuestionLibraryEntry> projectInsightQuestionLibraryEntryList = mongoTemplate.find(query,
                    ProjectInsightQuestionLibraryEntry.class);
            if (ValidationUtility.isListNotNullOrEmpty(projectInsightQuestionLibraryEntryList)) {
                for (ProjectInsightQuestionLibraryEntry projectInsightQuestionLibraryEntry : projectInsightQuestionLibraryEntryList) {
                    mapQuestionLibraryFacets(projectInsightQuestionLibraryEntry);
                }
            }
            return new PageImpl<>(projectInsightQuestionLibraryEntryList, pageable, total);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public Page<ProjectInsightQuestionLibraryEntry> getAllProjectInsightQuestionEntriesByFilter(PageDTO pageDTO) {
        try {
            Criteria criteria = new Criteria();
            if (pageDTO.getSearchKeyword() != null && !pageDTO.getSearchKeyword().isEmpty()) {
                criteria.and("question").regex(pageDTO.getSearchKeyword(), "i"); // case-insensitive search
            }

            Query query = new Query(criteria);
            long total = mongoTemplate.count(query, ProjectInsightQuestionLibraryEntry.class);

            // Sorting
            Sort sort = pageDTO.getSortDirection().equalsIgnoreCase("desc")
                    ? Sort.by(pageDTO.getSortColumn()).descending()
                    : Sort.by(pageDTO.getSortColumn()).ascending();

            // Pagination
            int pageIndex = Math.max(pageDTO.getPage() - 1, 0); // Convert to 0-based

            Pageable pageable = PageRequest.of(pageIndex, pageDTO.getSize(), sort);
            query.with(pageable);

            List<ProjectInsightQuestionLibraryEntry> projectInsightQuestionLibraryEntryList = mongoTemplate.find(query,
                    ProjectInsightQuestionLibraryEntry.class);
            if (ValidationUtility.isListNotNullOrEmpty(projectInsightQuestionLibraryEntryList)) {
                for (ProjectInsightQuestionLibraryEntry projectInsightQuestionLibraryEntry : projectInsightQuestionLibraryEntryList) {
                    mapQuestionLibraryFacets(projectInsightQuestionLibraryEntry);
                }
            }
            return new PageImpl<>(projectInsightQuestionLibraryEntryList, pageable, total);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public Page<ProjectInsightQuestionLibraryEntry> getAllProjectInsightQuestionsEntry(PageDTO pageDTO) {
        try {
            Sort sort = pageDTO.getSortDirection().equalsIgnoreCase("desc")
                    ? Sort.by(pageDTO.getSortColumn()).descending()
                    : Sort.by(pageDTO.getSortColumn()).ascending();
            int pageIndex = Math.max(pageDTO.getPage() - 1, 0); // Convert to 0-based

            Page<ProjectInsightQuestionLibraryEntry> tempPage = projectInsightQuestionLibraryEntryRepository
                    .findAll(PageRequest.of(pageIndex, pageDTO.getSize(), sort));
            if (tempPage != null && !tempPage.isEmpty()) {
                for (ProjectInsightQuestionLibraryEntry projectInsightQuestionLibraryEntry : tempPage) {
                    mapQuestionLibraryFacets(projectInsightQuestionLibraryEntry);
                }
            }
            return tempPage;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public ServiceResponse saveProjectInsightQuestionLibraryEntry(
            ProjectInsightQuestionLibraryEntry projectInsightQuestionLibraryEntry) {
        ServiceResponse serviceResponse = new ServiceResponse();
        try {
            if (projectInsightQuestionLibraryEntry == null) {
                throw new BadRequestException("Question Library Entry Details cannot be null.");
            }

            boolean isNew = (projectInsightQuestionLibraryEntry.getId() == null);
            String normalizeQuestion = normalizeQuestion(projectInsightQuestionLibraryEntry.getQuestion());

            ProjectInsightQuestionLibraryEntry similarQuestionObj = projectInsightQuestionLibraryEntryRepository
                    .findByNormalizedQuestion(normalizeQuestion);

            if (isNew && similarQuestionObj == null) {
                if (projectInsightQuestionLibraryEntry.getCreatedBy() != null) {
                    projectInsightQuestionLibraryEntry.setCreatedByName(
                            employeeRepository.findEmployeeNameById(projectInsightQuestionLibraryEntry.getCreatedBy()));
                }
                projectInsightQuestionLibraryEntry.setCreatedBy(projectInsightQuestionLibraryEntry.getCreatedBy());
                projectInsightQuestionLibraryEntry
                        .setCreatedOn(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
            } else {
                ProjectInsightQuestionLibraryEntry existing = null;
                if (similarQuestionObj != null) {
                    existing = similarQuestionObj;
                    projectInsightQuestionLibraryEntry.setId(existing.getId());
                } else {
                    Optional<ProjectInsightQuestionLibraryEntry> existingOpt = projectInsightQuestionLibraryEntryRepository
                            .findById(projectInsightQuestionLibraryEntry.getId());
                    if (existingOpt.isPresent()) {
                        existing = existingOpt.get();
                    }
                }

                if (existing != null) {
                    projectInsightQuestionLibraryEntry.setCreatedBy(existing.getCreatedBy());
                    projectInsightQuestionLibraryEntry.setCreatedOn(existing.getCreatedOn());
                    if (existing.getCreatedBy() != null) {
                        projectInsightQuestionLibraryEntry
                                .setCreatedByName(employeeRepository.findEmployeeNameById(existing.getCreatedBy()));
                    }
                } else {
                    projectInsightQuestionLibraryEntry.setCreatedBy(projectInsightQuestionLibraryEntry.getCreatedBy());
                    projectInsightQuestionLibraryEntry
                            .setCreatedOn(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
                    if (projectInsightQuestionLibraryEntry.getCreatedBy() != null) {
                        projectInsightQuestionLibraryEntry.setCreatedByName(employeeRepository
                                .findEmployeeNameById(projectInsightQuestionLibraryEntry.getCreatedBy()));
                    }
                }
                if (projectInsightQuestionLibraryEntry.getUpdatedBy() != null) {
                    projectInsightQuestionLibraryEntry.setUpdatedByName(
                            employeeRepository.findEmployeeNameById(projectInsightQuestionLibraryEntry.getUpdatedBy()));
                }
                projectInsightQuestionLibraryEntry.setUpdatedBy(projectInsightQuestionLibraryEntry.getUpdatedBy());
                projectInsightQuestionLibraryEntry
                        .setUpdatedOn(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
            }

            ProjectInsightQuestionDetails projectInsightQuestionDetails = new ProjectInsightQuestionDetails();
            projectInsightQuestionDetails.setFacetCategoryList(projectInsightQuestionLibraryEntry.getFacetCategoryList());
            saveQuestionLibraryFacetCategory(projectInsightQuestionDetails, projectInsightQuestionLibraryEntry);

            ProjectInsightQuestionLibraryEntry dbResponse = projectInsightQuestionLibraryEntryRepository
                    .save(projectInsightQuestionLibraryEntry);
            if (dbResponse == null) {
                throw new BadRequestException("Unable to save Question Library Entry Details.");
            }

            serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            serviceResponse.setServiceResponse(dbResponse);
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
            serviceResponse.setServiceMessage("Something went wrong.");
            serviceResponse.setServiceResponse("Something went wrong.");
        }
        return serviceResponse;
    }

    public ServiceResponse deleteProjectInsightQuestionLibraryEntryById(
            ProjectInsightQuestionLibraryEntry projectInsightQuestionLibraryEntry) {
        ServiceResponse serviceResponse = new ServiceResponse();
        try {
            if (projectInsightQuestionLibraryEntry == null || projectInsightQuestionLibraryEntry.getId() == null) {
                throw new BadRequestException("Question Library Entry Details cannot be null.");
            }
            projectInsightQuestionLibraryEntryRepository.deleteById(projectInsightQuestionLibraryEntry.getId());
            serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            return serviceResponse;
        } catch (BadRequestException e) {
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Something went wrong, unable to delete Project Insight Question Details !!", e);
        }
    }

    public ServiceResponse saveEntryToQuestionLibraryFromExcel(
            ProjectInsightQuestionLibraryEntryRequest projectInsightQuestionLibraryEntryRequest) {
        ServiceResponse serviceResponse = new ServiceResponse();
        try {

            if (projectInsightQuestionLibraryEntryRequest == null) {
                throw new BadRequestException("Upload Question Library Entry Details Request cannot be null.");
            }
            if (projectInsightQuestionLibraryEntryRequest.getProjectInsightQuestionLibraryEntryList() == null
                    || projectInsightQuestionLibraryEntryRequest.getProjectInsightQuestionLibraryEntryList()
                            .isEmpty()) {
                throw new BadRequestException("Upload Question Library Entry Details List cannot be null or Empty.");
            }

            for (ProjectInsightQuestionLibraryEntry projectInsightQuestionLibraryEntry : projectInsightQuestionLibraryEntryRequest
                    .getProjectInsightQuestionLibraryEntryList()) {
                projectInsightQuestionLibraryEntry
                        .setCreatedBy(projectInsightQuestionLibraryEntryRequest.getCreatedBy());
                saveProjectInsightQuestionLibraryEntry(projectInsightQuestionLibraryEntry);
            }

            serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            serviceResponse.setServiceResponse("Entries saved successfully.");
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
            serviceResponse.setServiceMessage("Something went wrong.");
            serviceResponse.setServiceResponse("Something went wrong.");
        }
        return serviceResponse;
    }

    private String normalizeQuestion(String question) {
        if (question == null)
            return null;
        return question.replaceAll("[^a-zA-Z0-9]", "").toLowerCase()
                .trim();
    }

    private String normalizeText(String input) {
        if (!StringUtils.hasText(input))
            return "";
        String noAccents = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return noAccents.replaceAll("[^a-zA-Z0-9]", "") // remove special chars & spaces
                .toLowerCase();
    }

    public List<ProjectInsightQuestionLibraryEntry> searchQuestionLibrary(String text) {
        String normalized = normalizeText(text); // lowercase, no spaces, no special chars
        String regex = ".*" + normalized + ".*";
        return projectInsightQuestionLibraryEntryRepository.findByNormalizedQuestionRegex(regex, "i"); // "i" for
    }

    public ServiceResponse getEntryFromsearchQuestionLibraryByText(String id) {
        ServiceResponse serviceResponse = new ServiceResponse();
        try {
            if (id == null) {
                throw new BadRequestException("Selected Question Id is null.");
            }

            Optional<ProjectInsightQuestionLibraryEntry> opt = projectInsightQuestionLibraryEntryRepository
                    .findById(id);
            if (opt.isPresent()) {
                ProjectInsightQuestionLibraryEntry projectInsightQuestionLibraryEntry = opt.get();
                mapQuestionLibraryFacets(projectInsightQuestionLibraryEntry);
                serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
                serviceResponse.setServiceResponse(projectInsightQuestionLibraryEntry);
            } else {
                serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
                serviceResponse.setServiceResponse("Question Not Found!!");
            }
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
            serviceResponse.setServiceMessage("Something went wrong.");
            serviceResponse.setServiceResponse("Something went wrong.");
        }
        return serviceResponse;
    }

    public void addQuestionToLibrary(ProjectInsightQuestionDetails projectInsightQuestionDetails) {
        try {
            if (projectInsightQuestionDetails == null) {
                throw new BadRequestException("Project Insight Question Details cannot be null.");
            }
            ProjectInsightQuestionLibraryEntry projectInsightQuestionLibraryEntry = new ProjectInsightQuestionLibraryEntry();
            String normalizeQuestion = normalizeQuestion(projectInsightQuestionDetails.getQuestion());

            ProjectInsightQuestionLibraryEntry similarQuestionObj = projectInsightQuestionLibraryEntryRepository
                    .findByNormalizedQuestion(normalizeQuestion);
            if (similarQuestionObj == null) {
                if (projectInsightQuestionDetails.getCreatedBy() != null) {
                    projectInsightQuestionLibraryEntry.setCreatedByName(
                            employeeRepository.findEmployeeNameById(
                                    Long.parseLong(projectInsightQuestionDetails.getCreatedBy())));
                }
                projectInsightQuestionLibraryEntry
                        .setCreatedOn(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
                projectInsightQuestionLibraryEntry.setCreatedBy(projectInsightQuestionLibraryEntry.getCreatedBy());
                projectInsightQuestionLibraryEntry
                        .setCreatedOn(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
            } else {
                ProjectInsightQuestionLibraryEntry existing = similarQuestionObj;
                projectInsightQuestionLibraryEntry.setId(existing.getId());
                projectInsightQuestionLibraryEntry.setCreatedBy(existing.getCreatedBy());
                projectInsightQuestionLibraryEntry.setUpdatedBy(Long.parseLong(projectInsightQuestionDetails.getCreatedBy()));
                projectInsightQuestionLibraryEntry.setCreatedOn(existing.getCreatedOn());
                projectInsightQuestionLibraryEntry.setUpdatedOn(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
                if (projectInsightQuestionDetails.getCreatedBy() != null) {
                    projectInsightQuestionLibraryEntry.setCreatedByName(employeeRepository.findEmployeeNameById(existing.getCreatedBy()));
                }
                if (projectInsightQuestionDetails.getUpdatedBy() != null) {
                    projectInsightQuestionLibraryEntry.setUpdatedByName(
                            employeeRepository.findEmployeeNameById(projectInsightQuestionLibraryEntry.getCreatedBy()));
                }
            }
            projectInsightQuestionLibraryEntry.setQuestion(projectInsightQuestionDetails.getQuestion());
            projectInsightQuestionLibraryEntry.setDescription(projectInsightQuestionDetails.getDescription());
            projectInsightQuestionLibraryEntry.setOptionType(projectInsightQuestionDetails.getOptionType());
            projectInsightQuestionLibraryEntry.setOptionsList(projectInsightQuestionDetails.getOptionsList());
            projectInsightQuestionLibraryEntry.setDeptIds(projectInsightQuestionDetails.getDeptIds());
            projectInsightQuestionLibraryEntry.setDepts(departmentService.getAllDeptNameByDeptId(projectInsightQuestionDetails.getDeptIds()));

            saveQuestionLibraryFacetCategory(projectInsightQuestionDetails, projectInsightQuestionLibraryEntry);
            projectInsightQuestionLibraryEntryRepository.save(projectInsightQuestionLibraryEntry);
        } catch (BadRequestException e) {
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveQuestionLibraryFacetCategory(ProjectInsightQuestionDetails projectInsightQuestionDetails,
            ProjectInsightQuestionLibraryEntry projectInsightQuestionLibraryEntry) {
        try {
            Set<String> allFacetNames = projectInsightQuestionDetails.getFacetCategoryList().stream()
                    .map(ProjectInsightFacetCategory::getCategoryName).collect(Collectors.toSet());

            List<ProjectInsightFacetCategory> existingFacets = projectInsightFacetCategoryRepository
                    .findAllByCategoryNameInIgnoreCase(
                            new ArrayList<>(allFacetNames.stream().map(String::toLowerCase)
                                    .collect(Collectors.toList())));

            Set<String> existingNamesLowered = existingFacets.stream()
                    .map(f -> f.getCategoryName().toLowerCase())
                    .collect(Collectors.toSet());

            List<ProjectInsightFacetCategory> newCategories = allFacetNames.stream()
                    .filter(entry -> !existingNamesLowered.contains(entry.toLowerCase()))
                    .map(entry -> {
                        ProjectInsightFacetCategory category = new ProjectInsightFacetCategory();
                        category.setCategoryName(entry);
                        return category;
                    })
                    .collect(Collectors.toList());

            existingFacets.addAll(projectInsightFacetCategoryRepository.saveAll(newCategories));

            Set<Long> facetIds = existingFacets.stream()
                    .map(ProjectInsightFacetCategory::getFacetCategoryId)
                    .collect(Collectors.toSet());

            if (ValidationUtility.isListNotNullOrEmpty(projectInsightQuestionLibraryEntry.getFacetCategoryIds())) {
                facetIds.addAll(projectInsightQuestionLibraryEntry.getFacetCategoryIds());
            }
            projectInsightQuestionLibraryEntry.setFacetCategoryIds(new ArrayList<>(facetIds));
            projectInsightQuestionLibraryEntry.setFacetCategoryList(Collections.emptyList());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void mapQuestionLibraryFacets(ProjectInsightQuestionLibraryEntry projectInsightQuestionLibraryEntry) {
        try {
            if (!ValidationUtility.isListNotNullOrEmpty(projectInsightQuestionLibraryEntry.getFacetCategoryIds())) {
                return;
            }
            List<ProjectInsightFacetCategory> facetCategoryList = projectInsightFacetCategoryRepository
                    .findAllByFacetCategoryIdIn(projectInsightQuestionLibraryEntry.getFacetCategoryIds());
            projectInsightQuestionLibraryEntry.setFacetCategoryList(facetCategoryList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
