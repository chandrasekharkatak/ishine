package com.apmosys.employeeportal.utility;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.apmosys.employeeportal.dto.TagDTO;
import com.apmosys.employeeportal.model.TagMaster;
import com.apmosys.employeeportal.repository.TagMasterRepository;

@Component
public class TagUtils {
	
	@Value("${pythonUrl.for.ocr}")
	private String pythonUrl;
	
	@Autowired
	NLPUtils nlpUtils;
	
	@Autowired
	TagMasterRepository tagMasterRepository;
	
	//save tags project-milestone-module-submodule wise
	public ServiceResponse saveTags(TagDTO tagDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
	        String projectText = tagDTO.getProjectText().toString();

	        if (tagDTO.getProjectId() == null || projectText == null || projectText.isEmpty() || tagDTO.getTagType() == null) {
	        	response.setServiceMessage("Invalid Project ID or Text or TagType");
	        	response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	        	return response;
	        }

	        List<String> extractedTags = nlpUtils.extractTags(projectText);
	        List<TagMaster> existingTags = tagMasterRepository.findByEntityIdAndEntityType(tagDTO.getEntityId(), tagDTO.getEntityType());
	        Set<String> existingTagNames = existingTags.stream()
	        											.map(TagMaster::getTag)
	        											.collect(Collectors.toSet());

	        if ("response".equalsIgnoreCase(tagDTO.getTagType())) {
	            extractedTags.removeIf(existingTagNames::contains);
	        } else {
	            tagMasterRepository.deleteAll(existingTags);
	        }

	        // Save new tags if any remain
	        if (!extractedTags.isEmpty()) {
	            List<TagMaster> newTags = extractedTags.stream()
	            	.filter(tag -> tag != null)
	                .map(tag -> {
	                	TagMaster tm = new TagMaster();
 	                    
 	                    tm.setEntityId(tagDTO.getEntityId());
	                	tm.setEntityType(tagDTO.getEntityType());
	                	tm.setProjectId(tagDTO.getProjectId());
	                	tm.setTag(tag);
	                	tm.setType(tagDTO.getType());
 	                    
 	                    return tm;
	                })
	                .collect(Collectors.toList());

	            List<TagMaster> savedTags = tagMasterRepository.saveAll(newTags);
	            if (!savedTags.isEmpty()) {
	            	response.setServiceMessage("Success");
		        	response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	            }else {
	            	response.setServiceMessage("Failed");
		        	response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            }
	        } else {
	        	response.setServiceMessage("No New Tags to Save");
	        	response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        }
		}catch(Exception e) {
			e.printStackTrace();
		}
		return response;
	}
	
	
	//Share file (pdf , image, excel, word etc) to python for OCR
	public ServiceResponse sendFileForOCR(MultipartFile document, Long entityId, String entityName, Long questionMasterId, Long projectId) {
		ServiceResponse response = new ServiceResponse();
		try {
			if (document != null) {
			    String dmsPortalUploadUrl = pythonUrl + "/upload";
			    RestTemplate restTemplate = new RestTemplate();

			    HttpHeaders headers = new HttpHeaders();
			    headers.setContentType(MediaType.MULTIPART_FORM_DATA);

			    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
			    body.add("file", new ByteArrayResource(document.getBytes()) {
			        @Override
			        public String getFilename() {
			            return document.getOriginalFilename();
			        }
			    });
			    // Add entityId and entityName to the request body
			    body.add("entityId", String.valueOf(entityId));
			    body.add("entityName", entityName);
			    body.add("projectId", String.valueOf(projectId));
			    body.add("questionMasterId", String.valueOf(questionMasterId));

			    HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
			    String syncResponse = restTemplate.postForObject(dmsPortalUploadUrl, requestEntity, String.class);
			    JSONObject json = new JSONObject(syncResponse);
			    if (!json.isNull("message")) {
			        String obj = json.getString("message");
			        if (obj != null) {
			        	response.setServiceMessage(obj);
			        }
			    }
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		return response;
	}
	

}
