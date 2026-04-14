package com.apmosys.employeeportal.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.CustomFilterDTO;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.dto.QueryTableDTO;
import com.apmosys.employeeportal.model.QueryTable;
import com.apmosys.employeeportal.repository.QueryTableRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;

@Service
public class QueryTableService {

	@Autowired
	private QueryTableRepository queryTableRepository; 
	

	@Value("${spring.datasource.url}")
	private String dbURL;

	@Value("${spring.datasource.username}")
	private String dbUsername;

	@Value("${spring.datasource.password}")
	private String dbPassword;

	public ServiceResponse createQuery(QueryTableDTO queryTableDto) {
		ServiceResponse response = new ServiceResponse();
		
		try {
			QueryTable createQuery = new QueryTable();
			
			createQuery.setQueryName(queryTableDto.getQueryName());
			createQuery.setQuery(queryTableDto.getQuery());
			createQuery.setCreatedBy(queryTableDto.getCreatedBy());
			createQuery.setCreatedOn(LocalDate.now());
			createQuery.setPublish(false);
			
			QueryTable saveQuery = queryTableRepository.save(createQuery);
			if(saveQuery != null) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("Query saved successfully !!");
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse(" Query not saved !!");
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something went wrong !!");
		}
		
		return response;
	}
	
	
	public ServiceResponse getNonPublishedQuery() {
		ServiceResponse response = new ServiceResponse();
	try {
		List<QueryTableDTO> listOfQuery = new ArrayList<QueryTableDTO>();
		List<Object[]> listOfNonPublishedQuery = queryTableRepository.findAllNonPublishedQuery();
		listOfNonPublishedQuery.forEach(object ->{
			QueryTableDTO dto = new QueryTableDTO();
			
			dto.setQueryId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
			dto.setQueryName(object[1] != null ? object[1].toString() : null);
			dto.setQuery(object[2] != null ? object[2].toString() : null);
			dto.setCreatedBy(object[3] != null ? Long.parseLong(object[3].toString()) : null);
			dto.setCreatedOn(object[4] != null ? LocalDate.parse(object[4].toString()) : null);
			dto.setCreatedByName(object[5] != null ? object[5].toString() : null);
			
			listOfQuery.add(dto);
		});
		
		if(listOfQuery != null) {
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(listOfQuery);
		}else {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Query list not fetched !!");
		}
	} catch (Exception e) {
		e.printStackTrace();
		response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
		response.setServiceResponse(" Something went wrong !! ");
	}
		return response;
	}
	
	public ServiceResponse getPublishedQuery() {
		ServiceResponse response = new ServiceResponse();
	try {
		List<QueryTableDTO> listOfQuery = new ArrayList<QueryTableDTO>();
		List<Object[]> listOfPublishedQuery = queryTableRepository.findAllPublishedQuery();
		listOfPublishedQuery.forEach(object ->{
			QueryTableDTO dto = new QueryTableDTO();
			
			dto.setQueryId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
			dto.setQueryName(object[1] != null ? object[1].toString() : null);
			dto.setQuery(object[2] != null ? object[2].toString() : null);
			dto.setCreatedBy(object[3] != null ? Long.parseLong(object[3].toString()) : null);
			dto.setCreatedOn(object[4] != null ? LocalDate.parse(object[4].toString()) : null);
			dto.setCreatedByName(object[5] != null ? object[5].toString() : null);
			dto.setUpdatedByName(object[6] != null ? object[6].toString() : null);
			dto.setUpdatedOn(object[7] != null ? LocalDate.parse(object[7].toString()) : null);
			listOfQuery.add(dto);
		});
		
		if(listOfQuery != null) {
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(listOfQuery);
		}else {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Query list not fetched !!");
		}
	} catch (Exception e) {
		e.printStackTrace();
		response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
		response.setServiceResponse(" Something went wrong !! ");
	}
		return response;
	}
	
	
	public ServiceResponse getQueryDataForPreview(QueryTableDTO queryTableDto) throws SQLException {
		ServiceResponse response = new ServiceResponse();

		List<Object[]> list = new ArrayList<Object[]>();
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		System.out.println("Query  "+queryTableDto.getQuery());
		try {
			if (queryTableDto.getQuery() != null) {
				try {
					String q = queryTableDto.getQuery();


					Class.forName("com.mysql.cj.jdbc.Driver");
					con = DriverManager.getConnection(dbURL, dbUsername, dbPassword);

					stmt = con.prepareStatement(q);

					if (stmt != null) {
						rs = stmt.executeQuery();
						ResultSetMetaData rsmd = (ResultSetMetaData) rs.getMetaData();

						int columnsNumber = rsmd.getColumnCount();
						System.out.println("column size  : " + columnsNumber);

						Object[] headers = new Object[columnsNumber];

						for (int i = 1; i <= columnsNumber; i++) {
							headers[i - 1] = rsmd.getColumnLabel(i);
						}

						list.add(headers);

						while (rs.next()) {
							Object[] dataArr = new Object[columnsNumber];

							for (int i = 1; i <= columnsNumber; i++) {
								String data = rs.getString(i);
								dataArr[i - 1] = data;

							}

							list.add(dataArr);
						}

						if (!list.isEmpty()) {
							response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
							response.setServiceResponse(list);
							
						} else {
							response.setServiceStatus(ServiceResponse.STATUS_FAIL);
							response.setServiceResponse("Result set is empty.");
							
						}
					} else {
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						response.setServiceResponse("Invalid Query.");
						
					}

				} catch (SQLException e) {
					e.printStackTrace();
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Invalid Query, Please Check entered query.");
				
					response.setServiceError(e.getMessage());
				} catch (Exception e) {
					e.printStackTrace();
					response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
					response.setServiceResponse("Something Went Wrong.");
					
					response.setServiceError(e.getMessage());
				} finally {
					try {
						if (stmt != null)
							stmt.close();
						if (rs != null)
							rs.close();
						if (con != null)
							con.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}

			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Custom Query Not Found.");
				
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			
			response.setServiceError(e.getMessage());
		}
		
		return response;
	}


	public ServiceResponse getQueryDetailsByQueryId(QueryTableDTO queryTableDto) {
		ServiceResponse response = new ServiceResponse ();
		try {
			QueryTable findQueryDetails = queryTableRepository.findByQueryId(queryTableDto.getQueryId());
			if(findQueryDetails != null) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(findQueryDetails);
				
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Unable to fetch query details !!");
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something went wrong !!");
		}
		
		return response;
	}


	public ServiceResponse updateQuery(QueryTableDTO queryTableDto) {
		ServiceResponse response = new ServiceResponse();
		try {
			QueryTable query = queryTableRepository.findByQueryId(queryTableDto.getQueryId());
			query.setQuery(queryTableDto.getQuery());
			query.setQueryName(queryTableDto.getQueryName());
			query.setUpdatedBy(queryTableDto.getUpdatedBy());
			query.setUpdatedOn(LocalDate.now());
			
			QueryTable dbQuery = queryTableRepository.save(query);
			
			if(dbQuery != null) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("Query updated successfully !!");
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Query updation failed !!");
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something went wrong !!");
		}
		
		return response;
	}


	public ServiceResponse deleteQuery(QueryTableDTO queryTableDto) {
		ServiceResponse response = new ServiceResponse();
		try {
			QueryTable queryToBeDeleted = queryTableRepository.findByQueryId(queryTableDto.getQueryId());
			if(queryToBeDeleted != null) {
				queryTableRepository.delete(queryToBeDeleted);
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(queryTableDto.getQueryName()+" deleted successfully !! ");
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse(" Query not found!! ");
			}
//			queryTableRepository.deleteById(queryTableDto.getQueryId());
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something went wrong !!");
		}
		return response;
	}


	public ServiceResponse publishQuery(QueryTableDTO queryTableDto) {
		ServiceResponse response = new ServiceResponse();
		
		try {
			
			QueryTable findQuery = queryTableRepository.findByQueryId(queryTableDto.getQueryId());
			if(findQuery != null) {
				findQuery.setPublish(true);
				findQuery.setUpdatedOn(LocalDate.now());
				findQuery.setUpdatedBy(queryTableDto.getUpdatedBy());
				QueryTable dbQuery = queryTableRepository.save(findQuery);
				if(dbQuery != null) {
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Query Globally Published !!");
				}else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Query not updated !! ");
				}
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Query not present !!");
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something went wrong !!");
		}
		
		return response;
	}

}
