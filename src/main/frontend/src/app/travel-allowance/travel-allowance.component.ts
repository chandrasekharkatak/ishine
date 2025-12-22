import { Component, OnInit } from '@angular/core';
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
  ) { 
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  ngOnInit(): void {
    let featureMap: Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    featureMap.subFeatures?.forEach(sub => {
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });
  }

}
