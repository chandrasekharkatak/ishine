package com.apmosys.employeeportal.utility;

import org.hibernate.envers.RevisionListener;
import org.springframework.security.core.context.SecurityContextHolder;

import com.apmosys.employeeportal.model.CustomRevisionEntity;

public class CustomRevisionEntityListener implements RevisionListener {

	@Override
	public void newRevision(Object revisionEntity) {
		CustomRevisionEntity entity= (CustomRevisionEntity) revisionEntity;
//		Long id = 122l;
		entity.setUpdatedBy(getEmpId());
	}
	private long getEmpId(){
		String empId= SecurityContextHolder.getContext().getAuthentication().getName();
		try {
			return Long.parseLong(empId);
		}catch(Exception e) {
			e.printStackTrace();
			return 0l;
		}
	}
	

}