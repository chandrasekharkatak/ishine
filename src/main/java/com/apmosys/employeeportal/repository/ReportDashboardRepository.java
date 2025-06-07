package com.apmosys.employeeportal.repository;

import java.util.List;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.apmosys.employeeportal.dto.ReportCountDTO;
import com.apmosys.employeeportal.model.Employee;

public interface ReportDashboardRepository extends JpaRepository<Employee, Long> {
	
    @Query(value = "SELECT " +
            "d.name AS department_name, " +
            "CASE WHEN e.is_user_info_updated = 'true' THEN 'Completed' ELSE 'Pending' END AS status, " +
            "COUNT(DISTINCT e.emp_id) AS emp_count " +
            "FROM employee e " +
            "INNER JOIN job_role jr ON e.job_role_id = jr.job_role_id " +
            "INNER JOIN department d ON d.dept_id = jr.dept_id " +
            "WHERE e.employmentstatus <> 'InActive' " + 
            "GROUP BY d.name, CASE WHEN e.is_user_info_updated = 'true' THEN 'Completed' ELSE 'Pending' END",
    nativeQuery = true)
   public List<Object[]> getDepartmentWiseKycCount();
   
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
		    "    AND (:employeement_id IS NULL OR e.emp_id IN (:employeement_id)) " +
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
		    "           j.apprentice_count, j.consultant_count, j.regular_count, " +
		    "           COALESCE(r.total_emp_count, 0) resign_count " +
		    "    FROM ( " +
		    "        SELECT DATE_FORMAT(date_of_joining, '%M') AS month_name, " +
		    "               YEAR(date_of_joining) AS year, " +
		    "               COUNT(DISTINCT CASE WHEN is_apprenticeship = 'true' THEN emp_id END) AS apprentice_count, " +
		    "               COUNT(DISTINCT CASE WHEN is_consultant = 'true' THEN emp_id END) AS consultant_count, " +
		    "               COUNT(DISTINCT CASE " +
		    "                   WHEN (is_consultant = 'false' AND is_apprenticeship = 'false') " +
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
		    "           j.apprentice_count, j.consultant_count, j.regular_count, " +
		    "           r.total_emp_count " +
		    "    FROM ( " +
		    "        SELECT DATE_FORMAT(date_of_joining, '%M') AS month_name, " +
		    "               YEAR(date_of_joining) AS year, " +
		    "               COUNT(DISTINCT CASE WHEN is_apprenticeship = 'true' THEN emp_id END) AS apprentice_count, " +
		    "               COUNT(DISTINCT CASE WHEN is_consultant = 'true' THEN emp_id END) AS consultant_count, " +
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
		    @Param("employeement_id") List<Integer> employeement_id,
		    @Param("name") String name,
		    @Param("dept_id") List<Integer> dept_id,
		    @Param("job_role_id") List<Integer> job_role_id,
		    @Param("manager_id") List<Integer> manager_id,
		    @Param("team_id") List<Integer> team_id,
		    @Param("project_id") List<Integer> project_id,
		    @Param("client_id") List<Integer> client_id,
		    @Param("employmentstatus") String employmentstatus,
		    @Param("date_of_joining") String date_of_joining,
		    @Param("city") String city,
		    @Param("blood_group") String blood_group,
		    @Param("gender") String gender,
		    @Param("probation_period") List<Integer> probation_period,
		    @Param("notice_period") List<Integer> notice_period,
		    @Param("marital_status") String marital_status,
		    @Param("bank_name") String bank_name,
		    @Param("state") String state,
		    @Param("created_on") String created_on,
		    @Param("created_by") List<Integer> created_by,
		    @Param("experience") String experience,
		    @Param("work_location") String work_location,
		    @Param("year") Integer year
		);

   
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
			+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND ((is_consultant = 'false' AND is_apprenticeship = 'false') OR (COALESCE(is_consultant, '') = '' AND COALESCE(is_apprenticeship, '') = '')) AND TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) / 12 >= 0 AND TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) / 12 <= 1 THEN 1 ELSE 0 END) AS Employees_Years_0_1,\n"
			+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND ((is_consultant = 'false' AND is_apprenticeship = 'false') OR (COALESCE(is_consultant, '') = '' AND COALESCE(is_apprenticeship, '') = '')) AND TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) / 12 > 1 AND TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) / 12 <= 2 THEN 1 ELSE 0 END) AS Employees_Years_1_2,\n"
			+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND ((is_consultant = 'false' AND is_apprenticeship = 'false') OR (COALESCE(is_consultant, '') = '' AND COALESCE(is_apprenticeship, '') = '')) AND TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) / 12 > 2 AND TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) / 12 <= 5 THEN 1 ELSE 0 END) AS Employees_Years_2_5,\n"
			+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND ((is_consultant = 'false' AND is_apprenticeship = 'false') OR (COALESCE(is_consultant, '') = '' AND COALESCE(is_apprenticeship, '') = '')) AND TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) / 12 > 5 AND TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) / 12 <= 10 THEN 1 ELSE 0 END) AS Employees_Years_5_10,\n"
			+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND ((is_consultant = 'false' AND is_apprenticeship = 'false') OR (COALESCE(is_consultant, '') = '' AND COALESCE(is_apprenticeship, '') = '')) AND TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) / 12 > 10 THEN 1 ELSE 0 END) AS Employees_Years_Above_10,\n"
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
			+ "    SUM(CASE WHEN employmentstatus = 'Probation' and TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) > 6 then 1 ELSE 0 end) as probation_count,\n"
			+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND is_apprenticeship = 'true' THEN 1 ELSE 0 END) as apprentice_count,\n"
			+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND is_consultant = 'true' THEN 1 ELSE 0 END) as consultant_count,\n"
			+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND ((is_consultant = 'false' AND is_apprenticeship = 'false') OR (COALESCE(is_consultant, '') = '' AND COALESCE(is_apprenticeship, '') = '')) THEN 1 ELSE 0 END) as regular_count\n"
			+ "FROM employee_data")
         public List<Object[]> getAllGraphEmployeeSummary();
	
         @Query(nativeQuery = true, value = "\n"
        		 + "SELECT distinct \n"
        		 + " CONCAT('A-', e.employeement_id) as EMP_ID,\n"
        		 + " CASE WHEN e.is_apprenticeship = 'true' THEN 'Apprentice'\n"
        		 + " WHEN ((e.is_consultant = 'false' AND e.is_apprenticeship = 'false') OR (COALESCE(e.is_consultant, '') = '' AND COALESCE(e.is_apprenticeship, '') = '')) THEN 'Regular'\n"
        		 + " WHEN e.is_consultant = 'true' THEN 'Consultant' \n"
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
        		 + "WHERE 1=1\n"
        		 + " and (\n"
        		 + " (:in_active_flag= 1 and e.employmentstatus = 'InActive')\n"
        		 + " or\n"
        		 + " (:in_active_flag = 0 and e.employmentstatus != 'InActive')\n"
        		 + " ) \n"
        		 + " and e.emp_id NOT BETWEEN 1 AND 6\n"
        		 + "and  (:billable is null or upper(e.billable) like CONCAT('%', UPPER(:billable), '%'))\n"
        		 + "and  (:billable_type is null or upper(e.billable_type) like CONCAT('%', UPPER(:billable_type), '%'))\n"
        		 + " and (:employmentstatus is null or UPPER(e.employmentstatus) like CONCAT('%', UPPER(:employmentstatus), '%'))\n"
        		 + " and (:gender IS NULL OR UPPER(e.gender) LIKE CONCAT('%', UPPER(:gender), '%'))\n"
        		 + " and (:experience IS NULL OR UPPER(e.experience) LIKE CONCAT('%', UPPER(:experience), '%'))\n"
        		 + " and (TIMESTAMPDIFF(YEAR, e.date_of_birth, CURRENT_DATE()) BETWEEN :lower_age AND :upper_age)\n"
        		 + "GROUP BY e.emp_id;")
        		 public List<Object[]> getAllPieGraphListSummary(
        		     @Param("in_active_flag") Integer inActiveFlag,
        		     @Param("billable") String billable,
        		     @Param("billable_type") String billableType,
        		     @Param("employmentstatus") String employmentStatus,
        		     @Param("gender") String gender,
        		     @Param("experience") String experience,
        		     @Param("lower_age") Integer lowerAge,
        		     @Param("upper_age") Integer upperAge
        		 );
	
	
}
