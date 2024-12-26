import { Component, OnInit, TemplateRef } from '@angular/core';
import { Rewards } from 'src/app/models/rewards';
import { RewardsServiceService } from 'src/app/services/rewards-service.service';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { Employee } from 'src/app/models/employee';


@Component({
  selector: 'app-employee360-rewards',
  templateUrl: './employee360-rewards.component.html',
  styleUrls: ['./employee360-rewards.component.css']
})
export class Employee360RewardsComponent implements OnInit {



  constructor(
    private rewardsService: RewardsServiceService,
    private modalService: BsModalService,
  ) { }

  modalRef: BsModalRef = new BsModalRef();

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










  ngOnInit(): void {
    this.getRewardsCategories(this.alertMessageTemplate);
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

  onCategoryChange(event: Event, template: TemplateRef<any>): void {
    // Cast event.target to HTMLSelectElement to access its value
    const selectElement = event.target as HTMLSelectElement;
    const categoryId = +selectElement.value; // Convert value to number
    this.getRewardsByCategoryId(categoryId, template);
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

  this.modalRef = this.modalService.show(template, { class: 'modal-xl' });
}

}
