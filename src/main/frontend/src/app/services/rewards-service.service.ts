import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';
import { Rewards } from '../models/rewards';

@Injectable({
  providedIn: 'root'
})
export class RewardsServiceService {

  private baseUrl:any = environment.baseUrl;

  constructor(private http: HttpClient) { }

  getAllRewardsCategory(){
    return this.http.get(`${this.baseUrl}` + `api/getAllRewardsCategory/`);
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
    return this.http.get(`${this.baseUrl}` + `api/showAllRewards/`);
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


fetchEmployeesForHomepage(){
  return this.http.get(`${this.baseUrl}`+`api/fetchEmployeesForHomepage`);
}

getAllActiveTeams(rewards: any){
  return this.http.post(`${this.baseUrl}`+`api/getAllActiveTeams`, rewards);
}


deleteEmployeeRewardByRewardId(rewardId: any) {
  return this.http.delete(`${this.baseUrl}api/deleteEmployeeRewardByRewardId/${rewardId}`);
}


}
