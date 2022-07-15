import { Component, OnInit } from '@angular/core';
import { Employee } from 'src/app/models/employee';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { TeamViewService } from 'src/app/services/team-view.service';
import { first } from 'rxjs/operators';
import { User } from 'src/app/models/user';
import { Feature } from 'src/app/models/feature';

@Component({
  selector: 'app-team-member',
  templateUrl: './team-member.component.html',
  styleUrls: ['./team-member.component.css']
})
export class TeamMemberComponent implements OnInit {

  feature="Team Members";
  currentUser:User;
  userMapping:any = {};

  viewTeamMemberList: any[] = []; 

  constructor(
    private authenticationService : AuthenticationService,
    private teamViewService : TeamViewService,
  ) { 
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
   }

  ngOnInit(): void {
    // Dynamic Subfeature Flags 
    let featureMap:Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    featureMap.subFeatures?.forEach(sub => {
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });
    console.log(this.feature, this.userMapping);
    
    this.getAllTeamMemberView();
  }

  getAllTeamMemberView(){
    this.viewTeamMemberList = []

    let employeeObj = new Employee();
    employeeObj.managerId = this.currentUser.managerId;
    this.teamViewService.getAllTeamMemberView(employeeObj).pipe(first()).subscribe((response : any) => {
      if (response.serviceStatus == "Success") {
        this.viewTeamMemberList = response.serviceResponse;
        console.log("viewTeamMemberList : ", this.viewTeamMemberList);
      } else {
        console.error(response.serviceResponse);
      }
    });

  }
}
