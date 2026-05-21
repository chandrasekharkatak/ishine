package com.apmosys.employeeportal.utility;

/**
 * Interview module feature / sub-feature names — must match feature_master and sub_feature_master in DB.
 */
public final class InterviewConstants {

    private InterviewConstants() {
    }

    /** feature_master.feature_id for "Interview Tracker" */
    public static final long INTERVIEW_FEATURE_ID = 81L;

    /** feature_master.feature_name */
    public static final String FEATURE_NAME = "Interview Tracker";

    public static final String SUB_VIEW = "View Interview";
    public static final String SUB_SCHEDULE = "Schedule Interview";
    public static final String SUB_EDIT = "Edit Interview";
    public static final String SUB_DELETE = "Delete Interview";
}
