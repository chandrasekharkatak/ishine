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
import { ProjectInsightQuestion } from 'src/app/models/projectInsightQuestion';
import { ProjectInsightService } from 'src/app/services/project-insight.service';
import { ProjectQuestion } from 'src/app/models/projectQuestion';
import { ValidationService } from 'src/app/services/validation.service';
import { Document } from 'src/app/models/document';
import * as moment from 'moment';
import { AppComponent } from 'src/app/app.component';
import { ProjectResponse } from 'src/app/models/projectResponse';
import { Sort } from '@angular/material/sort';


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

  filters: any = {};
  page = 1;
  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType: any;

  projectInsightQuestion: ProjectInsightQuestion = new ProjectInsightQuestion();

  allProjectInsightList: any[] = [];
  projectInsightColumnColumns: any[] = ['projectName', 'description', 'isActive', 'createdByName', 'createdOn'];
  projectInsightQuestionList: ProjectInsightQuestion[] = [new ProjectInsightQuestion()];
  projectInsightResponseList: ProjectInsightQuestion[] = [new ProjectInsightQuestion()];

  projectResponseModalRef: BsModalRef = new BsModalRef();
  documentPreviewModalRef: BsModalRef = new BsModalRef();

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

  toggleSearch() {
    this.isSearchEnabled = !this.isSearchEnabled;
    if (!this.isSearchEnabled) {
      this.filters = {};
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

  onSearch(searchData) {
    this.filters = searchData;
  }

  handlePageChange(event) {
    this.page = event;
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
    this.isResponsePreview = isPreview;
    this.projectInsightResponseList = [];
    this.isFinalResponseSubmitted = false;
    let projObj = new ProjectInsightQuestion();
    projObj.empId = this.viewPerformanceEmpId;
    projObj.projectId = projectObj.projectId;
    projObj.projectManagerId = projectObj.projectManagerId;
    projObj.projectManagerName = projectObj.projectManagerName;
    projObj.employeeRole = this.currentUser.employeeRole;

    this.projectInsightService.getAllProjectInsightResponsesByProjectId(projObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.projectInsightQuestion = response.serviceResponse;
        this.projectInsightResponseList = this.projectInsightQuestion.projectInsightQuestionList;

        this.projectInsightResponseList.forEach((milestone: ProjectInsightQuestion, mileIndex) => {
          milestone.isCollapsed = true;
          if (milestone.projectQuestion != null && milestone.projectQuestion.length != 0) {
            milestone.projectQuestion.forEach((question: ProjectQuestion, index) => {
              if (question.projectResponseList != null && question.projectResponseList.length != 0) {
                question.projectResponseList.forEach((response: ProjectResponse, index) => {
                  response.optionsList = JSON.parse(response.options);
                  if (question.optionType == 'checkbox') {
                    response.responseList = JSON.parse(response.response || '[]');
                    if (response.optionsList?.length) {
                      response.optionsList.forEach((option, index) => {
                        if (response?.responseList.includes(option?.optionValue)) {
                          option.isChecked = true
                        }
                      });
                    }
                  }
                  if (response.isDraft == 'Y') {
                    this.isFinalResponseSubmitted = false;
                  }
                });
              } else {
                let projectResponse = new ProjectResponse();
                projectResponse.optionsList = JSON.parse(question.options);
                if (question.optionType == 'checkbox') {
                  projectResponse.responseList = JSON.parse(projectResponse.response || '[]');
                }
                question.projectResponseList.push(projectResponse);
              }
            });
          }

          if (milestone.moduleList != null && milestone.moduleList.length != 0) {
            milestone.moduleList.forEach((module: any, modIndex) => {
              if (module.projectQuestion != null && module.projectQuestion.length != 0) {
                module.projectQuestion.forEach((question: ProjectQuestion, index) => {
                  if (question.projectResponseList != null && question.projectResponseList.length != 0) {
                    question.projectResponseList.forEach((response: ProjectResponse, index) => {
                      response.optionsList = JSON.parse(response.options);
                      if (question.optionType == 'checkbox') {
                        response.responseList = JSON.parse(response.response || '[]');
                        if (response.optionsList?.length) {
                          response.optionsList.forEach((option, index) => {
                            if (response?.responseList.includes(option?.optionValue)) {
                              option.isChecked = true
                            }
                          });
                        }
                      }
                      if (response.isDraft == 'Y') {
                        this.isFinalResponseSubmitted = false;
                      }
                    });
                  } else {
                    let projectResponse = new ProjectResponse();
                    projectResponse.optionsList = JSON.parse(question.options);
                    if (question.optionType == 'checkbox') {
                      projectResponse.responseList = JSON.parse(projectResponse.response || '[]');
                    }
                    question.projectResponseList.push(projectResponse);
                  }
                });
              }

              if (module.subModuleList != null && module.subModuleList.length != 0) {
                module.subModuleList.forEach((submodule: any, submodIndex) => {
                  if (submodule.projectQuestion != null && submodule.projectQuestion.length != 0) {
                    submodule.projectQuestion.forEach((question: ProjectQuestion, index) => {
                      if (question.projectResponseList != null && question.projectResponseList.length != 0) {
                        question.projectResponseList.forEach((response: ProjectResponse, index) => {
                          response.optionsList = JSON.parse(response.options);
                          if (question.optionType == 'checkbox') {
                            response.responseList = JSON.parse(response.response || '[]');
                            if (response.optionsList?.length) {
                              response.optionsList.forEach((option, index) => {
                                if (response?.responseList.includes(option?.optionValue)) {
                                  option.isChecked = true
                                }
                              });
                            }
                          }
                          if (response.isDraft == 'Y') {
                            this.isFinalResponseSubmitted = false;
                          }
                        });
                      } else {
                        let projectResponse = new ProjectResponse();
                        projectResponse.optionsList = JSON.parse(question.options);
                        if (question.optionType == 'checkbox') {
                          projectResponse.responseList = JSON.parse(projectResponse.response || '[]');
                        }
                        question.projectResponseList.push(projectResponse);
                      }
                    });
                  }
                });
              }
            });
          }
        });
        this.openProjectInsightResponeMod(insightResponseTemplate);
      } else {
        this.openAlertMod(alertTemplate, response.serviceResponse);
      }
    });
  }

  openProjectInsightResponeMod(insightResponseTemplate: TemplateRef<any>) {
    this.projectResponseModalRef = this.modalService.show(insightResponseTemplate, { class: 'modal-xl' });
  }

  closeDocumentPreviewTemplate() {
    this.documentPreviewModalRef.hide();
  }

  closeProjectInsightResponseModal() {
    this.projectResponseModalRef.hide();
  }

  onCheckboxChange(event: any, value: string, question: any, option: any, response: any) {
    if (response.responseList == null || response.responseList == undefined) {
      response.responseList = [];
    }

    if (event.target.checked) {
      option.isChecked = true;
      response.responseList.push(value);
    } else {
      response.responseList = response.responseList.filter((item: string) => item != value);
      option.isChecked = false;
    }
  }

  removeUploadedFile(response: any) {
    if (response?.uploadedFile) {
      response.uploadedFile = null;
    }
    if (response?.uploadedFileName) {
      response.uploadedFileName = null;
    }
  }

  downloadFile(file: any, fileName: any) {
    const reader = new FileReader();
    reader.readAsArrayBuffer(file);
    reader.onload = () => {
      const fileBlob = new Blob([reader.result as ArrayBuffer], { type: file.type });
      if (fileBlob) {
        const url = window.URL.createObjectURL(fileBlob);
        const a = document.createElement('a');
        a.href = url;
        a.download = fileName;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(url);
      }
    }
  }

  onQuestionFileChange(event: any, question: any, alertTemplate: TemplateRef<any>, previewElementId: any, documentPreviewTemplate: TemplateRef<any>, response: any) {
    const file = event.target.files[0];
    if (file) {
      response.uploadedFile = file;
      const inputId = event.target.id;
      var fileExtension = file.name.split('.').pop().toLowerCase();
      response.uploadedFileName = inputId + '.' + fileExtension;
    }

    const MAX_SIZE = 5 * 1024 * 1024;
    if (file) {
      if (file.size > MAX_SIZE) {
        this.alertMessage = "File size must be lesser than or equal to 1MB."
        this.openAlertMod(alertTemplate, this.alertMessage);
        response.uploadedFile = null;
        response.uploadedFileName = null;
        return false;
      }
    }
    this.previewUploadedFile(question, response.uploadedFile, response.uploadedFileName, previewElementId, documentPreviewTemplate, alertTemplate, false, response);
  }

  previewUploadedFile(question, uploadedFile: any, fileName: any, previewElementId: any, documentPreviewTemplate: TemplateRef<any>, alertTemplate: TemplateRef<any>, downloadFile: any, response: any) {

    if ((fileName != undefined && fileName != null)) {
      const MAX_SIZE = 5 * 1024 * 1024;
      const file = uploadedFile;

      if (uploadedFile != undefined && uploadedFile != null) {
        if (file.size > MAX_SIZE) {
          this.documentPreviewModalRef = this.modalService.show(documentPreviewTemplate, { class: 'modal-xl' });
          const previewContainer = document.getElementById(previewElementId);
          previewContainer.innerHTML = "<span class='mt-3' style='display:inline-block;'>File size must be lesser than or equal to 5MB. </span>";
          response.uploadedFile = null;
          response.uploadedFileName = null;
          return false;
        }

        if (file && file.type === 'application/pdf') {
          this.documentPreviewModalRef = this.modalService.show(documentPreviewTemplate, { class: 'modal-xl' });
          const previewContainer = document.getElementById(previewElementId);
          const reader = new FileReader();
          reader.onload = function (e) {
            const pdfData = e.target.result;
            previewContainer.innerHTML = `<embed src="${pdfData}" type="application/pdf" width="100%" height="100%">`;
          };
          reader.readAsDataURL(file);
        }
        else if (file && file.type.startsWith('image/')) {
          this.documentPreviewModalRef = this.modalService.show(documentPreviewTemplate, { class: 'modal-xl' });
          const previewContainer = document.getElementById(previewElementId);
          var fileName2 = file.name;
          var fileExtension = fileName2.split('.').pop().toLowerCase();
          var allowedExtensions = ['jpg', 'jpeg', 'png', 'jpg2'];
          if (allowedExtensions.indexOf(fileExtension) === -1) {
            previewContainer.innerHTML = "<span class='mt-3' style='display:inline-block;'> Please select only image file (jpg, jpeg, png, jpg2) Or PDF </span>";
            response.uploadedFile = null;
            response.uploadedFileName = null;
            return;
          }
          const reader = new FileReader();
          reader.onload = function (e) {
            const pdfData = e.target.result;
            previewContainer.innerHTML = `<img src="${pdfData}" class="img-fluid"  width="100%" height="100%">`;
          };
          reader.readAsDataURL(file);
        }
        else {
          if (downloadFile) {
            this.downloadFile(file, fileName);
            return;
          }
        }
      } else {
        this.documentPreviewModalRef = this.modalService.show(documentPreviewTemplate, { class: 'modal-xl' });
        const previewContainer = document.getElementById(previewElementId);
        this.getUserUploadedFileForQuestion(question, fileName, previewContainer, alertTemplate);
      }
    } else {
      this.documentPreviewModalRef = this.modalService.show(documentPreviewTemplate, { class: 'modal-xl' });
      const previewContainer = document.getElementById(previewElementId);
      response.uploadedFile = null;
      response.uploadedFileName = null;
      previewContainer.innerHTML = `<h3 style='padding: 12px; display:inline-block;'>No Data Found.</h3>`;
    }
  }

  getUserUploadedFileForQuestion(question: any, fileName: any, previewContainer: any, alertTemplate: TemplateRef<any>) {
    let documentObj = new Document();
    documentObj.empId = this.viewPerformanceEmpId;
    documentObj.documentName = fileName;
    documentObj.typeId = question.entityId;
    documentObj.typeName = question.entityType;
    this.projectInsightService.getUserUploadedFileForQuestion(documentObj).subscribe((response: any) => {
      response = JSON.parse(response)
      if (response.status === 'Fail') {
        previewContainer.innerHTML = "<span class='mt-3' style='display:inline-block;' > Invalid file format.</span>";
      } else {
        const pdfData = response.fileData;
        if (response.contentType == 'application/pdf') {
          previewContainer.innerHTML = `<embed src="data:application/pdf;base64,${pdfData}" type="application/pdf" width="100%" height="100%"> `
        } else {
          previewContainer.innerHTML = `<img src="data:${response.contentType};base64,${pdfData}" type="${response.contentType}" class="img-fluid" >`;
        }
      }
    });
  }

  saveProjectInsightResponse(template: TemplateRef<any>, finalSubmit: any) {
    // if(finalSubmit){
    //   let inputValidated: boolean = this.validateProjectInsightResponse(template,this.projectInsightResponseList,this.projectInsightQuestion);
    //   if (!inputValidated) return;
    // }
    let files: File[] = [];
    for (let milestoneIndex = 0; milestoneIndex < this.projectInsightResponseList.length; milestoneIndex++) {
      let milestone = this.projectInsightResponseList[milestoneIndex];
      if (milestone.projectQuestion?.length) {
        let questionList = milestone.projectQuestion;
        for (let index = 0; index < questionList.length; index++) {
          let question = questionList[index];
          for (let responseIndex = 0; responseIndex < question.projectResponseList.length; responseIndex++) {
            let response = question.projectResponseList[responseIndex];
            response.responseByEmpId = this.viewPerformanceEmpId;
            response.isDraft = finalSubmit ? 'N' : 'Y';
            if (question.optionType == 'checkbox') {
              response.response = JSON.stringify(response.responseList);
            }
            if (response?.uploadedFile != undefined && response?.uploadedFile != null) {
              const renamedFile = new File([response.uploadedFile], response?.uploadedFileName, { type: response.uploadedFile.type });
              files.push(renamedFile);
              response.uploadedFile = null;
            }
          }
        }
      }

      for (let moduleIndex = 0; moduleIndex < (milestone.moduleList?.length || 0); moduleIndex++) {
        let module = milestone.moduleList[moduleIndex];
        if (module.projectQuestion?.length) {
          let moduleQuestionList = module.projectQuestion;
          for (let index = 0; index < moduleQuestionList.length; index++) {
            let question = moduleQuestionList[index];
            for (let responseIndex = 0; responseIndex < question.projectResponseList.length; responseIndex++) {
              let response = question.projectResponseList[responseIndex];
              response.responseByEmpId = this.viewPerformanceEmpId;
              response.isDraft = finalSubmit ? 'N' : 'Y';
              if (question.optionType == 'checkbox') {
                response.response = JSON.stringify(response.responseList);
              }
              if (response?.uploadedFile != undefined && response?.uploadedFile != null) {
                const renamedFile = new File([response.uploadedFile], response?.uploadedFileName, { type: response.uploadedFile.type });
                files.push(renamedFile);
                response.uploadedFile = null;
              }
            }
          }
        }

        for (let submoduleIndex = 0; submoduleIndex < (module.subModuleList?.length || 0); submoduleIndex++) {
          let submodule = module.subModuleList[submoduleIndex];
          if (submodule.projectQuestion?.length) {
            let submoduleProjectQuestion = submodule.projectQuestion;
            for (let index = 0; index < submodule.projectQuestion.length; index++) {
              let question = submoduleProjectQuestion[index];
              for (let responseIndex = 0; responseIndex < question.projectResponseList.length; responseIndex++) {
                let response = question.projectResponseList[responseIndex];
                response.isDraft = finalSubmit ? 'N' : 'Y';
                response.responseByEmpId = this.viewPerformanceEmpId;
                if (question.optionType == 'checkbox') {
                  response.response = JSON.stringify(response.responseList);
                }
                if (response?.uploadedFile != undefined && response?.uploadedFile != null) {
                  const renamedFile = new File([response.uploadedFile], response?.uploadedFileName, { type: response.uploadedFile.type });
                  files.push(renamedFile);
                  response.uploadedFile = null;
                }
              }
            }
          }
        }
      }
    }

    let projObj = new ProjectInsightQuestion();
    projObj.projectId = this.projectInsightQuestion.projectId;
    projObj.projectManagerId = this.projectInsightQuestion.projectManagerId;
    projObj.projectManagerName = this.projectInsightQuestion.projectManagerName;
    projObj.projectInsightQuestionList = this.projectInsightResponseList;
    projObj.empId = this.viewPerformanceEmpId;
    this.projectInsightService.saveProjectInsightResponse(projObj, files).pipe(first()).subscribe((response: any) => {
      this.closeProjectInsightResponseModal();
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showProjectInsight();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  validateProjectInsightResponse(template: TemplateRef<any>, projectInsightResponseList: ProjectInsightQuestion[], projectInsightQuestion: any) {
    for (let milestoneIndex = 0; milestoneIndex < projectInsightResponseList.length; milestoneIndex++) {
      let milestone = projectInsightResponseList[milestoneIndex];

      if (milestone.projectQuestion?.length) {
        if (!this.validateQuestionAndResponse(milestone.projectQuestion, milestoneIndex, template, 'Milestone')) {
          return false;
        }
      }

      for (let moduleIndex = 0; moduleIndex < (milestone.moduleList?.length || 0); moduleIndex++) {
        let module = milestone.moduleList[moduleIndex];

        if (module.projectQuestion?.length) {
          if (!this.validateQuestionAndResponse(module.projectQuestion, moduleIndex, template, 'Milestone - ' + (milestoneIndex + 1) + ' Module')) {
            return false;
          }
        }

        for (let submoduleIndex = 0; submoduleIndex < (module.subModuleList?.length || 0); submoduleIndex++) {
          let submodule = module.subModuleList[submoduleIndex];

          if (submodule.projectQuestion?.length) {
            if (!this.validateQuestionAndResponse(submodule.projectQuestion, submoduleIndex, template, 'Milestone - ' + milestoneIndex + 1 + ' Module - ' + (moduleIndex + 1) + 'Sub-module')) {
              return false;
            }
          }
        }
      }
    }
    return true;
  }

  validateQuestionAndResponse(questionList: ProjectQuestion[], parentIndex: number, template: TemplateRef<any>, parentType: string) {
    for (let index = 0; index < questionList.length; index++) {
      let question = questionList[index];

      if (question.required) {
        let isInvalid =
          question.optionType === 'checkbox'
            ? !Array.isArray(question.responseList) || question.responseList.length === 0
            : this.validationService.validateNullUndefinedEmptyString(question.response) === false;

        if (isInvalid) {
          this.alertMessage = `Please provide response for ${parentType}-${parentIndex + 1} Question ${index + 1} !!`;
          this.openAlertMod(template, this.alertMessage);
          return false;
        }
      }
    }
    return true;
  }

}