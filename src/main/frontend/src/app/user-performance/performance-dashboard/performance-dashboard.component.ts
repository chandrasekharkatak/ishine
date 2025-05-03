import { Component, OnInit, TemplateRef } from '@angular/core';
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

@Component({
  selector: 'app-performance-dashboard',
  templateUrl: './performance-dashboard.component.html',
  styleUrls: ['./performance-dashboard.component.css']
})
export class PerformanceDashboardComponent implements OnInit {
  feature="performance_dashboard";
  userMapping:any = {};
  log:Log;
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
  selectedGoal?: Goal;
  modalRef?: BsModalRef;
  modalRef1?: BsModalRef;
  newKRAList: NewKRA[] = [];
  modalRef2?: BsModalRef;
  errorMessage: string;
  currentQuestionnaireId: any;

  alertMessage:any;
  domainSpecializationList: any;
  isViewOnly: boolean;

  constructor(
    private employeeService: EmployeeService,
    private modalService: BsModalService,
    private authenticationService : AuthenticationService,
    private performanceService:PerformanceService,
    private goalService:GoalService,
    private logService:LogService,
    private domainService:DomainService,
    private datePipe: DatePipe
  ) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  ngOnInit(): void {
    this.logService.updateLogInfo(this.log);
    this.onGetEmployeeInfo();
    this.fetchGoals();
    this.loadPerformanceStats();
    this.loadAwards();
    this.domainSpecializationList = [];
    
    let featureMap:Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    featureMap.subFeatures?.forEach(sub => {
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });
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
      { name: 'Goals Completed', y: goalsCompleted },
      { name: 'Goals Remaining', y: goalsRemaining }
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
    console.log('Active tab = ',this.activeTab);
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

  loadKpiList(): void {
    const quarterId = this.selectedQuarter;
    const empId = this.currentUser.empId;
    
    this.kpiList = [];
    
    if (!quarterId || !empId) return;
    
    this.performanceService.getKraKpi(empId, quarterId).subscribe({
      next: (response: any) => {
        if (response.serviceStatus === 'Success') {
          this.kpiList = response.serviceResponse.kpis.map(kpi => ({
            id: kpi.id, 
            description: kpi.description, 
            progress: kpi.progress || 0,
            response: 0,
            remark: '',
          }));
          
          this.kpiList.forEach(kpi => {
            this.originalProgressValues[kpi.id] = kpi.progress;
          });
          
          console.log('KPI list loaded:', this.kpiList);
          
          this.performanceService.showresponse(empId, quarterId).subscribe({
            next: (responseData: any) => {
              console.log('Saved KPI responses:', responseData);
              
              if (responseData && Array.isArray(responseData) && responseData.length > 0) {
                this.kpiList.forEach(kpi => {
                  const savedResponse = responseData.find((resp: any) => 
                    resp.description === kpi.description
                  );
                  
                  if (savedResponse) {
                    kpi.response = savedResponse.response || 0;
                    kpi.progress = savedResponse.progress || kpi.progress;
                    kpi.remark = savedResponse.remark || '';
                    console.log(`Found saved response for KPI ${kpi.id}:`, kpi.response);
                  }
                });
              }
            },
            error: (error) => {
              console.error('Error fetching saved KPI responses:', error);
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
    // Create payload with properly formatted KPI responses
    // const payload = this.kpiList.map(kpi => ({
    //   id: kpi.id,
    //   description: kpi.description,
    //   progress: kpi.progress,
    //   response: kpi.response, // Include the response rating
    //   remark: kpi.remark // Include any remarks if needed
    // }));
    
    const empId = this.currentEmployeeInfo.empId;
    const quarterId = this.selectedQuarter;

    // console.log("Sending KPI responses:", payload);

    this.performanceService.submitKpiResponses(this.kpiList, empId, quarterId).subscribe({
      next: (response: any) => {
        if (response.serviceStatus === 'Success') {
          this.alertMessage = "KPI responses submitted successfully!";
          this.openAlertMod(template, this.alertMessage);
          this.loadKpiList();
          this.loadPerformanceStats();
        } else {
          this.alertMessage = `Failed to submit responses: ${response.serviceMessage}`;
          this.openAlertMod(template, this.alertMessage);
        }
      },
      error: (error) => {
        console.error('Error submitting KPI responses:', error);
        this.alertMessage = `Error submitting KPI responses: ${error.message || error}`;
        this.openAlertMod(template, this.alertMessage);
      }
    });
  }
  

  saveUpdates(template: TemplateRef<any>) {

  const payload = {
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
  
  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef1 = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }

  onProgressChange(kpi: kpiList, event: Event): void {
    const newValue = +(event.target as HTMLInputElement).value;
    
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

  openAddKRAModal(template: TemplateRef<any>): void {
    // Initialize with one empty KRA
    this.newKRAList = [{
      description: '',
      progress: 0,
      isEnabled: false
    }];
    
    this.modalRef2 = this.modalService.show(template, { 
      class: 'modal-lg',
      backdrop: 'static',
      keyboard: false
    });
  }

  addAnotherKRA(): void {
    this.newKRAList.push({
      description: '',
      progress: 0,
      isEnabled: false
    });
  }

  deleteKRA(index: number): void {
    this.newKRAList.splice(index, 1);
  }

  toggleEnableKRA(index: number): void {
    this.newKRAList[index].isEnabled = !this.newKRAList[index].isEnabled;
  }

  submitNewKRA(kra: NewKRA, template: TemplateRef<any>): void {
    const empId = this.currentEmployeeInfo.empId;
    const quarterId = this.selectedQuarter;
    
    if (!kra.description.trim()) {
      this.alertMessage = "Description cannot be empty";
      this.openAlertMod(template, this.alertMessage);
      return;
    }
    
    this.performanceService.addNewKRA(kra, empId, quarterId).subscribe({
      next: (response: any) => {
        if (response.serviceStatus === 'Success') {
          this.alertMessage = "KRA added successfully!";
          this.openAlertMod(template, this.alertMessage);
          kra.isEnabled = true;
          this.loadKpiList();
        } else {
          this.alertMessage = `Failed to add KRA: ${response.serviceMessage}`;
          this.openAlertMod(template, this.alertMessage);
        }
      },
      error: (error) => {
        console.error('Error adding new KRA:', error);
        this.alertMessage = `Error adding new KRA: ${error.message || error}`;
        this.openAlertMod(template, this.alertMessage);
      }
    });
  }

  saveAllKRAs(template: TemplateRef<any>): void {
    const unenabled = this.newKRAList.filter(kra => !kra.isEnabled && kra.description.trim());
    
    if (unenabled.length > 0) {
      this.alertMessage = "You have unsaved KRAs. Please enable them or remove them before closing.";
      this.openAlertMod(template, this.alertMessage);
      return;
    }
    
    if (this.modalRef2) {
      this.modalRef2.hide();
    }
    
    this.loadKpiList();
  }


}