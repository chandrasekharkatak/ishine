

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
import { EncryptionService } from '../services/EncryptionService';
import { DomainService } from '../services/domain.service';
import { EmployeeService } from '../services/employee.service';
import { ImageService } from '../services/image.service';
import { Domain } from '../models/domain';


@Component({
  standalone: false,
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
  employeeObj: Employee = new Employee();
  userMapping:any = {};
  breadcrumbUrl:any[] = [];
  breadcrumbUrl1:any[] = [];
  breadcrumbs: any[] = [];
    domainSpecializationList: any[] = [];


  private navigationSubscription: Subscription;

  constructor(
    private authenticationService: AuthenticationService,
    private router: Router,
    private domainService: DomainService,
    private route: ActivatedRoute,
    private employee360Service: Employee360Service,
    private breadcrumbService: BreadcrumbService,
    private utillity:UtilityService,
   private sanitizer: DomSanitizer,
   private employeeService: EmployeeService,
   private imageService: ImageService,
   private encryptionService: EncryptionService,


  ) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
    this.breadcrumbService.currentBreadcrumb.subscribe(x => this.breadcrumbUrl = x);

  }


  currentab:String;
  leave:String;
  othertab:any;
  currentEmployeeInfo: Employee = new Employee();

  employeeId:any;
  ngOnInit(): void {

    setTimeout(() => {
      this.route.data.subscribe(data => {
        let encryptedData = this.encryptionService.encrypt(JSON.stringify(data.employeeData));
        data.employeeData = this.calculateTotalExperience(employeeData);
        sessionStorage.setItem("employee360Data", encryptedData);
      });
    }, 1000);
    this.navigationSubscription = this.employee360Service.getNavigationEvent().subscribe(() => {
      this.removeActiveTab();
      this.setActiveTab();
    });

    let encryptedEmployeeData = sessionStorage.getItem('employee360Data');
            let employeeData = null;
            if (encryptedEmployeeData) {
                const decryptedString = this.encryptionService.decrypt(encryptedEmployeeData);
                if (decryptedString) {
                    try {
                        employeeData = JSON.parse(decryptedString);
                    } catch (error) {
                        console.error('Failed to parse decrypted session user:', decryptedString, error);
                        employeeData = null;
                    }
                } else {
                    console.warn('Decryption returned empty string.');
                    employeeData = null;
                }
            } else {
                console.warn('No currentUser found in sessionStorage');
                employeeData = null;
            }
            const storedData = employeeData;

    const parsedData = storedData ? storedData : null;
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


    this.onGetEmployeeInfo();
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
        const enryptedEmployee360Data = this.encryptionService.encrypt(JSON.stringify(employee360Data));
         localStorage.setItem("employee360Data", enryptedEmployee360Data);

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
  async onGetEmployeeInfo() {


        console.log("inner fuction");

        this.domainSpecializationList = [];
        this.currentEmployeeInfo = new Employee();
        let currentEmp = new Employee();

        currentEmp.empId = this.employeeData.empId;
        currentEmp.isDraft = false;
        //console.log("currentEmp : ", currentEmp);

        const response: any = await this.employeeService.getEmployeeByEmpId(currentEmp).toPromise();
        sessionStorage.setItem('empId', currentEmp.empId);
        sessionStorage.setItem('empIdA', response.serviceResponse.employeementId);
        sessionStorage.setItem('eId', response.serviceResponse.empId);
        if (response.serviceStatus == "Success") {
          this.currentEmployeeInfo = response.serviceResponse;
          this.currentEmployeeInfo.totalCurrentExperience=this.employeeService.calculateTotalExperience(
              this.currentEmployeeInfo.totalExperience, this.currentEmployeeInfo.dateOfJoining );

          this.employeeObj = response.serviceResponse;
          this.employeeData.billableType=this.employeeObj.billableType;
          //this.currentEmployeeInfo = { ...response.serviceResponse };
          //console.log("currentEmployeeInfo : ", this.currentEmployeeInfo);
          this.loadProfileImage(this.currentEmployeeInfo.imageBytes)

        } else {
          console.error(response.serviceResponse);
        }

        const docResponse: any = await this.imageService.getEmployeeDocuments(currentEmp).toPromise();
        if (docResponse.serviceStatus == 'Success') {
          this.currentEmployeeInfo.documentList = docResponse.serviceResponse;

          //console.log("this.previewObj.documentList : ", this.currentEmployeeInfo.documentList);
        } else {
          //console.log(docResponse.serviceResponse);
        }

        let domainObj = new Domain();
        domainObj.empId = this.employeeData.empId;
        const domainResponse: any = await this.domainService.getDomainSpecializationByEmpId(domainObj).toPromise();

        console.log("yessss", domainResponse)
        if (domainResponse.serviceStatus == "Success") {
          this.domainSpecializationList = domainResponse.serviceResponse;

          this.domainSpecializationList.forEach((object: Domain) => {

            var letters = 'BCDEF'.split('');
            var color = '#';
            for (var i = 0; i < 6; i++) {
              color += letters[Math.floor(Math.random() * letters.length)];
            }

            object.colorCode = color;
          });

          //console.log(this.domainSpecializationList, " : this.domainSpecializationList");
        } else {
          console.error(domainResponse.serviceResponse);
        }

        setTimeout(() => {
          this.currentEmployeeInfo.documentList && this.currentEmployeeInfo.documentList.forEach((doc, index) => {
            if (doc.documentBytes) {
              let preview = document.getElementById(`docPreview${index + 1}`);
              let objectURL = 'data:image/*;base64,' + doc.documentBytes;
              let src: string = this.sanitizer.sanitize(SecurityContext.RESOURCE_URL, this.sanitizer.bypassSecurityTrustResourceUrl(objectURL));
              preview.setAttribute('src', src);
            }
          });
        }, 500);
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
      }else if(employee360title.includes("Grievance")){
        activeRouteLink = 'grievance';
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

   calculateTotalExperience(employeeData: any){
      const previousExp = employeeData.totalExperience ?? 0;

      let apmosysExp = 0;
      if (employeeData.dateOfJoining) {

          const [d, m, y] = employeeData.dateOfJoining.split('-');
          const doj = new Date(`${y}-${m}-${d}`);

          const today = new Date();
          const diff = today.getTime() - doj.getTime();
          apmosysExp = diff / (1000 * 60 * 60 * 24 * 365.25);
        }

      // Total = previous exp + apmosys exp
      let totalExp = previousExp + apmosysExp;
      employeeData.totalCurrentExperience = Number(totalExp.toFixed(1));
      const updatedString = JSON.stringify(employeeData);
      const encryptedUpdatedString = this.encryptionService.encrypt(updatedString);
      sessionStorage.setItem('employee360Data', encryptedUpdatedString);
      return employeeData;

    }

}
