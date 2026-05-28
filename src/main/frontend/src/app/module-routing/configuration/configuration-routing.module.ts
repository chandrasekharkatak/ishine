import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ConfigurationComponent } from 'src/app/configuration/configuration.component';
import { DeptConfigComponent } from 'src/app/configuration/dept-config/dept-config.component';
import { DesignationConfigComponent } from 'src/app/configuration/designation-config/designation-config.component';
import { DocumentComponent } from 'src/app/configuration/document/document.component';
import { DomainConfigComponent } from 'src/app/configuration/domain-config/domain-config.component';
import { EmployeeConfigComponent } from 'src/app/configuration/employee-config/employee-config.component';
import { HomeConfigComponent } from 'src/app/configuration/home-config/home-config.component';
import { LeaveConfigComponent } from 'src/app/configuration/leave-config/leave-config.component';
import { NewsletterConfigComponent } from 'src/app/configuration/newsletter-config/newsletter-config.component';
import { OnBoardingComponent } from 'src/app/configuration/on-boarding/on-boarding.component';
import { OthersComponent } from 'src/app/configuration/others/others.component';
import { PerformanceConfigComponent } from 'src/app/configuration/performance-config/performance-config.component';
import { PortalConfigComponent } from 'src/app/configuration/portal-config/portal-config.component';
import { ReimbursmentConfigComponent } from 'src/app/configuration/reimbursment-config/reimbursment-config.component';
import { RewardsConfigComponent } from 'src/app/configuration/rewards-config/rewards-config.component';
import { RoleConfigComponent } from 'src/app/configuration/role-config/role-config.component';
import { SkillCertfificationConfigComponent } from 'src/app/configuration/skill-certfification-config/skill-certfification-config.component';
import { SurveyConfigComponent } from 'src/app/configuration/survey-config/survey-config.component';
import { TimesheetConfigComponent } from 'src/app/configuration/timesheet-config/timesheet-config.component';
import { TravelConfigComponent } from 'src/app/configuration/travel-config/travel-config.component';
import { UploadPoliciesComponent } from 'src/app/configuration/upload-policies/upload-policies.component';
import { TrainingConfigComponent } from 'src/app/configuration/training-config/training-config.component';
import { TrainingQuizConfigComponent } from 'src/app/configuration/training-config/training-quiz-config/training-quiz-config.component';

import { AclConfigComponent } from 'src/app/configuration/acl-config/acl-config.component';
const routes: Routes = [
  {
    path: '', component: ConfigurationComponent,
    children: [
      { path: 'employee', component: EmployeeConfigComponent, },
      { path: 'domain', component: DomainConfigComponent, },
      { path: 'department', component: DeptConfigComponent, },
      { path: 'role', component: RoleConfigComponent, },
      { path: 'leave', component: LeaveConfigComponent, },
      { path: 'home-config', component: HomeConfigComponent, },
      { path: 'portal-config', component: PortalConfigComponent, },
      { path: 'survey-config', component: SurveyConfigComponent, },
      { path: 'upload-policies', component: UploadPoliciesComponent, },
      { path: 'on-boarding', component: OnBoardingComponent, },
      { path: 'designation', component: DesignationConfigComponent, },
      { path: 'newsletter', component: NewsletterConfigComponent, },
      { path: 'document', component: DocumentComponent, },
      { path: 'other', component: OthersComponent, },
      { path: 'rewards-config', component: RewardsConfigComponent, },
      { path: 'performance-config', component: PerformanceConfigComponent },
      { path: 'travel-config', component: TravelConfigComponent },
      { path: 'reimbursment-config', component: ReimbursmentConfigComponent },
      { path: 'timesheet-config', component: TimesheetConfigComponent },
      { path: 'skill-certfication-config', component: SkillCertfificationConfigComponent },
      { path: 'training-config', component: TrainingConfigComponent },
      { path: 'training-quiz-config', component: TrainingQuizConfigComponent },
      { path: 'acl-config', component: AclConfigComponent },
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ConfigurationRoutingModule { }
