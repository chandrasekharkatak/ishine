package com.apmosys.employeeportal.service;

import com.apmosys.employeeportal.dto.AssignableEmployeeDTO;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.dto.TrainingMappingRequestDTO;
import com.apmosys.employeeportal.model.EmployeeTrainingMapping;
import com.apmosys.employeeportal.model.TrainingMaster;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.EmployeeTrainingMappingRepository;
import com.apmosys.employeeportal.repository.TrainingMasterRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrainingMappingService {

    private final EmployeeTrainingMappingRepository mappingRepo;
    private final EmployeeRepository               employeeRepo;
    private final TrainingMasterRepository         trainingRepo;

    public List<AssignableEmployeeDTO> getExcludableEmployees(Long trainingId,
            List<Integer> deptIds) {

        ServiceResponse response = new ServiceResponse();
        LogDTO apiLogInfo = new LogDTO();
        apiLogInfo.setSubFeatureName("Get Excludable Employees");
        apiLogInfo.setApiUrl("/api/training/" + trainingId + "/excludable-employees");
        apiLogInfo.setLogLevel("INFO");

        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("Training ID: ").append(trainingId);
        logBuilder.append(", Dept IDs: ").append(deptIds);

        try {
            List<Object[]> rows;

            if (deptIds == null || deptIds.isEmpty()) {
                rows = employeeRepo.getAllActiveEmployeesForAssignment();
            } else {
                rows = employeeRepo.getAllEmployeeToExcludeFromTraining(deptIds);
            }

            if (rows == null || rows.isEmpty()) {
                apiLogInfo.setApiResponse("No employees found");
                apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
                return List.of();
            }

            Set<Long> excludedIds = new HashSet<>(
                mappingRepo.findExcludedEmpIdsByTrainingId(trainingId.intValue())
            );

            Map<Long, Object[]> uniqueMap = new LinkedHashMap<>();
            for (Object[] row : rows) {
                if (row[1] != null) {
                    Long empId = ((Number) row[1]).longValue();
                    uniqueMap.putIfAbsent(empId, row);
                }
            }

            List<AssignableEmployeeDTO> result = uniqueMap.values().stream()
                .map(row -> {
                    Long employeeId = row[0] != null ? ((Number) row[0]).longValue() : null;
                    Long empId      = row[1] != null ? ((Number) row[1]).longValue() : null;
                    String name     = row[2] != null ? (String) row[2] : "";
                    String deptName = row[7] != null ? (String) row[7] : "";
                    Integer deptId  = row[9] != null ? ((Number) row[9]).intValue() : null;

                    if (empId == null) return null;
                    Boolean alreadyAssigned = !excludedIds.contains(empId);

                    return new AssignableEmployeeDTO(
                        employeeId,
                        empId,
                        name,
                        deptName,
                        deptId,
                        alreadyAssigned
                    );
                })
                .filter(dto -> dto != null)
                .collect(Collectors.toList());

            apiLogInfo.setApiResponse("Fetched " + result.size() + " employees");
            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

            return result;

        } catch (Exception e) {
            e.printStackTrace();
            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
            apiLogInfo.setLogLevel("ERROR");
            throw e;
        } 
    }

    @Transactional
    public void excludeEmployeesToTraining(Long trainingId, TrainingMappingRequestDTO request) {

        LogDTO apiLogInfo = new LogDTO();
        apiLogInfo.setSubFeatureName("Exclude Employees To Training");
        apiLogInfo.setApiUrl("/api/training/" + trainingId + "/exclude");
        apiLogInfo.setLogLevel("INFO");

        try {
            TrainingMaster training = trainingRepo.findByTrainingId(trainingId.intValue())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Training not found: " + trainingId));

            Set<Long> excludedIds = new HashSet<>(
                request.getExcludedEmployeeIds() != null ?
                request.getExcludedEmployeeIds() : new ArrayList<>()
            );

            Timestamp now = Timestamp.from(Instant.now());

            Map<Long, EmployeeTrainingMapping> existingMap = mappingRepo
                    .findAllByTrainingId(trainingId.intValue())
                    .stream()
                    .collect(Collectors.toMap(
                        EmployeeTrainingMapping::getEmpId,
                        m -> m
                    ));

            List<EmployeeTrainingMapping> toSave = new ArrayList<>();
            for (Long empId : excludedIds) {
                if (existingMap.containsKey(empId)) {
                    EmployeeTrainingMapping existing = existingMap.get(empId);
                    if (!"false".equals(existing.getActiveStatus())) {
                        existing.setActiveStatus("false");
                        existing.setUpdatedBy(request.getUpdatedBy());
                        existing.setUpdatedOn(now);
                        toSave.add(existing);
                    }
                } else {
                    EmployeeTrainingMapping m = new EmployeeTrainingMapping();
                    m.setTrainingMaster(training);
                    m.setEmpId(empId);
                    m.setActiveStatus("false");
                    m.setCreatedBy(request.getUpdatedBy());
                    m.setUpdatedBy(request.getUpdatedBy());
                    m.setUpdatedOn(now);
                    toSave.add(m);
                }
            }
            for (Map.Entry<Long, EmployeeTrainingMapping> entry : existingMap.entrySet()) {
                if (!excludedIds.contains(entry.getKey()) && "false".equals(entry.getValue().getActiveStatus())) {
                    EmployeeTrainingMapping existing = entry.getValue();
                    existing.setActiveStatus("true");
                    existing.setUpdatedBy(request.getUpdatedBy());
                    existing.setUpdatedOn(now);
                    toSave.add(existing);
                }
            }

            if (!toSave.isEmpty()) mappingRepo.saveAll(toSave);

            apiLogInfo.setApiResponse("Processed " + toSave.size() + " records. Excluded: "
                    + excludedIds.size());
            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

        } catch (Exception e) {
            e.printStackTrace();
            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
            apiLogInfo.setLogLevel("ERROR");
            throw e;
        }
    }
}
