package com.apmosys.employeeportal.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.model.Client;
import com.apmosys.employeeportal.model.ClientLocation;
import com.apmosys.employeeportal.repository.ClientLocationRepository;
import com.apmosys.employeeportal.repository.ClientsRepository;
import com.apmosys.employeeportal.utility.ExceptionLogContext;

@Service
public class ClientService {
	
	@Autowired
	ClientsRepository clientRepository;
	
	@Autowired
	ClientLocationRepository clientLocationRepository;
	
	 public Client resolveClient(String clientName) {

	        if (clientName == null) {
	        	ExceptionLogContext.add("Client name Missing from PO");
	            throw new RuntimeException("Client name Missing from PO");
	        }

	        return clientRepository.findByClientName(clientName)
	                .orElseGet(() -> {
	                    Client c = new Client();
	                    c.setClientName(clientName);
	                    return clientRepository.save(c);
	                });
	    }
	 
	 
	 public ClientLocation resolveClientLocation(
	         Integer clientId,
	         String location,
	         String state) {

	     if (clientId == null || location == null || state == null) {
	    		ExceptionLogContext.add("ClientId or location missing or state missing");
	         throw new RuntimeException("ClientId or location missing or state missing");
	     }
	     
	     boolean hasAnyLocation =
	             clientLocationRepository.existsByClientId(clientId);

	     if (!hasAnyLocation) {

	         ClientLocation wfh = new ClientLocation();
	         wfh.setClientId(clientId);
	         wfh.setClientLocation("WFH");
	         wfh.setClientState(null); 

	         clientLocationRepository.save(wfh);

	      
	     }

	     
	     Optional<ClientLocation> exact =
	             clientLocationRepository
	                 .findByClientIdAndClientLocationAndClientState(
	                         clientId, location, state);

	     if (exact.isPresent()) {
	         return exact.get();
	     }

	    
	     Optional<ClientLocation> legacy =
	             clientLocationRepository
	                 .findByClientIdAndClientLocationAndClientStateIsNull(
	                         clientId, location);

	     if (legacy.isPresent()) {
	         ClientLocation cl = legacy.get();
	         cl.setClientState(state);
	         return clientLocationRepository.save(cl);
	     }
	     
	     

	    
	     ClientLocation cl = new ClientLocation();
	     cl.setClientId(clientId);
	     cl.setClientLocation(location);
	     cl.setClientState(state);
	     return clientLocationRepository.save(cl);
	 }


}
