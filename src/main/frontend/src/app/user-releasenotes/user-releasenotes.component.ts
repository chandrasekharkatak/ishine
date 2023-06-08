import { Component, OnInit } from '@angular/core';
import { User } from '../models/user';
import { Feature } from '../models/feature';
import { AuthenticationService } from '../services/authentication.service';
import { NotificationService } from '../services/notification.service';
import { NotificationMessage } from '../models/notification';
import { first } from 'rxjs/operators';

@Component({
  selector: 'app-user-releasenotes',
  templateUrl: './user-releasenotes.component.html',
  styleUrls: ['./user-releasenotes.component.css']
})
export class UserReleasenotesComponent implements OnInit {

  feature = "Release Notes";
  currentUser: User;
  userMapping: any = {};

  allReleaseNotes:any[] = [];

  constructor(
    private authenticationService: AuthenticationService,
    private notificationService: NotificationService,
  ) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
   }

  ngOnInit(): void {
    // Dynamic Subfeature Flags 
    // let featureMap: Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    // featureMap.subFeatures?.forEach(sub => {
    //   this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    // });

    this.getAllReleaseNotes();

  }

  getAllReleaseNotes(){
    this.allReleaseNotes = [];
    let notificationObj = new NotificationMessage();
    notificationObj.empId = this.currentUser.empId;
    notificationObj.notificationType = "releaseNotes";

    this.notificationService.getAllNotificationsByNotificationTypeAndEmpId(notificationObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allReleaseNotes = response.serviceResponse;
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

}
