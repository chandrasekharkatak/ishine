package com.apmosys.employeeportal.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.apmosys.employeeportal.dto.ClientAddressSyncDto;
import com.apmosys.employeeportal.dto.ClientDetailsSyncDto;
import com.apmosys.employeeportal.model.Client;
import com.apmosys.employeeportal.model.ClientLocation;
import com.apmosys.employeeportal.repository.ClientLocationRepository;

/**
 * Per-client PO → iShine sync in an isolated transaction so one bad row does not roll back the whole cron run.
 */
@Service
public class ClientPoPortalSyncTransactionalService {

	@Autowired
	private ClientService clientService;

	@Autowired
	private ClientLocationRepository clientLocationRepository;

	public static class OneClientSyncOutcome {
		public int syncedAddresses;
		public int skippedAddresses;
		public int deactivatedLocations;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	public OneClientSyncOutcome syncOneClientFromPo(ClientDetailsSyncDto poDto) {
		OneClientSyncOutcome out = new OneClientSyncOutcome();
		Long poClientId = poDto.getClientid();
		String rawName = poDto.getClientName();
		if (poClientId == null || rawName == null || rawName.trim().isEmpty()) {
			throw new IllegalArgumentException("PO client requires clientid and non-blank clientName");
		}

		Client client = clientService.resolveClient(rawName.trim(), poClientId);

		List<ClientAddressSyncDto> addresses = poDto.getClientAddress() != null ? poDto.getClientAddress()
				: Collections.emptyList();

		Map<Long, ClientAddressSyncDto> uniqueAddresses = new LinkedHashMap<>();
		for (ClientAddressSyncDto addr : addresses) {
			if (addr == null || addr.getClientAddressId() == null || addr.getClientLocation() == null
					|| addr.getClientState() == null) {
				out.skippedAddresses++;
				continue;
			}
			String loc = addr.getClientLocation().trim();
			String st = addr.getClientState().trim();
			if (loc.isEmpty() || st.isEmpty()) {
				out.skippedAddresses++;
				continue;
			}
			Long aid = addr.getClientAddressId();
			ClientAddressSyncDto existing = uniqueAddresses.get(aid);
			if (existing != null) {
				if (!sameAddressPayload(existing, addr)) {
					throw new IllegalStateException(
							"Duplicate clientAddressId=" + aid + " with conflicting location/state in PO payload for poClientId="
									+ poClientId);
				}
				out.skippedAddresses++;
				continue;
			}
			uniqueAddresses.put(aid, addr);
		}

		for (ClientAddressSyncDto addr : uniqueAddresses.values()) {
			String loc = addr.getClientLocation().trim();
			String st = addr.getClientState().trim();
			clientService.resolveClientLocation(client.getClientId(), loc, st, addr.getClientAddressId());
			out.syncedAddresses++;
		}

		List<ClientLocation> existing = clientLocationRepository.findByClientId(client.getClientId());
		List<ClientLocation> toDeactivate = new ArrayList<>();
		for (ClientLocation cl : existing) {
			if (cl.getClientAddressId() == null) {
				continue;
			}
			if (!uniqueAddresses.containsKey(cl.getClientAddressId()) && cl.isActiveInPo()) {
				cl.setActiveInPo(false);
				toDeactivate.add(cl);
				out.deactivatedLocations++;
			}
		}
		if (!toDeactivate.isEmpty()) {
			clientLocationRepository.saveAll(toDeactivate);
		}

		return out;
	}

	private static boolean sameAddressPayload(ClientAddressSyncDto a, ClientAddressSyncDto b) {
		return Objects.equals(a.getClientAddressId(), b.getClientAddressId())
				&& Objects.equals(normalizeLoc(a.getClientLocation()), normalizeLoc(b.getClientLocation()))
				&& Objects.equals(normalizeSt(a.getClientState()), normalizeSt(b.getClientState()));
	}

	private static String normalizeLoc(String s) {
		return s == null ? "" : s.trim().toLowerCase();
	}

	private static String normalizeSt(String s) {
		return s == null ? "" : s.trim().toLowerCase();
	}
}