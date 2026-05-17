package com.apmosys.employeeportal.skillmatrix;

/**
 * Canonical sub_feature_master.sub_feature_name values for Skill Matrix.
 * Must match rows inserted via db/skill_matrix_feature_seed.sql and frontend userMapping keys
 * (spaces → underscores, lowercased).
 */
public final class SkillMatrixSubFeatureNames {

	private SkillMatrixSubFeatureNames() {}

	public static final String SUBMIT_FOR_REVIEW = "Skill Matrix Submit For Review";
	public static final String MY_SUBMISSIONS = "Skill Matrix My Submissions";
	public static final String APPROVE_REQUESTS = "Skill Matrix Approve Skill Requests";
	public static final String MASTER_CONFIGURATION = "Skill Matrix Master Configuration";
}
