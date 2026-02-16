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

		    if (!hasAnyLocation) {
		        ClientLocation wfh = new ClientLocation();
		        wfh.setClientId(clientId);
		        wfh.setClientLocation("WFH");
		        wfh.setClientState(null);
		        clientLocationRepository.save(wfh);
		    }

		   
		    List<ClientLocation> addressMatches =
		            clientLocationRepository.findByClientAddressId(clientAddressId);

		    if (!addressMatches.isEmpty()) {

		        // 1️⃣ Update location/state if changed (sync from PO)
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

		        // 2️⃣ Check if this client already mapped with this addressId
		        Optional<ClientLocation> clientSpecific =
		                addressMatches.stream()
		                        .filter(cl -> cl.getClientId().equals(clientId))
		                        .findFirst();

		        if (clientSpecific.isPresent()) {
		            return clientSpecific.get();
		        }

		        // 3️⃣ If not mapped for this client → fall back to old logic
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

		        // 4️⃣ Finally create new mapping for this client
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
	    public int[] processSingleClientByName(ClientDetailsSyncDto poDto,Client iShineClient) {
		 
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
					Collectors.toMap(loc -> loc.getClientLocation().trim().toLowerCase(), 
							loc -> loc
							, (e1, e2) -> e1));

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
