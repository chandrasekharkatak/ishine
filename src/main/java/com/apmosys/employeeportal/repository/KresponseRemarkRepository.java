package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.KresponseRemark;

@Repository
public interface KresponseRemarkRepository extends JpaRepository<KresponseRemark, Long> {

	KresponseRemark save(KresponseRemark remark);

	List<KresponseRemark> findByKresponseId(Long kresponseId);
	
	

}
