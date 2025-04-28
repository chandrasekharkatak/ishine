import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { BsModalService, BsModalRef } from 'ngx-bootstrap/modal';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { Employee } from 'src/app/models/employee';
import { EmployeeService } from 'src/app/services/employee.service';
import { PerformanceService } from 'src/app/services/performance.service';
import { GoalService } from 'src/app/services/goal.service';
import { Feature } from 'src/app/models/feature';
import { LogService } from 'src/app/services/log.service';
import { Log } from 'src/app/models/log';
import { Sort } from '@angular/material/sort';
import { ProjectInsight } from 'src/app/models/projectInsight';
import { ProjectInsightService } from 'src/app/services/project-insight.service';
import { finalize, first } from 'rxjs/operators';
import { ProjectQuestion } from 'src/app/models/projectQuestion';
import { ValidationService } from 'src/app/services/validation.service';
import { Document } from 'src/app/models/document';
import * as moment from 'moment';
import { AppComponent } from 'src/app/app.component';
import { ProjectResponse } from 'src/app/models/projectResponse';
import { ProjectMilestone } from 'src/app/models/projectMilestone';
import { UserContribution } from 'src/app/models/userContribution';
import { AngularEditorConfig } from '@kolkov/angular-editor';
import { ProjectService } from 'src/app/services/project.service';
import { Observable } from 'rxjs';
import { HttpEvent, HttpResponse } from '@angular/common/http';

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

interface QuestionDTO {
remark: any;
response: any;
  id: number;
  questionText: string;
}

interface kpiList{
  remark: any;
  response:any;
  id:number;
  description: string;
}

interface UploadResponse {
  imageUrl: string;
}

@Component({
  selector: 'app-performance-dashboard',
  templateUrl: './performance-dashboard.component.html',
  styleUrls: ['./performance-dashboard.component.css']
})
export class PerformanceDashboardComponent implements OnInit {

  @ViewChild('alert_message') alertModal: TemplateRef<any>;

  feature="performance_dashboard";
  userMapping:any = {};
  log:Log;
  responses = [];
  currentUser: User;
  activeTab: string = 'kra-kpi';
  selectedQuarter: number;
  quarterCyclesList: any;
  selectedQuarter1: any;
  kraKpiMetrics: any[] = [];
  questionnaireQuestions: QuestionDTO[] = [];
  kpiList:kpiList[] = [];
  selectedgoalProgress: any;

  showContextMenu = false;
  contextMenuX = 0;
  contextMenuY = 0;
  selectedText = '';
  newTag: string = '';
  
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
    upload: (file: File): Observable<HttpEvent<UploadResponse>> => {
      return new Observable(observer => {
        if (this.isValidFileType(file)) {
          if (!this.userContributionObj.attachments) {
            this.userContributionObj.attachments = [];
          }
          this.userContributionObj.attachments.push(file);
          const reader = new FileReader();
          reader.onload = (e: any) => {
            const response: HttpResponse<UploadResponse> = new HttpResponse({
              body: {
                imageUrl: e.target.result
              }
            });
            observer.next(response);
            observer.complete();
          };
          reader.onerror = (e) => {
            observer.error('Upload failed');
          };
          reader.readAsDataURL(file);
        } else {
          this.alertMessage = `File type not allowed: ${file.name}. Only PNG, JPG, and PDF files are accepted.`;
          this.modalRef = this.modalService.show(this.alertModal, { class: 'modal-sm' });
          observer.error('Invalid file type');
        }
      });
    }
  };

  stats: Stats = {
    goalsCompleted: 0,
    goalsRemaining: 0,
    kraKpiScore: '',
    questionnaireScore: '',
  };
  minRating = 3;
  goals: Goal[] = [];
  summary: AppraisalSummary = {
    finalRating: 0,        
    finalRemarks: '',      
    appraisalScore: 0      
  };

  currentEmployeeInfo: Employee = new Employee();
  userContributionObj:UserContribution = new UserContribution();
  selectedGoal?: Goal;
  modalRef?: BsModalRef;
  docModalRef?: BsModalRef;
  errorMessage: string;
  currentQuestionnaireId: any;

  alertMessage:any;

  // Project Insight
  isQuestionForm:boolean = false;
  isCreation:boolean = false;
  isUpdation:boolean = false;
  isProjectInsightList:boolean = false;
  isSurveyResponseList:boolean = false;
  isProjectInsightResponseList:boolean = false;
  isResponsePreview:boolean = true;
  isSearchEnabled:boolean = false;
  isFinalResponseSubmitted:boolean = false;
  showReviewButton:boolean = false;
  toggleReviewView:boolean = false;
  showPreReviewerSelection:boolean = false;
  showValidationErrors: boolean = false;
  showPreviewDiv:boolean = false;

  validationErrors: string = '';
  selectedPreReviewer:any = '';

  filters:any = {};
  contributionFilters:any = {};
  isContributionSearchEnabled:boolean = false;
  myContributionPage = 1;
  page = 1;
  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType:any;

  projectInsight:ProjectInsight = new ProjectInsight();

  allProjectInsightList:any[] = [];
  projectInsightColumnColumns:any[] = ['projectName','description','isActive','createdByName','createdOn'];
  projectInsightContributionColumns:any[] = ['projectName','status','createdOn', 'blank']
  employeeList:any[] = [];
  myProjectInsightContributionList:any[] = [];
  getUserContributionForReviewList:any[] = [];
  finalContributionList:any[] = [];
  allProjectList:any[] = [];

  projectResponseModalRef: BsModalRef = new BsModalRef();
  documentPreviewModalRef: BsModalRef = new BsModalRef();

  projectId:any;
  actionType:any='Contribution';
  subActionType:any='Submit/View Response';
  responseByEmpId:any;

  constructor(
    private employeeService: EmployeeService,
    private modalService: BsModalService,
    private authenticationService : AuthenticationService,
    private performanceService:PerformanceService,
    private goalService:GoalService,
    private logService:LogService,
    private projectInsightService:ProjectInsightService,
    private validationService: ValidationService,
    private projectService: ProjectService,
  ) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  ngOnInit(): void {
    this.logService.updateLogInfo(this.log);
    this.isProjectInsightList = true;
    this.onGetEmployeeInfo();
    this.fetchGoals();
    this.loadPerformanceStats();
    this.getAllProjectInsightContributionList();
    this.getMyContributionList();
    this.getUserContributionForReview();
    this.getAllProjects();
    this.getEmployeeList();
    
    let featureMap:Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    featureMap.subFeatures?.forEach(sub => {
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });


  }
  async onGetEmployeeInfo() {
    this.currentEmployeeInfo = new Employee();
    let currentEmp = new Employee();
    currentEmp.empId = this.currentUser.empId;

    const response: any = await this.employeeService.getEmployeeByEmpId(currentEmp).toPromise();
    if (response.serviceStatus == "Success") {
      this.currentEmployeeInfo = response.serviceResponse;
      console.log('Employee info:', this.currentEmployeeInfo);
      this.fetchQuarters();
    } else {
      console.error(response.serviceResponse);
    }
  }


  fetchQuarters(): void {
    this.performanceService.getAllQuarterCycles().subscribe({
      next: (response: any) => {
        if (response.serviceStatus === 'Success') {
          this.quarterCyclesList = response.serviceResponse;
          this.selectedQuarter = this.quarterCyclesList[0].quarterId;
          this.selectedQuarter1 = this.quarterCyclesList[0].quarterId;
          // console.log('list of quarters:', this.quarterCyclesList);
          
          this.onQuarterChange();
          this.quarterChange2();
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
    this.loadAppraisalSummary();
    
  }




  loadAppraisalSummary(): void { 
    const empId = this.currentEmployeeInfo.empId;
    console.log('emp id: ',this.currentEmployeeInfo.empId);
    if (!empId) return;

    this.performanceService.getAppraisalSummary(empId).subscribe({
      next: (response) => {
        this.summary = response.serviceResponse[0];
        // console.log('appraisal summary:', this.summary);
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
          // console.log('list of goals',this.goals);
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
    const empId = this.currentUser?.empId;
    const quarter = this.selectedQuarter;


    if (!empId || !quarter) return;

    this.performanceService.getPerformanceStats(empId, quarter).subscribe({
      next: (response: any) => {
        this.stats = response.serviceResponse;
        // console.log('STATS::: ',this.stats);
      },
      error: (err) => {
        console.error('Error fetching performance stats:', err);
      },
    });
  }

  setActiveTab(tab: string) {
    this.activeTab = tab;
    
  }

  openModal(template: TemplateRef<any>, goal: any): void {
    this.selectedGoal = goal;
    this.modalRef = this.modalService.show(template, { class: 'modal-lg' });
  }


quarterChange2(): void{
  this.loadKpiList();
  this.loadQuestionnaireQuestions();
}

loadQuestionnaireQuestions(): void {
  
  const quarterId = this.selectedQuarter1;
  const departmentId = this.currentEmployeeInfo.departmentId;

  this.questionnaireQuestions = [];
  this.currentQuestionnaireId = null;
  
  if (!quarterId || !departmentId) return;

    this.performanceService.getQuestionnares(departmentId, quarterId).subscribe({
      next: (response: any) => {
        if (response.serviceStatus === 'Success') {
          this.questionnaireQuestions = response.serviceResponse[0].questions;
          console.log('Questionnaire response:',this.questionnaireQuestions);

        } else {
          console.error('Failed to load questionnaire questions:', response.serviceMessage);
        }
      },
      error: (error) => {
        console.error('Error fetching questionnaire questions:', error);
      }
    });
}


loadKpiList(): void {
  const quarterId = this.selectedQuarter1;
  const departmentId = this.currentEmployeeInfo.departmentId;

  this.kpiList = [];
  
  if (!quarterId || !departmentId) return;
  
    this.performanceService.getKraKpi(departmentId, quarterId).subscribe({
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
      goalProgress: this.selectedgoalProgress,
      employeeRemark: this.selectedGoal.employeeRemark,
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

  //context menu
  handleContextMenu(event: MouseEvent) {
    event.preventDefault();
    const selection = window.getSelection();
    if (selection && selection.toString().trim().length > 0) {
      this.selectedText = selection.toString();
      
      // Cast event.target to HTMLElement
      const element = event.target as HTMLElement;
      const rect = element.getBoundingClientRect();
      
      // Get click coordinates
      this.contextMenuX = event.clientX;
      this.contextMenuY = event.clientY;
      
      // Ensure menu stays within viewport
      const viewportWidth = window.innerWidth;
      const viewportHeight = window.innerHeight;
      const menuWidth = 200; // Approximate width of context menu
      const menuHeight = 160; // Approximate height of context menu
      
      // Adjust if menu would go outside viewport
      if (this.contextMenuX + menuWidth > viewportWidth) {
        this.contextMenuX = viewportWidth - menuWidth;
      }
      
      if (this.contextMenuY + menuHeight > viewportHeight) {
        this.contextMenuY = viewportHeight - menuHeight;
      }
      
      this.showContextMenu = true;
    }
  }

  addTag() {
    if (this.selectedText && this.selectedText.trim()) {
      if (!this.userContributionObj.tags) {
        this.userContributionObj.tags = [];
      }
      if (!this.userContributionObj.tags.includes(this.selectedText.trim())) {
        this.userContributionObj.tags.push(this.selectedText.trim());
      }
      this.showContextMenu = false;
    }
  }

  addManualTag() {
    if (this.newTag && this.newTag.trim()) {
      if (!this.userContributionObj.tags) {
        this.userContributionObj.tags = [];
      }
      if (!this.userContributionObj.tags.includes(this.newTag.trim())) {
        this.userContributionObj.tags.push(this.newTag.trim());
      }
      this.newTag = '';
    }
  }
  
  removeTag(index: number) {
    if (this.userContributionObj.tags) {
      this.userContributionObj.tags.splice(index, 1);
    }
  }

  closeContextMenu() {
    this.showContextMenu = false;
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

  getMyContributionList(){
    this.myProjectInsightContributionList = [];
    this.finalContributionList = [];

    let insightObj = {
      employeeRole: this.currentUser.employeeRole,
      empId: this.currentUser.empId   
    };

    this.projectInsightService.getContibutionByEmpId(insightObj).pipe(first()).subscribe({
      next: (response: any) => {
        this.myProjectInsightContributionList = response;
        this.finalContributionList = response;
      },
      error: (error) => {
        console.log(error);
      }
    });
  }

  extractPlainText(htmlContent: string): string {
    if (!htmlContent) return '';
    const tempElement = document.createElement('div');
    tempElement.innerHTML = htmlContent;
    return tempElement.textContent?.trim() || '';
  }
  

  createUserContribution() {
    this.cancelRequest();

    const formData = new FormData();
    const contributionData = { ...this.userContributionObj };
    contributionData.onlyText = this.extractPlainText(this.userContributionObj.response);
    contributionData.empId = this.currentUser.empId;
    const existingDocs = [];
    const newFiles = [];

    if (this.userContributionObj.attachments && this.userContributionObj.attachments.length > 0) {
        this.userContributionObj.attachments.forEach(attachment => {
            if ('isExisting' in attachment) {
                existingDocs.push({
                    documentId: attachment.documentId,
                    documentName: attachment.name
                });
            } else {
                newFiles.push(attachment);
            }
        });
    }

    contributionData.userDocument = existingDocs;
    delete contributionData.attachments;

    formData.append('userContribution', new Blob([JSON.stringify(contributionData)], {
        type: 'application/json'
    }));
    newFiles.forEach(file => {
        formData.append('attachments', file);
    });

    this.projectInsightService.createUserContribution(formData).pipe(first()).subscribe({
      next: (response: any) => {
        this.alertMessage = response.serviceMessage;
        this.modalRef = this.modalService.show(this.alertModal, { class: 'modal-sm' });

        this.getMyContributionList();
      },
      error: (error) => {
        this.alertMessage = error.serviceMessage;
        this.modalRef = this.modalService.show(this.alertModal, { class: 'modal-sm' });
      }
    });
  }

  previewDocument(file: any, previewElementId: any) {
    let documentObj = new Document();
    documentObj.empId = this.currentUser.empId;
    documentObj.documentName = file.name;
    this.showPreviewDiv = true;

    this.projectInsightService.getUserUploadedFileForQuestion(documentObj)
      .subscribe({
        next: (response: any) => {
          const previewContainer = document.getElementById(previewElementId);
          if (file instanceof File) {
            const objectUrl = URL.createObjectURL(file);

            if (file.type === 'application/pdf') {
              previewContainer.innerHTML = `
                      <iframe src="${objectUrl}" type="application/pdf" width="100%" height="800px"></iframe>`;
            }
            else if (file.type.startsWith('image/')) {
              previewContainer.innerHTML = `
                      <img src="${objectUrl}" class="img-fluid" style="max-height:800px;" />`;
            }
            setTimeout(() => {
              URL.revokeObjectURL(objectUrl);
            }, 100);
          } else{
            if (response.serviceStatus === 'Success' && response.serviceResponse?.body) {

              const base64Data = response?.serviceResponse?.body;
              let contentTypeList = response?.serviceResponse?.headers["Content-Type"];
              let contentType = contentTypeList[0]
              if (contentType == 'application/pdf') {
                previewContainer.innerHTML = `<embed src="data:application/pdf;base64,${base64Data}" type="application/pdf" width="100%" height="800px" />`;
              }
              else if (contentType.startsWith('image/')) {
                previewContainer.innerHTML = `<img src="data:${contentType};base64,${base64Data}" class="img-fluid" style="max-height:800px;" />`;
              }
            } else {
              this.alertMessage = "Failed to load document";
              this.modalRef = this.modalService.show(this.alertModal, { class: 'modal-sm' });
            }
          }
        },
        error: (error) => {
          this.alertMessage = "Error loading document";
          this.modalRef = this.modalService.show(this.alertModal, { class: 'modal-sm' });
          console.error('Error loading document:', error);
        }
      });
  }
  
  getUserContributionForReview(){
    this.getUserContributionForReviewList = [];
    this.finalContributionList = [];

    let userContributionObj = new UserContribution();
    userContributionObj.assignTo = this.currentUser.empId;
    this.projectInsightService.getUserContributionForReview(userContributionObj).pipe(first()).subscribe({
      next: (response: any) => {
        this.getUserContributionForReviewList = response;

        if(this.getUserContributionForReviewList != undefined
          && this.getUserContributionForReviewList != null &&
          this.getUserContributionForReviewList.length > 0){
            this.showReviewButton = true;
        }else{
          this.showReviewButton = false;
          this.toggleReviewView = false;
        }
      },
      error: (error) => {
        this.alertMessage = error.serviceMessage;
        this.modalRef = this.modalService.show(this.alertModal, { class: 'modal-sm' });
      }
    });
  }

  switchToReviewView() {
    this.toggleReviewView = !this.toggleReviewView;
    if (this.toggleReviewView) {
      this.finalContributionList = this.getUserContributionForReviewList;
    } else {
      this.finalContributionList = this.myProjectInsightContributionList;
    }
  }

  cancelRequestPreReviewer() {
    this.showPreReviewerSelection = false;
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
        this.modalRef = this.modalService.show(this.alertModal, { class: 'modal-sm' });
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
    userContributionObj.status = statusType;
    userContributionObj.assignTo = projectUSerContributionObj.assignTo;
    userContributionObj.projectId = projectUSerContributionObj.projectId;
    userContributionObj.remark = projectUSerContributionObj.remark;

    this.projectInsightService.processUserContribution(userContributionObj).pipe(first()).subscribe({
      next: (response: any) => {
        this.alertMessage = response.serviceMessage;
        this.modalRef = this.modalService.show(this.alertModal, { class: 'modal-sm' });

        this.getUserContributionForReview();
      },
      error: (error) => {
        this.alertMessage = error.serviceMessage;
        this.modalRef = this.modalService.show(this.alertModal, { class: 'modal-sm' });
      }
    });
  }

  openAddContributionModal(template: TemplateRef<any>){
    this.userContributionObj = new UserContribution();
    this.showPreviewDiv = false
    this.modalRef = this.modalService.show(template, { class: 'modal-xl' });
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

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files) {
      const files = Array.from(input.files);
      if (!this.userContributionObj.attachments) {
        this.userContributionObj.attachments = [];
      }
      files.forEach(file => {
        if (this.isValidFileType(file)) {
          this.userContributionObj.attachments.push(file);
        } else {
          this.alertMessage = `File type not allowed: ${file.name}. Only PNG, JPG, and PDF files are accepted.`;
          this.modalRef = this.modalService.show(this.alertModal, { class: 'modal-sm' });
        }
      });
      input.value = '';
    }
  }

  removeFile(index: number): void {
    if (this.userContributionObj.attachments) {
      this.userContributionObj.attachments.splice(index, 1);
    }
  }

  private isValidFileType(file: File): boolean {
    const allowedTypes = ['image/png', 'image/jpeg', 'application/pdf'];
    return allowedTypes.includes(file.type);
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

  getAllProjectInsightContributionList() {
    this.sortColumn = [];
    this.sortColumnType = [];
    this.sortDirection = '';
    this.allProjectInsightList = [];

    let insightObj = {
      employeeRole: this.currentUser.employeeRole,
      empId: this.currentUser.empId,
      performanceTabName : 'Performance Dashboard'      
    };

    this.projectInsightService.getAllProjectInsightContributionList(insightObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allProjectInsightList = response.serviceResponse;
        this.allProjectInsightList.forEach(project => {
          project.createdOn = (project.createdOn) ? moment(project.createdOn).format(AppComponent.LOCAL_DATE_FORMAT) : null;
        });
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  openProjectInsightResponeMod(insightResponseTemplate: TemplateRef<any>) {
    this.projectResponseModalRef = this.modalService.show(insightResponseTemplate, { class: 'modal-xl', ignoreBackdropClick: true, keyboard: false });
  }

  openUserContributionModal(projectObj: any, contributionModal: TemplateRef<any>) {
    this.userContributionObj = projectObj;

    if(this.showReviewButton){
      this.editorConfig = {
        ...this.editorConfig,
        editable: !this.showReviewButton,
        showToolbar: !this.showReviewButton
      };
    }

    this.userContributionObj.attachments = [];
    if (projectObj.userDocument && projectObj.userDocument.length > 0) {
      projectObj.userDocument.forEach(doc => {
        this.userContributionObj.attachments.push({
          name: doc.documentName,
          documentId: doc.documentId,
          isExisting: true
        });
      });
    }

    this.showPreviewDiv = false;
    this.modalRef = this.modalService.show(contributionModal, { class: 'modal-xl' });
  }

  getAllProjectInsightResponsesByProjectId(projectObj: any, alertTemplate: TemplateRef<any>, insightResponseTemplate: TemplateRef<any>, isPreview: any) {
    this.projectId = projectObj.projectId;
    this.subActionType = 'Submit/View Response';
    this.responseByEmpId = this.currentUser.empId;
    this.openProjectInsightResponeMod(insightResponseTemplate);
  }

  closeProjectInsightResponseModal() {
    this.projectResponseModalRef.hide();
  }

  cancelRequest() {
    this.showPreReviewerSelection = false;
    this.modalRef.hide();
    this.modalService.hide();
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
}