import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot, UrlTree } from '@angular/router';
import { Observable } from 'rxjs';
import { User } from '../models/user';
import { AuthenticationService } from '../services/authentication.service';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {

  exitEmployeeId:any;

  constructor(
    private router: Router,
    private authenticationService: AuthenticationService){

  }

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
      const currentUser:User = this.authenticationService.currentUserValue;
      if (currentUser) {
        if(!currentUser.tabList.find(tab => tab.tabRouteName == route.routeConfig.path?.split("/")[0])){
          // role not authorised so redirect to home page
          this.router.navigate(['/user-profile']); // home not mapped for default features yet
          return false;
        }   
        return true;
      }else{

        let employeeId = route.params['id'];
        let url: string = state.url;
        if(employeeId != null && url != null){
          this.exitEmployeeId = employeeId;
        }

          this.router.navigate(['/login'], { queryParams: { }});
          return false;
      }
  }
  
}
