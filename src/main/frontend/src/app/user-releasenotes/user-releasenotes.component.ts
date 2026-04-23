import { Component, OnInit } from '@angular/core';
import { User } from '../models/user';
import { Feature } from '../models/feature';
import { AuthenticationService } from '../services/authentication.service';
import { NotificationService } from '../services/notification.service';
import { NotificationMessage } from '../models/notification';
import { first } from 'rxjs/operators';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  standalone: false,
  selector: 'app-user-releasenotes',
  templateUrl: './user-releasenotes.component.html',
  styleUrls: ['./user-releasenotes.component.css']
})
export class UserReleasenotesComponent implements OnInit {

  feature = "Release Notes";
  currentUser: User;
  userMapping: any = {};
  playButton: boolean = false;
  allReleaseNotes: any[] = [];
  allReleaseNotesVideosId: any[] = [];
  selectedReleaseNoteVideoId: number = -1;
  selectedReleaseNoteVideoPath: string = "";
  videoSrc: SafeResourceUrl | null = null;

  constructor(
    private authenticationService: AuthenticationService,
    private notificationService: NotificationService,
    private sanitizer: DomSanitizer,
    private router : Router
  ) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
   }

  ngOnInit(): void {
    // Dynamic Subfeature Flags 
    // let featureMap: Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    // featureMap.subFeatures?.forEach(sub => {
    //   this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    // });

    this.getAllNotificationIds();
    this.getAllReleaseNotes();

  }

  getAllNotificationIds() {
    this.notificationService.getAllNotificationIds().subscribe(
      (response: any) => {
        if (response.serviceStatus === 'Success') {
          this.allReleaseNotesVideosId = response.serviceResponse;
        } else {
          console.error('Failed to retrieve notification IDs:', response.serviceError);
        }
      },
      (error) => {
        console.error('An error occurred while retrieving notification IDs:', error);
      }

    );
  }

  notificationIdExists(notificationId: number): boolean {
    return this.allReleaseNotesVideosId.some(id => id === notificationId);
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





  playButtonPress(id: number) {
    console.log("Video id ", id);
    try {
      if (this.playButton) {
        this.selectedReleaseNoteVideoId = -1;
        this.playButton = false;
        this.selectedReleaseNoteVideoPath = '';
      } else {
        this.selectedReleaseNoteVideoId = id;
        this.playButton = true;
        this.getReleaseNotesVideoName(id);
      }
    } catch (error) {
      console.error('Error fetching video name:', error);
    }
  }
  

 //added code by vishal..........
  getReleaseNotesVideoName(id: number) {
    console.log("getReleaseNotesVideoName called ", id);
    this.notificationService.getReleaseNotesVideoName(id).pipe(first()).subscribe(
      (response: Blob) => {
        const objectURL = URL.createObjectURL(response);
        this.videoSrc = this.sanitizer.bypassSecurityTrustResourceUrl(objectURL);
      },
      (error) => {
        console.error('An error occurred while retrieving the video name:', error);
      }
    );
  }

  //end..............



    goToGrievanceComponent(category: string, subCategory: string)  {
    this.router.navigate(['/grievance'], {
    queryParams: {
      category: category,
      subcategory: subCategory
    }
  });
}
  

}
