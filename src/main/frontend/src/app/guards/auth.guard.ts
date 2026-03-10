import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, Router, RouterStateSnapshot, UrlTree } from '@angular/router';
import { Observable } from 'rxjs';
import { first } from 'rxjs/operators';
import { UploadPolicy } from '../models/UploadPolicy';
import { User } from '../models/user';
import { AuthenticationService } from '../services/authentication.service';
import { PoliciesService } from '../services/policies.service';
import { TrainingService } from '../services/training.service';
import { Feature } from '../models/feature';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard  {

  id:any;
  currentUrl:any;

  userMapping:any = {};

  constructor(
    private router: Router,
    private authenticationService: AuthenticationService,
    private policiesService: PoliciesService,
    private trainingService: TrainingService){

  }

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
      const currentUser:User = this.authenticationService.currentUserValue;
      if (currentUser) {
        if(!currentUser.tabList.find(tab => tab.tabRouteName == route.routeConfig.path?.split("/")[0])){
          // role not authorised so redirect to home page
          this.router.navigate(['/home']);
          return false;
        }

        let featureMap:Feature[] = currentUser.userMapping.filter(userMap => userMap.tabName.toLowerCase() == 'training');
        featureMap?.forEach(feat => {
          let inActiveSubfeatures = feat.subFeatures.filter(sub => {
            if(sub.isActive === false)return sub;
          });
          this.userMapping[feat.featureName.replaceAll(' ', '_').toLowerCase()] = (inActiveSubfeatures.length === feat.subFeatures.length) ? false : true;
        });

        // Check training lock status
        if (this.userMapping.training_config && currentUser.trainingLockStatus) {
          // Get current route path
          const currentPath = state.url.split('?')[0]; // Remove query params
          const isTrainingRoute = currentPath === '/training' || currentPath === '/user-training';
          
          // Hard lock: deadline crossed (regardless of lock enabled) - user is frozen, cannot navigate anywhere except training page
          if (currentUser.trainingLockStatus.isHardLock === true) {
            if (!isTrainingRoute) {
              // Block navigation to any other page - redirect to training
              this.router.navigate(['/training']);
              return false;
            }
            // Allow navigation to training page
            return true;
          }
          
          // User is frozen: (mandatory + lock enabled) OR (mandatory + deadline crossed) - route to training
          if (currentUser.trainingLockStatus.isLocked === true) {
            if (!isTrainingRoute) {
              // Route to training page - user is frozen on training screen
              this.router.navigate(['/training']);
              return false;
            }
            // Allow navigation to training page
            return true;
          }
        
        } 
        if (currentUser.policyReadConsent != null) {
          let policyObj = new UploadPolicy();
          policyObj.empId = currentUser.empId;
          this.policiesService.isAllPolicyRead(policyObj).pipe(first()).subscribe((response: any) => {
            if (response.serviceStatus == "Success") {
              let policy = response.serviceResponse;

              if (currentUser.tabList.find(e => e.tabName === 'HR Policies')) {
                if (policy.isAllPolicyMarkAsRead == 'false') {
                  this.router.navigate(['/user-policies']);
                } else if (policy.isAllPolicyMarkAsRead == 'true') {
                  currentUser.policyReadConsent = null;
                }
              }
            }
          });
        }
        
        return true;
      }
      else if(route.routeConfig.path == "" || route.routeConfig.path == "login"){
        // If User is going to Login for the first Time
        return true;
      }else{

        let queryParamId = route.params['id'];
        let url: string = state.url;

        let rmgprojId = url.split("/")[3];
        
        if(queryParamId != null && url != null){
          this.id = queryParamId;
          this.currentUrl = url;
        }

        if((rmgprojId != undefined || rmgprojId != null) && url != null){
          this.id = rmgprojId;
          this.currentUrl = url;
        }

          this.router.navigate(['/login'], { queryParams: { }});
          return false;
      }
  }
  
}
