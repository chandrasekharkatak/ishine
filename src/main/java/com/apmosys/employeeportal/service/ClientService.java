package com.apmosys.employeeportal.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
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
import com.apmosys.employeeportal.dto.PoDetailsForProjectPoMappingDTO;
import com.apmosys.employeeportal.dto.ProjectPoMappingWithResourceDTO;
import com.apmosys.employeeportal.model.Client;
import com.apmosys.employeeportal.model.ClientLocation;
import com.apmosys.employeeportal.model.Project;
import com.apmosys.employeeportal.repository.ClientLocationRepository;
import com.apmosys.employeeportal.repository.ClientsRepository;
import com.apmosys.employeeportal.repository.EmployeeTeamMapRepository;
import com.apmosys.employeeportal.repository.ProjectRepository;
import com.apmosys.employeeportal.repository.TeamRepository;
import com.apmosys.employeeportal.utility.ExceptionLogContext;

@Service
public class ClientService {
	
	@Autowired
	ClientsRepository clientRepository;
	
	@Autowired
	ClientLocationRepository clientLocationRepository;
	
	@Autowired
	TeamRepository teamRepository;

	@Autowired
	EmployeeTeamMapRepository employeeTeamMapRepository;
	
	@Autowired
	ProjectRepository projectRepository;

	
	 public Client resolveClient(String clientName,Long poClientId) {
		 
		 String normalizedName = clientName.trim();
		    String normalizedLower = normalizedName.toLowerCase();

		 
		 
		 Optional<Client> byPoClient =
	                clientRepository.findByPoClientId(poClientId);
		 
		 if (byPoClient.isPresent()) {

	            Client existing = byPoClient.get();

	            String existingNormalized =
	                    existing.getClientName() != null
	                            ? existing.getClientName().trim()
	                            : "";

	            if (!existingNormalized.equalsIgnoreCase(normalizedName)) {
	                existing.setClientName(normalizedName);
	                return clientRepository.save(existing);
	            }

	            return existing;
	        }else {
	        	Optional<Client> byName =
	                    clientRepository.findByClientNameIgnoreCaseAndTrim(normalizedLower);

	            if (byName.isPresent()) {
	                Client existing = byName.get();
	                existing.setPoClientId(poClientId);
	                return clientRepository.save(existing);
	            }else {
	            
	            Client c = new Client();
	            c.setClientName(clientName);
	            c.setPoClientId(poClientId);
	            return clientRepository.save(c);
	            }
	        }
	    }
	 
	 //previously done when clienraddressid not introduced
//	 public ClientLocation resolveClientLocation(
//	         Integer clientId,
//	         String location,
//	         String state) {
//
//	     if (clientId == null || location == null || state == null) {
//	    		ExceptionLogContext.add("ClientId or location missing or state missing");
//	         throw new RuntimeException("ClientId or location missing or state missing");
//	     }
//	     
//	     boolean hasAnyLocation =
//	             clientLocationRepository.existsByClientId(clientId);
//
//	     if (!hasAnyLocation) {
//
//	         ClientLocation wfh = new ClientLocation();
//	         wfh.setClientId(clientId);
//	         wfh.setClientLocation("WFH");
//	         wfh.setClientState(null); 
//
//	         clientLocationRepository.save(wfh);
//
//	      
//	     }
//
//	     
//	     Optional<ClientLocation> exact =
//	             clientLocationRepository
//	                 .findByClientIdAndClientLocationAndClientState(
//	                         clientId, location, state);
//
//	     if (exact.isPresent()) {
//	         return exact.get();
//	     }
//
//	    
//	     Optional<ClientLocation> legacy =
//	             clientLocationRepository
//	                 .findByClientIdAndClientLocationAndClientStateIsNull(
//	                         clientId, location);
//
//	     if (legacy.isPresent()) {
//	         ClientLocation cl = legacy.get();
//	         cl.setClientState(state);
//	         return clientLocationRepository.save(cl);
//	     }
//	     
//	     
//
//	    
//	     ClientLocation cl = new ClientLocation();
//	     cl.setClientId(clientId);
//	     cl.setClientLocation(location);
//	     cl.setClientState(state);
//	     return clientLocationRepository.save(cl);
//	 }
	 
	 //when cron is working fine we need to do this
//	 public ClientLocation resolveClientLocation(
//		        Integer clientId,
//		        String location,
//		        String state,
//		        Long clientAddressId) {
//
//		    if (clientId == null || location == null || state == null || clientAddressId == null) {
//		        ExceptionLogContext.add("ClientId or location or state or clientAddressId missing");
//		        throw new RuntimeException("Required fields missing");
//		    }
//
//		    // Ensure client has at least one location (WFH default logic)
//		    boolean hasAnyLocation =
//		            clientLocationRepository.existsByClientId(clientId);
//
//		    if (!hasAnyLocation) {
//		        ClientLocation wfh = new ClientLocation();
//		        wfh.setClientId(clientId);
//		        wfh.setClientLocation("WFH");
//		        wfh.setClientState(null);
//		        clientLocationRepository.save(wfh);
//		    }
//
//		    // STEP 1: Find all rows by clientAddressId
//		    List<ClientLocation> addressMatches =
//		            clientLocationRepository.findByClientAddressId(clientAddressId);
//
//		    if (addressMatches.isEmpty()) {
//		        // As per assumption this should not happen,
//		        // but still safe handling
//		        ClientLocation newLoc = new ClientLocation();
//		        newLoc.setClientId(clientId);
//		        newLoc.setClientLocation(location);
//		        newLoc.setClientState(state);
//		        newLoc.setClientAddressId(clientAddressId);
//		        return clientLocationRepository.save(newLoc);
//		    }
//
//		    // STEP 2: Sync location + state from PO (update all if changed)
//		    for (ClientLocation cl : addressMatches) {
//
//		        boolean changed = false;
//
//		        if (!location.equalsIgnoreCase(cl.getClientLocation())) {
//		            cl.setClientLocation(location);
//		            changed = true;
//		        }
//
//		        if (cl.getClientState() == null ||
//		                !state.equalsIgnoreCase(cl.getClientState())) {
//		            cl.setClientState(state);
//		            changed = true;
//		        }
//
//		        if (changed) {
//		            clientLocationRepository.save(cl);
//		        }
//		    }
//
//		    // STEP 3: Check if this client already mapped with this addressId
//		    Optional<ClientLocation> clientSpecific =
//		            addressMatches.stream()
//		                    .filter(cl -> cl.getClientId().equals(clientId))
//		                    .findFirst();
//
//		    if (clientSpecific.isPresent()) {
//		        return clientSpecific.get();
//		    }
//
//		    // STEP 4: If not mapped → create new mapping for this client
//		    ClientLocation newLoc = new ClientLocation();
//		    newLoc.setClientId(clientId);
//		    newLoc.setClientLocation(location);
//		    newLoc.setClientState(state);
//		    newLoc.setClientAddressId(clientAddressId);
//
//		    return clientLocationRepository.save(newLoc);
//		}
	 
	 
	 
	 //when no cron than this
	 public ClientLocation resolveClientLocation(
		        Integer clientId,
		        String location,
		        String state,
		        Long clientAddressId) {

		 String normalizedLocation = location.trim();
		    String normalizedLocationLower = normalizedLocation.toLowerCase();

		    String normalizedState = state.trim() ;
		    String normalizedStateLower = normalizedState.toLowerCase();

		    // Ensure at least one location exists
		    boolean hasAnyLocation =
		            clientLocationRepository.existsByClientId(clientId);

//		    if (!hasAnyLocation) {
//		        ClientLocation wfh = new ClientLocation();
//		        wfh.setClientId(clientId);
//		        wfh.setClientLocation("WFH");
//		        wfh.setClientState(null);
//		        clientLocationRepository.save(wfh);
//		    }

		   
		    List<ClientLocation> addressMatches =
		            clientLocationRepository.findByClientAddressId(clientAddressId);

		    if (!addressMatches.isEmpty()) {

		        // 1 Update location/state if changed (sync from PO)
		        for (ClientLocation cl : addressMatches) {

		            boolean changed = false;

		            if (!normalizedLocation.equalsIgnoreCase(cl.getClientLocation().trim())) {
		                cl.setClientLocation(location);
		                changed = true;
		            }

		            if (!normalizedState.equalsIgnoreCase(cl.getClientState().trim())) {
		                cl.setClientState(state);
		                changed = true;
		            }

		            if (changed) {
		                clientLocationRepository.save(cl);
		            }
		        }

		        // 2 Check if this client already mapped with this addressId
		        Optional<ClientLocation> clientSpecific =
		                addressMatches.stream()
		                        .filter(cl -> cl.getClientId().equals(clientId))
		                        .findFirst();

		        if (clientSpecific.isPresent()) {
		            return clientSpecific.get();
		        }

		        // 3 If not mapped for this client → fall back to old logic
		        // (maybe exact match exists without addressId)

		        Optional<ClientLocation> exact =
		                clientLocationRepository
		                        .findByClientIdAndNLClientLocationAndClientState(
		                                clientId, normalizedLocationLower, normalizedStateLower);

		        if (exact.isPresent()) {
		            ClientLocation cl = exact.get();
		            cl.setClientAddressId(clientAddressId);
		            return clientLocationRepository.save(cl);
		        }

		        Optional<ClientLocation> legacy =
		                clientLocationRepository
		                        .findByClientIdAndNLClientLocationAndClientStateNull(
		                                clientId, normalizedLocationLower);

		        if (legacy.isPresent()) {
		            ClientLocation cl = legacy.get();
		            cl.setClientState(state);
		            cl.setClientAddressId(clientAddressId);
		            return clientLocationRepository.save(cl);
		        }

		        // 4 Finally create new mapping for this client
		        ClientLocation newLoc = new ClientLocation();
		        newLoc.setClientId(clientId);
		        newLoc.setClientLocation(location);
		        newLoc.setClientState(state);
		        newLoc.setClientAddressId(clientAddressId);

		        return clientLocationRepository.save(newLoc);
		    }

		    // =========================
		    // STEP 2: If addressId not found anywhere → full fallback
		    // =========================

		    Optional<ClientLocation> exact =
		            clientLocationRepository
		                    .findByClientIdAndNLClientLocationAndClientState(
		                            clientId, normalizedLocationLower, normalizedStateLower);

		    if (exact.isPresent()) {
		        ClientLocation cl = exact.get();
		        cl.setClientAddressId(clientAddressId);
		        return clientLocationRepository.save(cl);
		    }

		    Optional<ClientLocation> legacy =
		            clientLocationRepository
		                    .findByClientIdAndNLClientLocationAndClientStateNull(
		                            clientId, normalizedLocationLower);

		    if (legacy.isPresent()) {
		        ClientLocation cl = legacy.get();
		        cl.setClientState(state);
		        cl.setClientAddressId(clientAddressId);
		        return clientLocationRepository.save(cl);
		    }

		    ClientLocation cl = new ClientLocation();
		    cl.setClientId(clientId);
		    cl.setClientLocation(location);
		    cl.setClientState(state);
		    cl.setClientAddressId(clientAddressId);

		    return clientLocationRepository.save(cl);
		}

	 @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	 public int[] processSingleClientByName(ClientDetailsSyncDto poDto,Client iShineClient,
	 		       List<ClientLocation> existingLocations) {

	 	int updated = 0;
	 	int inserted = 0;
	 	int updatedLocation = 0;
	 	int insertedLocation = 0;


	 	if (iShineClient != null) {
	 		if (!Objects.equals(iShineClient.getPoClientId(), poDto.getClientid())) {
	 			iShineClient.setPoClientId(poDto.getClientid());
	 			clientRepository.save(iShineClient);
	 			updated++;
	 		}
	 		int[] result = syncClientLocations(iShineClient, poDto, false, existingLocations,true);
	 		updatedLocation += result[0];
	 		insertedLocation += result[1];


	 	} else {
	 		Client newClient = new Client();
	 		newClient.setClientName(poDto.getClientName());
	 		newClient.setPoClientId(poDto.getClientid());

	 		clientRepository.save(newClient);

	 		inserted++;

	 		int[] result = syncClientLocations(newClient, poDto, true, new ArrayList<>(),true);
	 		updatedLocation += result[0];
	 		insertedLocation += result[1];

	 	}

    	return new int[]{updated, inserted, updatedLocation, insertedLocation};
	 }	    
	    
	    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	    public int[] processSingleClientByPoId(ClientDetailsSyncDto poDto,Client iShineClient,
	    		List<ClientLocation> existingLocations) {

	    	int updated = 0;
	    	int inserted = 0;
		 	int updatedLocation = 0;
		 	int insertedLocation = 0;

	    	if (iShineClient != null) {
	    		if (!iShineClient.getClientName().trim().equalsIgnoreCase(poDto.getClientName().trim())) {

	    			iShineClient.setClientName(poDto.getClientName());
	    			clientRepository.save(iShineClient);
	    			updated++;
	    		}

	    		int[] result = syncClientLocations(iShineClient, poDto, false, existingLocations,false);
		 		updatedLocation += result[0];
		 		insertedLocation += result[1];


	    	} else {

	    		Client newClient = new Client();

	    		newClient.setClientName(poDto.getClientName());
	    		newClient.setPoClientId(poDto.getClientid());
	    		clientRepository.save(newClient);
	    		inserted++;

	    		int[] result = syncClientLocations(newClient, poDto, true, new ArrayList<>(),false);
		 		updatedLocation += result[0];
		 		insertedLocation += result[1];
	    	}

	    	return new int[]{updated, inserted, updatedLocation, insertedLocation};
	    }	 
	    
	    private int[] syncClientLocations(Client iShineClient,ClientDetailsSyncDto poDto,
              Boolean isNew,List<ClientLocation> iShineLocations,Boolean isOnce) {

	        int inserted = 0;
	        int updated = 0;

	        if (poDto.getClientAddress() == null || poDto.getClientAddress().isEmpty()) {
		    	return new int[]{updated, inserted};
	        }

	        // Map 1: By Location (for isOnce = true)
	        Map<String, ClientLocation> locationMap = iShineLocations.stream()
	                .filter(loc -> loc.getClientLocation() != null)
	                .collect(Collectors.toMap(
	                        loc -> loc.getClientLocation().trim().toLowerCase(),
	                        loc -> loc,(e1, e2) -> e1 ));

	        // Map 2: By AddressId (for isOnce = false)
	        Map<Long, ClientLocation> addressIdMap = iShineLocations.stream()
	                .filter(loc -> loc.getClientAddressId() != null)
	                .collect(Collectors.toMap(
	                        ClientLocation::getClientAddressId,
	                        loc -> loc,(e1, e2) -> e1 ));

	        List<ClientLocation> locationsToSave = new ArrayList<>();

	        for (ClientAddressSyncDto dto : poDto.getClientAddress()) {

	            if (dto.getClientLocation() == null) continue;

	            ClientLocation existingLocation = null;

	            if (Boolean.TRUE.equals(isOnce)) {
	                String key = dto.getClientLocation().trim().toLowerCase();
	                existingLocation = locationMap.get(key);
	            } else {
	                if (dto.getClientAddressId() != null) {
	                    existingLocation = addressIdMap.get(dto.getClientAddressId());
	                }
	            }

	            if (existingLocation == null) {
	                ClientLocation newLocation = new ClientLocation();
	                newLocation.setClientId(iShineClient.getClientId());
	                newLocation.setClientLocation(dto.getClientLocation());
	                newLocation.setClientState(dto.getClientState());
	                newLocation.setClientAddressId(dto.getClientAddressId());

	                locationsToSave.add(newLocation);
	                inserted++;
	                continue;
	            }

	            // UPDATE (only if changed)
	            boolean isUpdated = false;

	            if (!Objects.equals(existingLocation.getClientState(), dto.getClientState())) {
	                existingLocation.setClientState(dto.getClientState());
	                isUpdated = true;
	            }

	            if (!Objects.equals(existingLocation.getClientAddressId(), dto.getClientAddressId())) {
	                existingLocation.setClientAddressId(dto.getClientAddressId());
	                isUpdated = true;
	            }

	            if (!Objects.equals(existingLocation.getClientLocation(), dto.getClientLocation())) {
	                existingLocation.setClientLocation(dto.getClientLocation());
	                isUpdated = true;
	            }

	            if (isUpdated) {
	                locationsToSave.add(existingLocation);
	                updated++;
	            }
	        }

//	    	if (isNew) {
//    		ClientLocation newLocation = new ClientLocation();
//    		newLocation.setClientId(iShineClient.getClientId());
//    		newLocation.setClientLocation("WFH");
//    		locationsToSave.add(newLocation);
//    	}

	        if (!locationsToSave.isEmpty()) {
	            clientLocationRepository.saveAll(locationsToSave);
	        }

	    	return new int[]{updated, inserted};
	    }
	    	    
	 public void updateProjectAfterSuccessfulSync(
		        Project project,
		        ProjectPoMappingWithResourceDTO projectDto) {

		    if (project == null || projectDto == null) {
		        return;
		    }

		   
		    if (projectDto.getClientId() != null) {
		        project.setPoClientId(projectDto.getClientId());
		    }

		    List<PoDetailsForProjectPoMappingDTO> poList = projectDto.getPoDetailsList();

		    LocalDateTime minPoStart = null;
		    LocalDateTime maxPoEnd = null;

		    
		    if (poList != null && !poList.isEmpty()) {

		    	minPoStart = poList.stream()
		                .map(p -> (LocalDateTime) convert(p.getPoStartDate()))
		                .filter(Objects::nonNull)
		                .min(LocalDateTime::compareTo)
		                .orElse(null);

		        maxPoEnd = poList.stream()
		                .map(p -> (LocalDateTime) convert(p.getPoEndDate()))
		                .filter(Objects::nonNull)
		                .max(LocalDateTime::compareTo)
		                .orElse(null);
		        
		        
		    }

		    

		    List<Long> teamIds =
		            teamRepository.findTeamIdsByProjectId(project.getProjectId());

		    LocalDateTime employeeMinStart = null;

		    if (teamIds != null && !teamIds.isEmpty()) {

		        employeeMinStart =
		                employeeTeamMapRepository
		                        .findMinEmployeeStartDateByTeamIds(teamIds);
		    }

		    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

		    

		    LocalDateTime finalStart = null;

		    if (minPoStart != null && employeeMinStart != null) {

		        finalStart =
		                minPoStart.isBefore(employeeMinStart)
		                        ? minPoStart
		                        : employeeMinStart;

		    } else if (minPoStart != null) {

		        finalStart = minPoStart;

		    } else if (employeeMinStart != null) {

		        finalStart = employeeMinStart;
		    }

		   

		    if (finalStart != null) {

		        project.setStartDate(
		                finalStart.toLocalDate().format(formatter));
		    }

		   

		    if (maxPoEnd != null) {

		        project.setEndDate(
		                maxPoEnd.toLocalDate().format(formatter));
		    }

		   

		    projectRepository.save(project);
		}
		
		
		private LocalDateTime convert(Date date) {
		    if (date == null) return null;

		    if (date instanceof java.sql.Date) {
		        return ((java.sql.Date) date)
		                .toLocalDate()
		                .atStartOfDay();
		    }

		    return date.toInstant()
		            .atZone(ZoneId.systemDefault())
		            .toLocalDateTime();
		}
	 



}
