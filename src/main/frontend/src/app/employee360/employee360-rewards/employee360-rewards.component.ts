import { Component, OnInit, TemplateRef } from '@angular/core';
import { Rewards } from 'src/app/models/rewards';
import { RewardsServiceService } from 'src/app/services/rewards-service.service';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { Employee } from 'src/app/models/employee';
import { BreadcrumbService } from 'src/app/services/breadcrumb.service';
import { Breadcrumb } from 'src/app/models/breadcrumd';
import { EmployeeRewarsRequest } from 'src/app/models/employeeRewardRequest';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { User } from 'src/app/models/user';
import { Router } from '@angular/router';
import { Sort } from '@angular/material/sort';
import { EmployeeService } from 'src/app/services/employee.service';
import { UtilityService } from 'src/app/services/utility.service';
import * as moment from 'moment';
import { AppComponent } from 'src/app/app.component';
import { first } from 'rxjs/operators';
import { SortPipe } from 'src/app/sort.pipe';
import { ValidationService } from 'src/app/services/validation.service';

@Component({
  selector: 'app-employee360-rewards',
  templateUrl: './employee360-rewards.component.html',
  styleUrls: ['./employee360-rewards.component.css']
})

export class Employee360RewardsComponent implements OnInit {

  modalRef: BsModalRef = new BsModalRef();
  currentEmpId: number = Number(sessionStorage.getItem('empId'));
  currentUser: User;
  currentBreadcrumbList: any[] = [];
  rewardList: any[] = [];
  employeeData: any;
  activeCategoryId: number | null = null;
  rewards: Rewards[] = [];
  isTeam: any;
  selectedReward: Rewards | null = null;
  selectedRewardType: { [key: string]: string } = {};
  selectedIDdprimiryKey: any;
  alertMessage: any;
  employees: Employee[] = [];
  btnstring="create new";
  teams: any[] = [];
  rewardsCategories: Rewards[] = [];
  rewardsCategories1: Rewards[] = [];
  isRewards: boolean = true;
  alertMessageTemplate!: TemplateRef<any>;
  teamRewardList: any[] = [];
  availableYears: number[] = [];
  selectedYear: number | null = null;
  selectedPeriod: string | null = null;
  employeeList: any[] = [];
  employeeList360=new Employee();
  isTeamTableVisible: boolean = false;
  isTeamTableVisible1: boolean = false;
  matchedEmployees: any[] = [];
  rewardsColumns: any[] = ['','rewardCategory', 'rewardTypeName', 'name', 'createdOn','teamName', 'remark'];
  rewardsTeamColumns:any[] = ['','rewardCategoryName','rewardTypeName','name','createdByName','remark','createdOn'];
  page: number = 1;
  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType: any;
  isSearchEnabled: boolean = false;
  filters: any = {};
  items = 10;
  isrewardVisible:boolean=false;
  month: any;
  remarks: string;
  fromDatestr: any;
  todatestr: any;
  ofMonthYear: any;
  ofmonthyear:any;
  sumbitRewards: Rewards = new Rewards();
  allEmployeeList360:any[]=[];
  selectemmpName: string;
  employeeSearchText: any = '';
  constructor(
    private authenticationService: AuthenticationService,
    private rewardsService: RewardsServiceService,
    private modalService: BsModalService,
    private breadcrumbService: BreadcrumbService,
    private employeeService: EmployeeService,
    private utilityService: UtilityService,
     private validationService: ValidationService,
    private router: Router,
  ) {
    const empData = sessionStorage.getItem('AllEmployees');
    const empData360=localStorage.getItem("employee360Data");
    this.employeeList360=JSON.parse(empData360);
  
    if (empData) {
      this.employeeList = JSON.parse(empData);
     
    }
    
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
    this.breadcrumbService.currentBreadcrumb.subscribe(x => this.currentBreadcrumbList = x);

    const navigation = this.router.getCurrentNavigation();
    this.employeeData = navigation?.extras.state?.['employeeData'];
  

  }

  ngOnInit(): void {

    let findbreadcrumbObject = this.currentBreadcrumbList.findIndex(x => x.title == "Rewards");
    if (findbreadcrumbObject >= 0) {
      this.currentBreadcrumbList.splice(findbreadcrumbObject + 1);
      this.breadcrumbService.setBreadcrumbSubject(this.currentBreadcrumbList);
    } else {
      let breadcrumbObject = new Breadcrumb();
      breadcrumbObject.title = "Rewards";
      breadcrumbObject.url = "/employee-360/rewards";
      this.breadcrumbService.addObjectToAddInBreadcrumb(breadcrumbObject);
    }

    this.getRewardsCategories(this.alertMessageTemplate);
    const currentYear = new Date().getFullYear();
    for (let i = currentYear; i >= currentYear - 10; i--) {
      this.availableYears.push(i);
    }
    this.getAllEmployeeFor360View();
  }
  createReward(template: TemplateRef<any>){
    
    if(this.isrewardVisible){
      this.isrewardVisible=false;
      this.btnstring="create new";
    }else{
      this.isrewardVisible=true;
      
     
      this.btnstring="close";
    }
   
    
   
  
  }
  cancelRequest() {
    this.modalRef.hide();
  }

  rewardSubmit(template: TemplateRef<any>){
    this.sumbitRewards.remark = this.remarks;
    this.sumbitRewards.isActive = 1;
    this.sumbitRewards.createdBy = this.currentUser.empId;
    this.sumbitRewards.fromDate = this.fromDatestr;
    this.sumbitRewards.toDate = this.todatestr;
  
    this.sumbitRewards.rewardedTo=this.employeeList360.empId;
    this.sumbitRewards.rewardTypeName = this.selectedReward.selectedType;
    this.sumbitRewards.id = this.selectedIDdprimiryKey;
    this.sumbitRewards.ofmonthyear = this.ofmonthyear;
   
    this.validateRewardsWhileSubmit(template);
    this.rewardsService.submitRewardForEmployee(this.sumbitRewards).subscribe(
      (response: any) => {
        if (response.serviceStatus === 'Success') {

          console.log(response.serviceResponse);
          this.openAlertMod(template, response.serviceMessage);
          this.isRewards = false;
          this.isTeamTableVisible = true;
          this.isTeamTableVisible1=false;
               this.ofmonthyear = ''; 
               this.employeeSearchText = '';
               this.remarks = '';
               this.selectedReward = null;
              
              //  this.activeCategoryId = null;

              if (this.rewardsCategories && this.rewardsCategories.length > 0) {
                this.activeCategoryId = this.rewardsCategories[0].rewardCategoryId;
              }
            
              if (this.rewards && this.rewards.length > 0) {
                this.selectedReward = this.rewards[0];
                this.selectedReward.selectedType= null;
              }
              this.isrewardVisible=false;
              this.isTeamTableVisible=false;
              this.btnstring="create new";

        } else {
          this.openAlertMod(template, 'No reward categories available at the moment.');
        }
      },
      (error) => {
        this.openAlertMod(template, 'Error fetching reward categories. Please try again later.');
      }
    );
  }
  validateRewardsWhileSubmit(template: TemplateRef<any>) {

    if (!this.validationService.validateNullUndefinedEmptyString(this.sumbitRewards.ofmonthyear)) {
      this.alertMessage = "Please select the Month for which Employee is to be rewarded!!";
      this.openAlertMod(template, this.alertMessage);
      return false;
  }
    if (!this.validationService.validateNullUndefinedEmptyString(this.sumbitRewards.rewardedTo)) {
        this.alertMessage = "Please select an Employee to reward !!";
        this.openAlertMod(template, this.alertMessage);
        return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(this.sumbitRewards.rewardTypeName)) {
        this.alertMessage = "Please select a Reward Type !!";
        this.openAlertMod(template, this.alertMessage);
        return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(this.sumbitRewards.remark)) {
        this.alertMessage = "Please enter a Remark !!";
        this.openAlertMod(template, this.alertMessage);
        return false;
    }

    return true;
}
  onEmployeeChange(event: any) {
    this.selectemmpName = event?.option?.value;

    console.log(' this.selectemmpName',  this.selectemmpName);

    if (this.selectemmpName) {
      const filterEMP = this.employees.find(employee => employee.employeeNameForReward === this.selectemmpName);
      console.log('emp---', filterEMP);
      const latestReward = this.rewards[this.rewards.length - 1] || new Rewards();
      console.log('yessss', latestReward);
      this.sumbitRewards.rewardedTo = filterEMP.employeeIdForReward;
      this.sumbitRewards.rewardType = 0;
      this.sumbitRewards.rewardTypeName = latestReward.rewardTypes[0];
      this.sumbitRewards.managerId = filterEMP.managerIdForReward;
      this.sumbitRewards.teamId = null;
      this.sumbitRewards.teamLeadId = null;
    }

    console.log('yessss', this.sumbitRewards);
  }

  getAllEmployeeFor360View(){
    this.allEmployeeList360 = [];
    this.employeeService.getAllEmployeesFor360View().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allEmployeeList360 = response.serviceResponse;
        console.log("allEmployeeListFor360 : ", this.allEmployeeList360)
        this.allEmployeeList360.forEach(employeeObj => {
          employeeObj.employeementId = this.utilityService.appendEmployeementid(employeeObj.isConsultant, employeeObj.employeementId);
          employeeObj.dateOfJoining = (employeeObj.dateOfJoining) ? moment(employeeObj.dateOfJoining).format(AppComponent.DATE_FORMAT) : null;
          employeeObj.dateOfRelieving = (employeeObj.dateOfRelieving) ? moment(employeeObj.dateOfRelieving).format(AppComponent.DATE_FORMAT) : null;
          employeeObj.updatedOn = (employeeObj.updatedOn) ? moment(employeeObj.updatedOn).format(AppComponent.DATETIME_FORMAT) : null;
          employeeObj.createdOn = (employeeObj.createdOn) ? moment(employeeObj.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
          if (employeeObj.isConsultant == 'true')
            employeeObj.employeeType = 'Consultant';
          else if (employeeObj.isApprenticeship == 'true')
            employeeObj.employeeType = 'Apprentice';
          else
            employeeObj.employeeType = 'Regular';
          });
          this.allEmployeeList360 = this.allEmployeeList360;
          this.allEmployeeList360 = new SortPipe().transform(this.allEmployeeList360, ['name', 'string', 'asc']);
        } else {
          alert(response.serviceResponse);
        }
    });
  }
  

  onYearChange(event: Event): void {
    this.isTeamTableVisible = false;
    const selectElement = event.target as HTMLSelectElement;
    this.selectedYear = +selectElement.value; // Convert to number

    

    
    this.selectedPeriod = '';
    this.activeCategoryId = null;
    this.rewardsCategories = this.getCategoriesForYear();
    // this.rewardsCategories = [];
    setTimeout(() => {
      const monthDropdown = document.getElementById('rewardsDropdown') as HTMLSelectElement;
      if (monthDropdown) {
        monthDropdown.value = '';
        this.rewardList = [];
      }
    });
   
    console.log('Selected Year:', this.selectedYear);
  }

  getCategoriesForYear(): any[] {
    
    return [
        { rewardCategoryId: 1, categoryName: 'January' },
        { rewardCategoryId: 2, categoryName: 'February' },
        { rewardCategoryId: 3, categoryName: 'March' },
        { rewardCategoryId: 4, categoryName: 'April' },
        { rewardCategoryId: 5, categoryName: 'May' },
        { rewardCategoryId: 6, categoryName: 'June' },
        { rewardCategoryId: 7, categoryName: 'July' },
        { rewardCategoryId: 8, categoryName: 'August' },
        { rewardCategoryId: 9, categoryName: 'September' },
        { rewardCategoryId: 10, categoryName: 'October' },
        { rewardCategoryId: 11, categoryName: 'November' },
        { rewardCategoryId: 12, categoryName: 'December' }
    ];
}

  onPeriodChange(event: Event): void {
    const selectElement = event.target as HTMLSelectElement;
    this.selectedPeriod = selectElement.value;

    // Reset categories when period changes
    this.activeCategoryId = null;
    this.rewardsCategories = this.getCategoriesForPeriod(this.selectedPeriod);

    console.log('Selected Period:', this.selectedPeriod);
    if (this.selectedPeriod === 'monthly') {
      // Set month dynamically, assuming December if not previously set
      this.month = new Date().getMonth(); // Current month (1-12)
    } 
    // else if (this.selectedPeriod === 'halfYearly') {
    //   // Assume first half = June, second half = December
    //   this.month = new Date().getMonth() < 6 ? '06' : '12';
    // }
  
    // Ensure month is always in 'MM' format
  }

  changeEvent(value: string) {

  }

  async getRewardsCategories(template: TemplateRef<any>) {
    this.rewardsService.getAllRewardsCategory().subscribe(
      (response: any) => {
        if (response.serviceStatus === 'Success' && response.serviceResponse && response.serviceResponse.length > 0) {
          this.rewardsCategories1 = response.serviceResponse;
          const firstCategory = this.rewardsCategories1[0];
          if (firstCategory) {
            this.getRewardsByCategoryId(firstCategory.rewardCategoryId, template);
          }
          this.isRewards = true;
        } else {
          this.openAlertMod(template, 'No reward categories available at the moment.');
        }
      },
      (error) => {
        this.openAlertMod(template, 'Error fetching reward categories. Please try again later.');
      }
    );
  }

  //   onCategoryChange(event: Event, template: TemplateRef<any>): void {
  //     // Cast event.target to HTMLSelectElement to access its value
  //     const selectElement = event.target as HTMLSelectElement;
  //     const categoryId = +selectElement.value; // Convert value to number
  //     this.getRewardsByCategoryId(categoryId, template);
  // }
  refresh(){
    window.location.reload();
  }

  onCategoryChange(event: Event): void {
    this.isTeamTableVisible = false;
    const selectElement = event.target as HTMLSelectElement;
    const categoryId = +selectElement.value; // Convert value to number
    const formattedMonth = categoryId.toString().padStart(2, '0');
    
    this.ofMonthYear = `${this.selectedYear}-${formattedMonth}`;
    console.log("ofMonthYear  ",this.ofMonthYear)

    if (this.selectedYear && categoryId) {
      if (this.ofMonthYear) {
        // Prepare the API request payload
        const request = {
          empId: sessionStorage.getItem('empId'),
          ofMonthYear: this.ofMonthYear,
        };

        // Call the API
        this.rewardsService.getEmployeeRewardByEmpId(request).subscribe(
          (response: any) => {
             if(response.serviceStatus == 'Success'){
              this.rewardList = response.serviceResponse;
              this.rewardList.forEach(reward => {
                let matchingnameempId = this.allEmployeeList360.find(emp => emp.empId === reward.nameId);
                reward.emp360nameempid = matchingnameempId ? matchingnameempId : {}; 
                let matchingEmployee = this.allEmployeeList360.find(emp => emp.empId === reward.createdBY);
                console.log('matches++',matchingEmployee);
                reward.emp360 = matchingEmployee ? matchingEmployee : {};
              });
              console.log('Rewards Details:', response);
             }else{
              this.rewardList=[];
               console.error('No rewards to fetch');
             }
          },
          (error: any) => {
            console.error('Error fetching rewards:', error);
          }
        );
      } else {
        console.error('Invalid date range for category selection.');
      }
    }
  }

  // getStartDateForMonth(year: number, month: number): string {
  //   // Returns the first day of the month in yyyy-MM-dd format
  //   const date = new Date(year, month - 1, 1);
  //   return date.toISOString().split('T')[0];
  // }

  // getEndDateForMonth(year: number, month: number): string {
  //   // Returns the last day of the month in yyyy-MM-dd format
  //   const date = new Date(year, month, 0);
  //   return date.toISOString().split('T')[0];
  // }

  getStartDateForMonth(year: number, month: number): string {
    return `${year}-${month.toString().padStart(2, '0')}-01`;
  }

  getEndDateForMonth(year: number, month: number): string {
    const daysInMonth = new Date(year, month, 0).getDate(); // Get last day of the month
    return `${year}-${month.toString().padStart(2, '0')}-${daysInMonth}`;
  }

  months = [
    { id: 1, name: 'January' },
    { id: 2, name: 'February' },
    { id: 3, name: 'March' },
    { id: 4, name: 'April' },
    { id: 5, name: 'May' },
    { id: 6, name: 'June' },
    { id: 7, name: 'July' },
    { id: 8, name: 'August' },
    { id: 9, name: 'September' },
    { id: 10, name: 'October' },
    { id: 11, name: 'November' },
    { id: 12, name: 'December' },
  ];



  getCategoriesForPeriod(period: string): any[] {
    // Logic to return categories based on selected period
    if (period === 'monthly') {
      return [
        { rewardCategoryId: 1, categoryName: 'January' },
        { rewardCategoryId: 2, categoryName: 'February' },
        { rewardCategoryId: 3, categoryName: 'March' },
        { rewardCategoryId: 4, categoryName: 'April' },
        { rewardCategoryId: 5, categoryName: 'May' },
        { rewardCategoryId: 6, categoryName: 'June' },
        { rewardCategoryId: 7, categoryName: 'July' },
        { rewardCategoryId: 8, categoryName: 'August' },
        { rewardCategoryId: 9, categoryName: 'September' },
        { rewardCategoryId: 10, categoryName: 'October' },
        { rewardCategoryId: 11, categoryName: 'November' },
        { rewardCategoryId: 12, categoryName: 'December' }
        // Add more categories for months...
      ];
    } else if (period === 'halfYearly') {
      return [
        { rewardCategoryId: 101, categoryName: 'H1 (Jan-Jun)' },
        { rewardCategoryId: 102, categoryName: 'H2 (Jul-Dec)' }
      ];
    }
    return [];
  }


  subDropdownOptions: string[] = [];


  // onCategoryChange(event: Event, template: TemplateRef<any>): void {
  //   // Cast event.target to HTMLSelectElement to access its value
  //   const selectElement = event.target as HTMLSelectElement;
  //   const categoryId = +selectElement.value; // Convert value to number

  //   // Populate sub-dropdown options dynamically
  //   if (categoryId === 1) { // Assuming 1 corresponds to "Monthly"
  //       this.subDropdownOptions = [
  //           'January', 'February', 'March', 'April', 
  //           'May', 'June', 'July', 'August', 
  //           'September', 'October', 'November', 'December'
  //       ];
  //   } else if (categoryId === 2) { // Assuming 2 corresponds to "Half-Yearly"
  //       this.subDropdownOptions = ['January - June', 'July - December'];
  //   } else {
  //       this.subDropdownOptions = []; // Clear options for other selections
  //   }

  //   // Call the original logic to fetch rewards
  //   // this.getRewardsByCategoryId(categoryId, template);
  // }

  rewardlist: any[] = [];

  onSubCategoryChange(event: Event): void {
    const selectElement = event.target as HTMLSelectElement;
    const selectedOption = selectElement.value;

    const request = new EmployeeRewarsRequest();
    request.empId = this.currentUser.empId;  // Replace with actual employee ID
    request.fromDate = this.getFromDate(selectedOption);
    request.toDate = this.getToDate(selectedOption);


    this.rewardsService.getEmployeeRewardByEmpId(request).subscribe(
      (response: any) => {
        console.log('*****************');
        this.rewardList = response.rewardsDTO;
        this.rewardList.forEach(reward => {
          let matchingEmployee = this.allEmployeeList360.find(emp => emp.empId === reward.createdBY);
          console.log('matches++',matchingEmployee);
          reward.emp360 = matchingEmployee ? matchingEmployee : {};
        });
        console.log('Rewards Details:', this.rewardList);
      });
  }


  private getFromDate(option: string): string | null {
    if (option.includes('-')) {
      // Half-yearly case
      return option.startsWith('January') ? '2024-01-01' : '2024-07-01';
    } else {
      // Monthly case
      const monthIndex = [
        'January', 'February', 'March', 'April',
        'May', 'June', 'July', 'August',
        'September', 'October', 'November', 'December'
      ].indexOf(option);
      return monthIndex !== -1 ? `2024-${(monthIndex + 1).toString().padStart(2, '0')}-01` : null;
    }
  }

  private getToDate(option: string): string | null {
    if (option.includes('-')) {
      // Half-yearly case
      return option.startsWith('January') ? '2024-06-30' : '2024-12-31';
    } else {
      // Monthly case
      const monthIndex = [
        'January', 'February', 'March', 'April',
        'May', 'June', 'July', 'August',
        'September', 'October', 'November', 'December'
      ].indexOf(option);
      return monthIndex !== -1
        ? `2024-${(monthIndex + 1).toString().padStart(2, '0')}-${new Date(
          2024, monthIndex + 1, 0
        ).getDate()}`
        : null;
    }
  }





  getRewardsByCategoryId(categoryId: number, template: TemplateRef<any>) {
    this.activeCategoryId = categoryId;
    this.rewardsService.getAllRewardsByCategoryId(categoryId).subscribe(
      (response: any) => {
        if (response.serviceStatus === 'Success') {
          this.rewards = response.serviceResponse.map((reward: any) => ({
            ...reward,
            rewardTypes: reward.rewardTypes[0].split(',').map((type: string) => type.trim())
          }));
          console.log("Reward details fetched", this.rewards);

          const firstReward = this.rewards[0];
          if (firstReward) {
            this.isTeam = firstReward.isTeam;
            this.setSelectedReward(firstReward); // Select the first reward initially.
          }
        } else {
          this.openAlertMod(template, 'No rewards found for the selected category.');
        }
      },
      (error) => {
        this.openAlertMod(template, 'Error fetching rewards. Please try again later.');
      }
    );
  }

  setSelectedReward(reward: Rewards) {
    this.selectedReward = reward;
    this.selectReward(reward);
  }

  selectReward(reward: Rewards) {
    this.selectedReward = reward;
    console.log('Selected reward:', reward);
    this.selectedRewardType[reward.rewardName] = '';
    const rewardId = reward.id;
   
    this.selectedIDdprimiryKey = rewardId;
    console.log('Reward ID:', rewardId);
    this.getActiveTeams(reward);
  }

  openAlertMod(template: TemplateRef<any>, message: string) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }



  getActiveTeams(rewards): void {
    const payload = { isTeam: rewards.isTeam };
    this.rewardsService.getAllActiveTeams(payload).subscribe(
      (response: any) => {
        if (response.serviceStatus && response.serviceStatus.toLowerCase() === 'success') {
          if (Array.isArray(response.serviceResponse)) {
            this.teams = response.serviceResponse;
            console.log("Team list:", this.teams);
          } else {
            console.error('Expected serviceResponse to be an array but received:', response.serviceResponse);
          }
        } else {
          console.error('Service failed:', response);
        }
      },
      (error) => {
        console.error('Error fetching teams:', error);
      }
    );
  }

  async openEditModal(template: TemplateRef<any>) {

    //   const selectElement = event.target as HTMLSelectElement;
    // const selectedOption = selectElement.value;

    const request = new EmployeeRewarsRequest();
    request.empId = this.currentEmpId;
    request.ofMonthYear = this.ofMonthYear;

    // request.fromDate = this.getFromDate(selectedOption);
    // request.toDate = this.getToDate(selectedOption);

    this.rewardsService.getTeamRewardByEmpId(request).subscribe(
      (response: any) => {
        this.teamRewardList = response.rewardsDTO;
        this.getMatchingEmployees();
        console.log('Team Rewards Details:', response);
      });

    this.modalRef = this.modalService.show(template, { class: 'modal-xl' });
  }

  getMatchedEmployee(event: any) {
    return this.matchedEmployees.find(employee => employee.empId === event.rewardedTo);
  }
  
  showTeamTable(team: any): void {
 
    this.isTeamTableVisible1 = true;
    this.isTeamTableVisible = true;
    const request = new EmployeeRewarsRequest();
    request.empId = this.currentEmpId;
    request.ofMonthYear = this.ofMonthYear;
    request.teamId = team.teamId;

    this.rewardsService.getTeamRewardByEmpId(request).subscribe(
      (response: any) => {
        this.teamRewardList = response.rewardsDTO;
        this.teamRewardList.forEach(reward => {
          let matchingRewardedTo =  this.allEmployeeList360.find(emp => emp.empId === reward.id);
          reward.emp360rewardedToId = matchingRewardedTo ? matchingRewardedTo : {};
          let matchingEmployeeteam = this.allEmployeeList360.find(emp => emp.empId === reward.createdBy);
          reward.emp360teamcreatedBy = matchingEmployeeteam ? matchingEmployeeteam : {};
        });
        this.getMatchingEmployees();
     
      });
  }

  goBack(): void {
    this.isTeamTableVisible = false;

  }
  
  getMatchingEmployees(): void {
    this.teamRewardList.forEach((empObj: any) => {
      this.employeeList.forEach((listObj: any) => {
        if (empObj.rewardedTo === listObj.empId) {
          this.matchedEmployees.push(listObj);
        }
      });
      console.log('Matched Employees:', this.matchedEmployees);
    },
      (error) => {
        console.error('Error fetching employee data:', error);
      });
  }

  sortData(sort: Sort) {
    //console.log(sort);
    if (sort.active) {
      let sortParams: any[] = sort.active?.split("|");
      this.sortColumn = sortParams[0];
      this.sortColumnType = sortParams[1];
      this.sortDirection = sort.direction;
    }
  }

  toggleSearch() {
    this.isSearchEnabled = !this.isSearchEnabled;
    if (!this.isSearchEnabled) {
      this.filters = {};
    }
  }

  onSearch(searchData){
    if(this.isSearchEnabled == true){
      this.filters = searchData;
      console.log("Check Filter : ", this.filters);
    }
  }

}
