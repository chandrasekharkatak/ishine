import { Component, ElementRef, OnInit, TemplateRef, ViewChild } from '@angular/core';
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
import { DomainService } from 'src/app/services/domain.service';
import { Domain } from 'src/app/models/domain';
import * as Highcharts from 'highcharts';
import { DatePipe } from '@angular/common';
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
import { ExportExcelService } from 'src/app/services/export-excel.service';
import { ProjectInsightImportExportService } from 'src/app/services/project-insight-import-export.service';

interface Goal {
  goalStatus: string;
  goalProgress: any;
  goalId: number;
  goalTitle: string;
  description: string;
  managerRemark: string;
  employeeRemark: string;
  assignedBy: string;
  quarter: string;
  expectedCompletionDate: string;
  createdDate: string;
  remarks:{
    remarkBy: number;
    remarkByName: string;
    date: any;
    remarkText: string;
    id?: number;
  }[];
}

interface questionnaire {
  questionId?: number;
  questionTitle: string;
  questionDescription?: string;
  createdBy?: number;
  quarterId?: number;
  quarterCycle?: string;
  departmentId?: number;
  department?: string;
  response: number;
  questions: {
    id?: number;
    questionText: string;
    response: number;
    remark: string;
  }[];

}
interface questions {
  id?: number;
  existingId?: number; 
  questionText: string;
  managerRating: number;
  managerRemark: string;
  response?: number;   
}

interface remarks{
  remarkBy: number;
  remarkByName: string;
  date: any;
  remarkText: string;
  id?: any;
}

interface Stats {
  goalsCompleted: number;
  goalsRemaining: number;
  kraKpiScore: number;
  questionnaireScore: number;
}

interface AppraisalSummary {
  finalRating: number;
  finalRemarks: string;
  appraisalScore: number;
}

interface kpiList{
remarks: any;
  managerRemark: any;
  managerRating: any;
  progress: number;
  remark: String;
  response:any;
  id:number;
  description: string;
}

interface NewKRA {
  description: string;
  progress: number;
  isEnabled: boolean;
}

interface awards{
  id: number;
  reward_type_name: string;
  remark: string;
}

interface KpiItem {
  id: number;
  description: string;
  progress: number;
  response: number;
  remark: string;
  remarks?: KpiRemark[];
}

interface KpiRemark {
  id: number;
  remark: string;
  createdBy: string;  // This now contains the full name directly
  createdDate: string;
  kresponseId: number;
}

interface QuestionDTO {
  remark: any;
  response: any;
  id: number;
  questionText: string;
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
  @ViewChild('fileInput') fileInput: ElementRef;

  feature = "performance_dashboard";
  userMapping: any = {};
  log: Log;
  responses = [];
  currentUser: User;
  activeTab: string = 'kra-kpi';
  selectedQuarter: number;
  quarterCyclesList: any;
  kraKpiMetrics: any[] = [];
  questionnaireQuestions: questions[] = [];
  kpiList:kpiList[] = [];
  originalProgressValues: Record<number, number> = {};
  selectedgoalProgress: any;
  awards: awards[] = [];
  isLoading = false;
  error: string | null = null;
  chart:any = [] ;

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
    fonts: [{ class: 'arial', name: 'Arial' }],
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
    kraKpiScore: 0,
    questionnaireScore: 0,
  };
  minRating = 3;
  goals: Goal[] = [];

  summary: AppraisalSummary = {
    finalRating: 0,
    finalRemarks: '',
    appraisalScore: 0
  };

  goalRemarks: remarks[] =[{
    remarkBy: 0,
    remarkByName: '',
    date: '',
    remarkText: '',
  }];
  newRemarkText: string = '';
  currentEmployeeInfo: Employee = new Employee();
  userContributionObj: UserContribution = new UserContribution();
  selectedGoal?: Goal;
  modalRef?: BsModalRef;
  modalRef1?: BsModalRef;

  errorMessage: string;
  currentQuestionnaireId: any;

  alertMessage:any;
  domainSpecializationList: any;
  isViewOnly: boolean;
  docModalRef?: BsModalRef;

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
  showReviewButton: boolean = false;
  toggleReviewView: boolean = false;
  showPreReviewerSelection: boolean = false;
  showValidationErrors: boolean = false;
  showPreviewDiv: boolean = false;

  validationErrors: string = '';
  selectedPreReviewer: any = '';

  filters: any = {};
  contributionFilters: any = {};
  isContributionSearchEnabled: boolean = false;
  myContributionPage = 1;
  page = 1;
  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType: any;

  projectInsight: ProjectInsight = new ProjectInsight();

  allProjectInsightList: any[] = [];
  projectInsightColumnColumns: any[] = ['projectName', 'description', 'isActive', 'createdByName', 'createdOn'];
  projectInsightContributionColumns: any[] = ['projectName', 'status', 'createdOn', 'blank']
  employeeList: any[] = [];
  myProjectInsightContributionList: any[] = [];
  getUserContributionForReviewList: any[] = [];
  finalContributionList: any[] = [];
  allProjectList: any[] = [];

  projectResponseModalRef: BsModalRef = new BsModalRef();
  documentPreviewModalRef: BsModalRef = new BsModalRef();

  projectId: any;
  actionType: any = 'Contribution';
  subActionType: any = 'Submit/View Response';
  responseByEmpId: any;

  file: any;
  fileName: any;
  projectInsightExcelObj: ProjectInsight = new ProjectInsight();
  isExcelUploaded: boolean = false;

  constructor(
    private employeeService: EmployeeService,
    private modalService: BsModalService,
    private authenticationService : AuthenticationService,
    private performanceService:PerformanceService,
    private goalService:GoalService,
    private logService:LogService,
    private domainService:DomainService,
    private datePipe: DatePipe,
    private projectInsightService: ProjectInsightService,
    private validationService: ValidationService,
    private projectService: ProjectService,
    private exportExcelService: ExportExcelService,
    private projectInsightImportExportService: ProjectInsightImportExportService,
  ) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  ngOnInit(): void {
    // let featureMap: Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    // featureMap.subFeatures?.forEach(sub => {
    //   this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    // });

    
    this.logService.updateLogInfo(this.log);
    this.domainSpecializationList = [];
    this.isProjectInsightList = true;
    this.loadAwards();
    this.onGetEmployeeInfo();
    this.fetchGoals();
    this.loadPerformanceStats();
    this.getProjectInsightByAssignedToEmpId();
    this.getMyContributionList();
    this.getUserContributionForReview();
    this.getAllProjects();
    this.getEmployeeList();
  }
  
  setChart():void {
    this.renderPieSummaryChart("Goal Progress", "goalChart", this.stats.goalsCompleted, this.stats.goalsRemaining, "Goals", (name) => console.log(`Clicked on ${name}`));
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

    let domainObj = new Domain();
    domainObj.empId = this.currentUser.empId;
    const domainResponse:any = await this.domainService.getDomainSpecializationByEmpId(domainObj).toPromise();
    if (domainResponse.serviceStatus == "Success") {
      this.domainSpecializationList = domainResponse.serviceResponse;

      this.domainSpecializationList.forEach((object:Domain) =>{
        var letters = 'BCDEF'.split('');
        var color = '#';
        for (var i = 0; i < 6; i++) {
          color += letters[Math.floor(Math.random() * letters.length)];
        }

        object.colorCode = color;
      });
    } else {
      console.error(domainResponse.serviceResponse);
    }
  }

  renderPieSummaryChart(chartName: any, chartId: any, goalsCompleted: number, goalsRemaining: number, labelName: any, openMod: any) {
    let colors = ['#DDDF00', '#64E572', '#ED561B', '#FFBF00'];
  
    if (chartId === 'goalChart') {
      colors = ['#DDDF00', '#64E572'];
    }
  
    // Prepare chart data
    const chartData = [
      { name: 'Goals Remaining', y: goalsRemaining },
      { name: 'Goals Completed', y: goalsCompleted }
    ];
  
    Highcharts.chart(chartId, {
      credits: {
        enabled: false
      },
      chart: {
        plotBackgroundColor: null,
        plotBorderWidth: null,
        plotShadow: false,
        type: 'pie'
      },
      title: {
        text: chartName,
        style: {
          fontWeight: 'bold',
          color: '#000000'
        }
      },
      tooltip: {
        pointFormat: '{series.name}: <b>{point.percentage:.1f}%</b>'
      },
      accessibility: {
        point: {
          valueSuffix: '%'
        }
      },
      exporting: {
        enabled: true,
        buttons: {
          contextButton: {
            menuItems: [
              "viewFullscreen",
              "downloadPNG",
              "downloadJPEG",
              "downloadCSV",
              "downloadXLS",
              "downloadPDFDocument"
            ]
          }
        }
      },
      plotOptions: {
        pie: {
          borderWidth: 0,
          allowPointSelect: true,
          cursor: 'pointer',
          events: {
            click(event) {
              if (chartId === 'goalChart') {
                openMod(event.point.name);
              }
            }
          },
          dataLabels: {
            enabled: true,
            format: '<b>{point.name}</b>: {point.y:.1f}'
          },
          showInLegend: true
        }
      },
      legend: {
        enabled: true,
        labelFormatter() {
          const point = this as any;
          return this.name + ` : ${point.y}`;
        }
      },
      series: [{
        name: labelName,
        colorByPoint: true,
        type: undefined,
        data: chartData
      }],
      colors
    });
  }

  fetchQuarters(): void {
    this.performanceService.getAllQuarterCycles().subscribe({
      next: (response: any) => {
        if (response.serviceStatus === 'Success') {
          this.quarterCyclesList = response.serviceResponse;
          this.setDefaultQuarter();
          this.onQuarterChange();
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
    this.loadKpiList();
    this.loadQuestionnaireQuestions();

  }

  loadAwards(): void {
    this.isLoading = true;
    this.error = null;
    
    this.performanceService.getawards(this.currentUser.empId).subscribe({
      next: (data) => {
        this.awards = data;
        console.log('Awards:', this.awards);
      },
      error: (err) => {
        console.error('Error fetching awards:', err);
        this.error = 'Failed to load awards. Please try again.';
      }
    });
  }

  months: { full: string, short: string }[] = [
    { full: 'January', short: 'JAN' }, { full: 'February', short: 'FEB' }, { full: 'March', short: 'MAR' },
    { full: 'April', short: 'APR' }, { full: 'May', short: 'MAY' }, { full: 'June', short: 'JUN' },
    { full: 'July', short: 'JUL' }, { full: 'August', short: 'AUG' }, { full: 'September', short: 'SEP' },
    { full: 'October', short: 'OCT' }, { full: 'November', short: 'NOV' }, { full: 'December', short: 'DEC' }
  ];

  setDefaultQuarter(): void {
    const today = new Date();
    const currentMonth = today.getMonth(); 
    
    const currentQuarter = this.quarterCyclesList.find(quarter => {
      const [startMonthShort, endMonthShort] = quarter.quarterCycle.split('-');
      
      const startMonthIndex = this.months.findIndex(m => m.short === startMonthShort);
      const endMonthIndex = this.months.findIndex(m => m.short === endMonthShort);
      
      if (startMonthIndex === -1 || endMonthIndex === -1) return false;
      
      if (endMonthIndex < startMonthIndex) {
        return currentMonth >= startMonthIndex || currentMonth <= endMonthIndex;
      } else {
        return currentMonth >= startMonthIndex && currentMonth <= endMonthIndex;
      }
    });
    
    if (currentQuarter) {
      this.selectedQuarter = currentQuarter.quarterId.toString();
    } else {
      this.selectedQuarter = this.quarterCyclesList.length > 0 ? 
        this.quarterCyclesList[0].quarterId.toString() : null;
    }
  }

  loadAppraisalSummary(): void {
    const empId = this.currentEmployeeInfo.empId;
    const quarterId = this.selectedQuarter
    console.log('emp id: ',this.currentEmployeeInfo.empId);
    if (!empId) return;

    this.performanceService.getAppraisalSummary(empId,quarterId).subscribe({
      next: (response:any) => {
        this.summary = response.serviceResponse;
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
        console.log('STATS::: ',this.stats);
        this.setChart();
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
    this.selectedgoalProgress = goal.goalProgress;
    this.goalService.getGoalRemarks(this.selectedGoal.goalId).subscribe({
      next: (response: any) => {
        if (response.serviceStatus === 'Success') {
          this.selectedGoal.remarks= response.serviceResponse;
          this.goalRemarks = this.selectedGoal.remarks;
        }
        else {
         this.errorMessage = response.serviceMessage || 'No goal remarks found for this employee.';
        }
      },
        error: (error) => {
          this.errorMessage = error.message || 'Failed to fetch employee goals remarks.';
        },
      
    })


    this.modalRef = this.modalService.show(template, { class: 'modal-lg' });
  }

  initializeQuestions(): void {
    if (this.questionnaireQuestions) {
      this.questionnaireQuestions.forEach(questions => {
        if (questions.managerRating === undefined || questions.managerRating === null) {
          questions.managerRating = 0;
        }
        if (!questions.managerRemark) {
          questions.managerRemark = ''; 
        }
      });
    }
    
    if (this.kpiList) {
      this.kpiList.forEach(kpi => {
        if (kpi.response === undefined || kpi.response === null) {
          kpi.response = 0; 
        }
        if (!kpi.remark) {
          kpi.remark = ''; 
        }
      });
    }
  }

  initializeQuestionnaireData(): void {
    if (this.questionnaireQuestions && this.questionnaireQuestions.length > 0) {
      this.questionnaireQuestions.forEach(question => {
        if (question.managerRating === undefined || question.managerRating === null) {
          question.managerRating = 0;
        }
        
        if (question.managerRemark === undefined || question.managerRemark === null) {
          question.managerRemark = '';
        }
        
        if (question.response === undefined || question.response === null) {
          question.response = 0;
        }
      });
    }
  }

  loadQuestionnaireQuestions(): void {
    const quarterId = this.selectedQuarter;
    const departmentId = this.currentEmployeeInfo.departmentId;
    const empId = this.currentUser.empId;

    this.questionnaireQuestions = [];
    this.currentQuestionnaireId = null;
    
    if (!quarterId || !departmentId) return;

    this.performanceService.getQuestionnares(departmentId, quarterId).subscribe({
      next: (response: any) => {
        if (response.serviceStatus === 'Success') {
          this.questionnaireQuestions = response.serviceResponse[0].questions;
          this.currentQuestionnaireId = response.serviceResponse.questionId;
          console.log('Questionnaire template loaded:', this.questionnaireQuestions);
          
          this.performanceService.getQuestionnaireResponses(empId, quarterId).subscribe({
            next: (responseData: any) => {
              console.log('Saved responses:', responseData);
              
              if (responseData && Array.isArray(responseData) && responseData.length > 0) {
                this.questionnaireQuestions.forEach(question => {
                  const savedResponse = responseData.find((resp: any) => 
                    resp.id === question.id
                  );
                  
                  if (savedResponse) {
                    question.managerRating = savedResponse.managerRating || 0;
                    question.managerRemark = savedResponse.managerRemark || '';
                    console.log(`Found saved response for question ${question.id}:`, question.managerRating);
                  } else {
                    question.managerRating = 0;
                    question.managerRemark = '';
                  }
                });
              } else {
                this.initializeQuestions();
              }
            },
            error: (error) => {
              console.error('Error fetching saved responses:', error);
              this.initializeQuestions();
            }
          });
        } else {
          console.error('Failed to load questionnaire questions:', response.serviceMessage);
        }
      },
      error: (error) => {
        console.error('Error fetching questionnaire questions:', error);
      }
    });
  }
    
  quarterChange2(): void {
    this.loadKpiList();
    this.loadQuestionnaireQuestions();
  }



  loadKpiList(): void {
    const quarterId = this.selectedQuarter;
    const departmentId = this.currentEmployeeInfo.departmentId;

    this.kpiList = [];

    if (!quarterId || !departmentId || !this.currentUser.empId) return;

    this.performanceService.getKraKpi(departmentId, quarterId).subscribe({
      next: (response: any) => {
        if (response.serviceStatus === 'Success') {
          this.kpiList = response.serviceResponse.kpis.map(kpi => ({
            id: kpi.id, 
            description: kpi.description, 
            progress: kpi.progress || 0,
            response: 0,
            remark: '',
            remarks: [] 
          }));
          this.kpiList.forEach(kpi => {
            this.originalProgressValues[kpi.id] = kpi.progress;
          });
          console.log('KPI list loaded:', this.kpiList);
          
          this.performanceService.showresponse(this.currentEmployeeInfo.empId, quarterId).subscribe({
            next: (responseData: any) => {
              console.log('Saved KPI responses:', responseData);
              
              if (responseData && Array.isArray(responseData) && responseData.length > 0) {
                this.kpiList.forEach(kpi => {
                  const savedResponse = responseData.find((resp: any) => 
                    resp.description === kpi.description
                  );
                  
                  if (savedResponse) {                    
                    kpi.response = savedResponse.response || 0;
                    kpi.progress = savedResponse.progress || 0;
                    kpi.remark = savedResponse.remark;
                    this.originalProgressValues[kpi.id] = kpi.progress;
                    // Add remarks history if available
                    if (savedResponse.remarks && Array.isArray(savedResponse.remarks)) {
                      kpi.remarks = savedResponse.remarks.sort((a, b) => {
                        // Sort by date descending (newest first)
                        return new Date(b.createdDate).getTime() - new Date(a.createdDate).getTime();
                      });
                    }
                    
                    console.log(`Found saved response for KPI ${kpi.id}:`, kpi.response);
                  } 
                });
              } 
            },
            error: (error) => {
              console.error('Error fetching saved KPI responses:', error);
              this.initializeQuestions();
            }
          });
        } else {
          console.error('Failed to load KPI list:', response.serviceMessage);
        }
      },
      error: (error) => {
        console.error('Error fetching KPI list:', error);
      }
    });
  }

  saveKpiResponses(template: TemplateRef<any>): void {

    console.log("Response ======> " + JSON.stringify(this.kpiList));

    const response = this.kpiList;
    const empId = this.currentEmployeeInfo.empId;
    const quarterId = this.selectedQuarter;

    this.performanceService.submitKpiResponses(response, empId, quarterId, this.currentUser.empId).subscribe({
      next: (response: any) => {
        if (response.serviceStatus === 'Success') {
          this.loadKpiList();
          this.loadPerformanceStats();
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
      employeeRemark: this.selectedGoal.employeeRemark,
      goalProgress: this.selectedGoal.goalProgress,
      goalTitle: this.selectedGoal.goalTitle,
      description: this.selectedGoal.description,
      expectedCompletionDate: this.selectedGoal.expectedCompletionDate,
      remarks: this.selectedGoal.remarks,
    };

    this.goalService.updateGoal(this.selectedGoal.goalId, this.currentUser.empId, payload).subscribe(
      (response) => {
        if (response.serviceStatus === 'Success') {
          this.alertMessage = "Updates saved successfully!";
          
          if (this.modalRef) {
            this.modalRef.hide();
          }
          
          this.openAlertMod(template, this.alertMessage);
          this.fetchGoals();
        }
      },
      (error) => {
        console.error('Error saving updates:', error);
        this.alertMessage = "Error saving updates";
        this.openAlertMod(template, this.alertMessage);
      }
    );
  }

  saveQuestionnaireResponses(template: TemplateRef<any>): void {
    const empId = this.currentEmployeeInfo.empId;
    const quarterId = this.selectedQuarter;
  
    const preparedQuestions = this.questionnaireQuestions.map(question => ({
      id: question.id || question.existingId,
      questionText: question.questionText,
      response: question.response || 0,
      managerRating: question.managerRating || 0,
      managerRemark: question.managerRemark || ''
    }));
  
    console.log('Submitting questionnaire data:', JSON.stringify(preparedQuestions));
    
    this.performanceService.submitQuestionnaireResponses(
      preparedQuestions,
      empId,
      quarterId
    ).subscribe({
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
    this.loadAppraisalSummary();
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

  getMyContributionList() {
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
          } else {
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

  getUserContributionForReview() {
    this.getUserContributionForReviewList = [];
    this.finalContributionList = [];

    let userContributionObj = new UserContribution();
    userContributionObj.assignTo = this.currentUser.empId;
    this.projectInsightService.getUserContributionForReview(userContributionObj).pipe(first()).subscribe({
      next: (response: any) => {
        this.getUserContributionForReviewList = response;

        if (this.getUserContributionForReviewList != undefined
          && this.getUserContributionForReviewList != null &&
          this.getUserContributionForReviewList.length > 0) {
          this.showReviewButton = true;
        } else {
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

  processUserContribution(statusType: any, projectUSerContributionObj: any) {

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
    } else {
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

  openAddContributionModal(template: TemplateRef<any>) {
    this.userContributionObj = new UserContribution();
    this.showPreviewDiv = false;
    this.showReviewButton = false;
    this.modalRef = this.modalService.show(template, { class: 'modal-xl' });
  }

  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef1 = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }

  onKpiProgressChange(kpi: kpiList, event: Event): void {
    const newValue = +(event.target as HTMLInputElement).value;
    
    if (this.originalProgressValues[kpi.id] === undefined) {
      this.originalProgressValues[kpi.id] = kpi.progress;
    }
    
    const previousValue = this.originalProgressValues[kpi.id];
    
    if (newValue < previousValue) {
      kpi.progress = previousValue;
      (event.target as HTMLInputElement).value = previousValue.toString();
    } 
  }

  onGoalProgressChange(goal : Goal, event: Event): void {
    const newValue = +(event.target as HTMLInputElement).value;
    
    const previousValue = this.selectedgoalProgress;
    
    if (newValue < previousValue) {
      goal.goalProgress = previousValue;
      (event.target as HTMLInputElement).value = previousValue.toString();
    }
  }

  addRemark(): void {
    if (!this.newRemarkText.trim()) return;

    const newRemark: remarks = {
      remarkText: this.newRemarkText,
      remarkBy: this.currentUser.empId,
      remarkByName: this.currentUser.name,
      date: this.datePipe.transform(new Date(), 'yyyy-MM-dd'),
    };

    this.goalRemarks.push(newRemark);
    this.selectedGoal.remarks = this.goalRemarks;

    this.newRemarkText = '';
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
    } else {
      this.contributionFilters = searchData;
    }
  }

  handlePageChange(event: number, table: string): void {
    if (table === 'projectInsight') {
      this.page = event;
    } else if (table === 'contribution') {
      this.myContributionPage = event;
    }
  }

  // getAllProjectInsightContributionList() {
  //   this.sortColumn = [];
  //   this.sortColumnType = [];
  //   this.sortDirection = '';
  //   this.allProjectInsightList = [];

  //   let insightObj = {
  //     employeeRole: this.currentUser.employeeRole,
  //     empId: this.currentUser.empId,
  //     performanceTabName: 'Performance Dashboard'
  //   };

  //   this.projectInsightService.getAllProjectInsightContributionList(insightObj).pipe(first()).subscribe((response: any) => {
  //     if (response.serviceStatus == "Success") {
  //       this.allProjectInsightList = response.serviceResponse;
  //       this.allProjectInsightList.forEach(project => {
  //         project.createdOn = (project.createdOn) ? moment(project.createdOn).format(AppComponent.LOCAL_DATE_FORMAT) : null;
  //       });
  //     } else {
  //       console.error(response.serviceResponse);
  //     }
  //   });
  // }

  getProjectInsightByAssignedToEmpId() {
    this.sortColumn = [];
    this.sortColumnType = [];
    this.sortDirection = '';
    this.allProjectInsightList = [];

    let insightObj = {
      employeeRole: this.currentUser.employeeRole,
      empId: 1,
      performanceTabName: 'Performance Dashboard'
    };

    this.projectInsightService.getProjectInsightByAssignedToEmpId(insightObj).pipe(first()).subscribe(
      (response: any) => {

        this.allProjectInsightList = response?.serviceResponse1 || [];
        this.allProjectInsightList.forEach(project => {
          project.createdOn = (project.createdOn)
            ? moment(project.createdOn).format(AppComponent.LOCAL_DATE_FORMAT)
            : null;
        });

      },
      (error) => {
        console.error('No ProjectInsightByAssignedToEmpId found:', error);
      }
    );
  }

  openProjectInsightResponeMod(insightResponseTemplate: TemplateRef<any>) {
    this.projectResponseModalRef = this.modalService.show(insightResponseTemplate, { class: 'modal-xl', ignoreBackdropClick: true, keyboard: false });
  }

  openUserContributionModal(projectObj: any, contributionModal: TemplateRef<any>) {
    this.userContributionObj = projectObj;

    if (this.showReviewButton) {
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
    this.projectInsightExcelObj = null;
    this.isExcelUploaded = false;
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

  isValidList(list: any[]): boolean {
    return this.validationService.validateNullUndefinedEmptyList(list);
  }

  isValidString(value: any): boolean {
    return this.validationService.validateNullUndefinedEmptyString(value);
  }

  async getAllProjectInsightQuestionsByProjectIdAndEmpId(projectObj: any): Promise<any> {
    let projectInsightObj: any = await this.projectInsightImportExportService.callGetAllProjectInsightQuestionsByProjectIdAndEmpId(projectObj.projectId, this.currentUser.empId, 'Performance Dashboard', this.currentUser.employeeRole, true);
    if (!projectInsightObj) {
      this.openAlertMod(this.alertModal, 'Something went Wrong, while downloading excel.');
    }
  }

  clearFileInput(): void {
    this.file = null;
    this.fileName = null;
    const fileInput = document.getElementById('project-data-input-file') as HTMLInputElement;
    if (fileInput)
      fileInput.value = '';
  }

  uploadFile(): void {
    const fileInput = this.fileInput.nativeElement;
    fileInput.click();
  }

  onFileChange(event: any) {
    const file = event.target.files[0];
    if (file) {
      this.file = file;
      this.fileName = file.name;
      const fileExtension = this.fileName.split(".").pop();
      let elem = document.getElementById('project-data-input-file') as HTMLInputElement;
      if (fileExtension !== 'xlsx' && fileExtension !== 'xls') {
        this.alertMessage = "Only .xlsx file is allowed.";
        this.modalRef = this.modalService.show(this.alertModal, { class: 'modal-sm' });
        this.clearFileInput();
        return false;
      }
    } else {
      this.clearFileInput();
    }
  }

  async uploadXcelData(insightResponseTemplate: TemplateRef<any>): Promise<any> {
    try {
      let projectId: any[] = await this.projectInsightImportExportService.getProjectIdFromExcel(this.file);
      if (!projectId) {
        this.openAlertMod(this.alertModal, "Project Id Not Found in the Uploaded Excel file.");
        return false;
      }

      let excelQuestionList = await this.projectInsightImportExportService.convertJsonDataToEntityQuestionList(this.file);
      let projectInsightObj: any = await this.projectInsightImportExportService.callGetAllProjectInsightQuestionsByProjectIdAndEmpId(projectId, this.currentUser.empId, 'Performance Dashboard', this.currentUser.employeeRole, false);
      if (!projectInsightObj) {
        this.openAlertMod(this.alertModal, 'Something went Wrong, while downloading excel.');
      }
      let dbQuestionList: any = await this.projectInsightImportExportService.getQuestionListByEntityForResponseExcel(projectInsightObj, 'Project');
      let flag = await this.validateQuestionsAndResponseExcelFile(dbQuestionList, excelQuestionList);
      if (!flag) {
        return false;
      }
      let mergedQuestionList = await this.mergeQuestionResponses(dbQuestionList, excelQuestionList);
      console.log(mergedQuestionList);

      this.projectInsightExcelObj = await this.assignMergedQuestionsToParent(projectInsightObj, mergedQuestionList);

      this.isExcelUploaded = true;
      this.subActionType = 'Submit/View Response';
      this.openProjectInsightResponeMod(insightResponseTemplate);
      this.clearFileInput();
    } catch (error) {
      console.log(error);
      this.openAlertMod(this.alertModal, "Failed to process Excel file");
      return false;
    }
  }

  convertToProjectQuestion(dbQuestionListTemp: any[]) {
    let dbQuestionList: ProjectQuestion[] = [];
    if (this.isValidList(dbQuestionListTemp)) {
      for (let question of dbQuestionListTemp) {
        let dbQuestion: ProjectQuestion = new ProjectQuestion();
        dbQuestion.questionId = question.QuestionId;
        dbQuestion.question = question.Question;
        dbQuestion.description = question.Description;
        dbQuestion.optionType = question.OptionType;
        dbQuestion.options = question.Options;
        dbQuestion.entityId = question.EntityId;
        dbQuestion.entityType = question.EntityType;
        dbQuestion.optionsList = question.OptionsList;
        dbQuestion.response = question.Response;
        dbQuestionList.push(dbQuestion);
      }
    }
    return dbQuestionList;
  }

  async validateQuestionsAndResponseExcelFile(dbQuestionList: any[], excelQuestionList: any[]): Promise<any> {
    let excelValidated: any;
    if (!this.isValidList(excelQuestionList)) {
      this.openAlertMod(this.alertModal, "Questions sheet in the Excel file cannot be empty.");
      return false;
    }

    if (!this.isValidList(dbQuestionList)) {
      this.openAlertMod(this.alertModal, "Questions not found for the project ID provided in the uploaded Excel file.");
      return false;
    }

    let questionIdList: any[] = [];
    questionIdList = excelQuestionList.map(question => question?.questionId);
    const duplicates = questionIdList.filter((id, index, self) =>
      id !== null && self.indexOf(id) !== index && self.lastIndexOf(id) === index
    );
    if (this.isValidList(duplicates)) {
      this.openAlertMod(this.alertModal, `Kindly enter unique values for the Question ID in the Questions sheet for ${duplicates}`);
      return false;
    }

    for (let questionIndex = 0; questionIndex < excelQuestionList?.length; questionIndex++) {
      const question = excelQuestionList[questionIndex];
      if (!this.validationService.validateNullUndefinedEmptyString(question?.questionId)) {
        this.openAlertMod(this.alertModal, `Kindly enter the Question ID in the Questions sheet for ${questionIndex + 1}`);
        return false;
      }
      if (!this.validationService.validateNullUndefinedEmptyString(question?.entityId)) {
        this.openAlertMod(this.alertModal, `Kindly enter the Parent ID in the Questions sheet for ${questionIndex + 1}`);
        return false;
      }
      if (!this.validationService.validateNullUndefinedEmptyString(question?.entityType)) {
        this.openAlertMod(this.alertModal, `Kindly enter the Parent Type in the Questions sheet for ${questionIndex + 1}`);
        return false;
      }

      const dbQuestionObjEntityTypeList = dbQuestionList.filter(dbQuestionObj => {
        if (dbQuestionObj?.entityId == question?.entityId && dbQuestionObj?.entityType == question?.entityType) {
          return dbQuestionObj;
        }
      });

      if (!this.isValidList(dbQuestionObjEntityTypeList) && !dbQuestionObjEntityTypeList.includes(question?.questionId)) {
        this.openAlertMod(this.alertModal, `Question with the Question ID ${question?.questionId} in the Questions sheet for ${questionIndex + 1} was not found for the project ID specified in the uploaded Excel file.`);
        return false;
      }

      const dbQuestionObjList = dbQuestionObjEntityTypeList.filter(dbQuestionObj => {
        if (dbQuestionObj.questionId == question?.questionId) {
          return dbQuestionObj;
        }
      });

      const dbQuestion = dbQuestionObjList[0];
      if (dbQuestion?.optionType == 'checkbox') {
        if (this.validationService.validateNullUndefinedEmptyString(question?.response) && question?.response != '[]') {
          let responseList = this.projectInsightImportExportService.parseListToOptions(dbQuestion?.optionType, question?.response);
          if (!this.isValidList(responseList)) {
            this.openAlertMod(this.alertModal, `Kindly provide valid Response in the Questions sheet for ${questionIndex + 1}`);
            return false;
          }
          for (let response of responseList) {
            if (this.isValidList(dbQuestion.optionsList)) {
              let optionsList = dbQuestion.optionsList.map(optionsValue => optionsValue?.optionValue);
              if (!optionsList?.includes(response.optionValue)) {
                this.openAlertMod(this.alertModal, `Kindly provide Response from one of the Provided Options in the Questions sheet for ${questionIndex + 1}`);
                return false;
              }
            }
          }
        }
        if (question?.response == '[]') {
          question.response = null;
        }
      }
    }
    return true;
  }

  async mergeQuestionResponses(dbList: any[], excelList: any[]): Promise<any> {
    if (!this.isValidList(dbList)) {
      return this.isValidList(excelList) ? excelList : [];
    }
    if (!this.isValidList(excelList)) {
      return dbList;
    }
    for (const excelQ of excelList) {
      const dbQ = dbList.find(q => q.questionId === excelQ.questionId);
      if (dbQ) {
        if (this.isValidList(dbQ.projectResponseList)) {
          let dbR = dbQ.projectResponseList[0];
          if (dbR.response != excelQ.response && this.isValidString(dbR.response)) {
            dbQ.isUpdatedFromExcelUpload = 'Yes';
            dbR.response = excelQ.response;
          }
        } else {
          if (this.isValidString(excelQ.response)) {
            dbQ.isUpdatedFromExcelUpload = 'Yes';
          }
          dbQ.projectResponseList = [];
          let projectResponse = new ProjectResponse();
          let options = this.projectInsightImportExportService.parseListToOptions(dbQ.optionType, dbQ.options);
          projectResponse.options = dbQ.optionType != 'text' && options != null ? JSON.stringify(options) : null;
          projectResponse.response = excelQ.response;
          projectResponse.showDocDiv = true;
          dbQ.projectResponseList.push(projectResponse);
        }
        dbQ.actionType = excelQ.ActionType;
      }
    }
    return dbList;
  }

  async assignMergedQuestionsToParent(projectInsightObj: any, mergedQuestionList: any[]): Promise<any> {
    const assignQuestions = (entityList: any[], entityIdKey: string, entityType: string) => {
      entityList?.forEach(entity => {
        entity.questionList = mergedQuestionList?.filter(q => q.entityId === entity[entityIdKey] && q.entityType === entityType) || [];
      });
    };

    projectInsightObj.questionList = mergedQuestionList?.filter(q => q.entityId === projectInsightObj?.projectId && q.entityType === 'Project') || [];

    assignQuestions(projectInsightObj.projectInsightMilestoneList, 'milestoneId', 'Milestone');

    projectInsightObj.projectInsightMilestoneList.forEach(milestone => {
      assignQuestions(milestone.moduleList, 'moduleId', 'Module');

      milestone.moduleList?.forEach(module => {
        assignQuestions(module.subModuleList, 'subModuleId', 'SubModule');

        module.subModuleList?.forEach(subModule => {
          this.assignSubSubModuleQuestionsToParent(subModule.subSubModuleList, 'subModuleId', 'Sub-SubModule', mergedQuestionList);
        });
      });
    });
    return projectInsightObj;
  }

  assignSubSubModuleQuestionsToParent(subSubModuleList: any[], entityIdKey: any, entityType: any, mergedQuestionList: any[]) {
    subSubModuleList?.forEach(entity => {
      entity.questionList = mergedQuestionList?.filter(q => q.entityId === entity[entityIdKey] && q.entityType === entityType) || [];
      if (entity?.subSubModuleList) {
        this.assignSubSubModuleQuestionsToParent(entity?.subSubModuleList, 'subModuleId', 'Sub-SubModule', mergedQuestionList)
      }
    });
  }

}