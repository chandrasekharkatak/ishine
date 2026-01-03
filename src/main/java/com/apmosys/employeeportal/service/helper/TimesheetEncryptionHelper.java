package com.apmosys.employeeportal.service.helper;

import org.springframework.stereotype.Component;

import com.apmosys.employeeportal.dto.CreateTimesheetRequestDTONew;
import com.apmosys.employeeportal.dto.TimesheetDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.employeeTimesheetMappingDTO_new;
import com.apmosys.employeeportal.utility.EncryptionUtil;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.extern.slf4j.Slf4j;

/**
 * Helper service for handling encryption/decryption operations related to Timesheet DTOs.
 * 
 * This service encapsulates the logic for:
 * - Decrypting encrypted timesheet data
 * - Converting decrypted JSON to TimesheetDTO
 * - Configuring ObjectMapper for proper deserialization
 * 
 * @author Timesheet Refactoring - Phase 1
 */
@Slf4j
@Component
public class TimesheetEncryptionHelper {

    private final EncryptionUtil encryptionUtil;
    private final ObjectMapper objectMapper;

    /**
     * Constructor - Initializes EncryptionUtil and configures ObjectMapper
     */
    public TimesheetEncryptionHelper() {
        this.encryptionUtil = new EncryptionUtil();
        this.objectMapper = createConfiguredObjectMapper();
    }

    /**
     * Creates and configures an ObjectMapper instance for TimesheetDTO deserialization.
     * 
     * Configuration includes:
     * - JavaTimeModule for LocalDate/LocalDateTime support
     * - FAIL_ON_UNKNOWN_PROPERTIES = false (to handle version mismatches)
     * - ACCEPT_EMPTY_STRING_AS_NULL_OBJECT = true
     * - READ_UNKNOWN_ENUM_VALUES_AS_NULL = true
     * 
     * @return Configured ObjectMapper instance
     */
    private ObjectMapper createConfiguredObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        mapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
        return mapper;
    }

    /**
     * Decrypts encrypted timesheet data and converts it to TimesheetDTO.
     * 
     * @param encryptedDto Base64 encrypted JSON string containing timesheet data
     * @return TimesheetDTO object parsed from decrypted JSON
     * @throws Exception if decryption or parsing fails
     */
    public TimesheetDTO decryptAndParseTimesheetDto(String encryptedDto) throws Exception {
        try {
            log.debug("Decrypting timesheet DTO");
            
            // Decrypt the encrypted data
            String decryptedJson = encryptionUtil.decryptMinor(encryptedDto);
            
            log.debug("Decrypted JSON length: {}", decryptedJson != null ? decryptedJson.length() : 0);
            
            // Parse JSON to TimesheetDTO
            TimesheetDTO dto = objectMapper.readValue(decryptedJson, TimesheetDTO.class);
            
            log.debug("Successfully parsed TimesheetDTO for empId: {}", dto.getEmpId());
            
            return dto;
        } catch (Exception e) {
            log.error("Error decrypting/parsing timesheet DTO: {}", e.getMessage(), e);
            throw new Exception("Failed to decrypt or parse timesheet data: " + e.getMessage(), e);
        }
    }

    /**
     * Decrypts encrypted timesheet data and converts it to CreateTimesheetRequestDTONew.
     * 
     * @param encryptedDto Base64 encrypted JSON string containing timesheet data
     * @return CreateTimesheetRequestDTONew object parsed from decrypted JSON
     * @throws Exception if decryption or parsing fails
     */
    public CreateTimesheetRequestDTONew decryptAndParseTimesheetDtoNew(String encryptedDto) throws Exception {
        try {
            log.debug("Decrypting timesheet DTO (New)");
            
            // Decrypt the encrypted data
            String decryptedJson = encryptionUtil.decryptMinor(encryptedDto);
            
            log.debug("Decrypted JSON length: {}", decryptedJson != null ? decryptedJson.length() : 0);
            
            // Parse JSON to CreateTimesheetRequestDTONew
            CreateTimesheetRequestDTONew dto = objectMapper.readValue(decryptedJson, CreateTimesheetRequestDTONew.class);
            
            log.debug("Successfully parsed CreateTimesheetRequestDTONew for empId: {}", dto.getEmpId());
            
            return dto;
        } catch (Exception e) {
            log.error("Error decrypting/parsing timesheet DTO (New): {}", e.getMessage(), e);
            throw new Exception("Failed to decrypt or parse timesheet data: " + e.getMessage(), e);
        }
    }

    /**
     * Decrypts encrypted timesheet data and converts it to employeeTimesheetMappingDTO_new.
     * 
     * @param encryptedDto Base64 encrypted JSON string containing timesheet data
     * @return employeeTimesheetMappingDTO_new object parsed from decrypted JSON
     * @throws Exception if decryption or parsing fails
     */
    public employeeTimesheetMappingDTO_new decryptAndParseTimesheetDtoNewMapping(String encryptedDto) throws Exception {
        try {
            log.debug("Decrypting timesheet DTO (New Mapping)");
            
            // Decrypt the encrypted data
            String decryptedJson = encryptionUtil.decryptMinor(encryptedDto);
            
            log.debug("Decrypted JSON length: {}", decryptedJson != null ? decryptedJson.length() : 0);
            
            // Parse JSON to employeeTimesheetMappingDTO_new
            employeeTimesheetMappingDTO_new dto = objectMapper.readValue(decryptedJson, employeeTimesheetMappingDTO_new.class);
            
            log.debug("Successfully parsed employeeTimesheetMappingDTO_new for empId: {}", 
                    dto.getEmployeeTimesheet() != null ? dto.getEmployeeTimesheet().getEmpId() : null);
            
            return dto;
        } catch (Exception e) {
            log.error("Error decrypting/parsing timesheet DTO (New Mapping): {}", e.getMessage(), e);
            throw new Exception("Failed to decrypt or parse timesheet data: " + e.getMessage(), e);
        }
    }
}

