import {
  HttpErrorResponse,
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest,
  HttpResponse
} from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError, finalize, tap } from 'rxjs/operators';
import { environment } from 'src/environments/environment';
import { LoaderService } from '../services/loader.service';



@Injectable()
export class LoaderInterceptor implements HttpInterceptor {

  private baseUrl: any = environment.baseUrl;
  startDate: any;

  URL_whiteList = [

    `${this.baseUrl}` + `api/authenticateUser`,
    `${this.baseUrl}` + `api/authenticateUserWithOTP`,
    `${this.baseUrl}` + `api/upload/designationBulkUpload`,
    `${this.baseUrl}` + `api/employeeBulkUpload`,
    `${this.baseUrl}` + `api/createDepartment`,
    `${this.baseUrl}` + `api/updateDepartment`,
    `${this.baseUrl}` + `api/deleteDepartment`,
    `${this.baseUrl}` + `api/getAllDepartments`,
    `${this.baseUrl}` + `api/updateDepartmentHolidayMappings`,

    `${this.baseUrl}` + `api/createEmployee`,
    `${this.baseUrl}` + `api/updateEmployeeByEmpId`,
    `${this.baseUrl}` + `api/deleteEmployeeByEmpId`,
    `${this.baseUrl}` + `api/getAllEmployees`,
    `${this.baseUrl}` + `api/getEmployeeByEmpId`,
    `${this.baseUrl}` + `api/updateEmployeeProfileByEmpId`,
    `${this.baseUrl}` + `api/previewImage`,
    `${this.baseUrl}` + `api/uploadImage`,
    `${this.baseUrl}` + `api/createDraftEmployee`,
    `${this.baseUrl}` + `api/updateDraftEmployeeById`,
    `${this.baseUrl}` + `api/deleteDraftEmployeeById`,
    `${this.baseUrl}` + `api/getAllDraftEmployees`,

    `${this.baseUrl}` + `api/addHoliday`,
    `${this.baseUrl}` + `api/updateHoliday`,
    `${this.baseUrl}` + `api/getAllHolidays`,
    `${this.baseUrl}` + `api/getHolidayListByDeptId`,

    `${this.baseUrl}` + `api/createJobRole`,
    `${this.baseUrl}` + `api/updateJobRole`,
    `${this.baseUrl}` + `api/deleteJobRole`,
    `${this.baseUrl}` + `api/getAllJobRole`,

    `${this.baseUrl}` + `api/applyLeave`,
    `${this.baseUrl}` + `api/updateLeaveStatus`,
    `${this.baseUrl}` + `api/getAllMyLeaveApplicationsByEmpId`,
    `${this.baseUrl}` + `api/getAllMyTeamsPendingLeaveApplicationsByManagerId`,
    `${this.baseUrl}` + `api/getLeaveLogsByEmpId`,
    `${this.baseUrl}` + `api/getMyLeaveBalancesByEmpId`,
    `${this.baseUrl}` + `api/updateLeavesByEmpId`,
    `${this.baseUrl}` + `api/createLeaveType`,
    `${this.baseUrl}` + `api/updateLeaveType`,
    `${this.baseUrl}` + `api/getAllLeaveTypes`,

    `${this.baseUrl}` + `api/getAllCompOffReasons`,
    `${this.baseUrl}` + `api/applyForCompOff`,
    `${this.baseUrl}` + `api/updateCompOffById`,
    `${this.baseUrl}` + `api/getAllCompOffRequestsByEmpId`,
    `${this.baseUrl}` + `api/getPendingCompOffRequestsByManagerId`,

    `${this.baseUrl}` + `api/addTimesheet`,
    `${this.baseUrl}` + `api/updateTimesheet`,
    `${this.baseUrl}` + `api/getAllProjectsByEmpId`,
    `${this.baseUrl}` + `api/getAllActivitiesByProjectIdandEmpId`,
    `${this.baseUrl}` + `api/getAllMyTimesheetsByEmpId`,
    `${this.baseUrl}` + `api/getAllMyTeamTimesheets`,
    `${this.baseUrl}` + `api/getAllMyActivitiesByTimesheetId`,
    `${this.baseUrl}` + `api/getMyReporteesTimesheetRequests`,
    `${this.baseUrl}` + `api/getMyReporteesApprovedTimesheets`,
    `${this.baseUrl}` + `api/updateTimesheetRequestById`,
    `${this.baseUrl}` + `api/timesheetReport`,
    `${this.baseUrl}` + `api/bulkApproveTimesheetRequest`,
    `${this.baseUrl}` + `api/bulkRejectTimesheetRequest`,

    `${this.baseUrl}` + `api/getSubfeaturesByJobRoleId`,
    `${this.baseUrl}` + `api/getAllSubFeatures`,
    `${this.baseUrl}` + `api/updateRoleFeatureMapping`,

    `${this.baseUrl}` + `api/createUserType`,
    `${this.baseUrl}` + `api/getAllUserTypes`,
    `${this.baseUrl}` + `api/updateCrudMappingsByMapId`,
    `${this.baseUrl}` + `api/getUserTypeCrudMappingsByUserTypeId`,
    `${this.baseUrl}` + `api/getAllUserTypeCrudMappings`,
    `${this.baseUrl}` + `api/changeLeaveTypeMapping`,

    `${this.baseUrl}` + `api/customQueryForLeaveReport`,
    `${this.baseUrl}` + `api/timesheetReport`,
    `${this.baseUrl}` + `api/leaveReport`,
    `${this.baseUrl}` + `api/customQueryForEmployeeReport`,

    `${this.baseUrl}` + `api/createSurvey`,
    `${this.baseUrl}` + `api/getAllSurveys`,
    `${this.baseUrl}` + `api/getAllQuestionsBySurveyId`,
    `${this.baseUrl}` + `api/setSurveyResponseByEmpId`,
    `${this.baseUrl}` + `api/getAnsweredSurveysByEmpId`,
    `${this.baseUrl}` + `api/empdetails`,
    `${this.baseUrl}` + `api/getSurveyAllResponsesBySurveyId`,

    `${this.baseUrl}` + `api/generatePerviousMonthDSR`,
    `${this.baseUrl}` + `api/getAllDocument`,
    `${this.baseUrl}` + `api/logoutUser`,
    `${this.baseUrl}` + `api/viewAppreciation`,
    `${this.baseUrl}` + `api/getAllAppreciationEvent`,
    `${this.baseUrl}` + `api/enableAppreciation`,
    `${this.baseUrl}` + `api/OnCheckEventName`,
    `${this.baseUrl}` + `api/deleteAppreciationEvent`,
    `${this.baseUrl}` + `api/updateAppreciationEvent`,
    `${this.baseUrl}` + `api/getHierarchyChartByEmpId`,
    `${this.baseUrl}` + `api/getAllManagers`,
    `${this.baseUrl}` + `api/getAllTeamMemberView`,
    `${this.baseUrl}` + `api/getAllTeamsByProjectId`,
    `${this.baseUrl}` + `api/getAllActivitiesByProjectIdAndTeamId`,
    `${this.baseUrl}` + `api/getAllEmployeesByRole`,
    `${this.baseUrl}` + `api/getAllMyTeamsByEmpId`,
    `${this.baseUrl}` + `api/getAllProjectListByProjectManagerId`,
    `${this.baseUrl}` + `api/getAllProjects`,
    `${this.baseUrl}` + `api/getActivityTemplate`,
    `${this.baseUrl}` + `api/getAllEventPhotos`,
    `${this.baseUrl}` + `api/findEmployeeWorkingHistory`,
    `${this.baseUrl}` + `api/customTimesheetApplicationReport`,

    `${this.baseUrl}` + `api/getMappedActivityPreview`,
    `${this.baseUrl}` + `api/getMappedActivityInUpdateTeam`,

    `${this.baseUrl}` + `api/updateTimesheetLockCheck`,
    `${this.baseUrl}` + `api/revokeApprovedLeaveApplication`,
    `${this.baseUrl}` + `api/updateRevokeLeaveStatus`,
    `${this.baseUrl}` + `api/generateAllEmployeeDSR`,
    `${this.baseUrl}` + `api/getHierarchyByEmpId`,
    `${this.baseUrl}` + `api/alreadyCreatedTeam`,
    `${this.baseUrl}` + `api/createDraftProjectInfo`,
    `${this.baseUrl}` + `api/approvePendingProject`,
    `${this.baseUrl}` + `api/rejectPendingProject`,
    `${this.baseUrl}` + `api/bulkSyncProject`,
    `${this.baseUrl}` + `api/getInternalProject`,
    `${this.baseUrl}` + `api/getDomainSpecialization`,

    `${this.baseUrl}` + `api/bulkApproveLeaveRequest`,
    `${this.baseUrl}` + `api/bulkRejectLeaveRequest`,

    `${this.baseUrl}` + `api/updateTeam`,

    `${this.baseUrl}` + `api/reconsileHolidayTimesheet`,

    `${this.baseUrl}` + `api/updatePendingLeave`,
    `${this.baseUrl}` + `api/deletePendingLeave`,

    `${this.baseUrl}` + `api/approveResignationApplication`,
    `${this.baseUrl}` + `api/rejectResignationApplication`,
    `${this.baseUrl}` + `api/createResignationApplication`,
    `${this.baseUrl}` + `api/revokeResignationApplication`,
    `${this.baseUrl}` + `api/revokeMyResignationApplication`,

    `${this.baseUrl}` + `api/unlockAllTimesheet`,

    `${this.baseUrl}` + `api/changeDepartmentJobRoleMapping`,
    `${this.baseUrl}` + `api/updateOnBoardingCheckList`,

    `${this.baseUrl}` + `api/updateEmployeePassword`,

    `${this.baseUrl}` + `api/updateDefaultFeatureMapping`,
    `${this.baseUrl}` + `api/getDefaultMapping`,

    `${this.baseUrl}` + `api/checkEmailWhenForgotPassword`,
    `${this.baseUrl}` + `api/updateEmployeeForgotPassword`,

    `${this.baseUrl}` + `api/newsletters/uploadNewsletter`,
    `${this.baseUrl}` + `api/newsletters/`,
    `${this.baseUrl}` + `api/newsletters/download/`,
    `${this.baseUrl}` + `api/newsletters/setNewsletterReadResponseByEmpId`,
    `${this.baseUrl}` + `api/newsletters/getAllReadNewslettersByEmpId`,
    `${this.baseUrl}` + `api/getEmployeeAuditInfo`,

    `${this.baseUrl}` + `api/approveDraftEmployeeApplication`,
    `${this.baseUrl}` + `api/uploadHelpDocument`,
    `${this.baseUrl}` + `api/uploadPolicies`,
    `${this.baseUrl}` + `api/bulkCompOffReject`,
    `${this.baseUrl}` + `api/bulkCompOffApprove`,
    `${this.baseUrl}` + `api/getTeamListByProjectName`,

    // added by anurag 
    `${this.baseUrl}` + `api/deleteType`,
    `${this.baseUrl}` + `api/updateType`,
    `${this.baseUrl}` + `api/getAllTypeName`,
    `${this.baseUrl}` + `api/newsletters/getDocumentByType`,
    `${this.baseUrl}` + `api/newsletters/customQueryForDocument`,
    `${this.baseUrl}` + `api/pipGenerateToUser`,
    // query/createQuery
    `${this.baseUrl}` + `api/query/createQuery`,
    `${this.baseUrl}` + `api/query/getNonPublishedQuery`,
    `${this.baseUrl}` + `api/query/getQueryDataForPreview`,
    `${this.baseUrl}` + `api/query/getQueryDetailsByQueryId`,
    `${this.baseUrl}` + `api/query/updateQuery`,
    `${this.baseUrl}` + `api/query/deleteQuery`,
    `${this.baseUrl}` + `api/getEmployeeAuditInfo`,
    `${this.baseUrl}` + `api/customQueryForDepartmentWiseBillableEmployeeReport`,
    `${this.baseUrl}` + `api/getDepartmentWiseBillableData`,
    `${this.baseUrl}` + `api/pipReturnFromUser`,
    `${this.baseUrl}` + `api/setExtendPeriodByPipId`,
    `${this.baseUrl}` + `api/revokeAccount`,
    `${this.baseUrl}` + `api/upload/billableFile`,
    `${this.baseUrl}` + `api/getDepartmentPendingLeaveHistory`,
    `${this.baseUrl}` + `api/getEmployeeAuditInfo`,
    `${this.baseUrl}` + `api/deleteTeamByTeamId`,
    `${this.baseUrl}` + `api/upload/saveExcelDataForManagerMapping`,
    `${this.baseUrl}` + `api/getBioData`,

    //added by priyadarshini
    `${this.baseUrl}` + `api/saveRewardConfiguration`,
    `${this.baseUrl}` + `api/showAllRewards/`,
    `${this.baseUrl}` + `api/fetchEmployeesFromRewardCondition`,
    `${this.baseUrl}` + `api/submitRewardForEmployee`,
    `${this.baseUrl}` + `api/showAllEmployeeRewards`,
    `${this.baseUrl}` + `api/getReviewType`,
    `${this.baseUrl}` + `api/getProjectInfo`,
    `${this.baseUrl}` + `api/getTeamInfo`,
    `${this.baseUrl}` + `api/getAllEmployeesForPerformance`,
    `${this.baseUrl}` + `api/get360TimesheetDetails`,
    `${this.baseUrl}` + `api/submitEmployeePerformanceHOD`,
    `${this.baseUrl}` + `api/submitEmployeePerformanceHR`,
    `${this.baseUrl}` + `api/addReviewType`,
    `${this.baseUrl}` + `api/updateReviewType`,
    `${this.baseUrl}` + `getExistingProjectsAndTeamsByEmployee`,
    `${this.baseUrl}` + `api/getAllEmployeesFor360View`,
    `${this.baseUrl}` + `api/getAll360LeaveApplicationsByEmpId`,
    `${this.baseUrl}` + `api/biomax`,
    `${this.baseUrl}` + `api/saveExcelDataForReward`,
    `${this.baseUrl}` + `api/fetchEmployeesForHomepageByCategoryId`,
    `${this.baseUrl}` + `api/getAllTeamView`,
    `${this.baseUrl}` + `api/submitRewardForEmployee`,
    `${this.baseUrl}` + `api/showAllEmployeeRewards`,
    `${this.baseUrl}` + `api/getAllEmployeesReportByProjectTypeInConsolidated`,
    `${this.baseUrl}` + `api/getAllEmployeesReportByProjectType`,
    `${this.baseUrl}` + `api/saveAppreciation`,
    `${this.baseUrl}` + `api/deleteCompOff`,
    `${this.baseUrl}` + `api/getAllOrDeptWiseEmployeeTimesheetReport`,
    `${this.baseUrl}` + `api/getAllEmployeesForPerformance`,
    `${this.baseUrl}` + `api/syncPoProjectDetailsByProjectId`,
    `${this.baseUrl}` + `api/sendEmailNotificationToBDTeam`,
    `${this.baseUrl}` + `api/currentStatusForPerformanceTableView`,
    `${this.baseUrl}` + `api/poprojectclone`,
    `${this.baseUrl}` + `api/getEmployeeProjectReport`,
    `${this.baseUrl}` + `api/updateEmployeeReportBillableType`,
    `${this.baseUrl}` + `api/updateBulkBillableEmployeeReport`,
    `${this.baseUrl}` + `api/updateProjectResourcesAsInActiveBulk`,
    `${this.baseUrl}` + `api/deleteTeamsByIdsBulk`,
    `${this.baseUrl}` + `api/combinedPOINTERNALList`,
    `${this.baseUrl}` + `api/previewDocument`,
    `${this.baseUrl}` + `api/uploadTicket`,
    `${this.baseUrl}` + `api/totalTravelData`,
    `${this.baseUrl}` + `api/getAllInvoices`,
    `${this.baseUrl}` + `api/fetchTotalReimbursementData`,
    `${this.baseUrl}` + `api/uploadFileReimbursement`,
    `${this.baseUrl}` + `api/getAllInvoicesByEmpId`,
    `${this.baseUrl}` + `api/updateReimbursmentBasedOnTravelRequest`,
    `${this.baseUrl}` + `api/updateUploadedFile`,
    `${this.baseUrl}` + `api/saveTravelData`,
    `${this.baseUrl}` + `api/fetchTotalReimbursementData`,
    `${this.baseUrl}` + `api/fetchReimbursementData`,
    `${this.baseUrl}` + `api/fetchReimbursementDataforApproval`,
    `${this.baseUrl}` + `api/saveReimbursementData`,
    `${this.baseUrl}` + `api/updateReimbursementData`,
    `${this.baseUrl}` + `api/revokeReimbursement`,
    `${this.baseUrl}` + `api/approveOrRejectReimbursement`,
    `${this.baseUrl}` + `api/uploadFileReimbursement`,
    `${this.baseUrl}` + `api/approveOrRejectTravel`,
    `${this.baseUrl}` + `api/fetchTravelData`,
    `${this.baseUrl}` + `api/fetchTravelDataForApproval`,
    `${this.baseUrl}` + `api/saveTravelData`,
    `${this.baseUrl}` + `api/uploadFile`,
    `${this.baseUrl}` + `api/updateTravelData`,
    `${this.baseUrl}` + `api/revokeTravel`,
    `${this.baseUrl}`+`api/travel-reason/create`,
    `${this.baseUrl}` + `api/getTravelReason`,
    `${this.baseUrl}`+`api/saveTravelMode`,
    `${this.baseUrl}` + `api/getTravelMode`,
    `${this.baseUrl}`+`api/saveTravelClass`,
    `${this.baseUrl}api/getTravelModeByReason`,
    `${this.baseUrl}`+`api/saveHotelCategory`,
    `${this.baseUrl}` + `api/getHotelCategory`,
    `${this.baseUrl}`+`api/saveHotelSubCategory`,
    `${this.baseUrl}` + `api/getHotelSubCategory`,
    `${this.baseUrl}`+`api/saveCity`,
    `${this.baseUrl}api/getTravelClassByMode`,
    `${this.baseUrl}api/getCityBySubCategory`,
    `${this.baseUrl}` + `api/getCity`,
    `${this.baseUrl}` + `api/onGetTravelCass`,
    `${this.baseUrl}`+`api/uploadKycDocument`,
    `${this.baseUrl}` + `api/onGetFoodType`,
    `${this.baseUrl}`+`api/saveFoodType`, 
    `${this.baseUrl}` + `api/onGetVehicleType`,
    `${this.baseUrl}`+`api/saveVehicleType`,
    `${this.baseUrl}` + `api/getReimbursementTravelMode`,
    `${this.baseUrl}`+`api/saveReimbursementTravelMode`,
    `${this.baseUrl}`+`api/saveExpenditureType`,
    `${this.baseUrl}` + `api/onGetExpenditureType`,
    `${this.baseUrl}`+`api/markAsPaid`,
    `${this.baseUrl}` + `api/updateProjectResourceAsInActive`,
    `${this.baseUrl}` + `api/setDefaultProjectUpdateBillable`,
    `${this.baseUrl}`+`api/setProjectMappingAndDefaultProject`,
    `${this.baseUrl}`+`api/getProjectDetailsForBulkDefaultUpdate`,
    `${this.baseUrl}` + `api/getPreviousDefaultProjectDetails`,
    `${this.baseUrl}` + `api/combinedPOINTERNALCountList`,
    `${this.baseUrl}` + `api/combinedPOINTERNALDataList`,
    `${this.baseUrl}` + `api/showPolicyReadResponseByPolicyID`,
    `${this.baseUrl}`+`api/updateMilestoneExtendedDate`,
     `${this.baseUrl}` + `api/appreciationSentByCurrentUser`,
     `${this.baseUrl}` + `api/appreciationReceivedByCurrentUser`,
     `${this.baseUrl}` + `api/getAllEmployeeAppreciationListByCategory`,
    `${this.baseUrl}` + `api/showPolicyReadResponseByPolicyID`,
    `${this.baseUrl}`+`api/getAllEmployeeDSROfRM`,
    `${this.baseUrl}`+`api/approveTimesheetRequest`,
    `${this.baseUrl}`+`api/getTimesheetDashboardCountForProject`,
    `${this.baseUrl}`+`api/getTimesheetDashboardCountForEmployee`,
    `${this.baseUrl}`+`api/getEmployeeViewForClientAttendanceStatus`,
    `${this.baseUrl}`+`api/getProjectViewForClientAttendanceStatus`,
    `${this.baseUrl}` + `api/liftAndShiftTeams`,
    `${this.baseUrl}` + `api/updateHasClientSideId?flag`,
    `${this.baseUrl}` + `api/addTimesheetWithClient`,
     `${this.baseUrl}` + `api/getEmployeeByNameAndEmpidForTimesheet`,
     `${this.baseUrl}` + `updateMilestoneById`,
    `${this.baseUrl}` + `api/getWorkLocationSummaryDetails`,
    `${this.baseUrl}` + `api/getFixedCostCount`,
    // `${this.baseUrl}` + `api/getEmployeeByNameAndEmpidForTimesheet`
    `${this.baseUrl}` + `api/getClientAndProjectReportDataList`,
    `${this.baseUrl}` + `api/getClientAndProjectReport`,
    `${this.baseUrl}` + `api/completionDateOfProject`,
    `${this.baseUrl}` + `api/getAllProficiency`,
    `${this.baseUrl}` + `api/getAllPredefinedSkills`,
    `${this.baseUrl}` + `api/addSkillOfEmployee`,
    `${this.baseUrl}` + `api/updateSkillOfEmployee`,
    `${this.baseUrl}` + `api/getAllSkillsByEmpId`,
    `${this.baseUrl}` + `api/deleteSkillsOfEmployee`,
    `${this.baseUrl}` + `api/addCertificate`,
    `${this.baseUrl}` + `api/getAllCertificatesByEmpId`,
    `${this.baseUrl}` + `api/deleteCertificate`,
    `${this.baseUrl}` + `api/uploadSkillBulk`,
    `${this.baseUrl}` + `api/uploadCertificateBulk`,
    `${this.baseUrl}` + `api/searchEmployeesBySkillsAndCertificates`,
    `${this.baseUrl}` + `api/restorePreviousStateOfProject`,
    `${this.baseUrl}` + `api/getDeptsByRole`,
    `${this.baseUrl}` + `api/getAllTeamLeaveHistoryView`,
    `${this.baseUrl}` + `api/getAllTeamCompOffHistoryView`,
    `${this.baseUrl}` + `api/getPendingCompOffRequestsByManagerId`,
    `${this.baseUrl}` + `api/getAllMyTeamsPendingLeaveRevokeApplicationsByManagerId`,
    `${this.baseUrl}` + `api/getValueOptionData`,
    `${this.baseUrl}` + `api/totalEmployeeCountInDepartments`,
    `${this.baseUrl}` + `api/totalEmployeeCount`,
    `${this.baseUrl}` + `api/projectLessEmployeesDepartmentWise`,
    `${this.baseUrl}` + `api/employeesMappedProjectsDepartmentWise`,
    `${this.baseUrl}` + `api/getEmployeeTimesheetsByProject`,
    `${this.baseUrl}` + `api/getOneMonthTimesheetReport`,
    `${this.baseUrl}`+`api/graph-employee-summary`,
    `${this.baseUrl}`+`api/customgetLeaveTrendDetails`,
    `${this.baseUrl}`+`api/customgetJoiningVsResignationCount`,
    `${this.baseUrl}`+`api/work-location-details`,
    `${this.baseUrl}`+`api/getAllLeaveTimesheetsWithoutLeaveApplication`,
    `${this.baseUrl}` + `api/fetchInactivePOListOfEmployee`,
    `${this.baseUrl}` + `api/getDocumentsByEmpAndDate`,
    `${this.baseUrl}`+`api/getEmployeeTimesheetAsCalenderByProjectId`,
    `${this.baseUrl}`+`api/bulkFinalDocumentUpload`,
    `${this.baseUrl}` + `api/getEmployeeOnBoardingDetailByEmployeementId`,
    `${this.baseUrl}` + `api/fetchActivePOListOfEmployee`,
    `${this.baseUrl}` + `api/updateProjectResourcesAsInActiveBulk`,
   
    `${this.baseUrl}` + `api/biomax`,
    
    `${this.baseUrl}` + `api/createProjectInsightQuestion`,
    `${this.baseUrl}` + `api/updateProjectInsightQuestion`,
    `${this.baseUrl}` + `api/getAllProjectInsightList`,
    `${this.baseUrl}` + `api/getAllQuestionsByProjectId`,
    `${this.baseUrl}` + `api/getAllProjectInsightResponsesByProjectId`,
    `${this.baseUrl}` + `api/saveProjectInsightResponse`,
    `${this.baseUrl}` + `api/getUserUploadedFileForQuestion`,
    `${this.baseUrl}` + `api/getAllProjectInsightContributionList`,
    `${this.baseUrl}` + `api/saveReviewPoints`,
    `${this.baseUrl}` + `api/getAllProjectInsightQuestionsByProjectIdAndEmpId`,

    //Project insight
    `${this.baseUrl}` + `form/getAllDynamicForm`,
    `${this.baseUrl}` + `api/getProjectInsightDetailsByObjectId`,
    `${this.baseUrl}` + `api/getAllProjectInsightGroupsByParentId`,
    `${this.baseUrl}` + `api/getProjectInsightGroupDetailsByObjectId`,
    `${this.baseUrl}` + `api/getProjectInsightQuestionDetailsByObjectId`,
    `${this.baseUrl}` + `api/getProjectInsightQuestionDetailsByParentIdAndParentType`,
    `${this.baseUrl}` + `api/getProjectInsightGroupDetailsByParentIdAndParentType`,
    `${this.baseUrl}` + `api/saveProjectInsightDetails`,
    `${this.baseUrl}` + `api/saveProjectInsightGroupDetails`,
    `${this.baseUrl}` + `api/saveProjectInsightQuestionDetails`,
    `${this.baseUrl}` + `api/deleteProjectInsightQuestionDetails`,
    `${this.baseUrl}` + `api/getAllProjectInsightQuestionEntriesByDepartment`,
    `${this.baseUrl}` + `api/getAllProjectInsightQuestionsEntry`,
    `${this.baseUrl}` + `api/getAllProjectInsightQuestionEntriesByFilter`,
    `${this.baseUrl}` + `api/saveEntryToQuestionLibraryFromExcel`,
    `${this.baseUrl}` + `api/deleteProjectInsightQuestionLibraryEntryById`,
    `${this.baseUrl}` + `api/getEntryFromsearchQuestionLibraryByText`,
    `${this.baseUrl}` + `api/saveProjectInsightQuestionLibraryEntry`,
    `${this.baseUrl}` + `api/get-all-domain-with-projects`,
    `${this.baseUrl}` + `api/getAllProjectInsight`,
    `${this.baseUrl}` + `api/get-project-domain`,
    `${this.baseUrl}` + `api/get-domain`,
    `${this.baseUrl}` + `api/getAllApiList`,
    `${this.baseUrl}` + `api/getAllProject`,
    `${this.baseUrl}` + `api/getAllClients`,
    `${this.baseUrl}` + `api/getAllEmployee`,
    `${this.baseUrl}` + `api/getAllDeptsList`,
    `${this.baseUrl}` + `api/getProjectInsightQuestionDetailsByParentIdAndParentType`,
    `${this.baseUrl}` + `api/getProjectInsightDetailsByObjectId`,
    `${this.baseUrl}` + `api/getAllProjectInsightGroupsByParentId`,
    `${this.baseUrl}` + `api/getProjectInsightQuestionDetailsByParentIdAndParentType`,
    `${this.baseUrl}` + `api/getAllNextFieldAndOption`,
    `${this.baseUrl}` + `api/getProjectInsightQuestionDetailsByObjectId`,
    `${this.baseUrl}` + `api/getAllProjectInsightFacetCategory`,
    `${this.baseUrl}` + `api/saveProjectInsightFacetCategoryList`,
    
    `${this.baseUrl}` + `api/getAllQuestionsWith`,
    `${this.baseUrl}` + `api/getAllQuestionsForApprovalTab`,
    `${this.baseUrl}` + `api/getGroupStatusData`,
    `${this.baseUrl}` + `api/getAllgroupstatusdata`,
    `${this.baseUrl}` + `api/groupQuestionDetails`,
    `${this.baseUrl}` + `api/allGroupQuestions`,
    `${this.baseUrl}` + `api/saveAnswerDraft`,
    `${this.baseUrl}` + `api/assignQuestionforReview`,
    `${this.baseUrl}` + `api/refreshStatusCount`,
    `${this.baseUrl}` + `api/saveProjectInsightDetailsFromExcel`, 
    `${this.baseUrl}` + `api/search-project-insight`, 
    `${this.baseUrl}` + `api/deleteProjectInsightById/`,
    `${this.baseUrl}` + `api/searchDomain`,
    `${this.baseUrl}` + `api/load-all-filters`,
    `${this.baseUrl}` + `api/upload/project-insight-files`,
    `${this.baseUrl}` + `api/upload/download-project-insight-file`,
    `${this.baseUrl}` + `api/upload/view-project-insight-file`,


  ]

  constructor(private loaderService: LoaderService) { }

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    this.URL_whiteList.forEach((element) => {

      if (request.url == element) {

        request = request.clone({
          setHeaders: {
            loader: 'true'
          }
        });
      }



    })

    if (request.headers.get('loader')) {
      this.loaderService.requestStarted();
      return this.handle(next, request);
    }

    return next.handle(request);

  }

  handle(next: HttpHandler, request: HttpRequest<any>) {
  return next.handle(request).pipe(
    tap((event) => {
      if (event instanceof HttpResponse) {
        // Success response
        this.loaderService.requestEnded();
      }
    }),
    catchError((error: HttpErrorResponse) => {
      this.loaderService.resetSpinner();

      if (error instanceof HttpErrorResponse) {
        switch (error.status) {
          case 401:
            // Unauthorized: possibly redirect to login or show message
            console.error('Error 401: Unauthorized access.');
            // Example: this.authService.logout();
            break;

          case 403:
            // Forbidden: user doesn’t have permission
            console.error('Error 403: Forbidden.');
            // Example: this.router.navigate(['/forbidden']);
            break;

          case 500:
            // Internal server error: show generic message or alert
            console.error('Error 500: Internal server error.');
            // Example: this.toastr.error('Something went wrong on the server.');
            break;

          default:
            console.error(`Error ${error.status}: ${error.message}`);
            break;
        }
      } else {
        // Handle client-side or network errors
        console.error('Network or client error occurred:', error);
      }

      return throwError(() => error);
    })
  );
}

}