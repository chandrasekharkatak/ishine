export interface GrievanceRouteContext {
  category?: string;
  subcategory?: string;
}

/**
 * UAT-derived grievance launch defaults.
 *
 * Source used for these labels:
 * `db_emp_backup_new.tab_master`, `feature_master`, `sub_feature_master`
 * on the UAT database.
 */
const CATEGORY_BY_ROOT: Record<string, string> = {
  appreciation: 'Appreciation',
  configuration: 'Configurations',
  grievance: 'Grievance',
  home: 'Home',
  newsletters: 'Newsletters',
  'project-insight': 'Project Insight',
  'qr-code': 'QR Code',
  recruitment: 'Recruitment',
  reimbursement: 'Reimbursement',
  'release-notes': 'Release Notes',
  'rewards-tab': 'Rewards',
  'skill-matrix': 'Skill Matrix',
  training: 'Training',
  travelDesk: 'Travel Desk',
  'user-appreciation': 'Appreciation',
  'user-exit': 'Exit',
  'user-leaves': 'My Leave',
  'user-performance': 'Performance',
  'user-policies': 'HR Policies',
  'user-profile': 'My Profile',
  'user-reports': 'Reports',
  'user-survey': 'Survey',
  'user-team': 'My Team',
  'user-timesheet': 'Timesheets',
  'user-training': 'Training',
};

const SUBCATEGORY_BY_PATH: Record<string, string> = {
  'configuration/department': 'View All Department',
  'configuration/designation': 'View All Designation',
  'configuration/employee': 'View All Employee',
  'configuration/home-config': 'View All Notification',
  'configuration/leave': 'View Leave Policies',
  'configuration/newsletter': 'View All Newsletters',
  'configuration/performance-config': 'Performance Config',
  'configuration/portal-config': 'Update Portal Global Configuration',
  'configuration/reimbursment-config': 'Reimbursement Config',
  'configuration/role': 'View All Role',
  'configuration/survey-config': 'View All Surveys',
  'configuration/timesheet-config': 'View All Reject Reason',
  'configuration/training-config': 'Get All Trainings',
  'configuration/travel-config': 'Travel Config',
  'configuration/upload-policies': 'View All Documents',
  'employee-360/:id/grievance': 'View Own Tickets',
  'employee-360/:id/leave': 'View Leave History',
  'employee-360/:id/lms': 'Get User Trainings',
  'employee-360/:id/profile': 'View Profile',
  'employee-360/:id/project': 'Project Insight Details',
  'employee-360/:id/timesheet': 'View My Timesheets',
  'project-insight/project-insight-details': 'Project Insight Details',
  'project-insight/project-insight-details/department-forms': 'Department Forms',
  'project-insight/project-insight-details/domains': 'Domains',
  'project-insight/project-insight-details/knowledge-hub': 'Knowledge Hub',
  'project-insight/project-insight-details/question-library': 'Question Library',
  'reimbursement/approve-reimbursement': 'Approve Reimbursement',
  'reimbursement/my-reimbursement': 'My Reimbursement',
  'reimbursement/view-reimbursement': 'View Reimbursement',
  'rewards-tab/rewards-and-recognisation': 'Rewards And Recognisation',
  'rewards-tab/rewardsappreciation': 'Appreciation Event',
  'skill-matrix/approve-requests': 'Skill Matrix Approve Skill Requests',
  'skill-matrix/master-configuration': 'Skill Matrix Master Configuration',
  'skill-matrix/my-submissions': 'Skill Matrix My Submissions',
  'skill-matrix/submit-for-review': 'Skill Matrix Submit For Review',
  'travelDesk/approve-travelrequest': 'Approve Journey',
  'travelDesk/my-travelrequest': 'Apply Travel Request',
  'travelDesk/total-travelrequest': 'Total Travelrequest',
  'travelDesk/view-travelrequest': 'View Request',
  'user-exit/my-resignation': 'My Resignation',
  'user-exit/my-resignation/:id': 'My Resignation',
  'user-leaves/compOff': 'View Comp off req status',
  'user-leaves/holiday': 'View Holidays',
  'user-leaves/leave': 'View Leave History',
  'user-performance/performance-dashboard': 'Performance Management',
  'user-performance/performance-management-system': 'Performance Management',
  'user-performance/quarter-cycle': 'Performance Management',
  'user-policies': 'HR Policies',
  'user-profile': 'View Profile',
  'user-reports/attendance-reconciliation': 'Attendance Reconciliation',
  'user-reports/report-list': 'Employee Report',
  'user-survey': 'View Surveys',
  'user-team/my-team': 'View My Team',
  'user-team/resource-management': 'Search Employee',
  'user-team/resource-management/:id': 'Search Employee',
  'user-team/team-config': 'View Teams',
  'user-team/team-member': 'View My Team Members',
  'user-timesheet/hr-dashboard': 'Timesheets Dashboard',
  'user-timesheet/my-timesheet': 'View My Timesheets',
  'user-timesheet/team-timesheet': 'View My Teams Timesheets',
};

const ROOT_DEFAULT_SUBCATEGORY: Record<string, string> = {
  grievance: 'Raise Ticket',
  newsletters: 'View Newsletters',
  'qr-code': 'QR Code View',
  recruitment: 'Recruitment',
  'release-notes': 'View Release Notes',
  training: 'Get User Trainings',
  'user-appreciation': 'Appreciation',
  'user-policies': 'HR Policies',
  'user-profile': 'View Profile',
  'user-survey': 'View Surveys',
  'user-training': 'Get User Trainings',
};

function normalizeRouteUrl(url: string | null | undefined): string {
  const raw = (url || '').trim();
  if (!raw) {
    return '';
  }
  const withoutQuery = raw.split('?')[0].trim();
  const withoutHash = withoutQuery.startsWith('#') ? withoutQuery.slice(1) : withoutQuery;
  return withoutHash.replace(/^\/+/, '').replace(/\/+$/, '');
}

function normalizeSegment(segment: string): string {
  const value = (segment || '').trim();
  if (!value) {
    return '';
  }
  if (/^\d+$/.test(value)) {
    return ':id';
  }
  return value;
}

export function resolveGrievanceRouteContext(url: string | null | undefined): GrievanceRouteContext | null {
  const normalizedUrl = normalizeRouteUrl(url);
  if (!normalizedUrl) {
    return null;
  }

  const segments = normalizedUrl
    .split('/')
    .map(normalizeSegment)
    .filter(Boolean);
  if (!segments.length) {
    return null;
  }

  const root = segments[0];
  const category = CATEGORY_BY_ROOT[root];
  if (!category) {
    return null;
  }

  const pathKey = segments.join('/');
  const subcategory = SUBCATEGORY_BY_PATH[pathKey] || ROOT_DEFAULT_SUBCATEGORY[root] || undefined;

  return {
    category,
    subcategory,
  };
}
