package com.apmosys.employeeportal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.dto.AppreciationDTO;
import com.apmosys.employeeportal.dto.AppreciationEventDTO;
import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.model.Appreciation;
import com.apmosys.employeeportal.service.AppreciationService;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping("/api")
public class AppreciationController {
    @Autowired
    private AppreciationService appreciationService;

    @RequestMapping(value = "/saveAppreciation",method = RequestMethod.POST)
	public ServiceResponse saveAppreciation(@RequestBody AppreciationDTO appreciationDTO){
		ServiceResponse response=appreciationService.saveAppreciation(appreciationDTO);
		return response;
	} 
    
    @RequestMapping(value = "enableAppreciation", method = RequestMethod.POST )
    public ServiceResponse enableAppreciation(@RequestBody AppreciationEventDTO AppreciationEventDTO){
		ServiceResponse response=appreciationService.enableAppreciation(AppreciationEventDTO);
		return response;
	}
    
    @RequestMapping(value = "getAllAppreciationEvent", method = RequestMethod.GET )
    public ServiceResponse getAllAppreciationEvent(){
		ServiceResponse response=appreciationService.getAllAppreciationEvent();
		return response;
	}
   
    @RequestMapping(value = "viewAppreciation", method = RequestMethod.POST )
    public ServiceResponse viewAppreciation(@RequestBody AppreciationEventDTO AppreciationEventDTO){
		ServiceResponse response=appreciationService.viewAppreciation(AppreciationEventDTO);
		return response;
	}
//     OnCheckEventName
    @RequestMapping(value = "OnCheckEventName", method = RequestMethod.POST )
    public ServiceResponse OnCheckEventName(@RequestBody AppreciationEventDTO AppreciationEventDTO){
		ServiceResponse response=appreciationService.OnCheckEventName(AppreciationEventDTO);
		return response;
	}
    
    @RequestMapping(value = "updateAppreciationEvent", method = RequestMethod.POST )	
    public ServiceResponse updateAppreciationEvent(@RequestBody AppreciationEventDTO AppreciationEventDTO){	
		ServiceResponse response=appreciationService.updateAppreciationEvent(AppreciationEventDTO);	
		return response;	
	}	
    	
    @RequestMapping(value = "deleteAppreciationEvent", method = RequestMethod.POST )	
    public ServiceResponse deleteAppreciationEvent(@RequestBody AppreciationEventDTO AppreciationEventDTO){	
		ServiceResponse response=appreciationService.deleteAppreciationEvent(AppreciationEventDTO);	
		return response;	
	}
    
    @RequestMapping(value = "getAppreciateEmployeeByCurrentUser", method = RequestMethod.POST )	
    public ServiceResponse getAppreciateEmployeeByCurrentUser(@RequestBody AppreciationDTO appreciationDTO){	
		ServiceResponse response=appreciationService.getAppreciateEmployeeByCurrentUser(appreciationDTO);	
		return response;	
	}

    @RequestMapping(value = "CountMyAppreciationBYcurrentUser" ,method = RequestMethod.POST)
    public ServiceResponse CountMyAppreciationBYcurrentUser(@RequestBody AppreciationDTO appreciationDTO) {
    	ServiceResponse response=appreciationService.CountMyAppreciationBYcurrentUser(appreciationDTO);
    	return response;
    }
    @RequestMapping(value = "getAppreciationEventSummaryInfo" ,method = RequestMethod.POST)
   public ServiceResponse getAppreciationEventSummaryInfo(@RequestBody AppreciationEventDTO AppreciationEventDTO ) {
  	ServiceResponse response=appreciationService.getAppreciationEventSummaryInfo(AppreciationEventDTO);
   	return response;
    }
    
    @RequestMapping(value = "getAllEmployeeAppreciationListByCategory" ,method = RequestMethod.POST)
    public ServiceResponse getAllEmployeeAppreciationListByCategory(@RequestBody AppreciationEventDTO AppreciationEventDTO ) {
   	ServiceResponse response=appreciationService.getAllEmployeeAppreciationListByCategory(AppreciationEventDTO);
    	return response;
     }
    @RequestMapping(value = "viewAppreciationInfo" ,method = RequestMethod.POST)
    public ServiceResponse viewAppreciationInfo(@RequestBody AppreciationEventDTO AppreciationEventDTO ) {
   	ServiceResponse response=appreciationService.viewAppreciationInfo(AppreciationEventDTO);
    	return response;
     }
    
    @RequestMapping(value = "getMyAppreciationDetails" ,method = RequestMethod.POST)
    public ServiceResponse getMyAppreciationDetails(@RequestBody AppreciationDTO appreciationDTO) {
    ServiceResponse response = appreciationService.getMyAppreciationDetails(appreciationDTO);
    return response;
    }
    
    @RequestMapping(value = "getTeamAppreciationDetails", method = RequestMethod.POST)
    public ServiceResponse getTeamAppreciationDetails(@RequestBody AppreciationDTO appreciationDTO) {
        ServiceResponse response = appreciationService.getTeamAppreciationDetails(appreciationDTO);
        return response;
    }
    
    
    @RequestMapping(value = "appreciationSentByCurrentUser", method = RequestMethod.POST)
    public ServiceResponse appreciationSentByCurrentUser(@RequestBody EmployeeDTO employeeDTO) {
    	  ServiceResponse response = appreciationService.appreciationByCurrentUser(employeeDTO);
          return response;
    }
    
    
    @RequestMapping(value = "appreciationReceivedByCurrentUser", method = RequestMethod.POST)
    public ServiceResponse appreciationReceivedByCurrentUser(@RequestBody EmployeeDTO employeeDTO) {
    	  ServiceResponse response = appreciationService.appreciationToCurrentUser(employeeDTO);
          return response;
    }

   
    
}
