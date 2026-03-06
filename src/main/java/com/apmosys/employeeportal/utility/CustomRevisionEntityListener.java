package com.apmosys.employeeportal.utility;

import org.hibernate.envers.RevisionListener;
import org.springframework.security.core.Authentication;
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
		  Authentication auth = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (auth == null || auth.getName() == null) {
            return 0L; // system / cron job
        }

        try {
            return Long.parseLong(auth.getName());
        } catch (Exception e) {
            return 0L;
        }
	}
	

}