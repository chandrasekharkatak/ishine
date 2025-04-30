import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { BsModalService, BsModalRef } from 'ngx-bootstrap/modal';
import { Sort } from '@angular/material/sort';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { Employee } from 'src/app/models/employee';
import { TeamDashboardService } from 'src/app/services/team-dashboard.service';
import { environment } from 'src/environments/environment';
import { EmployeeService } from 'src/app/services/employee.service';
import { PerformanceService } from 'src/app/services/performance.service';
import { Log } from 'src/app/models/log';
import { Feature } from 'src/app/models/feature';
import { LogService } from 'src/app/services/log.service';
import { DatePipe } from '@angular/common';

interface GoalResponse {
  serviceStatus: string;
  serviceMessage: string;
  originalError?: any;
  requestData?: any;
  serviceResponse?: any;
}
@Component({
  selector: 'app-team-dashboard',
  templateUrl: './team-dashboard.component.html',
  styleUrls: ['./team-dashboard.component.css'],
})

export class TeamDashboardComponent implements OnInit {
  @ViewChild('bulkAssignTemplate') bulkAssignTemplate: TemplateRef<any>;
  @ViewChild('singleAssignTemplate') singleAssignTemplate: TemplateRef<any>;
  @ViewChild('multiGoalTemplate') multiGoalTemplate: TemplateRef<any>; 
  @ViewChild('assignKRATemplate') assignKRATemplate: TemplateRef<any>;

  

  viewPerformanceEmpId: any;

  currentUser: User;
  feature = 'team_dashboard';
  userMapping: any = {};
  log: Log;
  viewTeamMemberList: any[] = [];
  teamMemberColumns: any[] = ['blank', 'blank', 'employeementId', 'name'];
  selectedEmployeesColumns: any[] = [ 'blank', 'employeementId', 'name'];
  isSearchEnabled: boolean = false;
  isSearchEnabled1: boolean = false;
  filters: any = {};
  filters1: any = {};
  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType: any;
  hodId: number;
  selectedQuarter: any;
  selectedEmployees: any[] = [];
  assignGoalItem: any;
  goalTemplates: any[] = [];
  KraTemplates: any[] = [];
  selectedGoalTemplate: string = '';
  expectedCompletionDate: string = '';
  selectedEmployee: any = null;
  quarterCyclesList: any;
  page = 1;
  page1 = 1;
  quarters: any[] = [];
  loading = false;
  modalRef?: BsModalRef;
  modalRef1?: BsModalRef;

  selectedGoalTemplates: string[] = [];
  errorMessage: string;
  selectedGoalData: {
    quarterId: number;
    templateId: number;
    expectedCompletionDate: string;
  }[] = [];
  today: Date = new Date();
  alertMessage:any;
  selectedKraTemplateId: any;

  constructor(
    private router: Router,
    private http: HttpClient,
    private modalService: BsModalService,
    private authenticationService: AuthenticationService,
    private teamDashboardService: TeamDashboardService,
    private employeeService: EmployeeService,
    private performanceService: PerformanceService,
    private logService: LogService,
    private datePipe: DatePipe
  ) {
    this.authenticationService.currentUser.subscribe(
      (x) => (this.currentUser = x)
    );
  }

  employees: any[] = [];
  goals: any[] = [];

  ngOnInit() {
    this.logService.updateLogInfo(this.log);
    const today = new Date();
    console.log('today date: ', today);

    console.log('Current user:', this.currentUser);
    if (this.currentUser && this.currentUser.hodId) {
      this.hodId = this.currentUser.hodId;
      console.log('HOD ID:', this.hodId);
      this.getTeamEmployeeListInTeamDashboard();
      this.loadGoalTemplates();
      this.loadQuarters();
    } else {
      console.error('Current user or HOD ID is undefined');
    }
    this.fetchQuarters();

    let featureMap: Feature = this.currentUser.userMapping.find(
      (userMap) => userMap.featureName == this.feature
    );
    featureMap.subFeatures?.forEach((sub) => {
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] =
        sub.isActive;
    });

  }

  fetchQuarters(): void {
    this.performanceService.getAllQuarterCycles().subscribe({
      next: (response: any) => {
        if (response.serviceStatus === 'Success') {
          this.quarterCyclesList = response.serviceResponse;
          console.log(this.quarterCyclesList);
         
        } else {
          this.errorMessage =
            response.serviceMessage || 'Failed to load quarters.';
        }
      },
      error: (error) => {
        this.errorMessage = error.message || 'Error fetching quarters.';
      },
    });
  }
  

  getTeamEmployeeListInTeamDashboard() {
    this.viewTeamMemberList = [];
    let empObj = {
      empId: this.currentUser.empId,
      employeeRole: this.currentUser.employeeRole,
      departmentId: this.currentUser.departmentId
    };
  
    this.performanceService.getTeamEmployeeListInTeamDashboard(empObj).subscribe(
      (response: any) => {
        
        this.viewTeamMemberList = response.filter(employee => employee.empId !== this.currentUser.empId);
        console.log(this.viewTeamMemberList);
      },
      (error) => {
        console.error('Error fetching team members:', error);
        this.loading = false;
      }
    );
  }
  
  // getEmployeesInDepartment() {
  //   this.teamDashboardService.findEmployeesInSameDepartmentAsCurrentUser(this.hodId).subscribe(
  //     (response: any) => {
  //       console.log('Raw response:', JSON.stringify(response))
  //       if (response && response.serviceStatus && 
  //           response.serviceStatus.toUpperCase() === 'SUCCESS') {
  //         this.viewTeamMemberList = response.serviceResponse.map(employee => {
  //           return {
  //             id: employee.empId,
  //             name: employee.name,
  //             department: this.currentUser.departmentName || 'N/A',
  //             employeementId: employee.employeementId,
  //             totalGoals: employee.noOfGoals || 0,
  //             goalsCompleted: employee.goalsCompleted || 0
  //           };
  //         });
  //         console.log('Employees fetched successfully:', this.viewTeamMemberList);
  //       } else {
  //         console.error('Error in response:', response);
  //         this.viewTeamMemberList = [];
  //       }
  //       this.loading = false;
  //     },
  //     (error) => {
  //       console.error('Error fetching department members:', error);
  //       this.viewTeamMemberList = [];
  //       this.loading = false;
  //     }
  //   );
  // }
  
  loadQuarters() {
    console.log('Loading quarters...');
    this.http.get(`${environment.baseUrl}api/quarters`).subscribe(
      (response: any) => {
        console.log('Quarters response:', response);
        if (
          response &&
          response.serviceStatus &&
          response.serviceStatus.toUpperCase() === 'SUCCESS'
        ) {
          this.quarters = response.serviceResponse || [];
          console.log('Processed quarters:', this.quarters);
        } else {
          console.error('Error loading quarters:', response);
          this.quarters = [];
        }
      },
      (error) => {
        console.error('Error fetching quarters:', error);
        this.quarters = [];
      }
    );
  }



  loadGoalTemplates() {
    console.log('Loading goal templates...');
    this.http.get(`${environment.baseUrl}api/goal-templates`).subscribe(
      (response: any) => {
        console.log('Goal templates response:', response);
        if (
          response &&
          response.serviceStatus &&
          response.serviceStatus.toUpperCase() === 'SUCCESS'
        ) {
          this.goalTemplates = response.serviceResponse || [];
          console.log('Processed goal templates:', this.goalTemplates);
        } else {
          console.error('Error loading goal templates:', response);
          this.goalTemplates = [];
        }
      },
      (error) => {
        console.error('Error fetching goal templates:', error);
        this.goalTemplates = [];
      }
    );
  }
  loadKraTemplates() {
    const departmentId = this.currentUser.departmentId
    console.log('Loading Kra templates...');
    this.http.get(`${environment.baseUrl}api/kpi/department/${departmentId}`).subscribe(
      (response: any) => {
        console.log('Goal templates response:', response);
        if (
          response &&
          response.serviceStatus &&
          response.serviceStatus.toUpperCase() === 'SUCCESS'
        ) {
          this.KraTemplates = response.serviceResponse || [];
          console.log('Processed KRA templates:', this.KraTemplates);
        } else {
          console.error('Error loading KRA templates:', response);
          this.KraTemplates = [];
        }
      },
      (error) => {
        console.error('Error fetching goal templates:', error);
        this.goalTemplates = [];
      }
    );
  }

  toggleSearch() {
    this.isSearchEnabled = !this.isSearchEnabled;
    if (!this.isSearchEnabled) {
      this.filters = {};
    }
  }
  toggleSearch1() {
    this.isSearchEnabled1 = !this.isSearchEnabled1;
    if (!this.isSearchEnabled1) {
      this.filters = {};
    }
  }

  handlePageChange(event) {
    this.page = event;
  }
  handlePageChange1(event) {
    this.page1 = event;
  }

  sortData(sort: Sort) {
    if (sort.active) {
      let sortParams: any[] = sort.active?.split('|');
      this.sortColumn = sortParams[0];
      this.sortColumnType = sortParams[1];
      this.sortDirection = sort.direction;
    }
  }

  onSearch(searchData) {
    this.filters = searchData;
  }
  onSearch1(searchData) {
    this.filters1 = searchData;
  }

  getEmployeePerformance(viewTeamMember) {
    this.employeeService.setEmployee(viewTeamMember);
    this.router.navigate(['/user-performance/view-performance', viewTeamMember.empId]);
  }

  toggleEmployeeSelection(employee: any) {
    console.log('Toggling selection for employee:', employee);
    const index = this.selectedEmployees.findIndex((e) => e.empId === employee.empId);
    if (index === -1) {
      this.selectedEmployees.push(employee);
    } else {
      this.selectedEmployees.splice(index, 1);
    }
    console.log('Selected employees:', this.selectedEmployees);
  }

  isEmployeeSelected(employee: any): boolean {
    return this.selectedEmployees.some((e) => e.empId === employee.empId);
  }

  toggleAllSelection(event: any) {
    if (event.target.checked) {
      this.selectedEmployees = [...this.viewTeamMemberList];
    } else {
      this.selectedEmployees = [];
    }
  }

  openBulkAssignModal(template: TemplateRef<any>) {
    console.log('Opening bulk assign modal for:', this.selectedEmployees);
    this.modalRef = this.modalService.show(template, {
      class: 'modal-lg',
    });
  }

  months: { full: string, short: string }[] = [
    { full: 'January', short: 'JAN' }, { full: 'February', short: 'FEB' }, { full: 'March', short: 'MAR' },
    { full: 'April', short: 'APR' }, { full: 'May', short: 'MAY' }, { full: 'June', short: 'JUN' },
    { full: 'July', short: 'JUL' }, { full: 'August', short: 'AUG' }, { full: 'September', short: 'SEP' },
    { full: 'October', short: 'OCT' }, { full: 'November', short: 'NOV' }, { full: 'December', short: 'DEC' }
  ];
  
  futureDateFilter = (date: Date | null): boolean => {
    if (!date) return false;
    
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    
    if (!this.selectedQuarter) return false;
    
    const selectedQuarterCycle = this.quarterCyclesList.find(
      q => q.quarterId === Number(this.selectedQuarter)
    )?.quarterCycle;
    
    if (!selectedQuarterCycle) return true;
    
    const [startMonthShort, endMonthShort] = selectedQuarterCycle.split('-');
    
    const startMonthIndex = this.months.findIndex(m => m.short === startMonthShort);
    const endMonthIndex = this.months.findIndex(m => m.short === endMonthShort);
    
    if (startMonthIndex === -1 || endMonthIndex === -1) return true;
    
    const currentYear = today.getFullYear();
    
    const startDate = new Date(currentYear, startMonthIndex, 1);
    startDate.setHours(0, 0, 0, 0);
    
    let endYear = currentYear;
    if (endMonthIndex < startMonthIndex) endYear++;
    
    const lastDay = new Date(endYear, endMonthIndex + 1, 0).getDate();
    const endDate = new Date(endYear, endMonthIndex, lastDay);
    endDate.setHours(23, 59, 59, 999);
    
    const isCurrentQuarter = today >= startDate && today <= endDate;
    
    if (isCurrentQuarter) {
      return date >= today && date <= endDate;
    } else {
      return date >= startDate && date <= endDate;
    }
  };

  setCurrentQuarter():void{
    
  }

  onQuarterChange(): void {
    this.selectedGoalData.forEach(goal => {
      if (goal.expectedCompletionDate) {
        const isValid = this.futureDateFilter(new Date(goal.expectedCompletionDate));
        if (!isValid) {
          goal.expectedCompletionDate = null;
        }
      }
    });

    if(this.expectedCompletionDate){
      const isValid = this.futureDateFilter(new Date(this.expectedCompletionDate));
      if(!isValid){
        this.expectedCompletionDate = null;
      }
    }

  }

  disableManualDateInput() {
    return false;
  }

  closeModal() {
    if (this.modalRef) {
      this.modalRef.hide();
    }
  }

  assignGoalToEmployee(employee: any) {
    console.log('Setting up to assign goal to employee:', employee);
    this.selectedEmployee = employee;
    this.selectedEmployees = [employee]; 
    this.modalRef = this.modalService.show(this.singleAssignTemplate, {
      class: 'modal-md',
    });
  }


  openMultiGoalModal(employee: any) {
    console.log('Setting up to assign multiple goals to employee:', employee);
    this.selectedEmployee = employee;
    this.selectedGoalData = []; 
    
    this.loadGoalTemplates();
    this.addNewGoalSelection(); 
    
    this.modalRef = this.modalService.show(this.multiGoalTemplate, {
      class: 'modal-lg',
    });
  }

  openAssignKraModal(employee: any) {
    console.log('Setting up to assign KRA/KPI to employee:', employee);
    this.selectedEmployee = employee;
    
    this.loadKraTemplates();
    
    this.modalRef = this.modalService.show(this.assignKRATemplate, {
      class: 'modal-lg',
    });
  }
  
  addNewGoalSelection() {
    this.selectedGoalData.push({
      templateId: null,
      expectedCompletionDate: '',
      quarterId: null,
    });
  }
  
  removeGoalSelection(index: number) {
    if (this.selectedGoalData.length > 1) {
      this.selectedGoalData.splice(index, 1);
    }
  }

  assignGoalsToEmployees(template: TemplateRef<any>) {
    console.log('Attempting to assign goals to multiple employees');
  
    if (
      !this.selectedGoalTemplate ||
      !this.expectedCompletionDate ||
      this.selectedEmployees.length === 0 ||
      !this.selectedQuarter
    ) {
      console.error('Missing required data for bulk goal assignment');
      this.alertMessage = "Please select a goal template, set an expected completion date, select a quarter, and select at least one employee.";
      this.openAlertMod(template, this.alertMessage);
      return;
    }
    
    // Check if current user is in the selected employees list
    // Assuming this.currentUser contains the current user's information
    const selfAssignment = this.selectedEmployees.some(employee => employee.empId === this.currentUser.empId);
    
    if (selfAssignment) {
      this.alertMessage = "You cannot assign goals to yourself. Please remove yourself from the selected employees list.";
      this.openAlertMod(template, this.alertMessage);
      return;
    }
  
    // Extract only the employee IDs
    const empIds = this.selectedEmployees.map((empId) => empId.empId);
  
    // Format the date as required by your backend (assuming YYYY-MM-DD)
    const formattedDate = this.formatDate(this.expectedCompletionDate);
  
    const requestBody = {
      empIds: empIds,
      templateId: Number(this.selectedGoalTemplate),
      expectedCompletionDate: formattedDate,
      quarterId: Number(this.selectedQuarter),
    };
  
    console.log('Sending bulk assignment request:', requestBody);
  
    this.http
      .post(`${environment.baseUrl}api/EmployeeGoals/assign-bulk`, requestBody)
      .subscribe({
        next: (response: any) => {
          console.log('Bulk assignment response:', response);
          
          // Check for success without relying on specific response structure
          if (response && (
            (response.serviceStatus && response.serviceStatus.toUpperCase() === 'SUCCESS') || 
            Array.isArray(response) || 
            response.length > 0
          )) {
            this.selectedEmployees.forEach((emp) => {
              const empInList = this.viewTeamMemberList.find(
                (e) => e.id === emp.id
              );
  
              if (empInList) {
                empInList.totalGoals = (empInList.totalGoals || 0) + 1;
              }
            });
  
            this.closeModal();
            this.selectedEmployees = [];
            this.selectedGoalTemplate = '';
            this.expectedCompletionDate = '';
            this.selectedQuarter = '';
            
            this.getTeamEmployeeListInTeamDashboard();
            this.alertMessage = 'Goals assigned successfully!';
            this.openAlertMod(template, this.alertMessage);
          } else {
            console.error('Error response:', response);
            this.alertMessage = `Error assigning goals: ${response.serviceMessage || 'Unknown error'}`;
            this.openAlertMod(template, this.alertMessage);
          }
        },
        error: (error) => {
          console.error('HTTP error:', error);
          let errorMessage = "Error assigning goals. Please try again.";
          
          // Try to extract more specific error message if available
          if (error.error && error.error.message) {
            errorMessage += ` Details: ${error.error.message}`;
          } else if (error.message) {
            errorMessage += ` Details: ${error.message}`;
          } else if (typeof error === 'string') {
            errorMessage += ` Details: ${error}`;
          }
          
          this.alertMessage = errorMessage;
          this.openAlertMod(template, this.alertMessage);
        }
      });
  }
  
  // Helper function to ensure date is properly formatted
  private formatDate(date: string | Date): string {
    if (!date) return '';
    
    let d: Date;
    if (typeof date === 'string') {
      d = new Date(date);
    } else {
      d = date;
    }
    
    // Return in YYYY-MM-DD format for backend
    return d.toISOString().split('T')[0];
  }
  assignKraToEmployee(template: TemplateRef<any>){
    const requestBody = {
      id : this.selectedKraTemplateId,
      empId: this.selectedEmployee.empId,
      quarterId: this.selectedQuarter

    }
    if( !this.selectedQuarter || !this.selectedKraTemplateId) return false;
    else {
      return this.http.post(`${environment.baseUrl}api/kpi/assign/employeeId/quarterId`, requestBody)
        .subscribe({
         next:(response: any) => {
          if (response.serviceStatus && response.serviceStatus.toUpperCase() === 'SUCCESS'){
            this.alertMessage = 'KRA/KPI assigned successfully!';
            this.openAlertMod(template, this.alertMessage);
          }
          else {
            console.error('Error response:', response);
            this.alertMessage = `Error assigning KRA: ${response.serviceMessage || 'Unknown error'}`;
            this.openAlertMod(template, this.alertMessage);
          }
          (error) => {
            console.error('HTTP error:', error);
            let errorMessage = "Error assigning KRA. Please try again.";
            if (error.error && error.error.message) {
              errorMessage += ` Details: ${error.error.message}`;
            } else if (error.message) {
              errorMessage += ` Details: ${error.message}`;
            } else if (typeof error === 'string') {
              errorMessage += ` Details: ${error}`;
            }
            
            this.alertMessage = errorMessage;
            this.openAlertMod(template, this.alertMessage);
          }

        }
      });
    }

  }
  
  assignMultipleGoalsToEmployee(template: TemplateRef<any>) {

    if (this.selectedEmployee && this.selectedEmployee.empId === this.currentUser.empId) {
        this.alertMessage = "You cannot assign goals to yourself.";
        this.openAlertMod(template, this.alertMessage);
        return;
    }

    const invalidEntries = this.selectedGoalData.some(goal => 
      !goal.templateId || !goal.expectedCompletionDate || !this.selectedQuarter);
    
    if (invalidEntries || this.selectedGoalData.length === 0) {
      console.error('Missing required data for multi-goal assignment');
      this.alertMessage = "Please select a goal template, quarter, and set an expected completion date for each goal."
      this.openAlertMod(template, this.alertMessage);
      return;
    }
    

    
    const assignmentPromises = this.selectedGoalData.map(goal => {
      const requestBody = {
        empId: this.selectedEmployee.empId,
        templateId: Number(goal.templateId),
        expectedCompletionDate: this.datePipe.transform(goal.expectedCompletionDate, 'yyyy-MM-dd'),
        quarterId: Number(this.selectedQuarter) 
      };
      
      return this.http.post<GoalResponse>(`${environment.baseUrl}api/EmployeeGoals/assign`, requestBody)
        .toPromise()
        .catch((error) => {
          // Extract the actual error message from the backend response
          let errorMessage = 'Failed to assign goal';
          if (error.error && error.error.serviceError) {
            errorMessage = error.error.serviceError;
          } else if (error.message) {
            errorMessage = error.message;
          }
          
          console.error('Error assigning goal:', error, requestBody);
          return {
            serviceStatus: 'ERROR',
            serviceMessage: errorMessage,
            originalError: error,
            requestData: requestBody,
          } as GoalResponse;
        });
    });

    Promise.all(assignmentPromises)
      .then((responses: GoalResponse[]) => {
        console.log('All assignment responses:', responses);
        this.loading = false;

        const successfulAssignments = responses.filter(
          (response: GoalResponse) =>
            response &&
            response.serviceStatus &&
            response.serviceStatus.toUpperCase() === 'SUCCESS'
        );

        const failedAssignments =
          responses.length - successfulAssignments.length;

        if (failedAssignments === 0) {
          const empInList = this.viewTeamMemberList.find(
            (e) => e.id === this.selectedEmployee.id
          );
          if (empInList) {
            empInList.totalGoals =
              (empInList.totalGoals || 0) + this.selectedGoalData.length;
          }

          this.closeModal();
          this.selectedEmployee = null;
          this.selectedGoalData = [];
          this.selectedQuarter = '';
          
          this.getTeamEmployeeListInTeamDashboard();
          this.alertMessage = "All goals assigned successfully!"
          this.openAlertMod(template, this.alertMessage); 
        } else {
          console.error('Some assignments failed:', responses);

          if (successfulAssignments.length > 0) {
            // Get the error messages from the failed responses
            const errorMessages = responses
              .filter((r: GoalResponse) => r.serviceStatus !== 'SUCCESS')
              .map((r: GoalResponse) => r.serviceMessage)
              .join(', ');
              
            this.alertMessage = `${successfulAssignments.length} goals assigned successfully, but ${failedAssignments} failed: ${errorMessages}`;
            this.openAlertMod(template, this.alertMessage); 
            this.getTeamEmployeeListInTeamDashboard(); 
          } else {
            // Get the error messages from all responses
            const errorMessages = responses.map((r: GoalResponse) => r.serviceMessage).join(', ');
            this.alertMessage = `Failed to assign goals: ${errorMessages}`;
            this.openAlertMod(template, this.alertMessage);
          }
        }
      })
      .catch((error) => {
        this.loading = false;
        console.error('Fatal error assigning multiple goals:', error);
        
        let errorMessage = "Error assigning goals. Please try again";
        if (error && error.message) {
          errorMessage = `Error: ${error.message}`;
        }
        
        this.alertMessage = errorMessage;
        this.openAlertMod(template, this.alertMessage);
      });
  }

  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef1 = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }
}

