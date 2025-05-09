import { Component, OnInit, TemplateRef } from '@angular/core';
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
import * as Highcharts from 'highcharts';
// import { Domain } from 'domain';
import { DomainService } from 'src/app/services/domain.service';
import { Domain } from 'src/app/models/domain';
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

interface Stats {
  goalsCompleted: number;
  goalsRemaining: number;
  kraKpiScore: string;
  questionnaireScore: string;
}
interface remarks{
  remarkBy: number;
  remarkByName: string;
  date: any;
  remarkText: string;
  id?: any;
}

interface AppraisalSummary {
  finalRating: number;
  finalRemarks: string;
  appraisalScore: number;
}

interface kpiList{
  remarks: any;
  isFixed: any;
  review: any;
  managerRemark: any;
  managerRating: any;
  progress: any;
  remark: any;
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
interface questions {
  id?: number;
  existingId?: number; 
  questionText: string;
  managerRating: number;
  managerRemark: string;
   
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


@Component({
  selector: 'app-view-performance',
  templateUrl: './view-performance.component.html',
  styleUrls: ['./view-performance.component.css']
})
export class ViewPerformanceComponent implements OnInit {

  currentUser:User;
  feature="view_performance";
  userMapping:any = {};
  log:Log;
  activeTab: string = 'kra-kpi';
  quarterCyclesList: any;
  selectedQuarter:any;
  kraKpiMetrics: any[] = [];
  questionnaireQuestions: questions[] = [];
  currentQuestionnaireId: any;
  awards: awards[] = [];

  kraKpiReviewForm: FormGroup;
  questionnaireReviewForm: FormGroup;

  stats: Stats ={
    goalsCompleted: 0,
    goalsRemaining: 0,
    kraKpiScore: '',
    questionnaireScore: '',
  };
  goalRemarks: remarks[] =[{
    remarkBy: 0,
    remarkByName: '',
    date: '',
    remarkText: '',
  }];
  newRemarkText: string = '';
  goals: Goal[] = [];
  summary?: AppraisalSummary;

  currentEmployeeInfo:Employee = new Employee();
  selectedGoal?: Goal;
  subscription!: Subscription;
  modalRef?: BsModalRef;
  modalRef1?: BsModalRef;
  modalRef2?: BsModalRef;
  newKRAList: NewKRA[] = [];
  errorMessage: string;
  kpiList:kpiList[] = [];
  alertMessage: any;

  viewPerformanceEmpId: any;
  isLoading = false;
  error: string | null = null;
  // domainService: any;
  domainSpecializationList: any;

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
    private domainService:DomainService,
    private datePipe: DatePipe
  ) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  ngOnInit(): void {
    this.logService.updateLogInfo(this.log);

    this.route.params.subscribe((params:Params) => {
      this.viewPerformanceEmpId = params['id'];
    });

    this.onGetEmployeeInfo();
    this.fetchGoals();
    this.loadPerformanceStats();

    this.setActiveTab('kra-kpi');
    this.loadAwards();
    this.domainSpecializationList = [];
    

    let featureMap:Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    featureMap.subFeatures?.forEach(sub => {
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });
  }
  
  setChart():void{
    // let goalsCompleted = 0;
    // let goalsRemaining = 0;

    // this.goals.forEach((goal:Goal) => {
    //   if (goal.goalStatus == 'Completed') {
    //     goalsCompleted++;
    //   } else {
    //     goalsRemaining++;
    //   }
    // });
    this.renderPieSummaryChart("Goal Progress", "goalChart", this.stats.goalsCompleted, this.stats.goalsRemaining, "Goals", (name) => console.log(`Clicked on ${name}`));
  }
  loadAwards(): void {
    this.isLoading = true;
    this.error = null;
    
    this.performanceService.getawards(this.currentUser.empId).subscribe({
      next: (data) => {
        this.awards = data;
        // this.isLoading = false;
        console.log('Awards:', this.awards);
      },
      error: (err) => {
        console.error('Error fetching awards:', err);
        this.error = 'Failed to load awards. Please try again.';
        // this.isLoading = false;
      }
    });
  }
  renderPieSummaryChart(chartName: any, chartId: any, goalsCompleted: number, goalsRemaining: number, labelName: any, openMod: any) {
      let colors = ['#DDDF00', '#64E572', '#ED561B', '#FFBF00'];
    
      if (chartId === 'goalChart') {
        colors = ['#DDDF00', '#64E572'];
      }
    
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
          // this.selectedQuarter = this.quarterCyclesList[0].quarterId;
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




  loadAppraisalSummary(): void { 
    const empId = this.currentEmployeeInfo.empId;
    const quarterId = this.selectedQuarter
    console.log('emp id: ',this.currentEmployeeInfo.empId);
    if (!empId) return;

    this.performanceService.getAppraisalSummary(empId,quarterId).subscribe({
      next: (response:any) => {
        this.summary = response.serviceResponse;
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
        this.setChart();
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

    let domainObj = new Domain();
            domainObj.empId = currentEmp.empId;
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
        
                //console.log(this.domainSpecializationList, " : this.domainSpecializationList");
              } else {
                console.error(domainResponse.serviceResponse);
              }
  }


  setActiveTab(tab: string) {
    this.activeTab = tab;

  }

  openModal(template: TemplateRef<any>, goal: any): void {
    this.selectedGoal = goal;
    

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
    // Initialize questionnaire questions
    // if (this.questionnaireQuestions) {
    //   this.questionnaireQuestions.forEach(question => {
    //     if (question.response === undefined || question.response === null) {
    //       question.response = 0; // Set default value to 0
    //     }
    //     if (!question.remark) {
    //       question.remark = ''; // Set empty remark
    //     }
    //   });
    // }
    
    // Initialize KPI list
    if (this.kpiList) {
      this.kpiList.forEach(kpi => {
        if (kpi.response === undefined || kpi.response === null) {
          kpi.response = 0; // Set default value to 0
        }
        if (!kpi.remark) {
          kpi.remark = ''; // Set empty remark
        }
      });
    }
  }
  initializeQuestionnaireData(): void {
    if (this.questionnaireQuestions && this.questionnaireQuestions.length > 0) {
      this.questionnaireQuestions.forEach(question => {
        // Ensure all required fields have valid default values
        if (question.managerRating === undefined || question.managerRating === null) {
          question.managerRating = 0;
        }
        
        // Important: Initialize manager remarks if missing
        if (question.managerRemark === undefined || question.managerRemark === null) {
          question.managerRemark = '';
        }
        
      });
    }
  }


  loadQuestionnaireQuestions(): void {
    const quarterId = this.selectedQuarter;
    const departmentId = this.currentEmployeeInfo.departmentId;
    // const empId = this.currentUser.empId;
    let currentEmp = new Employee();
    currentEmp.empId = this.viewPerformanceEmpId;

  
    this.questionnaireQuestions = [];
    this.currentQuestionnaireId = null;
    
    if (!quarterId || !departmentId) return;
  
    // First load the questionnaire template
    this.performanceService.getQuestionnares(departmentId, quarterId).subscribe({
      next: (response: any) => {
        if (response.serviceStatus === 'Success') {
          // Store the questions template
          this.questionnaireQuestions = response.serviceResponse[0].questions;
          console.log('Questionnaire template loaded:', this.questionnaireQuestions);
          
          // Now fetch the saved responses
          this.performanceService.getQuestionnaireResponses(currentEmp.empId, quarterId).subscribe({
            next: (responseData: any) => {
              console.log('Saved responses:', responseData);
              
              if (responseData && Array.isArray(responseData) && responseData.length > 0) {
                // Map the saved responses to the questions
                this.questionnaireQuestions.forEach(question => {
                  // Look for the matching response using the question id
                  const savedResponse = responseData.find((resp: any) => 
                    resp.id === question.id
                  );
                  
                  if (savedResponse) {
                    // Update the question with saved response data
                    question.managerRemark = savedResponse.managerRemark || '';
                    question.managerRating = savedResponse.managerRating || 0;
                    // Store the database ID for later updates
                    question.existingId = savedResponse.id;
                  } else {
                    // No saved response found, initialize
                    question.managerRemark = '';
                    question.managerRating = 0;
                    question.existingId = null;
                  }
                });
              } else {
                // No responses found, initialize all questions
                this.initializeQuestionnaireData();
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
  
  saveQuestionnaireResponses(template: TemplateRef<any>): void {
    const empId = this.currentEmployeeInfo.empId;
    const quarterId = this.selectedQuarter;
  
    const preparedQuestions = this.questionnaireQuestions.map(question => ({
      id: question.id || question.existingId,
      questionText: question.questionText,
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


  goBack() {
    const previous = this.performanceService.getPreviousRoute();
    if (previous) {
      this.router.navigateByUrl(previous);
    } else {
      return
    }
  }

  // loadKpiList(): void {
  //   const quarterId = this.selectedQuarter;
  //   const departmentId = this.currentEmployeeInfo.departmentId;
  //   // let currentEmp = new Employee();
  //   // currentEmp.empId = this.viewPerformanceEmpId;
    
  //   this.kpiList = [];
    
  //   if (!quarterId || !this.viewPerformanceEmpId) return;
    
  //   this.performanceService.getKraKpi(this.currentEmployeeInfo.empId,quarterId).subscribe({
  //     next: (response: any) => {
  //       if (response.serviceStatus === 'Success') {
  //         this.kpiList = response.serviceResponse.kpis.map(kpi => ({
  //           id: kpi.id, 
  //           description: kpi.description, 
  //           progress: kpi.progress || 0,
  //           response: 0,
  //           remark: '',
  //         }));
  //         console.log('KPI list loaded:', this.kpiList);
          
  //         this.performanceService.showresponse(this.currentEmployeeInfo.empId, quarterId).subscribe({
  //           next: (responseData: any) => {
  //             console.log('Saved KPI responses:', responseData);
              
  //             if (responseData && Array.isArray(responseData) && responseData.length > 0) {
  //               this.kpiList.forEach(kpi => {
  //                 const savedResponse = responseData.find((resp: any) => 
  //                   resp.description === kpi.description
  //                 );
                  
  //                 if (savedResponse) {                    
  //                   kpi.response = savedResponse.response || 0;
  //                   kpi.progress = savedResponse.progress || 0;
  //                   kpi.remark = savedResponse.remark;
                    
  //                   console.log(`Found saved response for KPI ${kpi.id}:`, kpi.response);
  //                 } 
  //               });
  //             } 
  //           },
  //           error: (error) => {
  //             console.error('Error fetching saved KPI responses:', error);
  //             this.initializeQuestions();
  //           }
  //         });
  //       } else {
  //         console.error('Failed to load KPI list:', response.serviceMessage);
  //       }
  //     },
  //     error: (error) => {
  //       console.error('Error fetching KPI list:', error);
  //     }
  //   });
  // }

  loadKpiList(): void {
    const quarterId = this.selectedQuarter;
    const departmentId = this.currentEmployeeInfo.departmentId;
    
    this.kpiList = [];
    
    if (!quarterId || !this.viewPerformanceEmpId) return;
    
    this.performanceService.getKraKpi(this.currentEmployeeInfo.empId, quarterId).subscribe({
      next: (response: any) => {
        if (response.serviceStatus === 'Success') {
          this.kpiList = response.serviceResponse.kpis.map(kpi => ({
            id: kpi.id, 
            description: kpi.description, 
            progress: kpi.progress || 0,
            response: 0,
            remark: '',
            remarks: [] // Initialize empty remarks array
          }));
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
  
    let currentEmp = new Employee();
    currentEmp.empId = this.viewPerformanceEmpId;
    const quarterId = this.selectedQuarter;
  
    this.performanceService.submitKpiResponses(this.kpiList, currentEmp.empId, quarterId, this.currentUser.empId).subscribe({
      next: (response: any) => {
        if (response.serviceStatus === 'Success') {
          this.alertMessage = "KPI responses submitted successfully!";
          this.openAlertMod(template, this.alertMessage);
          this.loadPerformanceStats();
          this.loadKpiList();
        } else {
          this.alertMessage = `Failed to submit responses: ${response.serviceMessage}`;
          this.openAlertMod(template, this.alertMessage);
        }
      },
      error: (error) => {
        console.error('Error submitting KPI responses:', error);
        this.alertMessage = `Error submitting KPI responses: ${error}`;
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
  
    this.goalService.updateGoal(this.selectedGoal.goalId, this.currentUser.empId,payload).subscribe(
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
    this.modalRef1 = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
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

  deleteKRA(index: number): void {
    this.newKRAList.splice(index, 1);
  }

  toggleEnableKRA(index: number): void {
    this.newKRAList[index].isEnabled = !this.newKRAList[index].isEnabled;
  }

  addAnotherKRA(): void {
    this.newKRAList.push({
      description: '',
      progress: 0,
      isEnabled: false
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