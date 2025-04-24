import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BsModalService, BsModalRef } from 'ngx-bootstrap/modal';
import { FormBuilder, FormGroup } from '@angular/forms';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { Employee } from 'src/app/models/employee';
import { EmployeeService } from 'src/app/services/employee.service';
import { PerformanceService } from 'src/app/services/performance.service';
import { GoalService } from 'src/app/services/goal.service';
import { Subscription } from 'rxjs';
import { Feature } from 'src/app/models/feature';
import { LogService } from 'src/app/services/log.service';
import { Log } from 'src/app/models/log';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { first } from 'rxjs/operators';
import { ProjectInsight } from 'src/app/models/projectInsightQuestion';
import { ProjectInsightService } from 'src/app/services/project-insight.service';
import { ProjectQuestion } from 'src/app/models/projectQuestion';
import { ValidationService } from 'src/app/services/validation.service';
import { Document } from 'src/app/models/document';
import * as moment from 'moment';
import { AppComponent } from 'src/app/app.component';
import { ProjectResponse } from 'src/app/models/projectResponse';
import { Sort } from '@angular/material/sort';
import { ProjectMilestone } from 'src/app/models/projectMilestone';
import { ProjectService } from 'src/app/services/project.service';
import { AngularEditorConfig } from '@kolkov/angular-editor';
import { UserContribution } from 'src/app/models/userContribution';


interface Goal {
  goalStatus: string;
  goalProgress: any;
  goalId: number;
  goalTitle: string;
  description: string;
  checkpoints: any[];
  managerRemark: string;
  employeeRemark: string;
  assignedBy: string;
  quarter: string;
  expectedCompletionDate: string;
  createdDate: string;
}

interface Stats {
  goalsCompleted: number;
  goalsRemaining: number;
  kraKpiScore: string;
  questionnaireScore: string;
}

interface AppraisalSummary {
  finalRating: number;
  finalRemarks: string;
  appraisalScore: number;
}

interface kpiList{
  remark: any;
  response:any;
  id:number;
  description: string;
}

@Component({
  selector: 'app-view-performance',
  templateUrl: './view-performance.component.html',
  styleUrls: ['./view-performance.component.css']
})
export class ViewPerformanceComponent implements OnInit {
  @ViewChild('alert_message') alertMessageModal:TemplateRef<any>;

  currentUser:User;
  feature="view_performance";
  userMapping:any = {};
  log:Log;
  activeTab: string = 'kra-kpi';
  quarterCyclesList: any;
  selectedQuarter:any;
  selectedQuarter1:any;

  kraKpiMetrics: any[] = [];
  questionnaireQuestions: any[] = [];
  currentQuestionnaireId: any;

  allQuestionMarksList: any[] = [];
  allProjectList:any[] = [];
  getUserContributionForReviewList:any[] = [];
  finalContributionList:any[] = [];

  toggleReviewView:boolean = false;
  showPreReviewerSelection:boolean = false;
  showValidationErrors: boolean = false;

  validationErrors: string = '';
  selectedPreReviewer:any = '';

  kraKpiReviewForm: FormGroup;
  questionnaireReviewForm: FormGroup;

  stats: Stats ={
    goalsCompleted: 0,
    goalsRemaining: 0,
    kraKpiScore: '',
    questionnaireScore: '',
  };

  goals: Goal[] = [];
  summary?: AppraisalSummary;

  currentEmployeeInfo:Employee = new Employee();
  userContributionObj:UserContribution = new UserContribution();

  selectedGoal?: Goal;
  subscription!: Subscription;
  modalRef?: BsModalRef;
  errorMessage: string;
  kpiList:kpiList[] = [];
  alertMessage: any;

  viewPerformanceEmpId: any;

  // Project Insight
  isQuestionForm: boolean = false;
  isCreation: boolean = false;
  isUpdation: boolean = false;
  isProjectInsightList: boolean = false;
  isSurveyResponseList: boolean = false;
  isProjectInsightResponseList: boolean = false;
  isResponsePreview: boolean = true;
  isSearchEnabled: boolean = false;
  isFinalResponseSubmitted: boolean = false;

  contributionFilters:any = {};
  isContributionSearchEnabled:boolean = false;
  myContributionPage = 1;
  filters: any = {};
  page = 1;
  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType: any;

  projectInsight:ProjectInsight = new ProjectInsight();

  allProjectInsightList: any[] = [];
  projectInsightColumnColumns: any[] = ['projectName', 'description', 'isActive', 'createdByName', 'createdOn'];
  projectInsightContributionColumns:any[] = ['projectName','status','createdOn', 'blank']
  employeeList:any[] = [];

  projectResponseModalRef: BsModalRef = new BsModalRef();
  documentPreviewModalRef: BsModalRef = new BsModalRef();

  projectId:any;
  actionType:any='Contribution';
  subActionType:any='Review Response';
  responseByEmpId:any;

  //Text Editor
  editorConfig: AngularEditorConfig = {
    editable: true,
    spellcheck: true,
    height: '20rem',
    minHeight: '5rem',
    width: 'auto',
    minWidth: '0',
    translate: 'yes',
    enableToolbar: true,
    showToolbar: true,
    placeholder: 'Enter text here...',
    defaultParagraphSeparator: '',
    defaultFontName: '',
    defaultFontSize: '',
    uploadWithCredentials: false,
    sanitize: false,
    toolbarPosition: 'top',
    fonts: [{class: 'arial', name: 'Arial'}],
  };

  constructor(
    private router: Router,
    private http: HttpClient, 
    private employeeService:EmployeeService,
    private modalService: BsModalService,
    private fb: FormBuilder,
    private authenticationService : AuthenticationService,
    private performanceService:PerformanceService,
    private goalService:GoalService,
    private logService:LogService,
    private route: ActivatedRoute,
    private projectInsightService:ProjectInsightService,
    private validationService: ValidationService,
    private projectService: ProjectService

  ) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  ngOnInit(): void {
    this.logService.updateLogInfo(this.log);
    this.isProjectInsightList = true;
    this.route.params.subscribe((params:Params) => {
      this.viewPerformanceEmpId = params['id'];
    });

    this.onGetEmployeeInfo();
    this.setActiveTab('kra-kpi');
    this.getAllProjectInsightContributionList();
    this.getEmployeeList();
    this.getUserContributionForReview();
    this.getAllProjects();
    
    let featureMap:Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    featureMap.subFeatures?.forEach(sub => {
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });
  }

  fetchQuarters(): void {
    this.performanceService.getAllQuarterCycles().subscribe({
      next: (response: any) => {
        if (response.serviceStatus === 'Success') {
          this.quarterCyclesList = response.serviceResponse;
          this.selectedQuarter = this.quarterCyclesList[0].quarterId;
          this.selectedQuarter1 = this.quarterCyclesList[0].quarterId;
          
          this.onQuarterChange();
          this.onQuarterChange1();
        } else {
          this.errorMessage = response.serviceMessage || 'Failed to load quarters.';
        }
      },
      error: (error) => {
        this.errorMessage = error.message || 'Error fetching quarters.';
      }
    });
  }

  onQuarterChange(): void {
    if (!this.selectedQuarter) return;

    this.loadPerformanceStats();
    this.fetchGoals();
  }

  onQuarterChange1(): void{
    if (!this.selectedQuarter1) return;

    this.loadKpiList();
    this.loadQuestionnaireQuestions();
  }



  loadAppraisalSummary(): void {
    const empId = this.currentEmployeeInfo.empId;
    if (!empId ) return;

    this.performanceService.getAppraisalSummary(empId).subscribe({
      next: (data) => {
        this.summary = data;
      },
      error: (err) => {
        console.error('Error fetching Appraisal Summary:', err);
      },
    });
  }


  fetchGoals(): void {

    this.errorMessage = ''; 
    const empId = this.currentEmployeeInfo.empId;
    if (!empId) return;
    const quarter = Number(this.selectedQuarter);
    this.goalService.getGoalsByEmployeeAndQuarter(empId, quarter).subscribe({
      next: (response: any) => {
        if (response.serviceStatus === 'Success') {
          this.goals = response.serviceResponse; 
          console.log('list of goals',this.goals);
        } else {
          this.errorMessage = response.serviceMessage || 'No goals found for this employee.';
        }
      },
      error: (error) => {
        this.errorMessage = error.message || 'Failed to fetch employee goals.';
      },
    });
  }

  loadPerformanceStats(): void {
    const empId = this.viewPerformanceEmpId;
    const quarter = this.selectedQuarter;


    if (!empId || !quarter) return;

    this.performanceService.getPerformanceStats(empId, quarter).subscribe({
      next: (response: any) => {
        this.stats = response.serviceResponse;
        console.log('STATS::: ',this.stats);
      },
      error: (err) => {
        console.error('Error fetching performance stats:', err);
      },
    });
  }
 

  async onGetEmployeeInfo(){
    let currentEmp = new Employee();
    currentEmp.empId = this.viewPerformanceEmpId;

    const response: any = await this.employeeService.getEmployeeByEmpId(currentEmp).toPromise();
    if (response.serviceStatus == "Success") {
      this.currentEmployeeInfo = response.serviceResponse;
      console.log(this.currentEmployeeInfo);
      this.fetchQuarters();
    } else {
      console.error(response.serviceResponse);
    }
  }


  setActiveTab(tab: string) {
    this.activeTab = tab;

  }

  openModal(template: TemplateRef<any>, goal: any): void {
    this.selectedGoal = goal;
    this.modalRef = this.modalService.show(template, { class: 'modal-lg' });
  }


  processUserMarks(response: ProjectResponse, type: any, question:any){
    if(type == "approve" && !this.validationService.validateNullUndefinedEmptyString(response.marks)){
      this.alertMessage = `Please provide marks !!`;
      this.openAlertMod(this.alertMessageModal, this.alertMessage);
      return false;
    }
    response.markType = type;
    response.questionId = question.questionId;
    response.empId = response.responseByEmpId;
    this.allQuestionMarksList.push(response);
  }

  submitReview(template: TemplateRef<any>){
    let projectInsightObj = {
      empMarkList: this.allQuestionMarksList
    }
    this.performanceService.addRemarkAsPerQuestion(projectInsightObj).subscribe({
      next: (response: any) => {
        // this.closeProjectInsightResponseModal();
      },
      error: (error) => {
        console.error('Error fetching questionnaire questions:', error);
      }
    });
  }

  loadQuestionnaireQuestions(): void {
    const quarterId = this.selectedQuarter1;
    const departmentId = this.currentEmployeeInfo.departmentId;

    this.questionnaireQuestions = [];
    this.currentQuestionnaireId = null;    

    this.performanceService.loadQuestionnaireQuestions(departmentId, quarterId).subscribe({
        next: (response: any) => {
          if (response.serviceStatus === 'Success') {
            this.questionnaireQuestions = response.serviceResponse[0].questions;
            this.currentQuestionnaireId = response.serviceResponse[0].questionId;
            // console.log('Questionnaire response:',this.questionnaireQuestions);
            console.log('length of questionnaire: ', this.questionnaireQuestions.length);
  
           
          } else {
            console.error('Failed to load questionnaire questions:', response.serviceMessage);
          }
        },
        error: (error) => {
          console.error('Error fetching questionnaire questions:', error);
        }
      });
  }

  saveQuestionnaireResponses(template: TemplateRef<any>): void {
 
    const empId = this.currentEmployeeInfo.empId;
    const quarterId = this.selectedQuarter1;

    console.log('question response:', this.questionnaireQuestions);
  
  
    this.performanceService.submitQuestionnaireResponses(this.questionnaireQuestions,empId,quarterId).subscribe({
      next: (response: any) => {
        if (response.serviceStatus === 'Success') {
          this.alertMessage = "Questionnaire responses submitted successfully!"
          this.openAlertMod(template, this.alertMessage);
        } else {
          this.alertMessage = `Failed to submit responses: ${response.serviceMessage}`
          this.openAlertMod(template, this.alertMessage);
        }
      },
      error: (error) => {
        console.error('Error submitting questionnaire responses:', error);
        this.alertMessage = `An error occurred while submitting responses. Please try again.`
        this.openAlertMod(template, this.alertMessage);
      }
    });
  }


  navigateToteam(): void{
    this.router.navigate(['/user-performance/team-dashboard']);
  }

  loadKpiList(): void {
    const quarterId = this.selectedQuarter1;
    const departmentId = this.currentEmployeeInfo.departmentId;

    this.kpiList = [];

    
    if (!quarterId || !departmentId) return;
    this.performanceService.loadKpiList(quarterId, departmentId).subscribe({
        next: (response: any) => {
          if (response.serviceStatus === 'Success') {
            this.kpiList = response.serviceResponse[0].kpis;

          } else {
            console.error('Failed to load questionnaire questions:', response.serviceMessage);
          }
        },
        error: (error) => {
          console.error('Error fetching questionnaire questions:', error);
        }
      });
  
  }
  
  saveKpiResponses(template: TemplateRef<any>): void {
  
    console.log("Response ======> "+ JSON.stringify(this.kpiList));
  
    const response = this.kpiList;
    const empId = this.currentEmployeeInfo.empId;
    const quarterId = this.selectedQuarter1;
  
    this.performanceService.submitKpiResponses(response,empId,quarterId).subscribe({
      next: (response: any) => {
        if (response.serviceStatus === 'Success') {
          this.alertMessage = "KPI responses submitted successfully!"
          this.openAlertMod(template, this.alertMessage);
        } else {
          this.alertMessage = `Failed to submit responses: ${response.serviceMessage}`
          this.openAlertMod(template, this.alertMessage);
        }
      },
      error: (error) => {
        console.error('Error submitting KPI responses:', error);
        this.alertMessage = `Error submitting KPI responses: ${error}`
        this.openAlertMod(template, this.alertMessage);
      }
    });
  }

  saveUpdates(template: TemplateRef<any>) {
    const payload = {
      managerRemark: this.selectedGoal.managerRemark,
    };
  
    this.goalService.updateGoal(this.selectedGoal.goalId, payload).subscribe(
      (response) => {
        if (response.serviceStatus === 'Success') {
          this.alertMessage = "Updates saved successfully!"
          this.openAlertMod(template, this.alertMessage);
        }
      },
      (error) => {
        console.error('Error saving updates:', error);
        this.alertMessage = "'Error saving updates"
        this.openAlertMod(template, this.alertMessage);
      }
    );
  }

  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }

  // Project Insight

  toggleSearch(togleType: any) {
    if (togleType == 'projectInsight') {
      this.isSearchEnabled = !this.isSearchEnabled;
      if (!this.isSearchEnabled) {
        this.filters = {};
      }
    } else {
      this.isContributionSearchEnabled = !this.isContributionSearchEnabled;
      if (!this.isContributionSearchEnabled) {
        this.filters = {};
      }
    }
  }

  sortData(sort: Sort) {
    if (sort.active) {
      let sortParams: any[] = sort.active?.split("|");
      this.sortColumn = sortParams[0];
      this.sortColumnType = sortParams[1];
      this.sortDirection = sort.direction;
    }
  }

  onSearch(searchData, type: any) {
    if (type == 'projectInsight') {
      this.filters = searchData; 
    }else{
      this.contributionFilters = searchData;
    }
  }
  
  handlePageChange(event, type: any) {
    if(type == 'projectInsight'){
      this.page = event;
    }else{
      this.myContributionPage = event;
    }
  }


  showProjectInsight() {
    this.isProjectInsightList = true;
    this.isQuestionForm = false;
    this.isCreation = false;
    this.isUpdation = false;
    this.isProjectInsightResponseList = false;
    this.getAllProjectInsightContributionList();
  }

  getAllProjectInsightContributionList() {
    this.sortColumn = [];
    this.sortColumnType = [];
    this.sortDirection = '';
    this.allProjectInsightList = [];

    let insightObj = {
      employeeRole: this.currentUser.employeeRole,
      empId: this.viewPerformanceEmpId,
      performanceTabName : 'Team Dashboard'      
    };

    this.projectInsightService.getAllProjectInsightContributionList(insightObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allProjectInsightList = response.serviceResponse;
        this.allProjectInsightList.forEach(project => {
          project.createdOn = (project.createdOn) ? moment(project.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
        });
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  getAllProjectInsightResponsesByProjectId(projectObj: any, alertTemplate: TemplateRef<any>, insightResponseTemplate: TemplateRef<any>, isPreview: any) {
    this.projectId = projectObj.projectId;
    this.subActionType = 'Review Response';
    this.responseByEmpId = this.viewPerformanceEmpId;
    this.openProjectInsightResponeMod(insightResponseTemplate);
  }

  openProjectInsightResponeMod(insightResponseTemplate: TemplateRef<any>) {
    this.projectResponseModalRef = this.modalService.show(insightResponseTemplate, { class: 'modal-xl', ignoreBackdropClick: true, keyboard: false });
  }

  closeProjectInsightResponseModal() {
    this.projectResponseModalRef.hide();
  }

  getEmployeeList() {
    this.employeeList = [];
    this.employeeService.getAllEmployees().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.employeeList = response.serviceResponse;
        this.employeeList = this.employeeList.filter(x => x.employmentstatus != 'InActive');
      } else {
        console.error(response.serviceResponse)
      }
    });
  }

  getAllProjects() {
    this.allProjectList = [];
    this.projectService.getAllProjects().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allProjectList = response.serviceResponse;
        this.allProjectList.forEach(project => {
          project.createdOn = (project.createdOn) ? moment(project.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
          project.updatedOn = (project.updatedOn) ? moment(project.updatedOn).format(AppComponent.DATETIME_FORMAT) : null;
        })
        this.allProjectList = this.allProjectList.filter((value, index, self) =>
          index === self.findIndex((t) => (
            t.projectId === value.projectId
          ))
        );
        this.allProjectList = this.allProjectList.sort((a, b) => a.createdOn - b.createdOn);
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  processUserContribution(statusType: any, projectUSerContributionObj: any){

    if (statusType != 'preReviewer') {
      let errors: string[] = [];
      if (!projectUSerContributionObj.remark) {
        errors.push('Please enter remarks');
      }
      if (!projectUSerContributionObj.projectId) {
        errors.push('Please Tag Project');
      }

      if (errors.length > 0) {
        this.showValidationErrors = true;
        this.validationErrors = errors.join(', ');
        return;
      }
    }

    if (statusType === 'preReviewer') {
      if (!this.showPreReviewerSelection) {
        this.showPreReviewerSelection = true;
        return;
      } else if (!this.selectedPreReviewer) {
        this.alertMessage = "Please select a pre-reviewer";
        this.modalRef = this.modalService.show(this.alertMessageModal, { class: 'modal-sm' });
        return;
      }
      projectUSerContributionObj.assignTo = this.selectedPreReviewer;
    }else{
      projectUSerContributionObj.assignTo = this.currentUser.empId;
    }

    this.cancelRequest();

    let userContributionObj = new UserContribution();
    userContributionObj.userContributionId = projectUSerContributionObj.userContributionId;
    userContributionObj.processType = statusType;
    userContributionObj.assignTo = this.currentUser.empId;
    userContributionObj.status = statusType;
    userContributionObj.projectId = projectUSerContributionObj.projectId;
    userContributionObj.remark = projectUSerContributionObj.remark;

    this.projectInsightService.processUserContribution(userContributionObj).pipe(first()).subscribe({
      next: (response: any) => {
        this.alertMessage = response.serviceMessage;
        this.modalRef = this.modalService.show(this.alertMessageModal, { class: 'modal-sm' });
      },
      error: (error) => {
        this.alertMessage = error.serviceMessage;
        this.modalRef = this.modalService.show(this.alertMessageModal, { class: 'modal-sm' });
      }
    });
  }

  getUserContributionForReview(){
    this.getUserContributionForReviewList = [];
    this.finalContributionList = [];

    let userContributionObj = new UserContribution();
    userContributionObj.assignTo = this.currentUser.empId;
    userContributionObj.empId = this.viewPerformanceEmpId;
    this.projectInsightService.getUserContributionForReview(userContributionObj).pipe(first()).subscribe({
      next: (response: any) => {
        this.getUserContributionForReviewList = response;
        this.finalContributionList = response;
      },
      error: (error) => {
        this.alertMessage = error.serviceMessage;
        this.modalRef = this.modalService.show(this.alertMessageModal, { class: 'modal-sm' });
      }
    });
  }

  openUserContributionModal(projectObj: any, contributionModal: TemplateRef<any>) {
    this.userContributionObj = projectObj;
    this.editorConfig = {
      ...this.editorConfig,
      editable: false,
      showToolbar: false
    };
    this.modalRef = this.modalService.show(contributionModal, { class: 'modal-xl' });
  }

  cancelRequestPreReviewer() {
    this.showPreReviewerSelection = false;
  }

  cancelRequest() {
    this.showPreReviewerSelection = false;
    this.modalRef.hide();
    this.modalService.hide();
  }

}