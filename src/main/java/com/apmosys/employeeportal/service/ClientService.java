package com.apmosys.employeeportal.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.apmosys.employeeportal.dto.ClientAddressSyncDto;
import com.apmosys.employeeportal.dto.ClientDetailsSyncDto;
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
	 
	 @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	    public int[] processSingleClientByName(String clientNamePo,ClientDetailsSyncDto poDto,
	    		Client iShineClient) {
		 
		 	int updated = 0;
	        int inserted = 0;

	        if (iShineClient != null) {

	            if (!Objects.equals(iShineClient.getPoClientId(), poDto.getClientid())) {
	                iShineClient.setPoClientId(poDto.getClientid());
	                updated++;
	            }

	            syncClientLocations(iShineClient, poDto, false);

	        } else {

	            Client newClient = new Client();
	            newClient.setClientName(poDto.getClientName());
	            newClient.setPoClientId(poDto.getClientid());

	            clientRepository.save(newClient);
	            inserted++;

	            syncClientLocations(newClient, poDto, true);
	        }

	        return new int[] { updated, inserted };
	    }
	 @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	    public int[] processSingleClientByPoId(ClientDetailsSyncDto poDto,Client iShineClient) {

	        int updated = 0;
	        int inserted = 0;

	        if (iShineClient != null) {

	            if (!iShineClient.getClientName().trim().equalsIgnoreCase(poDto.getClientName().trim()))
	            {
	            	iShineClient.setClientName(poDto.getClientName());
	                updated++;
	            }

	            syncClientLocations(iShineClient, poDto, false);

	        } else {

	            Client newClient = new Client();
	            newClient.setClientName(poDto.getClientName());
	            newClient.setPoClientId(poDto.getClientid());
	            clientRepository.save(newClient);
	            inserted++;

	            syncClientLocations(newClient, poDto, true);
	        }

	        return new int[] { updated, inserted };
	    }
	 
	 private void syncClientLocations(Client iShineClient, ClientDetailsSyncDto poDto,Boolean isNew) {

			if (poDto.getClientAddress() == null || poDto.getClientAddress().isEmpty()) {
				return;
			}

			List<ClientLocation> iShineLocations = clientLocationRepository.findByClientIdPK(iShineClient.getClientId());

			Map<String, ClientLocation> iShineLocationMap = iShineLocations.stream().collect(
					Collectors.toMap(loc -> loc.getClientLocation().trim().toLowerCase(), loc -> loc, (e1, e2) -> e1));

			List<ClientLocation> locationsToSave = new ArrayList<>();

			for (ClientAddressSyncDto poLocationDto : poDto.getClientAddress()) {

				if (poLocationDto.getClientLocation() == null)
					continue;

				String poLoc = poLocationDto.getClientLocation().trim().toLowerCase();
				ClientLocation existingLocation = iShineLocationMap.get(poLoc);

				if (existingLocation != null) {

					boolean updated = false;

					if (existingLocation.getClientState() == null && poLocationDto.getClientState() != null) {
						existingLocation.setClientState(poLocationDto.getClientState());
						updated = true;
					}

					if (existingLocation.getClientAddressId() == null && poLocationDto.getClientAddressId() != null) {
						existingLocation.setClientAddressId(poLocationDto.getClientAddressId());
						updated = true;
					}

					if (updated) {
						locationsToSave.add(existingLocation);
					}

				} else {
					ClientLocation newLocation = new ClientLocation();
					newLocation.setClientId(iShineClient.getClientId());
					newLocation.setClientLocation(poLocationDto.getClientLocation());
					newLocation.setClientState(poLocationDto.getClientState());
					newLocation.setClientAddressId(poLocationDto.getClientAddressId());

					locationsToSave.add(newLocation);
				}
			}
			
			if(isNew) {
				ClientLocation newLocation = new ClientLocation();
				newLocation.setClientId(iShineClient.getClientId());
				newLocation.setClientLocation("WFH");
//				newLocation.setClientState(poLocationDto.getClientState());
//				newLocation.setClientAddressId(poLocationDto.getClientAddressId());

				locationsToSave.add(newLocation);
			}

			if (!locationsToSave.isEmpty()) {
				clientLocationRepository.saveAll(locationsToSave);
			}
		}



}
