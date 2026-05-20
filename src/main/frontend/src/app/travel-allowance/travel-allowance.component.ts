import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Feature } from '../models/feature';
import { User } from '../models/user';
import { AuthenticationService } from '../services/authentication.service';

@Component({
  standalone: false,
  selector: 'app-travel-allowance',
  templateUrl: './travel-allowance.component.html',
  styleUrls: ['./travel-allowance.component.css']
})
export class TravelAllowanceComponent implements OnInit {

  currentUser: User;
  feature = "Travel Desk";
  userMapping: any = {};

  constructor(
    private authenticationService: AuthenticationService,
    private router: Router,
  ) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  ngOnInit(): void {
    const featureMap: Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    featureMap?.subFeatures?.forEach(sub => {
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });
    this.redirectToDefaultChildRouteIfNeeded();
  }

  /** ACL: sub_feature_master "Apply Travel Request" (legacy: "Apply Journey"). */
  canApplyTravelRequest(): boolean {
    return !!(this.userMapping.apply_travel_request || this.userMapping.apply_journey);
  }

  private redirectToDefaultChildRouteIfNeeded(): void {
    const url = this.router.url.split('?')[0].replace(/\/$/, '');
    if (!/\/travelDesk$/.test(url)) {
      return;
    }
    const target = this.defaultChildRoute();
    void this.router.navigate(['/travelDesk', target], { replaceUrl: true });
  }

  private defaultChildRoute(): string {
    if (this.canApplyTravelRequest()) {
      return 'my-travelrequest';
    }
    if (this.userMapping.view_request) {
      return 'view-travelrequest';
    }
    if (this.userMapping.approve_journey) {
      return 'approve-travelrequest';
    }
    if (this.userMapping.total_travelrequest) {
      return 'total-travelrequest';
    }
    return 'my-travelrequest';
  }

}
