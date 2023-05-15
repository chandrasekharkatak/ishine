import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot, UrlTree } from '@angular/router';
import { Observable } from 'rxjs';
import { first } from 'rxjs/operators';
import { UploadPolicy } from '../models/UploadPolicy';
import { User } from '../models/user';
import { AuthenticationService } from '../services/authentication.service';
import { PoliciesService } from '../services/policies.service';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {

  id:any;
  currentUrl:any;

  constructor(
    private router: Router,
    private authenticationService: AuthenticationService,
    private policiesService: PoliciesService){

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
