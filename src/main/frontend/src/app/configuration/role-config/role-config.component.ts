import { Component, OnInit, TemplateRef } from '@angular/core';
import { NgModel } from '@angular/forms';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { first } from 'rxjs/operators';
import { Feature } from 'src/app/models/feature';
import { JobRole } from 'src/app/models/jobRole';
import { SubFeature } from 'src/app/models/subFeature';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { DepartmentService } from 'src/app/services/department.service';
import { JobRoleService } from 'src/app/services/job-role.service';
import { SubfeatureService } from 'src/app/services/subfeature.service';
import { ValidationService } from 'src/app/services/validation.service';
import { ExportExcelService } from 'src/app/services/export-excel.service';
import { Sort } from '@angular/material/sort';

@Component({
  selector: 'app-role-config',
  templateUrl: './role-config.component.html',
  styleUrls: ['./role-config.component.css']
})
export class RoleConfigComponent implements OnInit {

    data:string;
  //flags 
  isCreation: boolean = false;
  isUpdation: boolean = false;
  isForm: boolean = false;
  isTable: boolean = false;

  //modal 
  alertMessage: any;
  modalRef: BsModalRef = new BsModalRef();

  //obj 
  jobRoleObj: JobRole = new JobRole();
  oldJobRole: any;
  newJobRole: any;
  allJobRoleList: any;
  filterJobRoleListForMapping: any;
  filteredJobRoleList: any[] = [];
  allDeptList: any
  allSubFeatures: any = [];
  allMappedSubfeatures: any = [];
  featureList: any = [];
  selectedFeature: any;
  subFeatureList: any;
  isSubFeatureList: boolean = false;

  roleDataForExcel: any[];

  name = 'JobRole.xlsx';

  // for View Role By department 
  selectedDept:any = '';
  filterAllJobRoleList: any;


  feature = "Role Config";
  currentUser: User;
  userMapping: any = {};

  constructor(
    private validationService: ValidationService,
    private modalService: BsModalService,
    private jobRoleService: JobRoleService,
    private departmentService: DepartmentService,
    private subfeatureService: SubfeatureService,
    private authenticationService: AuthenticationService,
    private exportExcelService: ExportExcelService,) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  ngOnInit(): void {
    this.getAllDepartmentList();

    // Dynamic Subfeature Flags 
    let featureMap: Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    featureMap.subFeatures?.forEach(sub => {
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });
    console.log(this.feature, this.userMapping);

    //Deafult values for dropdown
    this.jobRoleObj.departmentId = '';

    this.sectionViewInit();
  }

  sectionViewInit() {
    if(this.userMapping.create_role){
      this.showCreateForm();
    }
    if (this.userMapping.view_all_role || this.userMapping.update_role || this.userMapping.update_role_feature_mapping || this.userMapping.delete_role) {
      //for role table data 
      this.showTable();
    }
  }

  getSubfeatureList() {
    if (this.selectedFeature == null) {
      alert('Select Feature');
      return;
    }

    this.subFeatureList = [];
    this.featureList.filter(feature => {
      if (feature.featureId == this.selectedFeature) {
        this.subFeatureList = feature.subFeatures;
      }
    });
    this.isSubFeatureList = true;
  }

  showCreateForm() {
    this.isForm = true;
    this.isCreation = true;

    this.isTable = false;
    this.isUpdation = false;
    this.isSubFeatureList = false;

    this.reset();
  }

  showTable() {
    this.isTable = true;

    this.isForm = false;
    this.isUpdation = false;
    this.isCreation = false;

    this.getAllJobRoleList();
    this.getAllSubFeatures();
  }

  reset() {
    this.jobRoleObj = new JobRole();
    //Deafult values for dropdown
    this.jobRoleObj.departmentId = '';
    this.selectedFeature = '';

    this.selectedFeature = null;
    this.allJobRoleList = [];
    this.allSubFeatures = [];
    this.allMappedSubfeatures = [];
    this.featureList = [];
    this.subFeatureList = [];
  }

  showUpdateForm(jobRole: JobRole) {
    this.isForm = true;
    this.isTable = false;
    this.isUpdation = true;
    this.isCreation = false;
    this.selectedFeature = '';
    this.subFeatureList = [];

    this.jobRoleObj = Object.assign({}, jobRole)
    this.getSubfeaturesByJobRoleId();
    console.log("showUpdateForm --> jobRoleObj : ", this.jobRoleObj);

  }

  onSelectDepartment() {
    if(this.selectedDept == 'all jobRole'){
      this.filterAllJobRoleList = this.allJobRoleList;
      this.page = 1;
    }else{
      this.filterAllJobRoleList = this.allJobRoleList.filter(x => x.departmentId == this.selectedDept);
      this.page = 1;
    } 
  }

  validateJobRoleObj(jobRole: JobRole, template: TemplateRef<any>) {

    if (!this.validationService.validateNullUndefinedEmptyString(jobRole.name)) {
      this.alertMessage = "Please enter Designation Name !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(jobRole.departmentId)) {
      this.alertMessage = "Please select Department !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(!this.validationService.validateNullUndefinedEmptyString(jobRole.employeeRole)) {
      this.alertMessage = "Please select Role !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    return true;
  }


  // CRUD
  onCreateJobRole(template: TemplateRef<any>) {
    let inputValidated: boolean = this.validateJobRoleObj(this.jobRoleObj, template)
    if (!inputValidated) return;

    this.jobRoleObj.createdById = this.currentUser.empId;;
    this.jobRoleService.createJobRole(this.jobRoleObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showTable();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }

    });
  }

  checkJobRole(template : TemplateRef<any>){
    const regex = /^[a-zA-Z ]+$/;
    if(regex.test(this.jobRoleObj.name)){
      this.jobRoleService.checkJobRole(this.jobRoleObj).pipe(first()).subscribe((response: any)=>{
        if(response.serviceStatus =='Fail'){
          this.openAlertMod(template, response.serviceResponse);
          this.jobRoleObj.name = '';
        }
      })
    } else {
      this.openAlertMod(template, "Enter valid Designation !!");
      this.jobRoleObj.name = '';
    }
  }

  onUpdateJobRole(template: TemplateRef<any>) {
    let inputValidated: boolean = this.validateJobRoleObj(this.jobRoleObj, template)
    if (!inputValidated) return;

    this.jobRoleObj.updatedBy = this.currentUser.empId;;
    this.jobRoleService.updateJobRole(this.jobRoleObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showTable();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  onDeleteJobRole(template: TemplateRef<any>, alertTemplate: TemplateRef<any>) {
    this.cancelRequest();
   // this.getJobRolesByDept(this.jobRoleObj.departmentId);

    this.newJobRole = this.jobRoleObj.newJobRoleId;
    this.oldJobRole = this.jobRoleObj.jobRoleId;
    this.jobRoleService.deleteJobRole(this.jobRoleObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(alertTemplate, response.serviceResponse);
        this.showTable();
      } else {
        this.jobRoleObj.departmentId = '';
      //  this.jobRoleObj.newJobRoleId = this.jobRoleObj.jobRoleId;
        this.jobRoleObj.newJobRoleId = '';
        this.modalRef = this.modalService.show(template);
      }
    });
  }

  validateChangeEmployeeJobRoleMappingObj(jobRole: JobRole, template: TemplateRef<any>) {

    if (!this.validationService.validateNullUndefinedEmptyString(jobRole.departmentId)) {
      this.alertMessage = "Please select Department !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(jobRole.newJobRoleId)) {
      this.alertMessage = "Please select Designation !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    return true;
  }

  onChangeEmployeeJobRoleMapping(template: TemplateRef<any>) {
    this.cancelRequest();
    let inputValidated: boolean = this.validateChangeEmployeeJobRoleMappingObj(this.jobRoleObj, template)
    if (!inputValidated) return;

    this.jobRoleObj.jobRoleId = this.jobRoleObj.newJobRoleId;
    this.jobRoleObj.oldJobRoleId = this.oldJobRole;
    
    this.jobRoleService.changeEmployeeJobRoleMapping(this.jobRoleObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {

        this.jobRoleObj.jobRoleId = this.oldJobRole;
        this.jobRoleService.deleteJobRole(this.jobRoleObj).pipe(first()).subscribe((response: any) => {
          if (response.serviceStatus == "Success") {
            this.openAlertMod(template, response.serviceResponse);
            this.showTable();
          } else {
            this.openAlertMod(template, response.serviceResponse);
          }
        });
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }


  getAllJobRoleList() {
    this.allJobRoleList = [];

    this.jobRoleService.getAllJobRole().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allJobRoleList = response.serviceResponse;
        this.filterAllJobRoleList = this.allJobRoleList;
      } else {
        console.error(response.serviceResponse)
      }
    });
  }

  getAllDepartmentList() {
    this.allDeptList = [];

    this.departmentService.getAllDepartments().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allDeptList = response.serviceResponse;
      } else {
        console.error(response.serviceResponse)
      }
    });
  }

  /* Features-Subfeature Mapping */
  openUpdateConfimationModal(template: TemplateRef<any>,) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
  }

  onUpdateFeatureMapping(template: TemplateRef<any>) {
    let updatedSubFeatureList : any[] = [];

    this.featureList.forEach(feature => {
        if(feature.subFeatures){
          const subFeatureList = feature.subFeatures;
          subFeatureList.forEach(subFeature => {
              if(subFeature.isActive){
                const _sub = new SubFeature();
                _sub.subFeatureMasterId =  subFeature.subFeatureMasterId;
                updatedSubFeatureList.push(_sub);
              }        
          });
        }
    });
    
    let updateFeatureObj = new Feature();
    updateFeatureObj.updatedBy = this.currentUser.empId; 
    updateFeatureObj.subFeatures = updatedSubFeatureList;
    updateFeatureObj.jobRoleId = this.jobRoleObj.jobRoleId;

    console.log("Update feature-mapping : ", updateFeatureObj);
    this.subfeatureService.updateRoleFeatureMapping(updateFeatureObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showTable();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  getAllSubFeatures() {
    this.subfeatureService.getAllSubFeatures().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allSubFeatures = response.serviceResponse;
        this.getFeatureList(this.allSubFeatures);
      } else {
        console.error(response.serviceResponse)
      }
    });
  }

  getFeatureList(allSubFeatures: any) {
    this.featureList = [];
    allSubFeatures.forEach(featureMap => {
      if (this.featureList.length == 0 || !this.featureList.find(feature => feature.featureName === featureMap.featureName)) {
        let feat = new Feature();
        feat.featureId = featureMap.featureId;
        feat.featureName = featureMap.featureName;

        this.featureList.push(feat);
      }
    });
  }

  getSubfeaturesByJobRoleId() {
    this.subfeatureService.getSubfeaturesByJobRoleId(this.jobRoleObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allMappedSubfeatures = response.serviceResponse;
      } else {
        this.allMappedSubfeatures = [];
      }
      this.getActiveSubFeatures();
    });
  }

  getActiveSubFeatures() {
    this.allSubFeatures.forEach(sub => {
      let _sub = new SubFeature();
      _sub.subFeatureMasterId = sub.subFeatureMasterId;
      _sub.subFeatureName = sub.subFeatureName;

      let subMap = this.allMappedSubfeatures.find(subMap => subMap.subFeatureMasterId == sub.subFeatureMasterId);
      if (subMap) {
        _sub.isActive = true;
        _sub.roleFeatureMapId = subMap.roleFeatureMapId;
      } else {
        _sub.isActive = false;
        _sub.roleFeatureMapId = null;
      }

      this.featureList.find(feature => feature.featureId == sub.featureId).subFeatures.push(_sub);
    });
  }

  // download excel
  exportToExcel(): void {

    this.jobRoleService.getAllJobRole().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.roleDataForExcel = response.serviceResponse;
      }

      const onlySpecificDataArr = this.roleDataForExcel.map(
        x => ({
          "Name": x.name,
          "Employee Role":x.employeeRole,
          "Department Name": x.departmentName,
          "Created By": x.createdBy,
          "created On": x.createdOn
        })
      )
      this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.name)
    });
  }

  getJobRolesByDept(departmentId: any) {
    console.log("departmentId : ", departmentId);
    console.log("this.allJobRoleList : ", this.allJobRoleList);


    this.filteredJobRoleList = [];
    this.filteredJobRoleList = this.allJobRoleList.filter(jobRole => jobRole.departmentId == departmentId);

    // for deleting the role
    this.filterJobRoleListForMapping = [];
    this.filterJobRoleListForMapping = this.filteredJobRoleList.filter(x => x.jobRoleId !== this.jobRoleObj.jobRoleId);

    console.log("filteredJobRoleList : ", this.filteredJobRoleList);
  }


  //modals
  openDeleteJobRole(template: TemplateRef<any>, jobRole: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.jobRoleObj = jobRole;
    console.log(this.jobRoleObj);
  }

  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }

  cancelRequest() {
    this.modalRef.hide();
  }

  page = 1;
  handlePageChange(event) {
    this.page = event;
  }

  sortData(sort:Sort){	
    console.log(sort);	
      
    const data=this.filterAllJobRoleList;	
    if(!sort.active || sort.direction==='')	
    {	
      this.filterAllJobRoleList=data;	
      return;	
    }else {	
      this.filterAllJobRoleList=data.sort(	
        (a,b)=>{	
          const isAsc=sort.direction==='asc';	
          switch(sort.active){	
            case 'name':	
              return compare(a.name.toLowerCase() , b.name.toLowerCase() , isAsc)	
              case 'departmentName':	
                return compare(a.departmentName.toLowerCase() , b.departmentName.toLowerCase() , isAsc)	
                case 'createdBy':	
                  return compare(a.createdBy.toLowerCase() , b.createdBy.toLowerCase() ,isAsc)	
                  case 'createdOn':	
                    return compare(a.createdOn , b.createdOn , isAsc)	
                    case 'updatedByName':	
                      return compare(a.updatedByName.toLowerCase() , b.updatedByName.toLowerCase() ,isAsc)	
                      case 'updatedOn':	
                        return compare(a.updatedOn , b.updatedOn , isAsc)	
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


/*

---- DUMMY DATA ---- 


featureList:Feature[] =[
    {
      featureId: 1,
      featureName: "Leave",
      subFeatures: [
        {
          subFeatureId: 1,
          subFeatureName: "Add Leave",
          isActive: true
        },
        {
          subFeatureId: 2,
          subFeatureName: "Approve Leave",
          isActive: true
        },
        {
          subFeatureId: 3,
          subFeatureName: "Reject Leave",
          isActive: true
        },
        {
          subFeatureId: 4,
          subFeatureName: "View My Leaves",
          isActive: true
        },
        {
          subFeatureId: 5,
          subFeatureName: "View Reportee Leaves",
          isActive: true
        }
      ]
    },
    {
      featureId: 2,
      featureName: "EOD",
      subFeatures: [
        {
          subFeatureId: 6,
          subFeatureName: "Add EOD",
          isActive: true
        },
        {
          subFeatureId: 7,
          subFeatureName: "Approve EOD",
          isActive: false
        },
        {
          subFeatureId: 8,
          subFeatureName: "Reject EOD",
          isActive: true
        },
        {
          subFeatureId: 9,
          subFeatureName: "View My EODs",
          isActive: false
        },
        {
          subFeatureId: 10,
          subFeatureName: "View Reportee EODs",
          isActive: true
        }
      ]
    },
    {
      featureId: 3,
      featureName: "Employee",
      subFeatures: [
        {
          subFeatureId: 11,
          subFeatureName: "Add Employee",
          isActive: true
        },
        {
          subFeatureId: 12,
          subFeatureName: "Update Employee",
          isActive: true
        },
        {
          subFeatureId: 13,
          subFeatureName: "Delete Employee",
          isActive: true
        },
        {
          subFeatureId: 14,
          subFeatureName: "View All Employees",
          isActive: true
        }
      ]
    },
  ]

  allFeatures:any = [
    {
      featureName : "Leave",
      featureId : 1
    },
    {
      featureName : "EOD",
      featureId : 2
    },
    {
      featureName : "Employee",
      featureId : 3
    }
  ]

  */