package com.apmosys.employeeportal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.Employee;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long>{

	public Employee findByEmailAndPassword(String email,String password);

	@Query(value = "SELECT e.emp_id, e.aadhar , e.about_me,e.address,e.bank_account_no,e.bankifsccode,e.bank_name,\r\n"
			+ "e.blood_group,e.city,e.country,e.date_of_birth,e.date_of_joining,e.email,e.emergency_contact_mobile,\r\n"
			+ "e.employmentstatus,e.esic_number,e.father_name,e.gender,e.graduation,e.hobbies,e.landline,e.marital_status\r\n"
			+ ",e.mobile_no,e.mother_tongue,e.name,e.notice_period,e.official_mobile_no,e.pan_number,e.passport_number,\r\n"
			+ "e.permanent_address,e.pf_account_number,e.pincode,e.place_of_birth,e.post_graduation,\r\n"
			+ "e.previous_pf_account_number,e.relation,e.state,e.uan,e.views_on_organisation,e.year_of_grad,\r\n"
			+ "e.year_of_post_grad, em.name as manager, j.name as jobrole , d.name as department\r\n"
			+ "FROM employee e \r\n"
			+ "INNER JOIN employee em ON e.manager_id = em.emp_id\r\n"
			+ "INNER JOIN job_role j ON j.job_role_id = e.job_role_id\r\n"
			+ "INNER JOIN department d ON d.dept_id = j.dept_id;",nativeQuery = true)
	public Object[] getEmployeeByEmpId(Integer empId);
}
