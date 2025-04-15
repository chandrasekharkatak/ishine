import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';
import { Rewards } from '../models/rewards';
import { EmployeeRewarsRequest } from '../models/employeeRewardRequest';
import { Router } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class RewardsServiceService {

  private baseUrl:any = environment.baseUrl;

  constructor(private http: HttpClient,    private router: Router,
    ) { }

  navigateToEmployee360(data: any) {
    this.router.navigate(['/employee-360/rewards'], { state: { data } });
  }

  getAllRewardsCategory(){
    return this.http.get(`${this.baseUrl}` + `api/getAllRewardsCategory`);
  }

  getAllRewardDetailsById(rewardId:any){
    return this.http.get(`${this.baseUrl}` + `api/getEmployeeRewardByRewardId/`+rewardId);
  }

  saveRewardConfiguration(rewards: Rewards){
    return this.http.post(`${this.baseUrl}`+`api/saveRewardConfiguration`, rewards);
  }

  editRewardConfiguration(rewards: Rewards){
    return this.http.post(`${this.baseUrl}`+`api/editRewardConfiguration`, rewards);
  }

  getAllRewardsByCategoryId(categoryId: number) {
    return this.http.get(`${this.baseUrl}` + `api/getAllRewardsByCategoryId/${categoryId}`);
  }

  showAllRewards(){
    return this.http.get(`${this.baseUrl}` + `api/showAllRewards`);
  }

  fetchEmployeesFromRewardCondition(rewardId: number) {
    return this.http.get(`${this.baseUrl}` + `api/fetchEmployeesFromRewardCondition`, {
      params: { rewardId: rewardId.toString()}
    });
  }


  getAllRewardsByRewardId(rewardId: number) {
    return this.http.get(`${this.baseUrl}api/getAllRewardsByRewardId/${rewardId}`);
  }
  

  deleteRewardsByRewardId(rewardId: any) {
    return this.http.delete(`${this.baseUrl}api/deleteRewardsByRewardId/${rewardId}`);
  }


  submitRewardForEmployee(rewards: Rewards){
    return this.http.post(`${this.baseUrl}`+`api/submitRewardForEmployee`, rewards);
  }

  updateRewardForEmployee(rewards: Rewards){
    return this.http.post(`${this.baseUrl}`+`api/updateRewardsForEmployees`, rewards);
  }

  getEmployeeRewardByRewardId(rewardId: number) {
    return this.http.get(`${this.baseUrl}api/getEmployeeRewardByRewardId/${rewardId}`);
  }


  showAllEmployeeRewards() {
    const url = `${this.baseUrl}api/showAllEmployeeRewards`;
    return this.http.get(url);
}

isActive(rewards: Rewards){
  return this.http.post(`${this.baseUrl}`+`api/isActive`, rewards);
}


fetchEmployeesForHomepageByCategoryId(rewardCategoryObj : any){
  return this.http.post(`${this.baseUrl}`+`api/fetchEmployeesForHomepageByCategoryId`, rewardCategoryObj);
}

fetchRewardCategoryForHomePage(){
  return this.http.get(`${this.baseUrl}`+`api/fetchRewardCategoryForHomePage`);
}

getAllActiveTeams(rewards: any){
  return this.http.post(`${this.baseUrl}`+`api/getAllActiveTeams`, rewards);
}


deleteEmployeeRewardByRewardId(rewardId: any) {
  return this.http.delete(`${this.baseUrl}api/deleteEmployeeRewardByRewardId/${rewardId}`);
}

getEmployeeRewardByEmpId(request : any){
  return this.http.post(`${this.baseUrl}`+`api/getEmployeeRewardByEmpId`,request);

}

getTeamRewardByEmpId(request : any){
  return this.http.post(`${this.baseUrl}`+`api/getTeamRewardByEmpId`,request);

}

bulkDisableRewards(){
  return this.http.post(`${this.baseUrl}`+`api/bulkDisableRewards`,null);

}

bulkEnableMonthYear(listofmonthyear:any){
  return this.http.post(`${this.baseUrl}`+`api/bulkEnableRewards`,listofmonthyear);
}

saveRewardsExcel(formData : FormData){
  return this.http.post(`${this.baseUrl}`+`api/saveExcelDataForReward`,formData);
}

}
