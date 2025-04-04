

import { ChangeDetectorRef, Component, OnInit, SecurityContext, TemplateRef, ViewChild } from '@angular/core';

import { ActivatedRoute, Router } from '@angular/router';
import { Subject, Subscription } from 'rxjs';
import { first, takeUntil } from 'rxjs/operators';
import { DeptConfigComponent } from '../configuration/dept-config/dept-config.component';
import { EmployeeConfigComponent } from '../configuration/employee-config/employee-config.component';
import { LeaveConfigComponent } from '../configuration/leave-config/leave-config.component';
import { RoleConfigComponent } from '../configuration/role-config/role-config.component';
import { AppreciationAndRewardsCount } from '../models/appreciationAndRewardCount';
import { Employee } from '../models/employee';
import { Feature } from '../models/feature';
import { User } from '../models/user';
import { AuthenticationService } from '../services/authentication.service';
import { BreadcrumbService } from '../services/breadcrumb.service';
import { Employee360Service } from '../services/employee360.service';
import { UtilityService } from '../services/utility.service';
import { DomSanitizer } from '@angular/platform-browser';


@Component({
  selector: 'app-employee360',
  templateUrl: './employee360.component.html',
  styleUrls: ['./employee360.component.css']
})
export class Employee360Component implements OnInit {

  private unsubscribe$ = new Subject<void>();

  appreciationAndRewardsCount:AppreciationAndRewardsCount = new AppreciationAndRewardsCount();
  employeeData: any;
  emplId:any;
  employeeConfig: EmployeeConfigComponent;
  departmentConfig: DeptConfigComponent;
  roleConfig: RoleConfigComponent;
  leaveConfig: LeaveConfigComponent;
  private hasLoadedData = false;
  tabName:any = 'Configurations';
  currentUser:User;
  userMapping:any = {};
  breadcrumbUrl:any[] = [];
  breadcrumbUrl1:any[] = [];
  breadcrumbs: any[] = [];

  private navigationSubscription: Subscription;

  constructor(
    private authenticationService: AuthenticationService,
    private router: Router,
    private route: ActivatedRoute,
    private employee360Service: Employee360Service,
    private breadcrumbService: BreadcrumbService,
    private utillity:UtilityService,
   private sanitizer: DomSanitizer
  
    
  ) { 
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
    this.breadcrumbService.currentBreadcrumb.subscribe(x => this.breadcrumbUrl = x);
   
  }


  currentab:String;
  leave:String;
  othertab:any;
  employeeId:any;
  ngOnInit(): void {
   
    this.route.data.subscribe(data => {
      sessionStorage.setItem("employee360Data", JSON.stringify(data.employeeData));
    });
    this.navigationSubscription = this.employee360Service.getNavigationEvent().subscribe(() => {
      this.removeActiveTab();
      this.setActiveTab();
    });
    
   
   
    const storedData = sessionStorage.getItem('employee360Data');
    const parsedData = storedData ? JSON.parse(storedData) : null;
    this.employeeData = parsedData
    if (parsedData !== null && parsedData !== undefined) {
      this.employeeData = parsedData;
    } else {
      this.employeeData = history.state?.data ?? {};
    }
   
    let findBreadcrumbObject = this.breadcrumbUrl.findIndex(x => x.title === "Employee-360-Profile");
    if (findBreadcrumbObject >= 0) {
      this.breadcrumbUrl.splice(findBreadcrumbObject + 1);
      this.breadcrumbService.setBreadcrumbSubject(this.breadcrumbUrl);
    } else {
      let breadcrumbObject = { title: "Employee-360-Profile", url: "/employee-360/profile" };
   this.breadcrumbService.addObjectToAddInBreadcrumb(breadcrumbObject);
    }

   

    this.getBioOverTimeandState();

  

    // Dynamic feature Flags 
    let featureMap:Feature[] = this.currentUser.userMapping.filter(userMap => userMap.tabName == this.tabName);

    console.log("checked logs   ",this.currentUser.userMapping)

    console.log("feature Name ",featureMap);featureMap
    featureMap?.forEach(feat => {
      let inActiveSubfeatures = feat.subFeatures.filter(sub => {
        if(sub.isActive === false)return sub;
      });
      this.userMapping[feat.featureName.replaceAll(' ', '_').toLowerCase()] = (inActiveSubfeatures.length === feat.subFeatures.length) ? false : true;
    });
  

    this.breadcrumbService.currentMessage.pipe(takeUntil(this.unsubscribe$)).subscribe(message =>  {
      this.setActiveTab();
    });

    this.getCountOfRewardsAndAppreciation();
  }
   
     loadProfileImage(imageByte: any) {
       let imageElement = document.getElementById('user-avatar');
       if (imageByte) {
         let objectURL = 'data:image/*;base64,' + imageByte;
         let src: string = this.sanitizer.sanitize(SecurityContext.RESOURCE_URL, this.sanitizer.bypassSecurityTrustResourceUrl(objectURL));
         imageElement.setAttribute("src", src);
       } else {
         imageElement.setAttribute("src", "assets/Images/default-user-image.jpeg");
       }
     }
  async loadEmployee360Data() {
    return new Promise((resolve) => {
      this.utillity.getAllEmployeesFor360Viewnew(this.employeeId).subscribe((response: any) => {
        const employee360Data = JSON.stringify(response.serviceResponse[0]);
         localStorage.setItem("employee360Data", employee360Data);

        // Add a 5-second delay before resolving
        setTimeout(() => {
          resolve(true); // Proceed after 5 seconds
          // this.refreshData(); // Uncomment if you want to refresh after delay
        }, 5000);
      });
    });
  }
  

  refreshData() {
    // Your logic to refresh the view/data
    console.log("Data refreshed!");
  }
  backhistory(){
    window.history.back();
   
  
  }
  backhistory1() {
    const breadcrumbData = sessionStorage.getItem("breadcrumb");
    return breadcrumbData ? JSON.parse(breadcrumbData) : [];
  }

  ngAfterViewInit(): void {
    this.setActiveTab();
  }

  ngOnDestroy(): void {
  if (this.navigationSubscription) {
    this.navigationSubscription.unsubscribe();
  }
  this.removeActiveTab();
  }

  // setActiveTab(){

  //   const tab = document.getElementById('Employee360Tab').querySelector('.nav-link');
  //   //console.log(tab);

  //   if (tab) {
  //     tab.classList.add('active');
  //     const activeRouteLink = tab.getAttribute('routerLink');
  //     this.router.navigate(['./' + activeRouteLink], { relativeTo: this.route });
  //   }
  // }

  setActiveTab(){
    const tabs = document.getElementById('Employee360Tab').querySelectorAll('.nav-link');
    let activeRouteLink:any;

    if ((this.breadcrumbUrl != undefined && this.breadcrumbUrl != null)) {
      let employee360title = this.breadcrumbUrl[this.breadcrumbUrl.length - 1]?.title;

      if(employee360title.includes("Employee-360-Profile")){
        activeRouteLink = 'profile';
      }else if(employee360title.includes("Leave")){
        activeRouteLink = 'leave';
      }else if(employee360title.includes("Project")){
        activeRouteLink = 'project';
      }else if(employee360title.includes("Timesheet")){
        activeRouteLink = 'timesheet';
      }else if(employee360title.includes("Biomax")){
        activeRouteLink = 'biomax';
      }else if(employee360title.includes("Rewards")){
        activeRouteLink = 'rewards';
      }else if(employee360title.includes("Appreciation")){
        activeRouteLink = 'appreciation';
      }else{
        activeRouteLink = 'profile';
      }
      
    } else {
      const tab = document.getElementById('Employee360Tab').querySelector('.nav-link');
        activeRouteLink = tab ? tab.getAttribute('routerLink') : 'profile';
    }

    tabs.forEach(tab => {
      let routeLink = tab.getAttribute('routerLink');
      if (activeRouteLink === routeLink) {
        tab.classList.add('active');
      } else {
        tab.classList.remove('active');
      }
    });
    
    this.router.navigate(['./' + activeRouteLink], { relativeTo: this.route });
  }

  removeActiveTab(){
    const tab = document.getElementById('Employee360Tab').querySelector('.nav-link.active');
    //console.log("active tab :", tab);
    if (tab) {
      tab.classList.remove('active');
    }
  }




  responseOvertime:any
  responsestate:any;

  getBioOverTimeandState(){

    let currentEmp = new Employee(); 
    currentEmp.empId = this.employeeData.empId;
    currentEmp.isDraft = false;

    this.employee360Service.getBioOverTimeandState(currentEmp).subscribe((response:any) =>
      
      {
       this.responseOvertime= response.serviceResponse[0];
       this.responsestate = response.serviceResponse[1];
      }
    
    );
     

  }
 
  leave360viewtab(tab:any){  
          this.currentab=tab;        
          console.log(this.currentab);
  }



  rewardsCount:any;
  appreciationCount:any;
  getCountOfRewardsAndAppreciation(){
    this.appreciationCount='';
    this.rewardsCount='';
    console.log("this.projectDetails ", this.rewardsCount);
    this.appreciationAndRewardsCount.empId=this.employeeData.empId;
    this.employee360Service.getRewardsAndAppreciationCount(this.appreciationAndRewardsCount).pipe(first()).subscribe((response: any) => {
          if (response.serviceStatus == "Success") {
            this.rewardsCount = response.serviceResponse[0].rewardsCount;
            this.appreciationCount = response.serviceResponse[0].appreciationCount;

            console.log("this.projectDetails ",  this.appreciationCount);

          }
        });
  }


   
}
