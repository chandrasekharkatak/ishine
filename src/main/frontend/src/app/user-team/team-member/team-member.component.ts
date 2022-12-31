import { Component, OnInit } from '@angular/core';
import { Employee } from 'src/app/models/employee';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { TeamViewService } from 'src/app/services/team-view.service';
import { first } from 'rxjs/operators';
import { User } from 'src/app/models/user';
import { Feature } from 'src/app/models/feature';
import { ExportExcelService } from 'src/app/services/export-excel.service';
import { Sort } from '@angular/material/sort';
import { LocationStrategy } from '@angular/common';

@Component({
  selector: 'app-team-member',
  templateUrl: './team-member.component.html',
  styleUrls: ['./team-member.component.css']
})
export class TeamMemberComponent implements OnInit {

    data:string;
  feature="Team Members";
  currentUser:User;
  userMapping:any = {};

  //excel
  excelName = '';
  elementName = '';

  viewTeamMemberList: any[] = []; 

  constructor(
    private authenticationService : AuthenticationService,
    private teamViewService : TeamViewService,
    private exportExcelService: ExportExcelService,
    private locationStrategy: LocationStrategy
  ) { 
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
   }

  ngOnInit(): void {
    // Dynamic Subfeature Flags 
    let featureMap:Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    featureMap.subFeatures?.forEach(sub => {
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });
    console.log(this.feature, this.userMapping);
    
    this.getAllTeamMemberView();
    this.preventBackButton();
  }
  preventBackButton(){
    history.pushState(null, null, location.href);
    this.locationStrategy.onPopState(()=>{
      history.pushState(null, null, location.href);
    })
  }

  getAllTeamMemberView(){
    this.viewTeamMemberList = []

    let employeeObj = new Employee();
    employeeObj.empId = this.currentUser.empId;
    this.teamViewService.getAllTeamMemberView(employeeObj).pipe(first()).subscribe((response : any) => {
      if (response.serviceStatus == "Success") {
        this.viewTeamMemberList = response.serviceResponse;
        for(let x of this.viewTeamMemberList){
          x.employeementId="A-".concat(x.employeementId)
        }
        console.log("viewTeamMemberList : ", this.viewTeamMemberList);
      } else {
        console.error(response.serviceResponse);
      }
    });

  }

  //excel

  exportToExcel(): void {

    this.excelName = "TeamMemberSheet.xlsx";

      const onlySpecificDataArr = this.viewTeamMemberList.map(
        x => ({
          "Employee Id": x.employeementId,
          "Name": x.name,
          "Email": x.email,
          "Designation": x.jobRoleName,
          "Mobile No": x.mobileNo
        })
      )
      this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.excelName)
    }

  //pagination 

  page = 1;
  handlePageChange(event) {
    this.page = event;
  }

  sortTeamMemberDetails(sort:Sort){
    console.log(sort);
    const data=this.viewTeamMemberList;
    //console.log(data , "-------------------------------///////////");
      if(!sort.active || sort.direction==='')
      {
        this.viewTeamMemberList=data;
        return ;
      }else {
        this.viewTeamMemberList=data.sort(
          (a , b)=>{
            const isAsc=sort.direction==='asc';
            switch(sort.active){
              case 'employeementId':
                return compare(a.employeementId , b.employeementId , isAsc)

                case 'name':
                  return compare(a.name , b.name , isAsc)

                  case 'email':
                  return compare(a.email , b.email , isAsc)

                    case 'jobRoleName':
                      return compare(a.jobRoleName , b.jobRoleName , isAsc)

                      case 'mobileNo':
                        return compare(a.mobileNo , b.mobileNo , isAsc)


                default:
                  return 0;
            }
          }
        )
      }
    

  }





}

function compare(a: number | string, b: number | string, isAsc: boolean) {
  return (a < b ? -1 : 1) * (isAsc ? 1 : -1);
}
