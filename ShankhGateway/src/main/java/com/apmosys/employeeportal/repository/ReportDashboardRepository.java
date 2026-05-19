package com.apmosys.employeeportal.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.apmosys.employeeportal.dto.DepartmentBillableDTO;
import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.dto.ReportCountDTO;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.utility.ServiceResponse;

public interface ReportDashboardRepository extends JpaRepository<Employee, Long> {

	//Grouped Query for employee status summary , gender summary and fresher-lateral summary
	@Query(nativeQuery = true , value = "WITH employee_data AS (\n"
			+ "    SELECT distinct \n"
			+ "        e.gender,\n"
			+ "        e.employmentstatus,\n"
			+ "        e.experience,\n"
			+ "        e.billable,\n"
			+ "        e.date_of_birth,\n"
			+ "        e.date_of_joining,\n"
			+ "        e.is_apprenticeship,\n"
			+ "        e.is_consultant,\n"
			+ "        e.billable_type,\n"
			+ "        e.is_apmosys_product,\n"
			+ "        e.emp_id\n"
			+ "    FROM employee e \n"
			+ "    left join job_role jr on e.job_role_id = jr.job_role_id\n"
			+ "	left join department d on jr.dept_id = d.dept_id\n"
			+ "	left join employee mg on e.manager_id = mg.emp_id \n"
			+ "	left join employee_team_mapping etm on e.emp_id = etm.emp_id and etm.active != '0'\n"
			+ "	left join teams t on etm.team_id = t.team_id and t.is_active = 'Y'\n"
			+ "	left join projects p on t.project_id = p.project_id and p.active = 'true'\n"
			+ "    WHERE 1=1\n"
			+ "    and e.emp_id NOT BETWEEN 1 AND 6\n"
//			+ "    and (:employeement_id is null or employeement_id in (:employeement_id))\n"
//			+ "	and (:name IS NULL OR UPPER(name) LIKE CONCAT('%', UPPER(:name), '%'))\n"
//			+ "	and (:dept_id is null or dept_id in (:dept_id))\n"
//			+ "	and (:job_role_id is null or job_role_id in (:job_role_id))\n"
//			+ "	and (:manager_id is null or mg.emp_id in (:manager_id))\n"
//			+ "	and (:team_id is null or t.team_id in (:team_id))\n"
//			+ "	and (:project_id is null or p.project_id in (:project_id))\n"
//			+ "	and (:client_id is null or p.client_id in (:client_id))\n"
//			+ "    and (:employmentstatus is null or UPPER(e.employmentstatus) like CONCAT('%', UPPER(:employmentstatus), '%'))\n"
//			+ "    and (:date_of_joining is null or e.date_of_joining like CONCAT('%', :date_of_joining, '%'))\n"
//			+ "    and (:city IS NULL OR UPPER(e.city) LIKE CONCAT('%', UPPER(:city), '%'))\n"
//			+ "    and (:blood_group IS NULL OR UPPER(e.blood_group) LIKE CONCAT('%', UPPER(:blood_group), '%'))\n"
//			+ "    and (:gender IS NULL OR UPPER(e.gender) LIKE CONCAT('%', UPPER(:gender), '%'))\n"
//			+ "    and (:probation_period is null or e.probation_period in (:probation_period))\n"
//			+ "    and (:notice_id is null or e.notice_id in (:notice_id))\n"
//			+ "    and (:marital_status IS NULL OR UPPER(e.marital_status) LIKE CONCAT('%', UPPER(:marital_status), '%'))\n"
//			+ "    and (:bank_name IS NULL OR UPPER(e.bank_name) LIKE CONCAT('%', UPPER(:bank_name), '%'))\n"
//			+ "    and (:state IS NULL OR UPPER(e.state) LIKE CONCAT('%', UPPER(:state), '%'))\n"
//			+ "    and (:created_on is null or e.created_on like CONCAT('%', :created_on, '%'))\n"
//			+ "    and (:created_by is null or e.created_by in (:created_by))\n"
//			+ "    and (:experience IS NULL OR UPPER(e.experience) LIKE CONCAT('%', UPPER(:experience), '%'))\n"
//			+ "    and (:work_location IS NULL OR UPPER(e.work_location) LIKE CONCAT('%', UPPER(:work_location), '%'))\n"
			+ ")\n"
			+ "SELECT\n"
			+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND billable = 'Yes' THEN 1 ELSE 0 END) AS Yes,\n"
			+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND billable = 'No' THEN 1 ELSE 0 END) AS No,\n"
			+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND billable = 'Other' THEN 1 ELSE 0 END) AS Billable_Other,\n"
			+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND TIMESTAMPDIFF(YEAR, date_of_birth, CURRENT_DATE()) BETWEEN 18 AND 25 THEN 1 ELSE 0 END) AS 'Years_18_25',\n"
			+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND TIMESTAMPDIFF(YEAR, date_of_birth, CURRENT_DATE()) BETWEEN 26 AND 35 THEN 1 ELSE 0 END) AS 'Years_26_35',\n"
			+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND TIMESTAMPDIFF(YEAR, date_of_birth, CURRENT_DATE()) BETWEEN 36 AND 45 THEN 1 ELSE 0 END) AS 'Years_36_45',\n"
			+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND TIMESTAMPDIFF(YEAR, date_of_birth, CURRENT_DATE()) >= 46 THEN 1 ELSE 0 END) AS 'Years_above_45',\n"
			+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND gender = 'Male' THEN 1 ELSE 0 END) AS Male,\n"
			+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND gender = 'Female' THEN 1 ELSE 0 END) AS Female,\n"
			+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND gender = 'Other' THEN 1 ELSE 0 END) AS Gender_Other,\n"
			+ "    SUM(CASE WHEN employmentstatus = 'Confirmed' THEN 1 ELSE 0 END) AS Confirmed,\n"
			+ "    SUM(CASE WHEN employmentstatus = 'Resigned' THEN 1 ELSE 0 END) AS Resigned,\n"
			+ "    SUM(CASE WHEN employmentstatus = 'Probation' THEN 1 ELSE 0 END) AS Probation,\n"
			+ "    SUM(CASE WHEN employmentstatus = 'Retain' THEN 1 ELSE 0 END) AS Retain,\n"
			+ "    SUM(CASE WHEN employmentstatus = 'InActive' THEN 1 ELSE 0 END) AS InActive,\n"
			+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND experience = 'Experienced' THEN 1 ELSE 0 END) AS Experienced,\n"
			+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND experience = 'Fresher' THEN 1 ELSE 0 END) AS Fresher,\n"
			+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND is_apprenticeship = 'true' AND TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) / 12 >= 0 AND TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) / 12 <= 1 THEN 1 ELSE 0 END) AS Apprentice_Years_0_1,\n"
			+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND is_apprenticeship = 'true' AND TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) / 12 > 1 AND TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) / 12 <= 2 THEN 1 ELSE 0 END) AS Apprentice_Years_1_2,\n"
			+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND is_apprenticeship = 'true' AND TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) / 12 > 2 AND TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) / 12 <= 5 THEN 1 ELSE 0 END) AS Apprentice_Years_2_5,\n"
			+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND is_apprenticeship = 'true' AND TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) / 12 > 5 AND TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) / 12 <= 10 THEN 1 ELSE 0 END) AS Apprentice_Years_5_10,\n"
			+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND is_apprenticeship = 'true' AND TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) / 12 > 10 THEN 1 ELSE 0 END) AS Apprentice_Years_Above_10,\n"
			+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND ((is_consultant = 'false' AND is_apprenticeship = 'false' AND is_apmosys_product = 'false') OR (COALESCE(is_consultant, '') = '' AND COALESCE(is_apprenticeship, '') = '')) AND TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) / 12 >= 0 AND TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) / 12 <= 1 THEN 1 ELSE 0 END) AS Employees_Years_0_1,\n"
			+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND ((is_consultant = 'false' AND is_apprenticeship = 'false' AND is_apmosys_product = 'false') OR (COALESCE(is_consultant, '') = '' AND COALESCE(is_apprenticeship, '') = '')) AND TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) / 12 > 1 AND TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) / 12 <= 2 THEN 1 ELSE 0 END) AS Employees_Years_1_2,\n"
			+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND ((is_consultant = 'false' AND is_apprenticeship = 'false' AND is_apmosys_product = 'false') OR (COALESCE(is_consultant, '') = '' AND COALESCE(is_apprenticeship, '') = '')) AND TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) / 12 > 2 AND TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) / 12 <= 5 THEN 1 ELSE 0 END) AS Employees_Years_2_5,\n"
			+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND ((is_consultant = 'false' AND is_apprenticeship = 'false' AND is_apmosys_product = 'false') OR (COALESCE(is_consultant, '') = '' AND COALESCE(is_apprenticeship, '') = '')) AND TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) / 12 > 5 AND TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) / 12 <= 10 THEN 1 ELSE 0 END) AS Employees_Years_5_10,\n"
			+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND ((is_consultant = 'false' AND is_apprenticeship = 'false' AND is_apmosys_product = 'false') OR (COALESCE(is_consultant, '') = '' AND COALESCE(is_apprenticeship, '') = '')) AND TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) / 12 > 10 THEN 1 ELSE 0 END) AS Employees_Years_Above_10,\n"
			+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND is_consultant = 'true' AND TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) / 12 >= 0 AND TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) / 12 <= 1 THEN 1 ELSE 0 END) AS Consultant_Years_0_1,\n"
			+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND is_consultant = 'true' AND TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) / 12 > 1 AND TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) / 12 <= 2 THEN 1 ELSE 0 END) AS Consultant_Years_1_2,\n"
			+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND is_consultant = 'true' AND TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) / 12 > 2 AND TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) / 12 <= 5 THEN 1 ELSE 0 END) AS Consultant_Years_2_5,\n"
			+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND is_consultant = 'true' AND TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) / 12 > 5 AND TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) / 12 <= 10 THEN 1 ELSE 0 END) AS Consultant_Years_5_10,\n"
			+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND is_consultant = 'true' AND TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) / 12 > 10 THEN 1 ELSE 0 END) AS Consultant_Years_Above_10,\n"
			+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND billable_type = 'Fixed Cost' THEN 1 ELSE 0 END) AS Fixed_Cost,\n"
			+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND billable_type = 'TNM' THEN 1 ELSE 0 END) AS TNM,\n"
			+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND billable_type = 'Bench' THEN 1 ELSE 0 END) AS Bench,\n"
			+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND billable_type = 'Shadow' THEN 1 ELSE 0 END) AS Shadow,\n"
			+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND billable_type = 'InternalRNDProducts' THEN 1 ELSE 0 END) AS InternalRNDProducts,\n"
			+ "    SUM(CASE WHEN employmentstatus != 'InActive' THEN 1 ELSE 0 END) AS Total_Employee,\n"
			+ "    SUM(CASE WHEN employmentstatus = 'Probation' and TIMESTAMPDIFF(DAY, date_of_joining, CURRENT_DATE()) > 180 then 1 ELSE 0 end) as probation_count,\n"
			+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND is_apprenticeship = 'true' THEN 1 ELSE 0 END) as apprentice_count,\n"
			+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND is_consultant = 'true' THEN 1 ELSE 0 END) as consultant_count,\n"
			+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND ((is_consultant = 'false' AND is_apprenticeship = 'false' AND is_apmosys_product = 'false') OR (COALESCE(is_consultant, '') = '' AND COALESCE(is_apprenticeship, '') = '')) THEN 1 ELSE 0 END) as regular_count,\n"
			+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND is_apmosys_product = 'true' THEN 1 ELSE 0 END) as apmosys_product_count,\n"
			+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND is_apmosys_product = 'true' AND TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) / 12 >= 0 AND TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) / 12 <= 1 THEN 1 ELSE 0 END) AS Apmosys_Product_Years_0_1,\n"
			+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND is_apmosys_product = 'true' AND TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) / 12 > 1 AND TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) / 12 <= 2 THEN 1 ELSE 0 END) AS Apmosys_Product_Years_1_2,\n"
			+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND is_apmosys_product = 'true' AND TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) / 12 > 2 AND TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) / 12 <= 5 THEN 1 ELSE 0 END) AS Apmosys_Product_Years_2_5,\n"
			+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND is_apmosys_product = 'true' AND TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) / 12 > 5 AND TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) / 12 <= 10 THEN 1 ELSE 0 END) AS Apmosys_Product_Years_5_10,\n"
			+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND is_apmosys_product = 'true' AND TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) / 12 > 10 THEN 1 ELSE 0 END) AS Apmosys_Product_Years_Above_10\n"
			+ "FROM employee_data")
         public List<Object[]> getAllGraphEmployeeSummary();
	
         //List query for all the pie charts 
//         @Query(nativeQuery = true, value = "\n"
//        		 + "SELECT distinct \n"
//        		 + " CONCAT('A-', e.employeement_id) as EMP_ID,\n"
//        		 + " CASE WHEN e.is_apprenticeship = 'true' THEN 'Apprentice'\n"
//        		 + " WHEN ((e.is_consultant = 'false' AND e.is_apprenticeship = 'false') OR (COALESCE(e.is_consultant, '') = '' AND COALESCE(e.is_apprenticeship, '') = '')) THEN 'Regular'\n"
//        		 + " WHEN e.is_consultant = 'true' THEN 'Consultant' \n"
//        		 + " END AS EMPLOYMENT_TYPE,\n"
//        		 + " e.name NAME,\n"
//        		 + " e.experience EXPERIENCE,\n"
//        		 + " d.name DEPARTMENT_NAME,\n"
//        		 + " e.email EMAIL_ID,\n"
//        		 + " m.name MANAGER_NAME,\n"
//        		 + " e.billable BILLABLE,\n"
//        		 + " e.billable_type BILLABLE_TYPE,\n"
//        		 + " GROUP_CONCAT(DISTINCT p.project_name SEPARATOR ', ') AS PROJECT_NAMES,\n"
//        		 + " GROUP_CONCAT(DISTINCT c.client_name SEPARATOR ', ') AS CLIENT_NAMES,\n"
//        		 + " e.date_of_joining DATE_OF_JOINING,\n"
//        		 + " e.mobile_no MOBILE_NO,\n"
//        		 + " e.employmentstatus STATUS,\n"
//        		 + " e.total_experience TOTAL_EXPERIENCE,\n"
//        		 + " e.gender GENDER,\n"
//        		 + " e.work_location WORK_LOCATION,\n"
//        		 + " TIMESTAMPDIFF(YEAR, e.date_of_birth, CURRENT_DATE()) age,\n"
//        		 + " e.is_user_info_updated KYC\n"
//        		 + "FROM employee e \n"
//        		 + "left join job_role jr on e.job_role_id = jr.job_role_id\n"
//        		 + "left join department d on jr.dept_id = d.dept_id\n"
//        		 + "left join employee mg on e.manager_id = mg.emp_id \n"
//        		 + "left join employee_team_mapping etm on e.emp_id = etm.emp_id and etm.active != '0'\n"
//        		 + "left join employee m on m.emp_id = e.manager_id\n"
//        		 + "left join teams t on etm.team_id = t.team_id and t.is_active = 'Y'\n"
//        		 + "left join projects p on t.project_id = p.project_id and p.active = 'true'\n"
//        		 + "left join clients c on p.client_id = c.client_id\n"
//        		 + "WHERE 1=1\n"
//        		 + " and (\n"
//        		 + " (:in_active_flag= 1 and e.employmentstatus = 'InActive')\n"
//        		 + " or\n"
//        		 + " (:in_active_flag = 0 and e.employmentstatus != 'InActive')\n"
//        		 + " ) \n"
//        		 + " and e.emp_id NOT BETWEEN 1 AND 6\n"
//        		 + "and  (:billable is null or e.billable = :billable)\n"
//        		 + "and  (:billable_type is null or e.billable_type = :billable_type)\n"
//        		 + " and (:employmentstatus is null or e.employmentstatus = :employmentstatus)\n"
//        		 + " and (:gender IS NULL OR e.gender = :gender)\n"
//        		 + " and (:experience IS NULL OR e.experience = :experience)\n"
//        		 + "and ((:lower_age IS NULL AND :upper_age IS NULL)\n "
//        		 + "OR TIMESTAMPDIFF(YEAR, e.date_of_birth, CURRENT_DATE())\n "
//        		 + "BETWEEN :lower_age AND :upper_age)\n"
//        		 + "GROUP BY e.emp_id;")
//        		 public List<Object[]> getAllPieGraphListSummary(
//        		     @Param("in_active_flag") Integer inActiveFlag,
//        		     @Param("billable") String billable,
//        		     @Param("billable_type") String billableType,
//        		     @Param("employmentstatus") String employmentStatus,
//        		     @Param("gender") String gender,
//        		     @Param("experience") String experience ,
//        		     @Param("lower_age") Integer lowerAge,
//        		     @Param("upper_age") Integer upperAge
//        		 );
         
         @Query(nativeQuery = true, value = "SELECT DISTINCT\n"
         		+ "    CASE \n"
         		+ "    WHEN e.is_apmosys_product = 'true' OR e.email LIKE '%ap2l.ai%' THEN CONCAT('AP-', e.employeement_id)\n"
         		+ "    ELSE CONCAT('A-', e.employeement_id)\n"
         		+ "END AS EMP_ID,\n"
         		+ "\n"
         		+ "    CASE \n"
         		+ "        WHEN e.is_apmosys_product = 'true' THEN 'Apmosys Product'\n"
         		+ "        WHEN e.is_apprenticeship = 'true' THEN 'Apprentice'\n"
         		+ "        WHEN (\n"
         		+ "            (e.is_consultant = 'false' AND e.is_apprenticeship = 'false') OR \n"
         		+ "            (COALESCE(e.is_consultant, '') = '' AND COALESCE(e.is_apprenticeship, '') = '')\n"
         		+ "        ) THEN 'Regular'\n"
         		+ "        WHEN e.is_consultant = 'true' THEN 'Consultant'\n"
         		+ "    END AS EMPLOYMENT_TYPE,\n"
         		+ "\n"
         		+ "    e.name AS NAME,\n"
         		+ "    e.experience AS EXPERIENCE,\n"
         		+ "    d.name AS DEPARTMENT_NAME,\n"
         		+ "    e.email AS EMAIL_ID,\n"
         		+ "    m.name AS MANAGER_NAME,\n"
         		+ "    e.billable AS BILLABLE,\n"
         		+ "    e.billable_type AS BILLABLE_TYPE,\n"
         		+ "    GROUP_CONCAT(DISTINCT p.project_name SEPARATOR ', ') AS PROJECT_NAMES,\n"
         		+ "    GROUP_CONCAT(DISTINCT c.client_name SEPARATOR ', ') AS CLIENT_NAMES,\n"
         		+ "    e.date_of_joining AS DATE_OF_JOINING,\n"
         		+ "    e.mobile_no AS MOBILE_NO,\n"
         		+ "    e.employmentstatus AS STATUS,\n"
         		+ "    e.total_experience AS TOTAL_EXPERIENCE,\n"
         		+ "    e.gender AS GENDER,\n"
         		+ "    e.work_location AS WORK_LOCATION,\n"
         		+ "    TIMESTAMPDIFF(YEAR, e.date_of_birth, CURRENT_DATE()) AS age,\n"
         		+ "    e.is_user_info_updated AS KYC\n"
         		+ "\n"
         		+ "FROM employee e\n"
         		+ "LEFT JOIN job_role jr ON e.job_role_id = jr.job_role_id\n"
         		+ "LEFT JOIN department d ON jr.dept_id = d.dept_id\n"
         		+ "LEFT JOIN employee mg ON e.manager_id = mg.emp_id\n"
         		+ "LEFT JOIN employee_team_mapping etm ON e.emp_id = etm.emp_id AND etm.active != '0'\n"
         		+ "LEFT JOIN employee m ON m.emp_id = e.manager_id\n"
         		+ "LEFT JOIN teams t ON etm.team_id = t.team_id AND t.is_active = 'Y'\n"
         		+ "LEFT JOIN projects p ON t.project_id = p.project_id AND p.active = 'true'\n"
         		+ "LEFT JOIN clients c ON p.client_id = c.client_id\n"
         		+ "\n"
         		+ "WHERE 1 = 1\n"
         		+ "AND (\n"
         		+ "     (:in_active_flag = 1 AND e.employmentstatus = 'InActive') OR\n"
         		+ "     (:in_active_flag = 0 AND e.employmentstatus != 'InActive')\n"
         		+ "   )\n"
         		+ "  AND e.emp_id NOT BETWEEN 1 AND 6\n"
         		+ "  AND (:billable IS NULL OR e.billable = :billable)\n"
         		+ "   AND (:billable_type IS NULL OR e.billable_type = :billable_type)\n"
         		+ "  AND (:employmentstatus IS NULL OR e.employmentstatus = :employmentstatus) \n"
         		+ "AND (:gender IS NULL OR e.gender = :gender)\n"
         		+ "  AND (:experience IS NULL OR e.experience = :experience)\n"
         		+ "  AND (\n"
         		+ "        (:lower_age IS NULL AND :upper_age IS NULL) OR\n"
         		+ "        TIMESTAMPDIFF(YEAR, e.date_of_birth, CURRENT_DATE()) BETWEEN :lower_age AND :upper_age\n"
         		+ "  )\n"
         		+ "\n"
         		+ "GROUP BY e.emp_id\n"
         		+ "")
        		 public List<Object[]> getAllPieGraphListSummary(
        		     @Param("in_active_flag") Integer inActiveFlag,
        		     @Param("billable") String billable,
        		     @Param("billable_type") String billableType,
        		     @Param("employmentstatus") String employmentStatus,
        		     @Param("gender") String gender,
        		     @Param("experience") String experience ,
        		     @Param("lower_age") Integer lowerAge,
        		     @Param("upper_age") Integer upperAge
        		 );



        		 @Query("SELECT " +
        		            "d.name AS departmentName, " +
        		            "CASE WHEN e.isUserInfoUpdated = 'true' THEN 'Completed' ELSE 'Pending' END AS status, " +
        		            "COUNT(DISTINCT e.empId) AS empCount " +
        		            "FROM Employee e " +
        		            "INNER JOIN JobRole jr ON e.jobRoleId = jr.jobRoleId " +
        		            "INNER JOIN Department d ON d.deptId = jr.deptId " +
        		            "WHERE e.employmentstatus <> 'InActive' " + 
        		            "GROUP BY d.name, CASE WHEN e.isUserInfoUpdated = 'true' THEN 'Completed' ELSE 'Pending' END")
        		   public List<Object[]> getDepartmentWiseKycCount();
        		   
//          		 @Query(value = "SELECT " +
//     		            "d.name AS department_name, " +
//     		            "CASE WHEN e.is_user_info_updated = 'true' THEN 'Completed' ELSE 'Pending' END AS status, " +
//     		            "COUNT(DISTINCT e.emp_id) AS emp_count " +
//     		            "FROM employee e " +
//     		            "INNER JOIN job_role jr ON e.job_role_id = jr.job_role_id " +
//     		            "INNER JOIN department d ON d.dept_id = jr.dept_id " +
//     		            "WHERE e.employmentstatus <> 'InActive' " + 
//     		            "GROUP BY d.name, CASE WHEN e.is_user_info_updated = 'true' THEN 'Completed' ELSE 'Pending' END",
//     		    nativeQuery = true)
//     		   public List<Object[]> getDepartmentWiseKycCount();
        		   
        		   
        		   @Query(value = 
        				    "WITH ALL_EMPLOYEES AS ( " +
        				    "    SELECT DISTINCT e.emp_id emp_ids " +
        				    "    FROM employee e " +
        				    "    LEFT JOIN job_role jr ON e.job_role_id = jr.job_role_id " +
        				    "    LEFT JOIN department d ON jr.dept_id = d.dept_id " +
        				    "    LEFT JOIN employee mg ON e.manager_id = mg.emp_id " +
        				    "    LEFT JOIN employee_team_mapping etm ON e.emp_id = etm.emp_id AND etm.active != '0' " +
        				    "    LEFT JOIN teams t ON etm.team_id = t.team_id AND t.is_active = 'Y' " +
        				    "    LEFT JOIN projects p ON t.project_id = p.project_id AND p.active = 'true' " +
        				    "    WHERE 1=1 " +
        				    "    AND e.emp_id NOT BETWEEN 1 AND 6 " +
        				    "    AND (:employeement_id IS NULL OR e.employeement_id IN (:employeement_id)) " +
        				    "    AND (:is_apmosys_product IS NULL OR e.is_apmosys_product = :is_apmosys_product)" +
        				    "    AND (:name IS NULL OR UPPER(e.name) LIKE CONCAT('%', UPPER(:name), '%')) " +
        				    "    AND (:dept_id IS NULL OR d.dept_id IN (:dept_id)) " +
        				    "    AND (:job_role_id IS NULL OR jr.job_role_id IN (:job_role_id)) " +
        				    "    AND (:manager_id IS NULL OR mg.emp_id IN (:manager_id)) " +
        				    "    AND (:team_id IS NULL OR t.team_id IN (:team_id)) " +
        				    "    AND (:project_id IS NULL OR p.project_id IN (:project_id)) " +
        				    "    AND (:client_id IS NULL OR p.client_id IN (:client_id)) " +
        				    "    AND (:employmentstatus IS NULL OR UPPER(e.employmentstatus) LIKE CONCAT('%', UPPER(:employmentstatus), '%')) " +
        				    "    AND (:date_of_joining IS NULL OR e.date_of_joining LIKE CONCAT('%', :date_of_joining, '%')) " +
        				    "    AND (:city IS NULL OR UPPER(e.city) LIKE CONCAT('%', UPPER(:city), '%')) " +
        				    "    AND (:blood_group IS NULL OR UPPER(e.blood_group) LIKE CONCAT('%', UPPER(:blood_group), '%')) " +
        				    "    AND (:gender IS NULL OR UPPER(e.gender) LIKE CONCAT('%', UPPER(:gender), '%')) " +
        				    "    AND (:probation_period IS NULL OR e.probation_period IN (:probation_period)) " +
        				    "    AND (:notice_period IS NULL OR e.notice_period IN (:notice_period)) " +
        				    "    AND (:marital_status IS NULL OR UPPER(e.marital_status) LIKE CONCAT('%', UPPER(:marital_status), '%')) " +
        				    "    AND (:bank_name IS NULL OR UPPER(e.bank_name) LIKE CONCAT('%', UPPER(:bank_name), '%')) " +
        				    "    AND (:state IS NULL OR UPPER(e.state) LIKE CONCAT('%', UPPER(:state), '%')) " +
        				    "    AND (:created_on IS NULL OR e.created_on LIKE CONCAT('%', :created_on, '%')) " +
        				    "    AND (:created_by IS NULL OR e.created_by IN (:created_by)) " +
        				    "    AND (:experience IS NULL OR UPPER(e.experience) LIKE CONCAT('%', UPPER(:experience), '%')) " +
        				    "    AND (:work_location IS NULL OR UPPER(e.work_location) LIKE CONCAT('%', UPPER(:work_location), '%')) " +
        				    ") " +
        				    "SELECT * FROM ( " +
        				    "    SELECT COALESCE(j.month_name, r.month_name) AS month_name, " +
        				    "           COALESCE(j.year, r.year) AS year, " +
        				    "           j.apprentice_count, j.consultant_count, j.regular_count, j.apmosys_product_count, " +
        				    "           COALESCE(r.total_emp_count, 0) resign_count " +
        				    "    FROM ( " +
        				    "        SELECT DATE_FORMAT(date_of_joining, '%M') AS month_name, " +
        				    "               YEAR(date_of_joining) AS year, " +
        				    "               COUNT(DISTINCT CASE WHEN is_apprenticeship = 'true' THEN emp_id END) AS apprentice_count, " +
        				    "               COUNT(DISTINCT CASE WHEN is_consultant = 'true' THEN emp_id END) AS consultant_count, " +
        				    "               COUNT(DISTINCT CASE WHEN is_apmosys_product = 'true' THEN emp_id END) AS apmosys_product_count, " +
        				    "               COUNT(DISTINCT CASE " +
        				    "                   WHEN (is_consultant = 'false' AND is_apprenticeship = 'false' AND is_apmosys_product = 'false') " +
        				    "                        OR (COALESCE(is_consultant, '') = '' AND COALESCE(is_apprenticeship, '') = '') " +
        				    "                   THEN emp_id END) AS regular_count " +
        				    "        FROM employee e1 " +
        				    "        WHERE date_of_joining IS NOT NULL " +
        				    "          AND e1.emp_id IN (SELECT emp_ids FROM ALL_EMPLOYEES) " +
        				    "        GROUP BY year, month_name " +
        				    "    ) j " +
        				    "    LEFT JOIN ( " +
        				    "        SELECT DATE_FORMAT(date_of_resign, '%M') AS month_name, " +
        				    "               YEAR(date_of_resign) AS year, " +
        				    "               COUNT(*) AS total_emp_count " +
        				    "        FROM employee e2 " +
        				    "        WHERE date_of_resign IS NOT NULL " +
        				    "          AND e2.emp_id IN (SELECT emp_ids FROM ALL_EMPLOYEES) " +
        				    "        GROUP BY year, month_name " +
        				    "    ) r ON j.year = r.year AND j.month_name = r.month_name " +
        				    "    UNION " +
        				    "    SELECT COALESCE(j.month_name, r.month_name) AS month_name, " +
        				    "           COALESCE(j.year, r.year) AS year, " +
        				    "           j.apprentice_count, j.consultant_count, j.regular_count, j.apmosys_product_count, " +
        				    "           r.total_emp_count " +
        				    "    FROM ( " +
        				    "        SELECT DATE_FORMAT(date_of_joining, '%M') AS month_name, " +
        				    "               YEAR(date_of_joining) AS year, " +
        				    "               COUNT(DISTINCT CASE WHEN is_apprenticeship = 'true' THEN emp_id END) AS apprentice_count, " +
        				    "               COUNT(DISTINCT CASE WHEN is_consultant = 'true' THEN emp_id END) AS consultant_count, " +
        				    "               COUNT(DISTINCT CASE WHEN is_apmosys_product = 'true' THEN emp_id END) AS apmosys_product_count, " +
        				    "               COUNT(DISTINCT CASE " +
        				    "                   WHEN (is_consultant = 'false' AND is_apprenticeship = 'false') " +
        				    "                        OR (COALESCE(is_consultant, '') = '' AND COALESCE(is_apprenticeship, '') = '') " +
        				    "                   THEN emp_id END) AS regular_count " +
        				    "        FROM employee e3 " +
        				    "        WHERE date_of_joining IS NOT NULL " +
        				    "          AND e3.emp_id IN (SELECT emp_ids FROM ALL_EMPLOYEES) " +
        				    "        GROUP BY year, month_name " +
        				    "    ) j " +
        				    "    RIGHT JOIN ( " +
        				    "        SELECT DATE_FORMAT(date_of_resign, '%M') AS month_name, " +
        				    "               YEAR(date_of_resign) AS year, " +
        				    "               COUNT(*) AS total_emp_count " +
        				    "        FROM employee e4 " +
        				    "        WHERE date_of_resign IS NOT NULL " +
        				    "          AND e4.emp_id IN (SELECT emp_ids FROM ALL_EMPLOYEES) " +
        				    "        GROUP BY year, month_name " +
        				    "    ) r ON j.year = r.year AND j.month_name = r.month_name " +
        				    "    WHERE j.month_name IS NULL " +
        				    ") AS combined_data " +
        				    "WHERE year = :year " +
        				    "ORDER BY year, STR_TO_DATE(CONCAT('01 ', month_name, ' 2012'), '%d %M %Y')", 
        				    nativeQuery = true)
        				List<Object[]> getJoiningVsResignationCount(
        				    @Param("employeement_id") List<Long> employeementId,
        				    @Param("name") String name,
        				    @Param("dept_id") List<Long> deptId,
        				    @Param("job_role_id") List<Long> jobRoleId,
        				    @Param("manager_id") List<Long> managerId,
        				    @Param("team_id") List<Long> teamId,
        				    @Param("project_id") List<Long> projectId,
        				    @Param("client_id") List<Long>clientId,
        				    @Param("employmentstatus") String employmentstatus,
        				    @Param("date_of_joining") String dateOfJoining,
        				    @Param("city") String city,
        				    @Param("blood_group") String bloodGroup,
        				    @Param("gender") String gender,
        				    @Param("probation_period") List<Long>probationPeriod,
        				    @Param("notice_period") List<Long> noticePeriod,
        				    @Param("marital_status") String maritalStatus,
        				    @Param("bank_name") String bankName,
        				    @Param("state") String state,
        				    @Param("created_on") String createdOn,
        				    @Param("created_by") List<Long> createdBy,
        				    @Param("experience") String experience,
        				    @Param("work_location") String workLocation,
        				    @Param("year") Long year,
        				    @Param("is_apmosys_product") String isApmosysProduct
        				);
	
	@Query(value = "SELECT " +
	        "d.name AS departmentName, " +
	        "CASE " +
	        "    WHEN e.is_apprenticeship = 'true' THEN 'Apprentice' " +
	        "    WHEN e.is_consultant = 'true' THEN 'Consultant' " +
	        "    WHEN ((e.is_consultant = 'false' AND e.is_apprenticeship = 'false' AND e.is_apmosys_product = 'false') OR " +
	        "          (COALESCE(e.is_consultant, '') = '' AND COALESCE(e.is_apprenticeship, '') = '')) " +
	        "    THEN 'Employee' " +
	        "    WHEN e.is_apmosys_product = 'true' THEN 'ApmosysProduct' " +
	        "END AS type, " +
	        "COUNT(DISTINCT e.emp_id) AS empCount " +
	        "FROM employee e " +
	        "INNER JOIN job_role jr ON e.job_role_id = jr.job_role_id " +
	        "INNER JOIN department d ON d.dept_id = jr.dept_id " +
	        "WHERE e.employmentstatus != 'InActive' " +
	        "GROUP BY d.name, " +
	        "CASE " +
	        "    WHEN e.is_apprenticeship = 'true' THEN 'Apprentice' " +
	        "    WHEN e.is_consultant = 'true' THEN 'Consultant' " +
	        "    WHEN ((e.is_consultant = 'false' AND e.is_apprenticeship = 'false' AND e.is_apmosys_product = 'false') OR " +
	        "          (COALESCE(e.is_consultant, '') = '' AND COALESCE(e.is_apprenticeship, '') = '')) " +
	        "    THEN 'Employee' " +
	        "    WHEN e.is_apmosys_product = 'true' THEN 'ApmosysProduct' " +
	        "END", 
	       nativeQuery = true)
       public List<Object[]> getAllEmployeeCountDepartmentWise();
       
      
       
       
//     @Query(value = "SELECT DISTINCT d.name AS department_name, " +
//              "e.billable_type AS billable_type, " +
//              "COUNT(DISTINCT e.emp_id) AS emp_count " +
//               "FROM employee e " +
//              "INNER JOIN job_role jr ON e.job_role_id = jr.job_role_id " +
//              "INNER JOIN department d ON jr.dept_id = d.dept_id " +
//              "WHERE e.employmentstatus != 'InActive' " +
//              "GROUP BY d.name, e.billable_type " +
//              "ORDER BY d.name, e.billable_type", 
//        nativeQuery = true)
//       public List<Object[]> getDepartmentWiseBillableNonBillableSummary();       
     
       
       //added by Dibya

       @Query("SELECT d.name, e.billableType, COUNT(DISTINCT e.empId) " +
    	       "FROM Employee e " +
    	       "JOIN JobRole jr ON jr.jobRoleId = e.jobRoleId " +
    	       "JOIN Department d ON d.deptId = jr.deptId " +
    	       "WHERE e.employmentstatus <> 'InActive' " +
    	       "GROUP BY d.name, e.billableType " +
    	       "ORDER BY d.name, e.billableType")
    	List<Object[]> getDepartmentWiseBillableNonBillableSummary();

       
       @Query(value = "SELECT DISTINCT " +
    	        "d.name AS department_name, " +
    	        "e.billable_type AS billable_type, " +
    	        "COUNT(DISTINCT e.emp_id) AS emp_count " +
    	        "FROM employee e " +
    	        "LEFT JOIN job_role jr ON e.job_role_id = jr.job_role_id " +
    	        "LEFT JOIN department d ON jr.dept_id = d.dept_id " +
    	        "LEFT JOIN employee mg ON e.manager_id = mg.emp_id " +
    	        "LEFT JOIN employee_team_mapping etm ON e.emp_id = etm.emp_id AND etm.active != '0' " +
    	        "LEFT JOIN teams t ON etm.team_id = t.team_id AND t.is_active = 'Y' " +
    	        "LEFT JOIN projects p ON t.project_id = p.project_id AND p.active = 'true' " +
    	        "WHERE 1=1 " +
    	        "AND e.emp_id NOT BETWEEN 1 AND 6 " +
    	        "AND e.employmentstatus != 'InActive' " +
    	        "AND (:employeement_id IS NULL OR e.emp_id IN (:employeement_id)) " +
    	        "AND (:name IS NULL OR UPPER(e.name) LIKE CONCAT('%', UPPER(:name), '%')) " +
    	        "AND (:dept_id IS NULL OR d.dept_id IN (:dept_id)) " +
    	        "AND (:job_role_id IS NULL OR jr.job_role_id IN (:job_role_id)) " +
    	        "AND (:manager_id IS NULL OR mg.emp_id IN (:manager_id)) " +
    	        "AND (:team_id IS NULL OR t.team_id IN (:team_id)) " +
    	        "AND (:project_id IS NULL OR p.project_id IN (:project_id)) " +
    	        "AND (:client_id IS NULL OR p.client_id IN (:client_id)) " +
    	        "AND (:employmentstatus IS NULL OR UPPER(e.employmentstatus) LIKE CONCAT('%', UPPER(:employmentstatus), '%')) " +
    	        "AND (:date_of_joining IS NULL OR e.date_of_joining LIKE CONCAT('%', :date_of_joining, '%')) " +
    	        "AND (:city IS NULL OR UPPER(e.city) LIKE CONCAT('%', UPPER(:city), '%')) " +
    	        "AND (:blood_group IS NULL OR UPPER(e.blood_group) LIKE CONCAT('%', UPPER(:blood_group), '%')) " +
    	        "AND (:gender IS NULL OR UPPER(e.gender) LIKE CONCAT('%', UPPER(:gender), '%')) " +
    	        "AND (:probation_period IS NULL OR e.probation_period IN (:probation_period)) " +
    	        "AND (:notice_id IS NULL OR e.notice_id IN (:notice_id)) " +
    	        "AND (:marital_status IS NULL OR UPPER(e.marital_status) LIKE CONCAT('%', UPPER(:marital_status), '%')) " +
    	        "AND (:bank_name IS NULL OR UPPER(e.bank_name) LIKE CONCAT('%', UPPER(:bank_name), '%')) " +
    	        "AND (:state IS NULL OR UPPER(e.state) LIKE CONCAT('%', UPPER(:state), '%')) " +
    	        "AND (:created_on IS NULL OR e.created_on LIKE CONCAT('%', :created_on, '%')) " +
    	        "AND (:created_by IS NULL OR e.created_by IN (:created_by)) " +
    	        "AND (:experience IS NULL OR UPPER(e.experience) LIKE CONCAT('%', UPPER(:experience), '%')) " +
    	        "AND (:work_location IS NULL OR UPPER(e.work_location) LIKE CONCAT('%', UPPER(:work_location), '%')) " +
    	        // New filters added below
    	        "AND (:year IS NULL OR YEAR(e.date_of_joining) = :year) " +
    	        "AND (:billable_type IS NULL OR UPPER(e.billable_type) LIKE CONCAT('%', UPPER(:billable_type), '%')) " +
    	        "GROUP BY d.name, e.billable_type " +
    	        "ORDER BY d.name, e.billable_type",
    	        nativeQuery = true)
    	List<Object[]> getSelectedDepartmentBillableSummary(
    	    @Param("employeement_id") List<Long> employeementId,
    	    @Param("name") String name,
    	    @Param("dept_id") List<Long> deptId,
    	    @Param("job_role_id") List<Long> jobRoleId,
    	    @Param("manager_id") List<Long> managerId,
    	    @Param("team_id") List<Long> teamId,
    	    @Param("project_id") List<Long> projectId,
    	    @Param("client_id") List<Long> clientId,
    	    @Param("employmentstatus") String employmentStatus,
    	    @Param("date_of_joining") String dateOfJoining,
    	    @Param("city") String city,
    	    @Param("blood_group") String bloodGroup,
    	    @Param("gender") String gender,
    	    @Param("probation_period") List<Long> probationPeriod,
    	    @Param("notice_id") List<Long> noticeId,
    	    @Param("marital_status") String maritalStatus,
    	    @Param("bank_name") String bankName,
    	    @Param("state") String state,
    	    @Param("created_on") String createdOn,
    	    @Param("created_by") List<Long> createdBy,
    	    @Param("experience") String experience,
    	    @Param("work_location") String workLocation,
    	    @Param("year") Long year,
    	    @Param("billable_type") String billableType
    	);



//		        		 @Query(nativeQuery = true ,value = "SELECT distinct " +
//        			        "CONCAT('A-', e.employeement_id) as EMPLOYEEMENT_ID, " +
//        			        "CASE WHEN e.is_apprenticeship = 'true' THEN 'Apprentice' " +
//        			        "     WHEN ((e.is_consultant = 'false' AND e.is_apprenticeship = 'false') OR (COALESCE(e.is_consultant, '') = '' AND COALESCE(e.is_apprenticeship, '') = '')) THEN 'Regular' " +
//        			        "     WHEN e.is_consultant = 'true' THEN 'Consultant' " +
//        			        "END AS EMPLOYMENT_TYPE, " +
//        			        "e.name NAME, " +
//        			        "e.experience EXPERIENCE, " +
//        			        "d.name DEPARTMENT_NAME, " +
//        			        "e.email EMAIL_ID, " +
//        			        "m.name MANAGER_NAME, " +
//        			        "e.billable BILLABLE, " +
//        			        "e.billable_type BILLABLE_TYPE, " +
//        			        "GROUP_CONCAT(DISTINCT p.project_name SEPARATOR ', ') AS PROJECT_NAMES, " +
//        			        "GROUP_CONCAT(DISTINCT c.client_name SEPARATOR ', ') AS CLIENT_NAMES, " +
//        			        "e.date_of_joining DATE_OF_JOINING, " +
//        			        "e.mobile_no MOBILE_NO, " +
//        			        "e.employmentstatus STATUS, " +
//        			        "e.total_experience TOTAL_EXPERIENCE, " +
//        			        "e.gender GENDER, " +
//        			        "e.work_location WORK_LOCATION, " +
//        			        "TIMESTAMPDIFF(YEAR, e.date_of_birth, CURRENT_DATE()) age, " +
//        			        "e.is_user_info_updated KYC, " +
//        			        "m.emp_id MANAGER_ID, " +
//        			        "e.emp_id EMP_ID " +
//        			        "FROM employee e " +
//        			        "LEFT JOIN job_role jr ON e.job_role_id = jr.job_role_id " +
//        			        "LEFT JOIN department d ON jr.dept_id = d.dept_id " +
//        			        "LEFT JOIN employee mg ON e.manager_id = mg.emp_id " +
//        			        "LEFT JOIN employee_team_mapping etm ON e.emp_id = etm.emp_id AND etm.active != '0' " +
//        			        "LEFT JOIN employee m ON m.emp_id = e.manager_id " +
//        			        "LEFT JOIN teams t ON etm.team_id = t.team_id AND t.is_active = 'Y' " +
//        			        "LEFT JOIN projects p ON t.project_id = p.project_id AND p.active = 'true' " +
//        			        "LEFT JOIN clients c ON p.client_id = c.client_id " +
//        			        "WHERE e.employmentstatus != 'InActive' " +
//        			        "AND d.dept_id IN (:deptIds) " +
//        			        "AND (" +
//        			        "    (:billableType = 'TNM' AND e.billable_type = 'TNM') OR " +
//        			        "    (:billableType = 'Shadow' AND e.billable_type = 'Shadow') OR " +
//        			        "    (:billableType = 'Bench' AND e.billable_type = 'Bench') OR " +
//        			        "    (:billableType = 'Fixed Cost' AND e.billable_type = 'Fixed Cost') OR " +
//        			        "    (:billableType = 'InternalRNDProducts' AND e.billable_type = 'InternalRNDProducts') " +
//        			        ") " +
//        			        "AND e.emp_id NOT BETWEEN 1 AND 6 " +
//        			        "GROUP BY e.emp_id")
//        			List<Object[]> findEmployeesByDepartmentAndBillableType(
//        			    @Param("deptIds") List<Long> deptIds,
//        			    @Param("billableType") String billableType
//        			);
    	
    	 @Query(nativeQuery = true ,value = "SELECT distinct\n"
    	 		+ "    CASE \n"
    	 		+ "        WHEN e.is_apmosys_product = 'true' THEN CONCAT('AP-', e.employeement_id)\n"
    	 		+ "        ELSE CONCAT('A-', e.employeement_id)\n"
    	 		+ "    END AS EMPLOYEEMENT_ID,\n"
    	 		+ "    \n"
    	 		+ "    CASE \n"
    	 		+ "    WHEN e.is_apmosys_product = 'true' THEN 'Apmosys Product'\n"
    	 		+ "    WHEN e.is_apprenticeship = 'true' THEN 'Apprentice'\n"
    	 		+ "    WHEN ((e.is_consultant = 'false' AND e.is_apprenticeship = 'false') OR \n"
    	 		+ "          (COALESCE(e.is_consultant, '') = '' AND COALESCE(e.is_apprenticeship, '') = '')) THEN 'Regular'\n"
    	 		+ "    WHEN e.is_consultant = 'true' THEN 'Consultant'\n"
    	 		+ "END AS EMPLOYMENT_TYPE,\n"
    	 		+ "\n"
    	 		+ "\n"
    	 		+ "    e.name AS NAME,\n"
    	 		+ "    e.experience AS EXPERIENCE,\n"
    	 		+ "    d.name AS DEPARTMENT_NAME,\n"
    	 		+ "    e.email AS EMAIL_ID,\n"
    	 		+ "    m.name AS MANAGER_NAME,\n"
    	 		+ "    e.billable AS BILLABLE,\n"
    	 		+ "    e.billable_type AS BILLABLE_TYPE,\n"
    	 		+ "    GROUP_CONCAT(DISTINCT p.project_name SEPARATOR ', ') AS PROJECT_NAMES,\n"
    	 		+ "    GROUP_CONCAT(DISTINCT c.client_name SEPARATOR ', ') AS CLIENT_NAMES,\n"
    	 		+ "    e.date_of_joining AS DATE_OF_JOINING,\n"
    	 		+ "    e.mobile_no AS MOBILE_NO,\n"
    	 		+ "    e.employmentstatus AS STATUS,\n"
    	 		+ "    e.total_experience AS TOTAL_EXPERIENCE,\n"
    	 		+ "    e.gender AS GENDER,\n"
    	 		+ "    e.work_location AS WORK_LOCATION,\n"
    	 		+ "    TIMESTAMPDIFF(YEAR, e.date_of_birth, CURRENT_DATE()) AS age,\n"
    	 		+ "    e.is_user_info_updated AS KYC,\n"
    	 		+ "    m.emp_id AS MANAGER_ID,\n"
    	 		+ "    e.emp_id AS EMP_ID\n"
    	 		+ "\n"
    	 		+ "FROM employee e\n"
    	 		+ "\n"
    	 		+ "LEFT JOIN job_role jr ON e.job_role_id = jr.job_role_id\n"
    	 		+ "LEFT JOIN department d ON jr.dept_id = d.dept_id\n"
    	 		+ "LEFT JOIN employee mg ON e.manager_id = mg.emp_id\n"
    	 		+ "LEFT JOIN employee_team_mapping etm ON e.emp_id = etm.emp_id AND etm.active != '0'\n"
    	 		+ "LEFT JOIN employee m ON m.emp_id = e.manager_id\n"
    	 		+ "LEFT JOIN teams t ON etm.team_id = t.team_id AND t.is_active = 'Y'\n"
    	 		+ "LEFT JOIN projects p ON t.project_id = p.project_id AND p.active = 'true'\n"
    	 		+ "LEFT JOIN clients c ON p.client_id = c.client_id\n"
    	 		+ "\n"
    	 		+ "WHERE e.employmentstatus != 'InActive'\n"
    	 		+ "  AND d.dept_id IN (:deptIds)\n"
    	 		+ "  AND (\n"
    	 		+ "        (:billableType = 'TNM' AND e.billable_type = 'TNM') OR\n"
    	 		+ "        (:billableType = 'Shadow' AND e.billable_type = 'Shadow') OR\n"
    	 		+ "        (:billableType = 'Bench' AND e.billable_type = 'Bench') OR\n"
    	 		+ "        (:billableType = 'Fixed Cost' AND e.billable_type = 'Fixed Cost') OR\n"
    	 		+ "        (:billableType = 'InternalRNDProducts' AND e.billable_type = 'InternalRNDProducts')\n"
    	 		+ "      )\n"
    	 		+ "  AND e.emp_id NOT BETWEEN 1 AND 6\n"
    	 		+ "GROUP BY e.emp_id\n"
    	 		+ "\n"
    	 		+ "\n"
    	 		+ "")
			List<Object[]> findEmployeesByDepartmentAndBillableType(
			    @Param("deptIds") List<Long> deptIds,
			    @Param("billableType") String billableType
			);
        			
        			
//			@Query(value = "SELECT distinct " +
//			        "CONCAT('A-', e.employeement_id) as EMP_ID, " +
//			        "CASE WHEN e.is_apprenticeship = 'true' THEN 'Apprentice' " +
//			        "     WHEN ((e.is_consultant = 'false' AND e.is_apprenticeship = 'false') OR " +
//			        "           (COALESCE(e.is_consultant, '') = '' AND COALESCE(e.is_apprenticeship, '') = '')) THEN 'Regular' " +
//			        "     WHEN e.is_consultant = 'true' THEN 'Consultant' " +
//			        "END AS EMPLOYMENT_TYPE, " +
//			        "e.name NAME, " +
//			        "e.experience EXPERIENCE, " +
//			        "d.name DEPARTMENT_NAME, " +
//			        "e.email EMAIL_ID, " +
//			        "m.name MANAGER_NAME, " +
//			        "e.billable BILLABLE, " +
//			        "e.billable_type BILLABLE_TYPE, " +
//			        "GROUP_CONCAT(DISTINCT p.project_name SEPARATOR ', ') AS PROJECT_NAMES, " +
//			        "GROUP_CONCAT(DISTINCT c.client_name SEPARATOR ', ') AS CLIENT_NAMES, " +
//			        "e.date_of_joining DATE_OF_JOINING, " +
//			        "e.mobile_no MOBILE_NO, " +
//			        "e.employmentstatus STATUS, " +
//			        "e.total_experience TOTAL_EXPERIENCE, " +
//			        "e.gender GENDER, " +
//			        "e.work_location WORK_LOCATION, " +
//			        "TIMESTAMPDIFF(YEAR, e.date_of_birth, CURRENT_DATE()) age, " +
//			        "e.is_user_info_updated KYC " +
//			        "FROM employee e " +
//			        "left join job_role jr on e.job_role_id = jr.job_role_id " +
//			        "left join department d on jr.dept_id = d.dept_id " +
//			        "left join employee mg on e.manager_id = mg.emp_id " +
//			        "left join employee_team_mapping etm on e.emp_id = etm.emp_id and etm.active != '0' " +
//			        "left join employee m on m.emp_id = e.manager_id " +
//			        "left join teams t on etm.team_id = t.team_id and t.is_active = 'Y' " +
//			        "left join projects p on t.project_id = p.project_id and p.active = 'true' " +
//			        "left join clients c on p.client_id = c.client_id " +
//			        "WHERE 1=1 and e.employmentstatus != 'InActive' " +
//			        "and ((:apprentice = true and e.is_apprenticeship = 'true') " +
//			        "     or (:consultant = true and e.is_consultant = 'true') " +
//			        "     or (:regular = true and ((e.is_consultant = 'false' AND e.is_apprenticeship = 'false') OR " +
//			        "                              (COALESCE(e.is_consultant, '') = '' AND COALESCE(e.is_apprenticeship, '') = ''))) " +
//			        "     or (:probation = true and e.employmentstatus = 'Probation' and TIMESTAMPDIFF(DAY, e.date_of_joining, current_date()) > 180) " +
//			        "     or (:allEmp = true)) " +
//			        "and e.emp_id NOT BETWEEN 1 AND 6 " +
//			        "GROUP BY e.emp_id", nativeQuery = true)
//			List<Object[]> findEmployeesByEmploymentType(
//			        @Param("apprentice") boolean apprentice,
//			        @Param("consultant") boolean consultant,
//			        @Param("regular") boolean regular,
//			        @Param("probation") boolean probation,
//			        @Param("allEmp") boolean allEmp
//			);
			
			@Query(nativeQuery = true, value = "SELECT distinct\n"
					+ " CASE \n"
					+ " WHEN e.is_apmosys_product = 'true' THEN CONCAT('AP-', e.employeement_id)\n"
					+ " ELSE CONCAT('A-', e.employeement_id)\n"
					+ " END AS EMP_ID,\n"
					+ " \n"
					+ " CASE \n"
					+ " WHEN e.is_apmosys_product = 'true' THEN 'Apmosys Product'\n"
					+ " WHEN e.is_apprenticeship = 'true' THEN 'Apprentice'\n"
					+ " WHEN ((e.is_consultant = 'false' AND e.is_apprenticeship = 'false') OR \n"
					+ " (COALESCE(e.is_consultant, '') = '' AND COALESCE(e.is_apprenticeship, '') = '')) THEN 'Regular'\n"
					+ " WHEN e.is_consultant = 'true' THEN 'Consultant'\n"
					+ " END AS EMPLOYMENT_TYPE,\n"
					+ "\n"
					+ " e.name AS NAME,\n"
					+ " e.experience AS EXPERIENCE,\n"
					+ " d.name AS DEPARTMENT_NAME,\n"
					+ " e.email AS EMAIL_ID,\n"
					+ " m.name AS MANAGER_NAME,\n"
					+ " e.billable AS BILLABLE,\n"
					+ " e.billable_type AS BILLABLE_TYPE,\n"
					+ " GROUP_CONCAT(DISTINCT p.project_name SEPARATOR ', ') AS PROJECT_NAMES,\n"
					+ " GROUP_CONCAT(DISTINCT c.client_name SEPARATOR ', ') AS CLIENT_NAMES,\n"
					+ " e.date_of_joining AS DATE_OF_JOINING,\n"
					+ " e.mobile_no AS MOBILE_NO,\n"
					+ " e.employmentstatus AS STATUS,\n"
					+ " e.total_experience AS TOTAL_EXPERIENCE,\n"
					+ " e.gender AS GENDER,\n"
					+ " e.work_location AS WORK_LOCATION,\n"
					+ " TIMESTAMPDIFF(YEAR, e.date_of_birth, CURRENT_DATE()) AS age,\n"
					+ " e.is_user_info_updated AS KYC\n"
					+ "\n"
					+ "FROM employee e\n"
					+ "\n"
					+ "LEFT JOIN job_role jr ON e.job_role_id = jr.job_role_id\n"
					+ "LEFT JOIN department d ON jr.dept_id = d.dept_id\n"
					+ "LEFT JOIN employee mg ON e.manager_id = mg.emp_id\n"
					+ "LEFT JOIN employee_team_mapping etm ON e.emp_id = etm.emp_id AND etm.active != '0'\n"
					+ "LEFT JOIN employee m ON m.emp_id = e.manager_id\n"
					+ "LEFT JOIN teams t ON etm.team_id = t.team_id AND t.is_active = 'Y'\n"
					+ "LEFT JOIN projects p ON t.project_id = p.project_id AND p.active = 'true'\n"
					+ "LEFT JOIN clients c ON p.client_id = c.client_id\n"
					+ "\n"
					+ "WHERE 1=1 AND e.employmentstatus != 'InActive'\n"
					+ " AND (\n"
					+ " (:apprentice = true AND e.is_apprenticeship = 'true') OR\n"
					+ " (:consultant = true AND e.is_consultant = 'true') OR\n"
					+ " (:regular = true AND ((e.is_consultant = 'false' AND e.is_apprenticeship = 'false') OR \n"
					+ " (COALESCE(e.is_consultant, '') = '' AND COALESCE(e.is_apprenticeship, '') = ''))) OR\n"
					+ " (:probation = true AND e.employmentstatus = 'Probation' AND TIMESTAMPDIFF(DAY, e.date_of_joining, CURRENT_DATE()) > 180) OR\n"
					+ " (:apmosysProduct = true AND e.is_apmosys_product = 'true') OR\n"
					+ " (:allEmp = true)\n"
					+ " )\n"
					+ " AND e.emp_id NOT BETWEEN 1 AND 6\n"
					+ "GROUP BY e.emp_id\n"
					+ "\n")
					List<Object[]> findEmployeesByEmploymentType(
					    @Param("apprentice") boolean apprentice,
					    @Param("consultant") boolean consultant,
					    @Param("regular") boolean regular,
					    @Param("probation") boolean probation,
					    @Param("apmosysProduct") boolean apmosysProduct,
					    @Param("allEmp") boolean allEmp
					);

        			
//        			@Query(nativeQuery = true, value = "SELECT distinct \n"
//        					+ "	CONCAT('A-', e.employeement_id) as EMP_ID,\n"
//        					+ "    CASE WHEN e.is_apprenticeship = 'true' THEN 'Apprentice'\n"
//        					+ "		 WHEN ((e.is_consultant = 'false' AND e.is_apprenticeship = 'false') OR (COALESCE(e.is_consultant, '') = '' AND COALESCE(e.is_apprenticeship, '') = '')) THEN 'Regular'\n"
//        					+ "         WHEN e.is_consultant = 'true' THEN 'Consultant' \n"
//        					+ "	END AS EMPLOYMENT_TYPE,\n"
//        					+ "    e.name NAME,\n"
//        					+ "    e.experience EXPERIENCE,\n"
//        					+ "    d.name DEPARTMENT_NAME,\n"
//        					+ "    e.email EMAIL_ID,\n"
//        					+ "    m.name MANAGER_NAME,\n"
//        					+ "    e.billable BILLABLE,\n"
//        					+ "    e.billable_type BILLABLE_TYPE,\n"
//        					+ "    GROUP_CONCAT(DISTINCT p.project_name SEPARATOR ', ') AS PROJECT_NAMES,\n"
//        					+ "    GROUP_CONCAT(DISTINCT c.client_name SEPARATOR ', ') AS CLIENT_NAMES,\n"
//        					+ "    e.date_of_joining DATE_OF_JOINING,\n"
//        					+ "    e.mobile_no MOBILE_NO,\n"
//        					+ "	e.employmentstatus STATUS,\n"
//        					+ "	e.total_experience TOTAL_EXPERIENCE,\n"
//        					+ "    e.gender GENDER,\n"
//        					+ "    e.work_location WORK_LOCATION,\n"
//        					+ "    TIMESTAMPDIFF(YEAR, e.date_of_birth, CURRENT_DATE()) age,\n"
//        					+ "    e.is_user_info_updated KYC\n"
//        					+ "FROM employee e \n"
//        					+ "left join job_role jr on e.job_role_id = jr.job_role_id\n"
//        					+ "left join department d on jr.dept_id = d.dept_id\n"
//        					+ "left join employee mg on e.manager_id = mg.emp_id \n"
//        					+ "left join employee_team_mapping etm on e.emp_id = etm.emp_id and etm.active != '0'\n"
//        					+ "left join employee m on m.emp_id = e.manager_id\n"
//        					+ "left join teams t on etm.team_id = t.team_id and t.is_active = 'Y'\n"
//        					+ "left join projects p on t.project_id = p.project_id and p.active = 'true'\n"
//        					+ "left join clients c on p.client_id = c.client_id\n"
//        					+ "WHERE 1=1 and e.employmentstatus != 'InActive' \n"
//        					+ "	and (\n"
//        					+ "			(:employee_type = 'apprentice' and e.is_apprenticeship = 'true')\n"
//        					+ "					or\n"
//        					+ "			(:employee_type = 'consultant' and e.is_consultant = 'true')\n"
//        					+ "					or\n"
//        					+ "			(:employee_type = 'regular' and ((e.is_consultant = 'false' AND e.is_apprenticeship = 'false') OR (COALESCE(e.is_consultant, '') = '' AND COALESCE(e.is_apprenticeship, '') = '')))\n"
//        					+ "		)\n"
//        					+ "	and (TIMESTAMPDIFF(MONTH, e.date_of_joining, CURRENT_DATE())/12 > :lower_value AND TIMESTAMPDIFF(MONTH, e.date_of_joining, CURRENT_DATE())/12 <= :upper_value)\n"
//        					+ "	and e.emp_id NOT BETWEEN 1 AND 6\n"
//        					+ "GROUP BY e.emp_id\n"
//        					+ ";")
//        			List<Object[]> findEmployeesExperience(
//        				    @Param("employee_type") String employeeType,
//        				    @Param("lower_value") Long lowerValue,
//        				    @Param("upper_value") Long upperValue
//        				);
					
        			@Query(nativeQuery = true, value = "SELECT distinct \n"
        					+ " CASE WHEN e.is_apmosys_product = 'true' THEN CONCAT('AP-', e.employeement_id)\n"
        					+ "      ELSE CONCAT('A-', e.employeement_id) \n"
        					+ " END as EMP_ID,\n"
        					+ " CASE WHEN e.is_apmosys_product = 'true' THEN 'Apmosys Product'\n"
        					+ "      WHEN e.is_apprenticeship = 'true' THEN 'Apprentice'\n"
        					+ "      WHEN ((e.is_consultant = 'false' AND e.is_apprenticeship = 'false') OR (COALESCE(e.is_consultant, '') = '' AND COALESCE(e.is_apprenticeship, '') = '')) THEN 'Regular'\n"
        					+ "      WHEN e.is_consultant = 'true' THEN 'Consultant' \n"
        					+ " END AS EMPLOYMENT_TYPE,\n"
        					+ " e.name NAME,\n"
        					+ " e.experience EXPERIENCE,\n"
        					+ " d.name DEPARTMENT_NAME,\n"
        					+ " e.email EMAIL_ID,\n"
        					+ " m.name MANAGER_NAME,\n"
        					+ " e.billable BILLABLE,\n"
        					+ " e.billable_type BILLABLE_TYPE,\n"
        					+ " GROUP_CONCAT(DISTINCT p.project_name SEPARATOR ', ') AS PROJECT_NAMES,\n"
        					+ " GROUP_CONCAT(DISTINCT c.client_name SEPARATOR ', ') AS CLIENT_NAMES,\n"
        					+ " e.date_of_joining DATE_OF_JOINING,\n"
        					+ " e.mobile_no MOBILE_NO,\n"
        					+ " e.employmentstatus STATUS,\n"
        					+ " e.total_experience TOTAL_EXPERIENCE,\n"
        					+ " e.gender GENDER,\n"
        					+ " e.work_location WORK_LOCATION,\n"
        					+ " TIMESTAMPDIFF(YEAR, e.date_of_birth, CURRENT_DATE()) age,\n"
        					+ " e.is_user_info_updated KYC\n"
        					+ "FROM employee e \n"
        					+ "left join job_role jr on e.job_role_id = jr.job_role_id\n"
        					+ "left join department d on jr.dept_id = d.dept_id\n"
        					+ "left join employee mg on e.manager_id = mg.emp_id \n"
        					+ "left join employee_team_mapping etm on e.emp_id = etm.emp_id and etm.active != '0'\n"
        					+ "left join employee m on m.emp_id = e.manager_id\n"
        					+ "left join teams t on etm.team_id = t.team_id and t.is_active = 'Y'\n"
        					+ "left join projects p on t.project_id = p.project_id and p.active = 'true'\n"
        					+ "left join clients c on p.client_id = c.client_id\n"
        					+ "WHERE 1=1 and e.employmentstatus != 'InActive' \n"
        					+ " and (\n"
        					+ "     (:employee_type = 'apmosys_product' and e.is_apmosys_product = 'true')\n"
        					+ "     or\n"
        					+ "     (:employee_type = 'apprentice' and e.is_apprenticeship = 'true')\n"
        					+ "     or\n"
        					+ "     (:employee_type = 'consultant' and e.is_consultant = 'true')\n"
        					+ "     or\n"
        					+ "     (:employee_type = 'regular' and ((e.is_consultant = 'false' AND e.is_apprenticeship = 'false') OR (COALESCE(e.is_consultant, '') = '' AND COALESCE(e.is_apprenticeship, '') = '')))\n"
        					+ " )\n"
        					+ " and (TIMESTAMPDIFF(MONTH, e.date_of_joining, CURRENT_DATE())/12 > :lower_value AND TIMESTAMPDIFF(MONTH, e.date_of_joining, CURRENT_DATE())/12 <= :upper_value)\n"
        					+ " and e.emp_id NOT BETWEEN 1 AND 6\n"
        					+ "GROUP BY e.emp_id\n"
        					+ ";")
        					List<Object[]> findEmployeesExperience(
        					    @Param("employee_type") String employeeType,
        					    @Param("lower_value") Long lowerValue,
        					    @Param("upper_value") Long upperValue
        					);
        			
//        			@Query(nativeQuery = true, value = "SELECT distinct \n"
//        					+ "	CONCAT('A-', e.employeement_id) as EMPLOYEEMENT_ID,\n"
//        					+ "    CASE WHEN e.is_apprenticeship = 'true' THEN 'Apprentice'\n"
//        					+ "		 WHEN ((e.is_consultant = 'false' AND e.is_apprenticeship = 'false') OR (COALESCE(e.is_consultant, '') = '' AND COALESCE(e.is_apprenticeship, '') = '')) THEN 'Regular'\n"
//        					+ "         WHEN e.is_consultant = 'true' THEN 'Consultant' \n"
//        					+ "	END AS EMPLOYMENT_TYPE,\n"
//        					+ "    e.name NAME,\n"
//        					+ "    e.experience EXPERIENCE,\n"
//        					+ "    d.name DEPARTMENT_NAME,\n"
//        					+ "    e.email EMAIL_ID,\n"
//        					+ "    m.name MANAGER_NAME,\n"
//        					+ "    e.billable BILLABLE,\n"
//        					+ "    e.billable_type BILLABLE_TYPE,\n"
//        					+ "    GROUP_CONCAT(DISTINCT p.project_name SEPARATOR ', ') AS PROJECT_NAMES,\n"
//        					+ "    GROUP_CONCAT(DISTINCT c.client_name SEPARATOR ', ') AS CLIENT_NAMES,\n"
//        					+ "    e.date_of_joining DATE_OF_JOINING,\n"
//        					+ "    e.mobile_no MOBILE_NO,\n"
//        					+ "	e.employmentstatus STATUS,\n"
//        					+ "	e.total_experience TOTAL_EXPERIENCE,\n"
//        					+ "    e.gender GENDER,\n"
//        					+ "    e.work_location WORK_LOCATION,\n"
//        					+ "    TIMESTAMPDIFF(YEAR, e.date_of_birth, CURRENT_DATE()) age,\n"
//        					+ "    e.is_user_info_updated KYC,\n"
//        					+ "    m.emp_id MANAGER_ID,\n"
//        					+ "    e.emp_id EMP_ID\n"
//        					+ "FROM employee e \n"
//        					+ "left join job_role jr on e.job_role_id = jr.job_role_id\n"
//        					+ "left join department d on jr.dept_id = d.dept_id\n"
//        					+ "left join employee mg on e.manager_id = mg.emp_id \n"
//        					+ "left join employee_team_mapping etm on e.emp_id = etm.emp_id and etm.active != '0'\n"
//        					+ "left join employee m on m.emp_id = e.manager_id\n"
//        					+ "left join teams t on etm.team_id = t.team_id and t.is_active = 'Y'\n"
//        					+ "left join projects p on t.project_id = p.project_id and p.active = 'true'\n"
//        					+ "left join clients c on p.client_id = c.client_id\n"
//        					+ "WHERE 1=1 and e.employmentstatus != 'InActive' \n"
//        					+ "	and (\n"
//        					+ "			(:employ_type = 'apprentice' and e. is_apprenticeship= 'true')\n"
//        					+ "					or\n"
//        					+ "			(:employ_type = 'consultant' and e.is_consultant = 'true')\n"
//        					+ "					or\n"
//        					+ "			(:employ_type = 'regular' and ((e.is_consultant = 'false' AND e.is_apprenticeship = 'false') OR (COALESCE(e.is_consultant, '') = '' AND COALESCE(e.is_apprenticeship, '') = '')))\n"
//        					+ "		)\n"
//        					+ "	and (d.dept_id in (:dept_id))\n"
//        					+ "	and e.emp_id NOT BETWEEN 1 AND 6\n"
//        					+ "GROUP BY e.emp_id\n"
//        					+ ";")
//        			List<Object[]> findDepartmentwiseEmployees(
//            			    @Param("dept_id") List<Long> deptIds,
//            			    @Param("employ_type") String employeeType
//            			);
        			
        			@Query(nativeQuery = true, value = "SELECT distinct \n" + 
        				    " CASE WHEN e.is_apmosys_product = 'true' THEN CONCAT('AP-', e.employeement_id)\n" +
        				    "      ELSE CONCAT('A-', e.employeement_id) \n" +
        				    " END as EMPLOYEEMENT_ID,\n" + 
        				    " CASE WHEN e.is_apmosys_product = 'true' THEN 'Apmosys Product'\n" +
        				    "      WHEN e.is_apprenticeship = 'true' THEN 'Apprentice'\n" + 
        				    "      WHEN ((e.is_consultant = 'false' AND e.is_apprenticeship = 'false') OR (COALESCE(e.is_consultant, '') = '' AND COALESCE(e.is_apprenticeship, '') = '')) THEN 'Regular'\n" + 
        				    "      WHEN e.is_consultant = 'true' THEN 'Consultant' \n" + 
        				    " END AS EMPLOYMENT_TYPE,\n" + 
        				    " e.name NAME,\n" + 
        				    " e.experience EXPERIENCE,\n" + 
        				    " d.name DEPARTMENT_NAME,\n" + 
        				    " e.email EMAIL_ID,\n" + 
        				    " m.name MANAGER_NAME,\n" + 
        				    " e.billable BILLABLE,\n" + 
        				    " e.billable_type BILLABLE_TYPE,\n" + 
        				    " GROUP_CONCAT(DISTINCT p.project_name SEPARATOR ', ') AS PROJECT_NAMES,\n" + 
        				    " GROUP_CONCAT(DISTINCT c.client_name SEPARATOR ', ') AS CLIENT_NAMES,\n" + 
        				    " e.date_of_joining DATE_OF_JOINING,\n" + 
        				    " e.mobile_no MOBILE_NO,\n" + 
        				    " e.employmentstatus STATUS,\n" + 
        				    " e.total_experience TOTAL_EXPERIENCE,\n" + 
        				    " e.gender GENDER,\n" + 
        				    " e.work_location WORK_LOCATION,\n" + 
        				    " TIMESTAMPDIFF(YEAR, e.date_of_birth, CURRENT_DATE()) age,\n" + 
        				    " e.is_user_info_updated KYC,\n" + 
        				    " m.emp_id MANAGER_ID,\n" + 
        				    " e.emp_id EMP_ID\n" +
        				    "FROM employee e \n" + 
        				    "left join job_role jr on e.job_role_id = jr.job_role_id\n" +
        				    "left join department d on jr.dept_id = d.dept_id\n" +
        				    "left join employee mg on e.manager_id = mg.emp_id \n" +
        				    "left join employee_team_mapping etm on e.emp_id = etm.emp_id and etm.active != '0'\n" +
        				    "left join employee m on m.emp_id = e.manager_id\n" +
        				    "left join teams t on etm.team_id = t.team_id and t.is_active = 'Y'\n" +
        				    "left join projects p on t.project_id = p.project_id and p.active = 'true'\n" +
        				    "left join clients c on p.client_id = c.client_id\n" +
        				    "WHERE 1=1 and e.employmentstatus != 'InActive' \n" + 
        				    " and (\n" + 
        				    "     (:employ_type = 'apmosys_product' and e.is_apmosys_product = 'true')\n" +
        				    "     or\n" +
        				    "     (:employ_type = 'apprentice' and e.is_apprenticeship = 'true')\n" + 
        				    "     or\n" + 
        				    "     (:employ_type = 'consultant' and e.is_consultant = 'true')\n" + 
        				    "     or\n" + 
        				    "     (:employ_type = 'regular' and ((e.is_consultant = 'false' AND e.is_apprenticeship = 'false') OR (COALESCE(e.is_consultant, '') = '' AND COALESCE(e.is_apprenticeship, '') = '')))\n" + 
        				    " )\n" + 
        				    " and (d.dept_id in (:dept_id))\n" + 
        				    " and e.emp_id NOT BETWEEN 1 AND 6\n" +
        				    "GROUP BY e.emp_id\n" + 
        				    ";")
        				List<Object[]> findDepartmentwiseEmployees(
        				    @Param("dept_id") List<Long> deptIds, 
        				    @Param("employ_type") String employeeType
        				);
        			
        			
//        			@Query(nativeQuery = true, value ="SELECT distinct \n"
//        					+ "	CONCAT('A-', e.employeement_id) as EMPLOYEEMENT_ID,\n"
//        					+ "    CASE WHEN e.is_apprenticeship = 'true' THEN 'Apprentice'\n"
//        					+ "		 WHEN ((e.is_consultant = 'false' AND e.is_apprenticeship = 'false') OR (COALESCE(e.is_consultant, '') = '' AND COALESCE(e.is_apprenticeship, '') = '')) THEN 'Regular'\n"
//        					+ "         WHEN e.is_consultant = 'true' THEN 'Consultant' \n"
//        					+ "	END AS EMPLOYMENT_TYPE,\n"
//        					+ "    e.name NAME,\n"
//        					+ "    e.experience EXPERIENCE,\n"
//        					+ "    d.name DEPARTMENT_NAME,\n"
//        					+ "    e.email EMAIL_ID,\n"
//        					+ "    m.name MANAGER_NAME,\n"
//        					+ "    e.billable BILLABLE,\n"
//        					+ "    e.billable_type BILLABLE_TYPE,\n"
//        					+ "    GROUP_CONCAT(DISTINCT p.project_name SEPARATOR ', ') AS PROJECT_NAMES,\n"
//        					+ "    GROUP_CONCAT(DISTINCT c.client_name SEPARATOR ', ') AS CLIENT_NAMES,\n"
//        					+ "    e.date_of_joining DATE_OF_JOINING,\n"
//        					+ "    e.mobile_no MOBILE_NO,\n"
//        					+ "	e.employmentstatus STATUS,\n"
//        					+ "	e.total_experience TOTAL_EXPERIENCE,\n"
//        					+ "    e.gender GENDER,\n"
//        					+ "    e.work_location WORK_LOCATION,\n"
//        					+ "    TIMESTAMPDIFF(YEAR, e.date_of_birth, CURRENT_DATE()) age,\n"
//        					+ "    e.is_user_info_updated KYC,\n"
//        					+ "    m.emp_id MANAGER_ID,\n"
//        					+ "    e.emp_id EMP_ID\n"
//        					+ "FROM employee e \n"
//        					+ "left join job_role jr on e.job_role_id = jr.job_role_id\n"
//        					+ "left join department d on jr.dept_id = d.dept_id\n"
//        					+ "left join employee mg on e.manager_id = mg.emp_id \n"
//        					+ "left join employee_team_mapping etm on e.emp_id = etm.emp_id and etm.active != '0'\n"
//        					+ "left join employee m on m.emp_id = e.manager_id\n"
//        					+ "left join teams t on etm.team_id = t.team_id and t.is_active = 'Y'\n"
//        					+ "left join projects p on t.project_id = p.project_id and p.active = 'true'\n"
//        					+ "left join clients c on p.client_id = c.client_id\n"
//        					+ "WHERE 1=1 and e.employmentstatus != 'InActive' \n"
//        					+ "		and (d.dept_id in (:dept_id))\n"
//        					+ "        and (\n"
//        					+ "				(:is_user_info_updated = 'true' and e.is_user_info_updated = 'true')\n"
//        					+ "							or\n"
//        					+ "				(:is_user_info_updated != 'true' and e.is_user_info_updated != 'true')\n"
//        					+ "			)\n"
//        					+ "	and e.emp_id NOT BETWEEN 1 AND 6 \n"
//        					+ "GROUP BY e.emp_id\n"
//        					)
//        			List<Object[]> findEmployeesByDepartmentAndKyc(
//        				    @Param("dept_id") List<Long> deptId,
//        				    @Param("is_user_info_updated") String isUserInfoUpdated
//        				);
        			
        			@Query(nativeQuery = true, value ="SELECT distinct \n"
        					+ " CASE \n"
        					+ "  WHEN e.is_apmosys_product = 'true' THEN CONCAT('AP-', e.employeement_id)\n"
        					+ "  ELSE CONCAT('A-', e.employeement_id)\n"
        					+ " END as EMP_ID,\n"
        					+ " CASE \n"
        					+ "  WHEN e.is_apmosys_product = 'true' THEN 'Apmosys Product'\n"
        					+ "  WHEN e.is_apprenticeship = 'true' THEN 'Apprentice'\n"
        					+ "  WHEN ((e.is_consultant = 'false' AND e.is_apprenticeship = 'false') OR (COALESCE(e.is_consultant, '') = '' AND COALESCE(e.is_apprenticeship, '') = '')) THEN 'Regular'\n"
        					+ "  WHEN e.is_consultant = 'true' THEN 'Consultant' \n"
        					+ " END AS EMPLOYMENT_TYPE,\n"
        					+ " e.name NAME,\n"
        					+ " e.experience EXPERIENCE,\n"
        					+ " d.name DEPARTMENT_NAME,\n"
        					+ " e.email EMAIL_ID,\n"
        					+ " m.name MANAGER_NAME,\n"
        					+ " e.billable BILLABLE,\n"
        					+ " e.billable_type BILLABLE_TYPE,\n"
        					+ " GROUP_CONCAT(DISTINCT p.project_name SEPARATOR ', ') AS PROJECT_NAMES,\n"
        					+ " GROUP_CONCAT(DISTINCT c.client_name SEPARATOR ', ') AS CLIENT_NAMES,\n"
        					+ " e.date_of_joining DATE_OF_JOINING,\n"
        					+ " e.mobile_no MOBILE_NO,\n"
        					+ " e.employmentstatus STATUS,\n"
        					+ " e.total_experience TOTAL_EXPERIENCE,\n"
        					+ " e.gender GENDER,\n"
        					+ " e.work_location WORK_LOCATION,\n"
        					+ " TIMESTAMPDIFF(YEAR, e.date_of_birth, CURRENT_DATE()) age,\n"
        					+ " e.is_user_info_updated KYC\n"
        					+ "FROM employee e \n"
        					+ "left join job_role jr on e.job_role_id = jr.job_role_id\n"
        					+ "left join department d on jr.dept_id = d.dept_id\n"
        					+ "left join employee mg on e.manager_id = mg.emp_id \n"
        					+ "left join employee_team_mapping etm on e.emp_id = etm.emp_id and etm.active != '0'\n"
        					+ "left join employee m on m.emp_id = e.manager_id\n"
        					+ "left join teams t on etm.team_id = t.team_id and t.is_active = 'Y'\n"
        					+ "left join projects p on t.project_id = p.project_id and p.active = 'true'\n"
        					+ "left join clients c on p.client_id = c.client_id\n"
        					+ "WHERE 1=1 and e.employmentstatus != 'InActive' \n"
        					+ " and (d.dept_id in (:dept_id))\n"
        					+ " and (\n"
        					+ "  (:is_user_info_updated = 'true' and e.is_user_info_updated = 'true')\n"
        					+ "  or\n"
        					+ "  (:is_user_info_updated != 'true' and e.is_user_info_updated != 'true')\n"
        					+ " )\n"
        					+ " and e.emp_id NOT BETWEEN 1 AND 6 \n"
        					+ "GROUP BY e.emp_id\n"
        					)
        					List<Object[]> findEmployeesByDepartmentAndKyc(
        					    @Param("dept_id") List<Long> deptId,
        					    @Param("is_user_info_updated") String isUserInfoUpdated
        					);
        			
//        			@Query(value = 
//        	    	        "SELECT DISTINCT " +
//        	    	        "CONCAT('A-', e.employeement_id) AS EMP_ID, " +
//        	    	        "CASE " +
//        	    	        "   WHEN e.is_apprenticeship = 'true' THEN 'Apprentice' " +
//        	    	        "   WHEN ((e.is_consultant = 'false' AND e.is_apprenticeship = 'false') " +
//        	    	        "      OR (COALESCE(e.is_consultant, '') = '' AND COALESCE(e.is_apprenticeship, '') = '')) THEN 'Regular' " +
//        	    	        "   WHEN e.is_consultant = 'true' THEN 'Consultant' " +
//        	    	        "END AS EMPLOYMENT_TYPE, " +
//        	    	        "e.name AS NAME, " +
//        	    	        "e.experience AS EXPERIENCE, " +
//        	    	        "d.name AS DEPARTMENT_NAME, " +
//        	    	        "e.email AS EMAIL_ID, " +
//        	    	        "m.name AS MANAGER_NAME, " +
//        	    	        "e.billable AS BILLABLE, " +
//        	    	        "e.billable_type AS BILLABLE_TYPE, " +
//        	    	        "GROUP_CONCAT(DISTINCT p.project_name SEPARATOR ', ') AS PROJECT_NAMES, " +
//        	    	        "GROUP_CONCAT(DISTINCT c.client_name SEPARATOR ', ') AS CLIENT_NAMES, " +
//        	    	        "e.date_of_joining AS DATE_OF_JOINING, " +
//        	    	        "e.mobile_no AS MOBILE_NO, " +
//        	    	        "e.employmentstatus AS STATUS, " +
//        	    	        "e.total_experience AS TOTAL_EXPERIENCE, " +
//        	    	        "e.gender AS GENDER, " +
//        	    	        "e.work_location AS WORK_LOCATION, " +
//        	    	        "TIMESTAMPDIFF(YEAR, e.date_of_birth, CURRENT_DATE()) AS AGE, " +
//        	    	        "e.is_user_info_updated AS KYC " +
//        	    	        "FROM employee e " +
//        	    	        "LEFT JOIN job_role jr ON e.job_role_id = jr.job_role_id " +
//        	    	        "LEFT JOIN department d ON jr.dept_id = d.dept_id " +
//        	    	        "LEFT JOIN employee mg ON e.manager_id = mg.emp_id " +
//        	    	        "LEFT JOIN employee_team_mapping etm ON e.emp_id = etm.emp_id AND etm.active != '0' " +
//        	    	        "LEFT JOIN employee m ON m.emp_id = e.manager_id " +
//        	    	        "LEFT JOIN teams t ON etm.team_id = t.team_id AND t.is_active = 'Y' " +
//        	    	        "LEFT JOIN projects p ON t.project_id = p.project_id AND p.active = 'true' " +
//        	    	        "LEFT JOIN clients c ON p.client_id = c.client_id " +
//        	    	        "WHERE e.emp_id NOT BETWEEN 1 AND 6 " +
//        	    	        "AND ( " +
//        	    	        "   (:type = 'apprenticeship' AND e.is_apprenticeship = 'true' AND MONTHNAME(e.date_of_joining) = :month_name AND YEAR(e.date_of_joining) = :YEAR_VALUE) " +
//        	    	        "   OR (:type = 'consultant' AND e.is_consultant = 'true' AND MONTHNAME(e.date_of_joining) = :month_name AND YEAR(e.date_of_joining) = :YEAR_VALUE) " +
//        	    	        "   OR (:type = 'regular' AND ((e.is_consultant = 'false' AND e.is_apprenticeship = 'false') " +
//        	    	        "       OR (COALESCE(e.is_consultant, '') = '' AND COALESCE(e.is_apprenticeship, '') = '')) " +
//        	    	        "       AND MONTHNAME(e.date_of_joining) = :month_name AND YEAR(e.date_of_joining) = :YEAR_VALUE) " +
//        	    	        "   OR (:type = 'resign' AND e.date_of_resign IS NOT NULL AND MONTHNAME(e.date_of_resign) = :month_name AND YEAR(e.date_of_resign) = :YEAR_VALUE) " +
//        	    	        ") " +
//        	    	        "GROUP BY e.emp_id", 
//        	    	       nativeQuery = true)
//        	    	List<Object[]> getJoinVsResignEmployeeDetails(
//        	    	    @Param("type") String type,
//        	    	    @Param("month_name") String monthName,
//        	    	    @Param("YEAR_VALUE") Long yearValue
//        	    	);
        			
        			@Query(value = 
        					"SELECT DISTINCT " +
        							"CASE " +
        							"   WHEN e.is_apmosys_product = 'true' OR e.email LIKE '%ap2l.ai%' THEN CONCAT('AP-', e.employeement_id) " +
        							"   ELSE CONCAT('A-', e.employeement_id) " +
        							"END AS EMP_ID, " +
        							"CASE " +
        							"   WHEN e.is_apmosys_product = 'true' OR e.email LIKE '%ap2l.ai%' THEN 'Apmosys Product' " +
        							"   WHEN e.is_apprenticeship = 'true' THEN 'Apprentice' " +
        							"   WHEN ((e.is_consultant = 'false' AND e.is_apprenticeship = 'false') " +
        							"      OR (COALESCE(e.is_consultant, '') = '' AND COALESCE(e.is_apprenticeship, '') = '')) THEN 'Regular' " +
        							"   WHEN e.is_consultant = 'true' THEN 'Consultant' " +
        							"END AS EMPLOYMENT_TYPE, " +
        							"e.name AS NAME, " +
        							"e.experience AS EXPERIENCE, " +
        							"d.name AS DEPARTMENT_NAME, " +
        							"e.email AS EMAIL_ID, " +
        							"m.name AS MANAGER_NAME, " +
        							"e.billable AS BILLABLE, " +
        							"e.billable_type AS BILLABLE_TYPE, " +
        							"GROUP_CONCAT(DISTINCT p.project_name SEPARATOR ', ') AS PROJECT_NAMES, " +
        							"GROUP_CONCAT(DISTINCT c.client_name SEPARATOR ', ') AS CLIENT_NAMES, " +
        							"e.date_of_joining AS DATE_OF_JOINING, " +
        							"e.mobile_no AS MOBILE_NO, " +
        							"e.employmentstatus AS STATUS, " +
        							"e.total_experience AS TOTAL_EXPERIENCE, " +
        							"e.gender AS GENDER, " +
        							"e.work_location AS WORK_LOCATION, " +
        							"TIMESTAMPDIFF(YEAR, e.date_of_birth, CURRENT_DATE()) AS AGE, " +
        							"e.is_user_info_updated AS KYC " +
        							"FROM employee e " +
        							"LEFT JOIN job_role jr ON e.job_role_id = jr.job_role_id " +
        							"LEFT JOIN department d ON jr.dept_id = d.dept_id " +
        							"LEFT JOIN employee mg ON e.manager_id = mg.emp_id " +
        							"LEFT JOIN employee_team_mapping etm ON e.emp_id = etm.emp_id AND etm.active != '0' " +
        							"LEFT JOIN employee m ON m.emp_id = e.manager_id " +
        							"LEFT JOIN teams t ON etm.team_id = t.team_id AND t.is_active = 'Y' " +
        							"LEFT JOIN projects p ON t.project_id = p.project_id AND p.active = 'true' " +
        							"LEFT JOIN clients c ON p.client_id = c.client_id " +
        							"WHERE e.emp_id NOT BETWEEN 1 AND 6 " +
        							"AND ( " +
        							"   (:type = 'apmosysproduct' AND (e.is_apmosys_product = 'true' OR e.email LIKE '%ap2l.ai%') AND MONTHNAME(e.date_of_joining) = :month_name AND YEAR(e.date_of_joining) = :YEAR_VALUE) " +
        							"   OR (:type = 'apprenticeship' AND e.is_apprenticeship = 'true' AND MONTHNAME(e.date_of_joining) = :month_name AND YEAR(e.date_of_joining) = :YEAR_VALUE) " +
        							"   OR (:type = 'consultant' AND e.is_consultant = 'true' AND MONTHNAME(e.date_of_joining) = :month_name AND YEAR(e.date_of_joining) = :YEAR_VALUE) " +
        							"   OR (:type = 'regular' AND ((e.is_consultant = 'false' AND e.is_apprenticeship = 'false') " +
        							"       OR (COALESCE(e.is_consultant, '') = '' AND COALESCE(e.is_apprenticeship, '') = '')) " +
        							"       AND MONTHNAME(e.date_of_joining) = :month_name AND YEAR(e.date_of_joining) = :YEAR_VALUE) " +
        							"   OR (:type = 'resign' AND e.date_of_resign IS NOT NULL AND MONTHNAME(e.date_of_resign) = :month_name AND YEAR(e.date_of_resign) = :YEAR_VALUE) " +
        							") " +
        							"GROUP BY e.emp_id", 
        	    	       nativeQuery = true)
        	    	List<Object[]> getJoinVsResignEmployeeDetails(
        	    	    @Param("type") String type,
        	    	    @Param("month_name") String monthName,
        	    	    @Param("YEAR_VALUE") Long yearValue
        	    	);
        	    	
        	    	@Query(nativeQuery = true, value = "SELECT type_of_leave,\n"
        	    			+ "    ld.leave_date,  \n"
        	    			+ "    SUM(ld.no_of_days) AS total_leave_days\n"
        	    			+ "FROM (\n"
        	    			+ "    SELECT \n"
        	    			+ "        DATE_ADD(CURDATE(), INTERVAL -n DAY) AS leave_date,\n"
        	    			+ "        CASE WHEN leave_type_master_id = 1 then 'Paid Leave'\n"
        	    			+ "			 WHEN leave_type_master_id = 2 then 'Casual Leave'\n"
        	    			+ "             WHEN leave_type_master_id = 3 then 'Leave Without Pay'\n"
        	    			+ "             WHEN leave_type_master_id = 4 then 'Compensatory Off'\n"
        	    			+ "             WHEN leave_type_master_id = 5 then 'Maternity Leave'\n"
        	    			+ "		END AS type_of_leave,\n"
        	    			+ "        CASE \n"
        	    			+ "            WHEN WEEKDAY(DATE_ADD(CURDATE(), INTERVAL -n DAY)) = 6 -- Exclude Sundays\n"
        	    			+ "                 OR (DAYOFWEEK(DATE_ADD(CURDATE(), INTERVAL -n DAY)) = 7 \n"
        	    			+ "                     AND (DAY(DATE_ADD(CURDATE(), INTERVAL -n DAY)) BETWEEN 8 AND 14 \n"
        	    			+ "                          OR DAY(DATE_ADD(CURDATE(), INTERVAL -n DAY)) BETWEEN 22 AND 28)) -- Exclude 2nd & 4th Saturdays\n"
        	    			+ "            THEN 0\n"
        	    			+ "            WHEN from_date <= DATE_ADD(CURDATE(), INTERVAL -n DAY) \n"
        	    			+ "                 AND to_date >= DATE_ADD(CURDATE(), INTERVAL -n DAY) \n"
        	    			+ "            THEN LEAST(1, GREATEST(1, abs(8 - DATEDIFF(CURDATE(), from_date)))) -- Ensuring a max of 8 days counted per employee\n"
        	    			+ "            ELSE 0\n"
        	    			+ "        END AS no_of_days\n"
        	    			+ "    FROM employee_leave el, \n"
        	    			+ "    (SELECT 0 AS n UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 \n"
        	    			+ "     UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8) AS days_range\n"
        	    			+ "    WHERE from_date <= CURDATE()\n"
        	    			+ "    AND el.leave_status_id = 2\n"
        	    			+ ") AS ld  -- Giving an alias for the subquery (ld)\n"
        	    			+ "WHERE ld.no_of_days > 0\n"
        	    			+ "GROUP BY type_of_leave, ld.leave_date\n"
        	    			+ "ORDER BY ld.leave_date;")
        	    	List<Object[]> getLeaveTrend();
        	    	
        	    	
        	    	@Query(nativeQuery = true,value = "SELECT emp_id, department, employee_name, from_date, to_date, status, from_date_day_type, to_date_day_type,\n"
        	    			+ "employment_type, manager_id, type_of_leave, ld.leave_date \n"
        	    			+ "FROM (\n"
        	    			+ "    SELECT CONCAT('A-', e.employeement_id) as emp_id, d.name department, e.name employee_name, el.from_date from_date, el.to_date to_date, \n"
        	    			+ "    ls.status status,el.from_date_day_type from_date_day_type, el.to_date_day_type to_date_day_type, \n"
        	    			+ "    CASE WHEN e.is_apprenticeship = 'true' THEN 'Apprentice'\n"
        	    			+ "		 WHEN ((e.is_consultant = 'false' AND e.is_apprenticeship = 'false') OR (COALESCE(e.is_consultant, '') = '' AND COALESCE(e.is_apprenticeship, '') = '')) THEN 'Regular'\n"
        	    			+ "         WHEN e.is_consultant = 'true' THEN 'Consultant' \n"
        	    			+ "	END AS employment_type,\n"
        	    			+ "		e.manager_id manager_id, \n"
        	    			+ "        DATE_ADD(CURDATE(), INTERVAL -n DAY) AS leave_date,\n"
        	    			+ "        CASE WHEN leave_type_master_id = 1 then 'Paid Leave'\n"
        	    			+ "			 WHEN leave_type_master_id = 2 then 'Casual Leave'\n"
        	    			+ "             WHEN leave_type_master_id = 3 then 'Leave Without Pay'\n"
        	    			+ "             WHEN leave_type_master_id = 4 then 'Compensatory Off'\n"
        	    			+ "             WHEN leave_type_master_id = 5 then 'Maternity Leave'\n"
        	    			+ "		END AS type_of_leave,\n"
        	    			+ "        CASE \n"
        	    			+ "            WHEN WEEKDAY(DATE_ADD(CURDATE(), INTERVAL -n DAY)) = 6 -- Exclude Sundays\n"
        	    			+ "                 OR (DAYOFWEEK(DATE_ADD(CURDATE(), INTERVAL -n DAY)) = 7 \n"
        	    			+ "                     AND (DAY(DATE_ADD(CURDATE(), INTERVAL -n DAY)) BETWEEN 8 AND 14 \n"
        	    			+ "                          OR DAY(DATE_ADD(CURDATE(), INTERVAL -n DAY)) BETWEEN 22 AND 28)) -- Exclude 2nd & 4th Saturdays\n"
        	    			+ "            THEN 0\n"
        	    			+ "            WHEN from_date <= DATE_ADD(CURDATE(), INTERVAL -n DAY) \n"
        	    			+ "                 AND to_date >= DATE_ADD(CURDATE(), INTERVAL -n DAY) \n"
        	    			+ "            THEN LEAST(1, GREATEST(1, abs(8 - DATEDIFF(CURDATE(), from_date)))) -- Ensuring a max of 8 days counted per employee\n"
        	    			+ "            ELSE 0\n"
        	    			+ "        END AS no_of_days\n"
        	    			+ "    FROM employee_leave el\n"
        	    			+ "    INNER JOIN employee e ON e.emp_id = el.emp_id \n"
        	    			+ "	INNER JOIN job_role jr ON jr.job_role_id = e.job_role_id \n"
        	    			+ "	INNER JOIN department d ON d.dept_id = jr.dept_id \n"
        	    			+ "    INNER JOIN leave_status ls ON ls.leave_status_id = el.leave_status_id \n"
        	    			+ "    , (SELECT 0 AS n UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 \n"
        	    			+ "     UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8) AS days_range\n"
        	    			+ "    WHERE from_date <= CURDATE()\n"
        	    			+ "    AND el.leave_status_id = 2\n"
        	    			+ ") AS ld  -- Giving an alias for the subquery (ld)\n"
        	    			+ "WHERE ld.no_of_days > 0\n"
        	    			+ "and ld.leave_date = :fetch_date\n"
        	    			+ "and ld.type_of_leave = :type_of_leave")
        	    	List<Object[]> leaveTrendAnalysis(@Param("fetch_date") LocalDate fetch_date,
            	    	    @Param("type_of_leave") String type_of_leave
            	    	    );
//        	    	
//        	    	@Query(nativeQuery = true , value = "select distinct cl.client_location, count(distinct e.emp_id) from employee e\n"
//        	    			+ "INNER JOIN employee_team_mapping etm on etm.emp_id = e.emp_id\n"
//        	    			+ "INNER JOIN employee_timesheets et ON et.emp_id = etm.emp_id\n"
//        	    			+ "INNER JOIN employee_timesheet_activities_mapping etam ON etam.timesheet_id = et.timesheet_id \n"
//        	    			+ "INNER JOIN activities a ON a.activity_id = etam.activity_id\n"
//        	    			+ "INNER JOIN teams t on etm.team_id = t.team_id and etm.team_id = a.team_id\n"
//        	    			+ "INNER JOIN projects p on p.project_id = t.project_id \n"
//        	    			+ "INNER JOIN client_locations cl on cl.client_location_id = etam.client_location_id\n"
//        	    			+ "where e.employmentstatus != 'InActive' and etm.active != 0\n"
//        	    			+ "and t.is_active = 'Y' and p.active = 'true'\n"
//        	    			+ "and e.emp_id not between 1 and 6\n"
//        	    			+ "group by cl.client_location\n"
//        	    			+ ";")
//        	    	List<Object[]> getWorkLocation();
        	    	
        	    	
        	    	//Added by Dibya
        	    	
//        	    	@Query("SELECT DISTINCT cl.clientLocation, COUNT(DISTINCT e.empId) " +
//        	    		       "FROM Employee e " +
//        	    		       "JOIN EmployeeTeamMap etm ON etm.empId = e.empId " +
//        	    		       "JOIN Timesheet et ON et.empId = etm.empId " +
//        	    		       "JOIN TimesheetActivityMap etam ON etam.timesheetId = et.timesheetId " +
//        	    		       "JOIN Activity a ON a.activityId = etam.activityId " +
//        	    		       "JOIN Team t ON etm.teamId = t.teamId AND a.teamId = t.teamId " +
//        	    		       "JOIN ClientLocation cl ON cl.clientLocationId = etam.clientLocationId " +
//        	    		       "WHERE e.employmentstatus <> 'InActive' " +
//        	    		       "AND etm.active <> 0 " +
//        	    		       "AND t.isActive = 'Y' " +
//        	    		       "AND e.empId NOT BETWEEN 1 AND 6 " +
//        	    		       "GROUP BY cl.clientLocation")
        	    		    	    		
        	    	
//        	    	@Query(nativeQuery = true, value = "SELECT \n"
//        	    			+ "	CONCAT('A-', REPLACE(e.employeement_id, '-', '')) as EMP_ID,\n"
//        	    			+ "    CASE WHEN e.is_apprenticeship = 'true' THEN 'Apprentice'\n"
//        	    			+ "		 WHEN ((e.is_consultant = 'false' AND e.is_apprenticeship = 'false') OR (COALESCE(e.is_consultant, '') = '' AND COALESCE(e.is_apprenticeship, '') = '')) THEN 'Regular'\n"
//        	    			+ "         WHEN e.is_consultant = 'true' THEN 'Consultant' \n"
//        	    			+ "	END AS EMPLOYMENT_TYPE,\n"
//        	    			+ "    e.name NAME,\n"
//        	    			+ "    e.experience EXPERIENCE, \n"
//        	    			+ "    d.name DEPARTMENT_NAME, \n"
//        	    			+ "    e.email EMAIL_ID, \n"
//        	    			+ "    m.name MANAGER_NAME, \n"
//        	    			+ "    e.billable BILLABLE, \n"
//        	    	  		+ "    e.billable_type BILLABLE_TYPE, \n"
//        	    			+ "    GROUP_CONCAT(DISTINCT p.project_name SEPARATOR ', ') AS PROJECT_NAMES,\n"
//        	    			+ "    GROUP_CONCAT(DISTINCT c.client_name SEPARATOR ', ') AS CLIENT_NAMES,\n"
//        	    			+ "    e.date_of_joining DATE_OF_JOINING,\n"
//        	    			+ "    e.mobile_no MOBILE_NO,\n"
//        	    			+ "	e.employmentstatus STATUS,\n"
//        	    			+ "	e.total_experience TOTAL_EXPERIENCE,\n"
//        	    			+ "    e.gender GENDER,\n"
//        	    			+ "    e.work_location WORK_LOCATION,\n"
//        	    			+ "    cl.client_location,\n"
//        	    			+ "    TIMESTAMPDIFF(YEAR, e.date_of_birth, CURRENT_DATE()) age,\n"
//        	    			+ "    e.is_user_info_updated KYC\n"
//        	    			+ "from employee e\n"
//        	    			+ "INNER JOIN job_role jr on e.job_role_id = jr.job_role_id\n"
//        	    			+ "INNER JOIN department d on jr.dept_id = d.dept_id\n"
//        	    			+ "INNER JOIN employee_team_mapping etm on etm.emp_id = e.emp_id\n"
//        	    			+ "INNER JOIN employee_timesheets et ON et.emp_id = etm.emp_id\n"
//        	    			+ "INNER JOIN employee_timesheet_activities_mapping etam ON etam.timesheet_id = et.timesheet_id \n"
//        	    			+ "INNER JOIN activities a ON a.activity_id = etam.activity_id\n"
//        	    			+ "INNER JOIN teams t on etm.team_id = t.team_id and etm.team_id = a.team_id\n"
//        	    			+ "INNER JOIN projects p on p.project_id = t.project_id \n"
//        	    			+ "INNER JOIN clients c on c.client_id = p.client_id\n"
//        	    			+ "INNER JOIN employee m on m.emp_id = e.manager_id\n"
//        	    			+ "INNER JOIN client_locations cl on cl.client_location_id = etam.client_location_id\n"
//        	    			+ "where e.employmentstatus != 'InActive' and etm.active != 0\n"
//        	    			+ "and t.is_active = 'Y' and p.active = 'true'\n"
//        	    			+ "and e.emp_id not between 1 and 6 \n"
//        	    			+ "and cl.client_location = :work_location\n"
//        	    			+ "group by e.emp_id\n"
//        	    			+ ";")
//        	    	List<Object[]> workLocationSummary(
//            	    	    @Param("work_location") String workLocation
//            	    	    );
        	    		
        	    		@Query(nativeQuery = true, value = "SELECT \n"
            	    			+ "	CASE WHEN e.is_apmosys_product = 'true' OR e.email LIKE '%ap2l.ai%' THEN CONCAT('AP-', REPLACE(e.employeement_id, '-', ''))\n"
            	    			+ "		 ELSE CONCAT('A-', REPLACE(e.employeement_id, '-', ''))\n"
            	    			+ "	END as EMP_ID,\n"
            	    			+ "    CASE WHEN e.is_apmosys_product = 'true' OR e.email LIKE '%ap2l.ai%' THEN 'Apmosys Product'\n"
            	    			+ "		 WHEN e.is_apprenticeship = 'true' THEN 'Apprentice'\n"
            	    			+ "		 WHEN ((e.is_consultant = 'false' AND e.is_apprenticeship = 'false') OR (COALESCE(e.is_consultant, '') = '' AND COALESCE(e.is_apprenticeship, '') = '')) THEN 'Regular'\n"
            	    			+ "         WHEN e.is_consultant = 'true' THEN 'Consultant' \n"
            	    			+ "	END AS EMPLOYMENT_TYPE,\n"
            	    			+ "    e.name NAME,\n"
            	    			+ "    e.experience EXPERIENCE, \n"
            	    			+ "    d.name DEPARTMENT_NAME, \n"
            	    			+ "    e.email EMAIL_ID, \n"
            	    			+ "    m.name MANAGER_NAME, \n"
            	    			+ "    e.billable BILLABLE, \n"
            	    	  		+ "    e.billable_type BILLABLE_TYPE, \n"
            	    			+ "    GROUP_CONCAT(DISTINCT p.project_name SEPARATOR ', ') AS PROJECT_NAMES,\n"
            	    			+ "    GROUP_CONCAT(DISTINCT c.client_name SEPARATOR ', ') AS CLIENT_NAMES,\n"
            	    			+ "    e.date_of_joining DATE_OF_JOINING,\n"
            	    			+ "    e.mobile_no MOBILE_NO,\n"
            	    			+ "	e.employmentstatus STATUS,\n"
            	    			+ "	e.total_experience TOTAL_EXPERIENCE,\n"
            	    			+ "    e.gender GENDER,\n"
            	    			+ "    e.work_location WORK_LOCATION,\n"
            	    			+ "    cl.client_location,\n"
            	    			+ "    TIMESTAMPDIFF(YEAR, e.date_of_birth, CURRENT_DATE()) age,\n"
            	    			+ "    e.is_user_info_updated KYC\n"
            	    			+ "from employee e\n"
            	    			+ "INNER JOIN job_role jr on e.job_role_id = jr.job_role_id\n"
            	    			+ "INNER JOIN department d on jr.dept_id = d.dept_id\n"
            	    			+ "INNER JOIN employee_team_mapping etm on etm.emp_id = e.emp_id\n"
            	    			+ "INNER JOIN employee_timesheets et ON et.emp_id = etm.emp_id\n"
            	    			+ "INNER JOIN employee_timesheet_activities_mapping etam ON etam.timesheet_id = et.timesheet_id \n"
            	    			+ "INNER JOIN activities a ON a.activity_id = etam.activity_id\n"
            	    			+ "INNER JOIN teams t on etm.team_id = t.team_id and etm.team_id = a.team_id\n"
            	    			+ "INNER JOIN projects p on p.project_id = t.project_id \n"
            	    			+ "INNER JOIN clients c on c.client_id = p.client_id\n"
            	    			+ "INNER JOIN employee m on m.emp_id = e.manager_id\n"
            	    			+ "INNER JOIN client_locations cl on cl.client_location_id = etam.client_location_id\n"
            	    			+ "where e.employmentstatus != 'InActive' and etm.active != 0\n"
            	    			+ "and t.is_active = 'Y' and p.active = 'true'\n"
            	    			+ "and e.emp_id not between 1 and 6 \n"
            	    			+ "and cl.client_location = :work_location\n"
            	    			+ "group by e.emp_id\n"
            	    			+ ";")
            	    	List<Object[]> workLocationSummary(
                	    	    @Param("work_location") String workLocation
                	    	    );
            	    	
            	    	
            	    	
          
            	    	
            	    	
            	    	
            	    	@Query("SELECT new com.apmosys.employeeportal.dto.EmployeeDTO(" +
            	    		       "e.empId, " +
            	    		       "e.name, " +
            	    		       "e.email, " +
            	    		       "e.employmentstatus, " +
            	    		       "e.employeementId, " +
            	    		       "FUNCTION('DATE_FORMAT', e.dateOfJoining, '%Y-%m-%d'), " +
            	    		       "d.name, " +               
            	    		       "jr.name, " +              
            	    		       "e2.name, " +  
            	    		       "e.managerId, " + 
            	    		       "FUNCTION('DATE_FORMAT', e.dateOfResign, '%Y-%m-%d'), " +
            	    		       "FUNCTION('DATE_FORMAT', e.dateOfRelieving, '%Y-%m-%d'), " +
            	    		       "e.noticePeriod, " + 
            	    		       "COALESCE(e.isConsultant, null), " +
            	    		       "COALESCE(e.isApprenticeship, null), " +
            	    		       "COALESCE(e.isApmosysProduct, null)) " +
            	    		       "FROM Employee e " +
            	    		       "JOIN JobRole jr ON jr.jobRoleId = e.jobRoleId " +
            	    		       "JOIN Department d ON d.deptId = jr.deptId " +
            	    		       "LEFT JOIN Employee e2 ON e.managerId = e2.empId " +
            	    		       "WHERE e.employmentstatus = 'Resigned' " +
            	    		       "ORDER BY e.name")
            	    	Page<EmployeeDTO> getAllResignedEmployees(Pageable pageable);

		@Query("SELECT cl.clientLocation, COUNT(DISTINCT e.empId) " +
        	        "FROM Employee e " +
        	        "JOIN EmployeeTeamMap etm ON etm.empId = e.empId " +
        	        "JOIN Team t ON t.teamId = etm.teamId " +
        	        "JOIN Project p ON p.projectId = t.projectId " + 
        	        "JOIN EmployeeTimesheetsNew et ON et.empId = e.empId " +
        	        "JOIN ProjectTimesheetStatusNew pts ON pts.id.timesheetId = et.timesheetId " +
        	        "AND pts.id.projectId = p.projectId " + 
        	        "JOIN ClientLocation cl ON cl.clientLocationId = pts.clientLocationId " +
        	        "WHERE e.employmentstatus <> 'InActive' " +
        	        "AND etm.active <> 0 " +
        	        "AND t.isActive = 'Y' " +
        	        "AND e.empId NOT BETWEEN 1 AND 6 " +
        	        "GROUP BY cl.clientLocation")
        	    	List<Object[]> getWorkLocation();
}
