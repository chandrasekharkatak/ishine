package com.apmosys.employeeportal.utility;

import org.hibernate.envers.RevisionListener;

import com.apmosys.employeeportal.model.CustomRevisionEntity;

public class CustomRevisionEntityListener implements RevisionListener {

	@Override
	public void newRevision(Object revisionEntity) {
		CustomRevisionEntity entity= (CustomRevisionEntity) revisionEntity;
//		Long id = 122l;
//		entity.setUpdatedBy(id);
	}

}
