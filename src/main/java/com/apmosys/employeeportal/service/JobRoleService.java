package com.apmosys.employeeportal.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.hibernate.internal.build.AllowSysOut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apmosys.employeeportal.dto.JobRoleDTO;
import com.apmosys.employeeportal.dto.SubFeatureMasterDTO;
import com.apmosys.employeeportal.model.Department;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.EmployeeRole;
import com.apmosys.employeeportal.model.JobRole;
import com.apmosys.employeeportal.model.RoleFeatureMap;
import com.apmosys.employeeportal.model.SubFeatureMaster;
import com.apmosys.employeeportal.repository.DepartmentRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.EmployeeRoleMasterRepository;
import com.apmosys.employeeportal.repository.JobRoleRepository;
import com.apmosys.employeeportal.repository.RoleFeatureMapRepository;
import com.apmosys.employeeportal.repository.SubFeatureMasterRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;
import com.apmosys.employeeportal.utility.StringToDateTimeParser;

@Service
public class JobRoleService {

	@Autowired
	JobRoleRepository jobRoleRepository;

	@Autowired
	DepartmentRepository departmentRepository;

	@Autowired
	SubFeatureMasterRepository subFeatureMasterRepository;

	@Autowired
	RoleFeatureMapRepository roleFeatureMapRepository;

	@Autowired
	StringToDateTimeParser stringToDateTimeParser;

	@Autowired
	EmployeeRepository employeeRepository;

	@Autowired
	EmployeeRoleMasterRepository employeeRoleMasterRepository;

	public ServiceResponse createJobRole(JobRoleDTO jobRoleDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
			JobRole newJobRole = new JobRole();
			newJobRole.setCreatedBy(jobRoleDTO.getCreatedById());
			newJobRole.setName(jobRoleDTO.getName());
			newJobRole.setEmployeeRole(jobRoleDTO.getEmployeeRole());
			newJobRole.setDeptId(jobRoleDTO.getDepartmentId());

			JobRole dbResponse = jobRoleRepository.save(newJobRole);
			if (dbResponse != null) {

//				List<SubFeatureMaster> defaultSubFeatureMasterList = subFeatureMasterRepository
//						.findBySubFeatureType((short) 1);
				List<EmployeeRole> defaultSubFeatureList = employeeRoleMasterRepository
						.findByEmployeeRoleAndPermission(newJobRole.getEmployeeRole(), "Y");

				if (defaultSubFeatureList.isEmpty()) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("New Job Role Created. But Default SubFeatures List Is Empty.");
					return response;
				} else {
					List<RoleFeatureMap> roleFeatureMapList = new ArrayList<RoleFeatureMap>();
					for (EmployeeRole employeeRole : defaultSubFeatureList) {
						RoleFeatureMap roleFeatureMap = new RoleFeatureMap();
						roleFeatureMap.setSubFeatureMasterId(employeeRole.getSubFeatureMasterId());
						roleFeatureMap.setJobRoleId(dbResponse.getJobRoleId());
						roleFeatureMapList.add(roleFeatureMap);
					}
					List<RoleFeatureMap> savedRoleFeatureMapList = roleFeatureMapRepository.saveAll(roleFeatureMapList);
					if (savedRoleFeatureMapList.isEmpty()) {
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						response.setServiceResponse(
								"New Job Role Created. But Default SubFeatures Was Not Assigned To The Role.");
						return response;
					} else {
						response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
						response.setServiceResponse("New Job Role Created.");
					}
				}
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("New Job Role Creation Failed.");
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public ServiceResponse createJobRoleByList(JobRoleDTO jobRoleDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
			JobRole newJobRole = new JobRole();
			// Department department = new Department();
			newJobRole.setJobRoleId(jobRoleDTO.getJobRoleId());
			newJobRole.setCreatedBy(jobRoleDTO.getCreatedById());
			newJobRole.setName(jobRoleDTO.getName());
			// department.setDept_id(jobRoleDTO.getDepartmentId());
			newJobRole.setDeptId(jobRoleDTO.getDepartmentId());
			newJobRole.setEmployeeRole(jobRoleDTO.getEmployeeRole());

			JobRole dbResponse = jobRoleRepository.save(newJobRole);
			if (dbResponse != null) {

				List<SubFeatureMaster> defaultSubFeatureMasterList = subFeatureMasterRepository
						.findBySubFeatureType((short) 1);

				if (defaultSubFeatureMasterList.isEmpty()) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("New Job Role Created. But Default SubFeatures List Is Empty.");
					return response;
				} else {
					List<RoleFeatureMap> roleFeatureMapList = new ArrayList<RoleFeatureMap>();
					for (SubFeatureMaster subFeatureMaster : defaultSubFeatureMasterList) {
						RoleFeatureMap roleFeatureMap = new RoleFeatureMap();
						roleFeatureMap.setSubFeatureMasterId(subFeatureMaster.getSubFeatureMasterId());
						roleFeatureMap.setJobRoleId(dbResponse.getJobRoleId());
						roleFeatureMapList.add(roleFeatureMap);
					}
					List<RoleFeatureMap> savedRoleFeatureMapList = roleFeatureMapRepository.saveAll(roleFeatureMapList);
					if (savedRoleFeatureMapList.isEmpty()) {
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						response.setServiceResponse(
								"New Job Role Created. But Default SubFeatures Was Not Assigned To The Role.");
						return response;
					} else {
						response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
						response.setServiceResponse("New Job Role Created.");
					}
				}
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("New Job Role Creation Failed.");
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public ServiceResponse getAllJobRole() {
		ServiceResponse response = new ServiceResponse();
		try {
			List<Object[]> allJobRoleList = jobRoleRepository.getAllJobRoles();
			List<JobRoleDTO> dtoList = new ArrayList<>();
			if (!allJobRoleList.isEmpty()) {

				for (Object[] object : allJobRoleList) {
					JobRoleDTO jobRoleDTO = new JobRoleDTO();
					jobRoleDTO.setName(object[0] != null ? object[0].toString() : null);
					jobRoleDTO.setEmployeeRole(object[1] != null ? object[1].toString() : null);
					jobRoleDTO.setCreatedBy(object[2] != null ? object[2].toString() : null);
					jobRoleDTO.setCreatedOn(object[3] != null ? object[3].toString() : null);
					jobRoleDTO.setDepartmentId(object[4] != null ? Long.parseLong(object[4].toString()) : null);
					jobRoleDTO.setDepartmentName(object[5] != null ? object[5].toString() : null);
					jobRoleDTO.setJobRoleId(object[6] != null ? Long.parseLong(object[6].toString()) : null);
					jobRoleDTO.setUpdatedByName(object[7] != null ? object[7].toString() : null);
					jobRoleDTO.setUpdatedOn(object[8] != null ? object[8].toString() : null);
					dtoList.add(jobRoleDTO);
				}
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Job Role List is empty.");
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	@Transactional
	public ServiceResponse updateJobRole(JobRoleDTO jobRoleDTO) {
		ServiceResponse response = new ServiceResponse();

		try {
			Optional<JobRole> jobRoleObject = jobRoleRepository.findById(jobRoleDTO.getJobRoleId());
			if (jobRoleObject.isPresent()) {
				JobRole jobRoleToBeUpdated = jobRoleObject.get();

				if (!jobRoleDTO.getEmployeeRole().equals(jobRoleToBeUpdated.getEmployeeRole())) {

					roleFeatureMapRepository.deleteByJobRoleId(jobRoleDTO.getJobRoleId());

					List<RoleFeatureMap> roleFeatureMapList = new ArrayList<>();

					List<EmployeeRole> defaultSubFeatureList = employeeRoleMasterRepository
							.findByEmployeeRoleAndPermission(jobRoleDTO.getEmployeeRole(), "Y");
					defaultSubFeatureList.forEach(dto -> {

						RoleFeatureMap roleFeatureMap = new RoleFeatureMap();
						roleFeatureMap.setJobRoleId(jobRoleDTO.getJobRoleId());
						roleFeatureMap.setSubFeatureMasterId(dto.getSubFeatureMasterId());
						roleFeatureMapList.add(roleFeatureMap);

					});

					roleFeatureMapRepository.saveAll(roleFeatureMapList);

				}

				jobRoleToBeUpdated.setUpdatedBy(jobRoleDTO.getUpdatedBy());
				jobRoleToBeUpdated.setUpdatedOn(stringToDateTimeParser.getCurrentDateTime());
				// jobRoleToBeUpdated.setName(jobRoleDTO.getName() + "-" +
				// jobRoleDTO.getEmployeeRole());
				jobRoleToBeUpdated.setName(jobRoleDTO.getName());
				jobRoleToBeUpdated.setEmployeeRole(jobRoleDTO.getEmployeeRole());
				jobRoleToBeUpdated.setDeptId(jobRoleDTO.getDepartmentId());
				JobRole dbResponse = jobRoleRepository.save(jobRoleToBeUpdated);

				if (dbResponse != null) {
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Job Role Updated.");
				} else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Job Role Updation Failed.");
				}
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Job Role Not Found");
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public ServiceResponse deleteJobRole(JobRoleDTO jobRoleDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
			Optional<JobRole> jobRoleObject = jobRoleRepository.findById(jobRoleDTO.getJobRoleId());
			if (jobRoleObject.isPresent()) {
				JobRole jobRoleToBeDeleted = jobRoleObject.get();

				Long count = employeeRepository.countByJobRoleId(jobRoleToBeDeleted.getJobRoleId());
				if (count == 0) {

					jobRoleRepository.deleteById(jobRoleToBeDeleted.getJobRoleId());
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Job role deleted.");
				} else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Job role cannot be deleted as it is mapped to employee.");
				}

			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Job Role Not Found.");
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public ServiceResponse changeEmployeeJobRoleMapping(JobRoleDTO jobRoleDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
			List<Employee> employeeJobRole = employeeRepository.findByJobRoleId(jobRoleDTO.getOldJobRoleId());

			if (!employeeJobRole.isEmpty()) {
				for (Employee newJobRole : employeeJobRole) {

					newJobRole.setJobRoleId(jobRoleDTO.getJobRoleId());

					Employee dbResponse = employeeRepository.save(newJobRole);

					if (dbResponse != null) {
						response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
						response.setServiceResponse("Job Role deleted");
					} else {
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						response.setServiceResponse("Employee Job role mapping Failed.");
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public ServiceResponse addNewSubFeatures(SubFeatureMasterDTO subFeatureMasterDTO) {
		ServiceResponse response = new ServiceResponse();

		try {

			List<EmployeeRole> employeeRoles = subFeatureMasterDTO.getEmployeeRoleList();

			SubFeatureMaster subFeatureMaster = subFeatureMasterRepository
					.findBySubFeatureName(subFeatureMasterDTO.getSubFeatureName());

			if (subFeatureMaster != null) {

				List<EmployeeRole> employeeRoleList = new ArrayList<>();

				for (EmployeeRole role : employeeRoles) {
					EmployeeRole employeeRole = new EmployeeRole();

					employeeRole.setSubFeatureMasterId(subFeatureMaster.getSubFeatureMasterId());
					employeeRole.setSubFeatureName(subFeatureMasterDTO.getSubFeatureName());

					employeeRole.setEmployeeRole(role.getEmployeeRole());
					employeeRole.setPermission(role.getPermission());

					employeeRoleList.add(employeeRole);
				}

				List<EmployeeRole> list = employeeRoleMasterRepository.saveAll(employeeRoleList);
				if (list.size() > 0) {
					List<EmployeeRole> defaultSubFeatureList = employeeRoleMasterRepository
							.findBySubFeatureMasterIdAndPermission(subFeatureMaster.getSubFeatureMasterId(), "Y");

					if (defaultSubFeatureList.size() > 0) {

						List<String> employeeRolelist = defaultSubFeatureList.stream().map((employeeRole) -> {
							return employeeRole.getEmployeeRole();
						}).collect(Collectors.toList());

						List<Object[]> objectArrayList = roleFeatureMapRepository
								.getRolesToBeMappedWithNewSubFeature(employeeRolelist);

						Optional.ofNullable(objectArrayList).ifPresentOrElse((objectlist) -> {
							if (objectlist.isEmpty()) {
								response.setServiceStatus(ServiceResponse.STATUS_FAIL);
								response.setServiceResponse("Employee role list is empty.");
							} else {
								List<RoleFeatureMap> roleFeatureMapList = new ArrayList<RoleFeatureMap>();
								for (EmployeeRole role : defaultSubFeatureList) {
									for (Object[] object : objectlist) {
										if (object[1].toString().equals(role.getEmployeeRole())) {
											RoleFeatureMap roleFeatureMap = new RoleFeatureMap();
											roleFeatureMap
													.setSubFeatureMasterId(subFeatureMaster.getSubFeatureMasterId());
											roleFeatureMap.setJobRoleId(
													object[0] != null ? Long.parseLong(object[0].toString()) : null);
											System.out.println("Subfeature mapped to " + role.getEmployeeRole()
													+ " role. With job role id " + object[0].toString());

											roleFeatureMapList.add(roleFeatureMap);
										}
									}
								}

								List<RoleFeatureMap> savedRoleFeatureMapList = roleFeatureMapRepository
										.saveAll(roleFeatureMapList);

								response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
								response.setServiceResponse("Subfeature added to employee_role_master table.Role mappings added to role_subfeature_mapping");

							}
						}, () -> {
							response.setServiceStatus(ServiceResponse.STATUS_FAIL);
							response.setServiceResponse("Employee role list is null.");
						});

					} else {
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						response.setServiceResponse(
								"Subfeature added to employee_role_master table. But no role mapping done as permission was set to"
										+ " 'N'.");
					}

				} else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Failed to add subfeature in subfeature_master_table.");
				}

			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Subfeature not found in subfeature_master_table.");
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public ServiceResponse checkJobRole(JobRoleDTO jobRoleDto) {
	
		ServiceResponse response = new ServiceResponse();
		try {
			JobRole checkExistingRole = jobRoleRepository.findByNameAndDeptId(jobRoleDto.getName(), jobRoleDto.getDepartmentId());
			System.out.println("  jobRoleDto.getDeptId()  : -- " +jobRoleDto.getDepartmentId());
			System.out.println("  jobRoleDto.getName()  : --"+jobRoleDto.getName());
			if(checkExistingRole == null) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("JobRole created !");
			}else if(checkExistingRole !=null) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("JobRole already exist!");
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}
	
}
