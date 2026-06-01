package com.apmosys.employeeportal.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReimbursementRolePolicyResolveDTO {
	private Long jobRoleId;
	private String jobRoleName;
	private Map<String, BigDecimal> amountLimits = new LinkedHashMap<>();
	/** Per-day cap keyed by travel mode name (e.g. Cab / Taxi, Own Vehicle). */
	private Map<String, BigDecimal> travelModeDailyLimits = new LinkedHashMap<>();
	private Map<String, BigDecimal> vehicleRatesPerKm = new LinkedHashMap<>();
	private List<String> allowedTravelModes = new ArrayList<>();
	private List<String> allowedVehicleTypes = new ArrayList<>();
	private List<String> allowedFoodAllowanceTypes = new ArrayList<>();
	private boolean travelModeRestricted;
	private boolean vehicleTypeRestricted;
	private boolean foodAllowanceRestricted;
}
