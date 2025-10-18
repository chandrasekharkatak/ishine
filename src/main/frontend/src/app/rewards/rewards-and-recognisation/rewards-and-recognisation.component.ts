import { LocationStrategy } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit, TemplateRef, ViewChild ,ElementRef} from '@angular/core';
import { Sort } from '@angular/material/sort';
import * as moment from 'moment';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { first } from 'rxjs/operators';
import { AppComponent } from 'src/app/app.component';
import { Employee } from 'src/app/models/employee';
import { Feature } from 'src/app/models/feature';
import { Rewards } from 'src/app/models/rewards';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { EmployeeService } from 'src/app/services/employee.service';
import { ExportExcelService } from 'src/app/services/export-excel.service';
import { RewardsServiceService } from 'src/app/services/rewards-service.service';
import { UtilityService } from 'src/app/services/utility.service';
import { ValidationService } from 'src/app/services/validation.service';
import { SortPipe } from 'src/app/sort.pipe';
import * as XLSX from 'xlsx';

@Component({
  selector: 'app-rewards-and-recognisation',
  templateUrl: './rewards-and-recognisation.component.html',
  styleUrls: ['./rewards-and-recognisation.component.css'],

})
export class RewardsAndRecognisationComponent implements OnInit {

  @ViewChild('alert_message')
  alertMessageTemplate!: TemplateRef<any>;
  items = 10;

  currentUser: User;

  @ViewChild('fileInput') fileInput!: ElementRef; 
  file: File | null = null;


  userMapping: any = {};
  rewardsCategories: Rewards[] = [];
  rewards: Rewards[] = [];
  employees: Employee[] = [];
  selectedEmployee: string = '';
  modalRef: BsModalRef = new BsModalRef();
  alertMessage: any;
  selectedReward: Rewards | null = null;
  selectedRewardType: { [key: string]: string } = {};
  isRewards: boolean = true;
  isRewardsExcel: boolean = false;
  isRewardshitory: boolean = false;
  iswalloffame: boolean = false;
  activeCategoryId: number | null = null;
  sumbitRewards: Rewards = new Rewards();
  rewardsExcel: Rewards = new Rewards();
  selectedSubReward: any;
  selectemmpName: string;
  fromDatestr: any;
  todatestr: any;
  teams: any[] = [];
  selectedTeamId: any;
  selectedIDdprimiryKey: any;
  // @ViewChild('alert_message')
  rewardHistoryList: any = [];
  isSearchEnabled: boolean = false;
  filters: any = {};
  isTeamRewardHistory: boolean = false;
  rewardHistoryColumns: any[] = ['blank' ,'rewardedToByName','rewardCategoryName', 'rewardTypeName', 'createdByName', 'createdOn', 'managerName', 'ofmonthyear', 'updatedByName', 'updatedOn', 'remark'];
  page: number = 1;
  HistoryList: any[] = [];
  isSelectAll: boolean = false;
  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType: any;
  name: 'Employee_Reward.xlsx';
  rewardsDataForExcel: any[];
  isEditing: boolean = false;
  isTeam: any;
  rewardTypeOptions: string[] = [];
  employeeSearchText: any = '';
  teamSearchText: any = '';
  ofmonthyear: any;
  ofMonthYear: any;
  editRewardssss: Rewards = new Rewards();
 
  feature = "Rewards";
  annuallyreward: Date;

  @ViewChild('confirmDelete')
  delete_template: any;
wallOfFameQuarters: { quarter: string, year: number | null }[] = [
  { quarter: '', year: null }
];

  constructor(
    private cdr: ChangeDetectorRef,
    private locationStrategy: LocationStrategy,
    private authenticationService: AuthenticationService,
    private rewardsService: RewardsServiceService,
    private modalService: BsModalService,
    private exportExcelService: ExportExcelService,
    private validationService: ValidationService,
    private employeeService: EmployeeService,
    private utilityService: UtilityService

  ) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  ngOnInit(): void {
    this.preventBackButton();
    this.getRewardsCategories(this.alertMessageTemplate);
    this.fetchRewardHistory();
    let featureMap: Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    console.log("feature Name ", featureMap);
    featureMap.subFeatures?.forEach(sub => {
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });

    console.log('usermapping -- ', this.userMapping);
  }



  preventBackButton() {
    history.pushState(null, null, location.href);
    this.locationStrategy.onPopState(() => {
      history.pushState(null, null, location.href);
    });
  }

  getRewardsCategories(template: TemplateRef<any>) {
    this.rewardsService.getAllRewardsCategory().subscribe(
      (response: any) => {
        console.log(response);
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

  // getRewardsByCategoryId(categoryId: number, template: TemplateRef<any>) {
  //   const rewardCategoryIddd = categoryId;
  //   this.activeCategoryId = categoryId;

  //   this.rewardsService.getAllRewardsByCategoryId(categoryId).subscribe(
  //     (response: any) => {
  //       if (response.serviceStatus === 'Success') {
  //         this.rewards = response.serviceResponse.map((reward: any) => ({
  //           ...reward,
  //           rewardTypes: reward.rewardTypes[0].split(',').map((type: string) => type.trim())
  //         }));

  //         const firstReward = this.rewards[0];
  //         if (firstReward) {
  //           this.isTeam = firstReward.isTeam;
  //           this.setSelectedReward(firstReward); 
  //         }
  //       } else {
  //         this.openAlertMod(template, 'No rewards found for the selected category.');
  //         this.activeCategoryId = this.rewardsCategories[0].rewardCategoryId;
  //       }
  //     },
  //     (error) => {
  //       this.openAlertMod(template, 'Error fetching rewards. Please try again later.');
  //     }
  //   );
  // }

  getRewardsByCategoryId(categoryId: number, template: TemplateRef<any>) {
  const rewardCategoryIddd = categoryId;
  this.activeCategoryId = categoryId;

  // Clear quarterly fields when switching categories
  if (categoryId !== 4) {
    this.clearQuarterlyFields();
  }

  this.rewardsService.getAllRewardsByCategoryId(categoryId).subscribe(
    (response: any) => {
      if (response.serviceStatus === 'Success') {
        this.rewards = response.serviceResponse.map((reward: any) => ({
          ...reward,
          rewardTypes: reward.rewardTypes[0].split(',').map((type: string) => type.trim())
        }));

        const firstReward = this.rewards[0];
        if (firstReward) {
          this.isTeam = firstReward.isTeam;
          this.setSelectedReward(firstReward); 
        }
      } else {
        this.openAlertMod(template, 'No rewards found for the selected category.');
        this.activeCategoryId = this.rewardsCategories[0].rewardCategoryId;
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
    this.selectedRewardType[reward.rewardName] = '';
    const rewardId = reward.id;
    this.fetchEmployees(rewardId);
    this.selectedIDdprimiryKey = rewardId;
    // this.getActiveTeams(reward);
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

  openAlertMod(template: TemplateRef<any>, message: string) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }

  cancelRequest() {
    this.modalRef.hide();
  }

  rewardsHistoryfun() {
    this.isRewards = false;
    this.isRewardsExcel = false;
    this.isRewardshitory = true;
    this.isEditing = false;
    this.iswalloffame = false;
    this.fetchRewardHistory();
  }

  isRewardsfuc() {
    this.isRewards = true;
    this.isRewardshitory = false;
    this.iswalloffame = false;
    this.isRewardsExcel = false;
  }

  isRewardsExcelfuc() {
    this.isRewardsExcel = true;
    this.isRewards = false;
    this.isRewardshitory = false;
    this.iswalloffame = false;
  }

  wallOffame() {
    this.isRewards = false;
    this.isRewardshitory = false;
    this.isRewardsExcel = false;
    this.iswalloffame = true;
    this.wallOfFameMonths = [''];
  }

  rewardstypeName: any

  onTeamSelected(event: any): void {

    const selectedTeam = this.teams.find((team) => team.teamId === event.option.value);

    if (selectedTeam) {

      this.teamSearchText = selectedTeam.teamName;
      this.sumbitRewards.teamId = selectedTeam.teamId;
      this.sumbitRewards.teamLeadId = selectedTeam.teamLeadId;
      this.sumbitRewards.rewardedTo = null;
    }
  }


  submitRewardForEmployees(template: TemplateRef<any>) {
  this.sumbitRewards.remark = this.remarks;
  this.sumbitRewards.isActive = 1;
  this.sumbitRewards.createdBy = this.currentUser.empId;
  this.sumbitRewards.fromDate = this.fromDatestr;
  this.sumbitRewards.toDate = this.todatestr;
  this.sumbitRewards.rewardTypeName = this.selectedReward.selectedType;
  this.sumbitRewards.id = this.selectedIDdprimiryKey;
  this.sumbitRewards.rewardCategoryId = this.activeCategoryId;
  this.sumbitRewards.ofmonthyear = this.ofmonthyear;

  if (!this.validateRewardsWhileSubmit(template)) {
    return; // Stop execution if validation fails
  }

  // For quarterly rewards, we need to ensure the quarter is enabled first
  // But don't call the enable API if we're just submitting (it should already be enabled)
  this.rewardsService.submitRewardForEmployee(this.sumbitRewards).subscribe(
    (response: any) => {
      if (response.serviceStatus === 'Success') {
        console.log(response.serviceResponse);
        this.openAlertMod(template, response.serviceMessage);
        this.isRewards = false;
        this.isRewardshitory = true;
        this.ofmonthyear = '';
        this.employeeSearchText = '';
        this.remarks = '';
        this.selectedReward = null;
        this.isEditing = false;

        if (this.rewardsCategories && this.rewardsCategories.length > 0) {
          this.activeCategoryId = this.rewardsCategories[0].rewardCategoryId;
        }

        if (this.rewards && this.rewards.length > 0) {
          this.selectedReward = this.rewards[0];
          this.selectedReward.selectedType = null;
        }

        this.fetchRewardHistory();
      } else {
        this.openAlertMod(template, 'No reward categories available at the moment.');
      }
    },
    (error) => {
      this.openAlertMod(template, 'Error fetching reward categories. Please try again later.');
    }
  );
  this.isEditing = false;
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

    // if (!this.validationService.validateNullUndefinedEmptyString(this.sumbitRewards.remark)) {
    //   this.alertMessage = "Please enter a Remark !!";
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // }

    return true;
  }


  editReward(rewardId: number) {
    this.isEditing = true;
    this.isRewards = true;
    this.isRewardshitory = false;
      this.sumbitRewards = new Rewards();;
    this.rewardsService.getEmployeeRewardByRewardId(rewardId).subscribe(
      (response: any) => {
        if (response.serviceStatus === 'Success' && response.serviceResponse.length > 0) {
          const rewardData = response.serviceResponse[0];

          this.sumbitRewards.rewardId = rewardData.rewardId;
          this.selectemmpName = rewardData.rewardedToByName;
          this.selectedTeamId = rewardData.teamId;
          this.rewardstypeName = rewardData.rewardTypeName;
          this.remarks = rewardData.remark;
          this.fromDatestr = rewardData.fromDate;
          this.todatestr = rewardData.toDate;
          this.ofmonthyear = rewardData.ofmonthyear;
          this.employeeSearchText = rewardData.rewardedToByName;

          this.selectedReward = {
            rewardName: rewardData.rewardName,
            categoryId: rewardData.categoryId || null,
            categoryName: rewardData.categoryName || null,
            rewardTypes: rewardData.rewardTypes || [],
            customFilterDTOList: rewardData.customFilterDTOList || [],
            rewardCategoryId: rewardData.rewardCategoryId || null,
            employeeName: rewardData.employeeName || null,
            managerName: rewardData.managerName || null,
            managerEmpId: rewardData.managerEmpId || null,
            id: rewardData.id || null,
            createdBy: rewardData.createdBy || null,
            updatedBy: rewardData.updatedBy || null,
            rewardId: rewardData.rewardId || null,
            rewardedTo: rewardData.rewardedTo || null,
            rewardedToByName: rewardData.rewardedToByName || null,
            rewardType: rewardData.rewardType || null,
            managerId: rewardData.managerId || null,
            teamLeadId: rewardData.teamLeadId || null,
            fromDate: rewardData.fromDate || null,
            toDate: rewardData.toDate || null,
            remark: rewardData.remark || null,
            createdByName: rewardData.createdByName || null,
            updatedByName: rewardData.updatedByName || null,
            updatedOn: rewardData.updatedOn || null,
            isActive: rewardData.isActive || null,
            rewardTypeName: rewardData.rewardTypeName || null,
            teamLeadName: rewardData.teamLeadName || null,
            isTeam: rewardData.isTeam || null,
            createdOn: rewardData.createdOn || null,
            teamId: rewardData.teamId || null,
            selectedType: rewardData.rewardTypeName || null,
            ofmonthyear: rewardData.ofmonthyear || null,
            emp360CreatedBy: rewardData.emp360CreatedBy || null,
            emp360UpdatedBy: rewardData.emp360UpdatedBy || null,
          };
                  console.log('response.serviceResponse', response.serviceResponse);
                  if (rewardData.ofmonthyear) {
          const [quarter, year] = rewardData.ofmonthyear.split(' ');
          const quarterObj = this.quarterOptions.find(q => q.value === quarter.trim());
          this.selectedQuarter = quarterObj ? quarterObj.value : '';
          this.quarterYear = year
        } else {
          this.selectedQuarter = '';
          this.quarterYear = null;
        }


          this.sumbitRewards.managerName = rewardData.managerName;
          this.sumbitRewards.teamLeadId = rewardData.teamLeadId;
                  this.activeCategoryId = rewardData.categoryId || null;
                  this.ofmonthyear = rewardData.ofmonthyear || null;
        } else {
          console.log('Error: Reward data not found');
        }
      },
      (error) => {
        console.log('Error fetching reward data:', error);
      }
    );
  }

  reloadPage() {
    // window.location.reload();
    this.rewardsHistoryfun();
    this.ofmonthyear = '';
    this.employeeSearchText = '';
    this.remarks = '';
    this.selectedReward = null;
    this.isEditing = false;
    //  this.activeCategoryId = 
    if (this.rewardsCategories && this.rewardsCategories.length > 0) {
      this.activeCategoryId = this.rewardsCategories[0].rewardCategoryId;
    }

    if (this.rewards && this.rewards.length > 0) {
      this.selectedReward = this.rewards[0];
      this.selectedReward.selectedType = null;
    }
  }

  wallOfFameMonths: string[] = [''];
  addMonthYear(template: TemplateRef<any>) {
    const lastMonthYear = this.wallOfFameMonths[this.wallOfFameMonths.length - 1];
    if (lastMonthYear.trim() === '') {
    this.openAlertMod(template,'Please select a Month-Year before adding a new one.');
    return;
    }
    this.wallOfFameMonths.push('');
  }

  removeMonthYear(index: number,template: TemplateRef<any>) {
    if (this.wallOfFameMonths.length > 1) {
      this.wallOfFameMonths.splice(index, 1); 
    } else {
      this.openAlertMod(template,'At least one month-year must be selected.');
    }
  }

  checkDuplicateMonthYear(index: number) {
    const selectedMonthYear = this.wallOfFameMonths[index];

    if (!selectedMonthYear) return; // If no value is selected, do nothing.

    // Check if the selected month-year already exists (excluding the current index)
    const duplicateExists = this.wallOfFameMonths.some((month, i) => i !== index && month === selectedMonthYear);

    if (duplicateExists) {
        alert('Duplicate Month-Year! Please select a different one.');
        setTimeout(() => {
            this.wallOfFameMonths[index] = ''; // Reset the duplicate entry
        }, 100); // Small delay to avoid UI flicker
    }
  }


  updateRewards(template: TemplateRef<any>) {

    this.editRewardssss.rewardId = this.selectedReward.rewardId;
    this.editRewardssss.ofmonthyear = this.ofmonthyear;
    this.editRewardssss.rewardedTo = this.sumbitRewards.rewardedTo || this.selectedReward.rewardedTo;
    this.editRewardssss.rewardTypeName = this.selectedReward.selectedType;
    this.editRewardssss.remark = this.remarks;
    this.editRewardssss.updatedBy = this.currentUser.empId;
    this.editRewardssss.managerId = this.sumbitRewards.managerId || this.selectedReward.managerId;

    //console.log("updateddddddddddddd--", this.editRewardssss);
    this.rewardsService.updateRewardForEmployee(this.editRewardssss).subscribe(
      (response: any) => {
        if (response.serviceStatus === 'Success') {

          console.log(response.serviceResponse);
          this.openAlertMod(template, response.serviceResponse);
          this.isRewards = false;
          this.isRewardshitory = true;
          this.fetchRewardHistory();
          this.selectedReward = null;
          this.ofmonthyear=null;
          this.remarks=null;
          this.sumbitRewards=null;
          // this.editRewardssss=null;
          this.employeeSearchText = null;
        } else {
          this.openAlertMod(template, 'No reward categories available at the moment.');
        }
      },
      (error) => {
        this.openAlertMod(template, 'Error while updating');
      }
    );
    this.isEditing = false;
  }


  bulkDisableRewards(template: TemplateRef<any>) {

    this.rewardsService.bulkDisableRewards().subscribe(
      (response: any) => {
        if (response.serviceStatus === 'Success') {
          console.log(response.serviceResponse);
          this.openAlertMod(template, response.serviceResponse);
          this.fetchRewardHistory(); // Refresh the reward history
        } else {
          this.openAlertMod(template, 'Bulk disable operation failed.');
        }
      },
      (error) => {
        this.openAlertMod(template, 'An error occurred while performing bulk disable.');
      }
    );
  }

  get isFormValid(): boolean {
   
    const hasEmpty = this.wallOfFameMonths.some(monthYear => !monthYear.trim());

   
    const hasDuplicates = this.wallOfFameMonths.some((month, index) =>
        this.wallOfFameMonths.indexOf(month) !== index && month !== ''
    );

    return !hasEmpty && !hasDuplicates; // Form is valid only if no empty fields & no duplicates
}

  checkDuplicateMonthYearr(index: number, template: TemplateRef<any>) {
    const selectedMonthYear = this.wallOfFameMonths[index];

    if (!selectedMonthYear) return; // If no value is selected, do nothing.

    // Check if the selected month-year already exists (excluding the current index)
    const duplicateExists = this.wallOfFameMonths.some((month, i) => i !== index && month === selectedMonthYear);

    if (duplicateExists) {
        this.openAlertMod(template, 'Duplicate Month-Year! Please select a different one.');
        setTimeout(() => {
            this.wallOfFameMonths[index] = ''; // Reset the duplicate entry
        }, 100); // Small delay to avoid UI flicker
    }
}

  bulkEnable(template: TemplateRef<any>) {

   

    const isAnyEmpty = this.wallOfFameMonths.some(monthYear => !monthYear.trim());

    if (this.wallOfFameMonths.length === 0 || isAnyEmpty) {
      this.openAlertMod(template, 'Please select at least one Month-Year before proceeding.');
      return; 
  }

  if (!this.isFormValid) {
    this.openAlertMod(template,'Please ensure all Month-Years are selected and unique before proceeding.');
      return;
  }

    this.rewardsService.bulkEnableMonthYear(this.wallOfFameMonths).subscribe(
      (response: any) => {
        if (response.serviceStatus === 'Success') {
          console.log(response.serviceResponse);
          this.openAlertMod(template, response.serviceResponse);
          this.isRewards = false;
          this.isRewardshitory = true;
          this.iswalloffame = false;
          this.fetchRewardHistory();
        } else {
          this.openAlertMod(template, 'Bulk Enable for the following month-year failed.');
        }
      },
      (error) => {
        this.openAlertMod(template, 'An error occurred while performing bulk enable.');
      }
    );
  }


addQuarter() {
  this.wallOfFameQuarters.push({ quarter: '', year: null });
}

removeQuarter(index: number) {
  this.wallOfFameQuarters.splice(index, 1);
}


 bulkEnableQuarter(template: TemplateRef<any>) {
  const isInvalid = this.wallOfFameQuarters.some(q => !q.quarter || !q.year);
  if (this.wallOfFameQuarters.length === 0 || isInvalid) {
    this.openAlertMod(template, 'Please select at least one valid Quarter and Year before proceeding.');
    return; 
  }
  const payload = {
    ofMonthYears: this.wallOfFameQuarters.map(q => `${q.quarter} ${q.year}`)
  };
  this.rewardsService.bulkEnableQuarter(payload).subscribe(
    (response: any) => {
      if (response.serviceStatus === 'Success') {
        console.log(response.serviceResponse);
        this.openAlertMod(template, response.serviceResponse);
        this.isRewards = false;
        this.isRewardshitory = true;
        this.iswalloffame = false;
        this.fetchRewardHistory();
      } else {
        this.openAlertMod(template, 'Bulk Enable Quarter-Year failed for the selected quarters.');
      }
    },
    (error) => {
      this.openAlertMod(template, 'An error occurred while performing bulk enable quarter-year.');
    }
  );
}

  remarks: string;

  onEmployeeChange(event: any) {
    this.selectemmpName = event?.option?.value;

    console.log(' this.selectemmpName', this.selectemmpName);

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

  toggleSearch() {
    this.isSearchEnabled = !this.isSearchEnabled;
    if (!this.isSearchEnabled) {
      this.filters = {};
    }
  }

  onSearch(searchData) {
    this.filters = searchData;
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

  handlePageChange(event) {
    this.page = event;
    this.isSelectAll = false
    this.rewardHistoryList.forEach(x => {
      x.isSelected = false;
    })
  }

  fetchRewardHistory() {
    this.rewardHistoryList = [];
    this.filters = {};
    this.rewardsService.showAllEmployeeRewards().subscribe(
      (response: any) => {
        if (response.serviceStatus === 'Success') {

          console.log("Reward History", response);
          this.rewardHistoryList = response.serviceResponse;
          this.rewardHistoryList.forEach(rewards => {
            rewards.createdOn = (rewards.createdOn) ? moment(rewards.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
            rewards.updatedOn = (rewards.updatedOn) ? moment(rewards.updatedOn).format(AppComponent.DATETIME_FORMAT) : null;
            rewards.emp360 = rewards.empId;
            rewards.emp360RewardBy = rewards.createdBy;
            rewards.emp360Manager = rewards.managerId;
            rewards.emp360UpdatedBy = rewards.updatedBy;


          });
        } else {
          console.error('No rewards data available');
        }
      },
      (error) => {
        console.error('Error fetching rewards history:', error);
      }
    );
  }


  // downloadRewardFileTemplate(): void {
  //   const ws: XLSX.WorkSheet = XLSX.utils.json_to_sheet(this.headers, { skipHeader: false });
  //   const wb: XLSX.WorkBook = XLSX.utils.book_new();
  //   XLSX.utils.book_append_sheet(wb, ws, 'Template');
  //   XLSX.writeFile(wb, 'Reward_Data_Template.xlsx');
  // }

  downloadRewardFileTemplatee(): void {

    const headers = [
      ['Employee Id', 'Employee Name', 'Reward Category', 'Reward Type Name', 'Of Month-Year', 'Remarks'],
      ['e.g. 240017', 'e.g. Prarthana Lenka', 'e.g. Monthly/Half Yearly/Annual/Quarterly', 'e.g. Gem Of The Month', 'e.g. January 2025', 'e.g. Did their best in their respective fields']
    ];


    const ws: XLSX.WorkSheet = XLSX.utils.aoa_to_sheet(headers);


    const placeholderStyle = {
      font: { italic: true, color: { rgb: '808080' } }
    };


    const placeholderCells = ['A2', 'B2', 'C2', 'D2', 'E2', 'F2'];
    placeholderCells.forEach(cell => {
      if (!ws[cell]) ws[cell] = {};
      ws[cell].s = placeholderStyle;
    });


    const wb: XLSX.WorkBook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, 'Template');


    XLSX.writeFile(wb, 'Reward_Data_Template.xlsx');
  }
  

  expectedHeaders = ['Employee Id', 'Employee Name', 'Reward Category', 'Reward Type Name', 'Of Month-Year', 'Remarks'];
  validateHeaders(uploadedHeaders: string[]): boolean {
    return JSON.stringify(uploadedHeaders) === JSON.stringify(this.expectedHeaders);
  }

  clearFileInput() {
    if (this.fileInput) {
      this.fileInput.nativeElement.value = ''; 
    }
  }


  
  onRewardFileSelect(event: any, template: TemplateRef<any>) {
    
    const uploadedFiles = event.target.files;
    console.log("uploadedFiles ", uploadedFiles);
    this.file = uploadedFiles[0];
      const formData = new FormData();
      formData.append('file', this.file);
      formData.append('createdBy', this.currentUser.empId.toString());
      this.rewardsService.saveRewardsExcel(formData).pipe(first()).subscribe(
        (response: any) => {
          if (response.serviceStatus === "Success") {
            this.openAlertMod(template, response.serviceResponse);
            this.isRewardshitory = true;
            this.fetchRewardHistory();
            this.isRewards = false;
            this.iswalloffame = false;
            this.isRewardsExcel = false;
          } else if (response.serviceStatus === "Fail") {
            this.openAlertMod(template, `Error found: ${response.serviceResponse}`);
          } else {
            this.openAlertMod(template, response.serviceResponse);
          }

          this.clearFileInput();
        });
    }
  






    exportToExcelList: any[] = [];
    exportToExcel() {
      this.rewardsService.showAllEmployeeRewards().subscribe(
        (response: any) => {
          if (response.serviceStatus === 'Success') {
            console.log("Reward exportToExcelList", response.serviceResponse);
            this.exportToExcelList = response.serviceResponse;
          } else {
            this.exportToExcelList = [];
            console.error('No rewards data available');
          }
        },
        (error) => {
          console.error('Error fetching rewards history:', error);
        },
        () => {
          // Ensure the list is defined before mapping
          if (Array.isArray(this.exportToExcelList)) {
            const onlySpecificDataArr = this.exportToExcelList.map(
              (x) => ({
                "Employee": x.rewardedToByName || 'N/A',
                "Reward Type": x.rewardTypeName ? x.rewardTypeName : "null",
                "Rewarded On": (x.createdOn) ? moment(x.createdOn).format(AppComponent.DATETIME_FORMAT) : null,
                "Manager Name": x.managerName || 'N/A',
                "Month Year": x.ofmonthyear || 'N/A',
                "Updated By": x.updatedByName || 'N/A',
                "Updated On": (x.updatedOn) ? moment(x.updatedOn).format(AppComponent.DATETIME_FORMAT) : ' - ',
                "Remarks": x.remark || 'N/A',
              })
            );

            // Call the export function with the processed data
            this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, 'Reward_History.xlsx');
          } else {
            console.error('exportToExcelList is not an array');
          }
        }
      );
    }

    Approve(rewardId: any, template: TemplateRef<any>) {
      const obj = new Rewards();
      obj.isActive = 1;
      obj.rewardId = rewardId;
      this.rewardsService.isActive(obj).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.openAlertMod(template, 'Approved');
          this.fetchRewardHistory();
        }
      });
    }

    undoApprove(rewardId: any, template: TemplateRef<any>) {
      const obj = new Rewards();
      obj.isActive = 0;
      obj.rewardId = rewardId;

      this.rewardsService.isActive(obj).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.openAlertMod(template, 'Undone');
          this.fetchRewardHistory();
        }
      });
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

    onTeamSelect(event: Event): void {
      const selectedElement = event.target as HTMLSelectElement;
      this.selectedTeamId = selectedElement.value;
      console.log('Selected Team ID:', this.selectedTeamId);
    }

    openConfirmDeleteModal(template: TemplateRef<any>, rewardID: any) {
      this.selectedReward = rewardID;
      this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    }
    confirmDeleteReward(template: TemplateRef<any>) {

      if (!this.selectedReward) return;

      this.rewardsService.deleteEmployeeRewardByRewardId(this.selectedReward).subscribe(
        (response: any) => {
          if (response.serviceStatus === 'Success') {
            console.log(response.serviceResponse);
            // this.openAlertMod(this.modalRef?.content, response.serviceResponse);
            this.isRewards = false;
            this.isRewardshitory = true;
            this.openAlertMod(template, response.serviceResponse);
            this.fetchRewardHistory();
          } else {
            this.openAlertMod(this.modalRef?.content, 'No reward categories available at the moment.');
          }
        },
        (error) => {
          this.openAlertMod(this.modalRef?.content, 'Error fetching reward categories. Please try again later.');
        }
      );
      this.modalRef?.hide();
    }


quarterOptions = [
  { value: 'Q1', label: 'Quarter 1 (Apr-Jun)' },
  { value: 'Q2', label: 'Quarter 2 (Jul-Sep)' },
  { value: 'Q3', label: 'Quarter 3 (Oct-Dec)' },
  { value: 'Q4', label: 'Quarter 4 (Jan-Mar)' }
];
selectedQuarter: string = '';
quarterYear: string = '';

onQuarterChange(event: any): void {
  this.selectedQuarter = event.target.value;
  this.updateQuarterlyMonthYear(); 
}

onQuarterYearChange(event: any): void {
  this.quarterYear = event.target.value;
  this.updateQuarterlyMonthYear();
}

private updateQuarterlyMonthYear(): void {
  if (this.selectedQuarter && this.quarterYear) {
    this.ofmonthyear = `${this.selectedQuarter} ${this.quarterYear}`;
  }
}

private clearQuarterlyFields(): void {
  this.selectedQuarter = '';
  this.quarterYear = '';
}

getMonthYearDisplay(ofmonthyear: string): string {
  if (!ofmonthyear) return 'N/A';
  if (ofmonthyear.includes('-') && ofmonthyear.length === 7) {
    const [year, month] = ofmonthyear.split('-');
    const monthNames = [
      'January', 'February', 'March', 'April', 'May', 'June',
      'July', 'August', 'September', 'October', 'November', 'December'
    ];
    const monthIndex = parseInt(month) - 1;
    if (monthIndex >= 0 && monthIndex < 12) {
      return `${monthNames[monthIndex]} ${year}`;
    }
  }
  return ofmonthyear;
}
// onEnableQuarterClick() {
  
//   const payload={
//     ofMonthYear:this.ofmonthyear
//   };
//   console.log(this.ofmonthyear , "++++++++++++++++++++++++++++++++++++");
//   this.rewardsService.isEnableQuarter(payload).subscribe({
//     next: (response: any) => {
//       console.log('Enable Quarter Response:', response);
//       if (response.success) {
//         alert('Quarter enabled successfully!');
//       } else {
//         alert('Failed to enable quarter: ' + response.message);
//       }
//     },
//     error: (error: any) => {
//       console.error('Enable Quarter Error:', error);
//       alert('Error enabling quarter. Please try again.');
//     }
//   });
// }
onEnableQuarterClick(template: TemplateRef<any>) {
  
  if (!this.selectedQuarter || !this.quarterYear) {
    this.openAlertMod(template, 'Please select both Quarter and Year before enabling.');
    return;
  }

  const payload = {
    ofMonthYear: this.ofmonthyear,
    enableOnly: true 
  };
  
  console.log(this.ofmonthyear, "++++++++++++++++++++++++++++++++++++");
  
  this.rewardsService.isEnableQuarter(payload).subscribe({
    next: (response: any) => {
      console.log('Enable Quarter Response:', response);
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, 'Quarter enabled successfully!');
      } else {
        this.openAlertMod(template, 'Failed to enable quarter: ' + (response.message || 'Unknown error'));
      }
    },
    error: (error: any) => {
      console.error('Enable Quarter Error:', error);
      this.openAlertMod(template, 'Error enabling quarter. Please try again.');
    }
  });
}
  }
  