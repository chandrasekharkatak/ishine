import { ActivatedRoute, Router } from '@angular/router';
import { Component, OnInit } from '@angular/core';
import { Log } from '../models/log';
import { User } from '../models/user';
import { Feature } from '../models/feature';
import { LogService } from 'src/app/services/log.service';
import { AuthenticationService } from '../services/authentication.service';
import { PerformanceService } from '../services/performance.service';
import { first } from 'rxjs/operators';

@Component({
  selector: 'app-user-performance',
  templateUrl: './user-performance.component.html',
  styleUrls: ['./user-performance.component.css']
})
export class UserPerformanceComponent implements OnInit {

  tabName:any = 'Performance ';
  feature = "Performance"
  currentUser:User;
  userMapping:any = {};
  mappTeamDashboard:boolean = false;

  log:Log;
  activeTab: string = 'performance-dashboard';
  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private logService:LogService,
    private authenticationService:AuthenticationService,
    private performanceService:PerformanceService,
  ) {     this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  ngOnInit(): void {
    this.logService.updateLogInfo(this.log);

    if (!this.route.firstChild) {
      this.router.navigate(['performance-dashboard'], { relativeTo: this.route });
    }    
    
    let featureMap: Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    console.log('featureMap -- ',featureMap);
    featureMap.subFeatures?.forEach(sub => {
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });
    console.log('user mapping--',this.userMapping);
    
    this.checkUserHaveTeam()
  }
 
  checkUserHaveTeam(){
    let userObj = {
      empId: this.currentUser.empId
    };
    this.performanceService.checkUserHaveTeam(userObj).pipe(first()).subscribe({
      next: (response: any) => {
        if(response.isUserHaveTeam || this.userMapping.team_dashboard){
          this.mappTeamDashboard = true;
        }else{
          this.mappTeamDashboard = false;
        }
      },
      error: (error: any) => {}
    });
  }
  
}
