import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, Params } from '@angular/router';
import { Log } from '../models/log';
import { User } from '../models/user';
import { Feature } from '../models/feature';
import { LogService } from 'src/app/services/log.service';
import { AuthenticationService } from '../services/authentication.service';

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

  log:Log;
  activeTab: string = 'performance-dashboard';
  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private logService:LogService,
    private authenticationService:AuthenticationService,
  ) {     this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  ngOnInit(): void {
    this.logService.updateLogInfo(this.log);

    if (!this.route.firstChild) {
      this.router.navigate(['performance-dashboard'], { relativeTo: this.route });
    }    

    // let featureMap:Feature[] = this.currentUser.userMapping.filter(userMap => userMap.tabName == this.tabName);
    // featureMap?.forEach(feat => {
    //   let inActiveSubfeatures = feat.subFeatures.filter(sub => {
    //     if(sub.isActive === false)return sub;
    //   });
    //   this.userMapping[feat.featureName.replaceAll(' ', '_').toLowerCase()] = (inActiveSubfeatures.length === feat.subFeatures.length) ? false : true;
      
    // });
    
    let featureMap: Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    console.log('featureMap -- ',featureMap);
    featureMap.subFeatures?.forEach(sub => {
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });
    console.log('user mapping--',this.userMapping);
    
  }
 
  
}
