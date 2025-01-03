import { Component, OnInit, TemplateRef } from '@angular/core';
import { Rewards } from 'src/app/models/rewards';
import { RewardsServiceService } from 'src/app/services/rewards-service.service';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { Employee } from 'src/app/models/employee';
import { BreadcrumbService } from 'src/app/services/breadcrumb.service';
import { EmployeeRewarsRequest } from 'src/app/models/employeeRewardRequest';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { User } from 'src/app/models/user';
import { Router } from '@angular/router';
import { SharedService } from 'src/app/services/shared.service';


@Component({
  selector: 'app-employee360-rewards',
  templateUrl: './employee360-rewards.component.html',
  styleUrls: ['./employee360-rewards.component.css']
})

export class Employee360RewardsComponent implements OnInit {



  constructor(
    private authenticationService: AuthenticationService,
    private rewardsService: RewardsServiceService,
    private modalService: BsModalService,
    private breadcrumbService: BreadcrumbService,
    private router:Router,
    private sharedService : SharedService,
  ) { 
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);

    const navigation = this.router.getCurrentNavigation();
        this.employeeData = navigation?.extras.state?.['employeeData'];
        console.log("cheked",this.employeeData); 

   }

  modalRef: BsModalRef = new BsModalRef();
  currentEmpId: number = Number(sessionStorage.getItem('empId'));


  currentUser: User;

  employeeData:any;

  activeCategoryId: number | null = null;
  rewards: Rewards[] = [];
  isTeam: any;
  selectedReward: Rewards | null = null;
  selectedRewardType: { [key: string]: string } = {};
  selectedIDdprimiryKey: any;
  alertMessage: any;
  employees: Employee[] = [];
  teams: any[] = [];
  rewardsCategories: Rewards[] = [];
  isRewards: boolean = true;
  alertMessageTemplate!: TemplateRef<any>;
  teamRewardList:any[] = [];
  availableYears: number[] = [];
  selectedYear: number | null = null;
  selectedPeriod: string | null = null;
  employeeList :any[] = [];








  ngOnInit(): void {

    this.getRewardsCategories(this.alertMessageTemplate);
    const currentYear = new Date().getFullYear();
    for (let i = currentYear; i >= currentYear - 10; i--) {
      this.availableYears.push(i);
    }
    this.sharedService.employeeList$.subscribe((employeeList) => {
      this.employeeList = employeeList;
      console.log('Received employee list:', this.employeeList);
    });
  }

  onYearChange(event: Event): void {
    const selectElement = event.target as HTMLSelectElement;
    this.selectedYear = +selectElement.value; // Convert to number

    // Reset other selections when year changes
    this.selectedPeriod = null;
    this.activeCategoryId = null;
    this.rewardsCategories = []; // Reset categories if applicable
    console.log('Selected Year:', this.selectedYear);
  }

  onPeriodChange(event: Event): void {
    const selectElement = event.target as HTMLSelectElement;
    this.selectedPeriod = selectElement.value;

    // Reset categories when period changes
    this.activeCategoryId = null;
    this.rewardsCategories = this.getCategoriesForPeriod(this.selectedPeriod);

    console.log('Selected Period:', this.selectedPeriod);
  }

  changeEvent(value:string){

  }

  getRewardsCategories(template: TemplateRef<any>) {
    this.rewardsService.getAllRewardsCategory().subscribe(
      (response: any) => {
        if (response.serviceStatus === 'Success' && response.serviceResponse && response.serviceResponse.length > 0) {
          this.rewardsCategories = response.serviceResponse;
          const firstCategory = this.rewardsCategories[0];
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

onCategoryChange(event: Event): void {
  const selectElement = event.target as HTMLSelectElement;
  const categoryId = +selectElement.value; // Convert value to number

  if (this.selectedYear && categoryId) {
    let fromDate: string | null = null;
    let toDate: string | null = null;

    if (this.selectedPeriod === 'monthly') {
      fromDate = this.getStartDateForMonth(this.selectedYear, categoryId);
      toDate = this.getEndDateForMonth(this.selectedYear, categoryId);
    } else if (this.selectedPeriod === 'halfYearly') {
      if (categoryId === 101) { // H1 (Jan-Jun)
        fromDate = `${this.selectedYear}-01-01`;
        toDate = `${this.selectedYear}-06-30`;
      } else if (categoryId === 102) { // H2 (Jul-Dec)
        fromDate = `${this.selectedYear}-07-01`;
        toDate = `${this.selectedYear}-12-31`;
      }
    }

    if (fromDate && toDate) {
      // Prepare the API request payload
      const request = {
        empId: this.currentEmpId, // Replace with actual employee ID
        fromDate: fromDate,
        toDate: toDate,
      };

      // Call the API
      this.rewardsService.getEmployeeRewardByEmpId(request).subscribe(
        (response: any) => {
          this.employee = response.rewardsDTO;
          console.log('Rewards Details:', response);
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

employee : any[]=[];

onSubCategoryChange(event: Event): void {
  const selectElement = event.target as HTMLSelectElement;
  const selectedOption = selectElement.value;

  const request = new EmployeeRewarsRequest();
  request.empId = this.currentUser.empId;  // Replace with actual employee ID
  request.fromDate = this.getFromDate(selectedOption);
  request.toDate = this.getToDate(selectedOption);
  

  this.rewardsService.getEmployeeRewardByEmpId(request).subscribe(
    (response: any) =>{
      this.employee = response.rewardsDTO;
      console.log('Rewards Details:', response);
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
  this.fetchEmployees(rewardId);
  this.selectedIDdprimiryKey = rewardId;
  console.log('Reward ID:', rewardId);
  this.getActiveTeams(reward);
}

openAlertMod(template: TemplateRef<any>, message: string) {
  this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
  this.alertMessage = message;
}

fetchEmployees(rewardId: number) {
  this.rewardsService.fetchEmployeesFromRewardCondition(rewardId).subscribe(
    (response: any) => {
      this.employees = response.serviceResponse.map(emp => {
        return {
          employeeNameForReward: emp.employeeName,
          employeeIdForReward: emp.employeeEmpId,
          managerNameForReward: emp.managerName,
          managerIdForReward: emp.managerEmpId,
          rewardId: rewardId
        } as Employee;
      });
      console.log(this.employees);
    },
    (error) => {
      console.error('Error fetching employees:', error);
    }
  );
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
  // request.fromDate = this.getFromDate(selectedOption);
  // request.toDate = this.getToDate(selectedOption);

  this.rewardsService.getTeamRewardByEmpId(request).subscribe(
    (response: any) =>{
      this.teamRewardList = response.rewardsDTO;
      this.getMatchingEmployees();
      console.log('Team Rewards Details:', response);
  }); 
    
  this.modalRef = this.modalService.show(template, { class: 'modal-xl' });
  }


  matchedEmployees:any[] = [];
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

}
