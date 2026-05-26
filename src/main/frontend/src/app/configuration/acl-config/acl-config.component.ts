import { CdkDragDrop, CdkDragRelease, moveItemInArray } from "@angular/cdk/drag-drop";
import { Component, OnInit, Renderer2, TemplateRef, ViewChild } from '@angular/core';
import { Sort } from '@angular/material/sort';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { first } from 'rxjs/operators';
import { Employee } from 'src/app/models/employee';
import { Feature } from 'src/app/models/feature';
import { JobRole } from 'src/app/models/jobRole';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { ExportExcelService } from 'src/app/services/export-excel.service';
import { JobRoleService } from 'src/app/services/job-role.service';
import * as XLSX from 'xlsx';
import { LoaderService } from 'src/app/services/loader.service';
@Component({
  standalone: false,
  selector: 'app-acl-config',
  templateUrl: './acl-config.component.html',
  styleUrls: ['./acl-config.component.css']
})
export class AclConfigComponent implements OnInit {

  @ViewChild("alert_message") alertModal: TemplateRef<any>;
  @ViewChild("acl_update_confirm_template") aclConfirmTemplate: TemplateRef<any>;

  feature = 'Configurations';
  currentUser: User;
  userMapping: any = {};

  alertMessage: any;
  modalRef: NgbModalRef;

  isAccessFeatureMapping: boolean = false;
  isDefaultFeatureMapping: boolean = false;
  employeeRole: any;

  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType: any;

  columns: any[] = [];
  paginateData: any[] = [];
  paginateDataCopy: any[] = [];
  finalColumns: any[] = [];
  copyFinalColumns: any[] = [];
  pos: any;
  release: boolean = true;
  insideCols: any[] = [];
  allJobRoleList: any[] = [];
  personaWiseJobRole: any[] = [];
  subfeatureList: any[] = [];
  mappedSubFeatureList: any[] = [];
  updatedRoleSubFeature: any[] = [];
  hiddenColumnObj: any[] = [];
  showColumnList: any[] = [];
  defaultMappingList: any[] = [];
  defaultMappingListFilter: any[] = [];
  updateDefaultMapping: any[] = [];
  disableUpdateButton: boolean = true;
  excelName: any;
  isSearchEnabled: boolean = false;
  filters: any = {};
  searchText: string = '';
  employeeObj: Employee = new Employee();

  personas = [
    { label: 'Employee', value: 'Employee' },
    { label: 'Team Lead', value: 'TeamLead' },
    { label: 'Manager', value: 'Manager' },
    { label: 'HR', value: 'HR' },
    { label: 'RMG', value: 'RMG' },
    { label: 'HOD', value: 'HOD' },
    { label: 'Super Admin', value: 'SuperAdmin' }
  ];

  // ACL Adv Search
  selectedDepartments: string[] = [];
  selectedDesignations: string[] = [];
  selectedSubFeature: any[] = [];
  designationDropdown: boolean = false;
  mappedDesignationList: any[] = [];
  filteredDesignationsForDropdown: any[] = [];
  filteredDepartments: any[] = [];
  subFeatureListForDropdown: any[] = [];
  subFeatureListForDropdownCopy: any[] = [];
  subFeatureSearch: boolean = false;
  advSearchFlag: boolean = false;
  filteredDistinctValues: any = {};
  aclAdvColumns: any[] = [
    { column: 'Tab Name', value: [], distinctValues: [], searchText: '' },
    { column: 'Feature', value: [], distinctValues: [], searchText: '' },
    { column: 'Sub Feature', value: [], distinctValues: [], searchText: '' },
    { column: 'blank' }, { column: 'blank' }, { column: 'blank' },
    { column: 'blank' }, { column: 'blank' }, { column: 'blank' }
  ];

  constructor(
    private authenticationService: AuthenticationService,
    private modalService: NgbModal,
    private jobRoleService: JobRoleService,
    private exportExcelService: ExportExcelService,
    private renderer2: Renderer2,
    private loaderService: LoaderService
  ) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  ngOnInit(): void {
    let featureMap: Feature[] = this.currentUser.userMapping.filter(
      userMap => userMap.tabName == this.feature
    );
    featureMap?.forEach(feat => {
      feat.subFeatures?.forEach(sub => {
        this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
      });
    });

    // Initialize ACL view
    this.isAccessFeatureMapping = true;
  }

  toggleAccessList(event?: any) {
    if (!event) {
      this.isAccessFeatureMapping = true;
      this.isDefaultFeatureMapping = false;
      return;
    }
    if (event.target.checked) {
      this.isAccessFeatureMapping = false;
      this.isDefaultFeatureMapping = true;
      this.employeeRole = '';
      this.getDefaultMapping(this.alertModal);
      this.finalColumns = [];
      this.paginateData = [];
    } else {
      this.isAccessFeatureMapping = true;
      this.isDefaultFeatureMapping = false;
    }
    this.resetAclAdv();
  }

  selectPersona(event: any) {
    this.employeeRole = event.target.value;
    this.showColumnList = [];
    this.hiddenColumnObj = [];
    this.getAllJobRoleList(this.employeeRole);
    this.resetAclAdv();
  }

  getAllJobRoleList(persona: any) {
    this.personaWiseJobRole = [];
    this.columns = [];
    this.mappedSubFeatureList = [];
    this.subfeatureList = [];
    this.paginateData = [];
    this.finalColumns = [];
    this.subFeatureListForDropdown = [];
    this.subFeatureListForDropdownCopy = [];

    this.jobRoleService.getAllSubFeatureList().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.subfeatureList = response.serviceResponse;
        this.columns.push({ "field": "subFeature", "header": "Sub-Feature" });

        this.jobRoleService.getAllJobRole().pipe(first()).subscribe((response: any) => {
          if (response.serviceStatus == "Success") {
            this.allJobRoleList = response.serviceResponse;
            this.personaWiseJobRole = this.allJobRoleList.filter((x) => x.employeeRole == persona);
            this.personaWiseJobRole.forEach(role => {
              this.columns.push({
                "field": role.jobRoleId,
                "header": role.name,
                "department": role.departmentName
              });
            });

            var final = [];
            this.columns.forEach(function (e) {
              var match = false;
              final.forEach(function (i) {
                if (e.department == i.department[0].department) match = true;
              });
              if (!match) {
                final.push({ "header": e.department, "department": [e] });
              } else {
                final.forEach(function (i) {
                  if (e.department == i.department[0].department) i.department.push(e);
                });
              }
            });

            this.finalColumns = final;
            this.copyFinalColumns = JSON.parse(JSON.stringify(final));
            this.filteredDepartments = this.finalColumns.filter(c => !!c.header);

            this.mappedDesignationList = [];
            this.finalColumns.forEach(item => {
              item.department.forEach(dep => {
                this.mappedDesignationList.push({
                  department: item.header || null,
                  designation: dep.header,
                });
              });
            });

            this.subfeatureList.forEach(subfeature => {
              let paginateDataItem: any = {};
              this.columns.forEach((column, index) => {
                if (index === 0) {
                  paginateDataItem[column.field] = subfeature.subFeatureName;
                  paginateDataItem['subfeatureId'] = subfeature.subFeatureId;
                } else {
                  paginateDataItem[column.field] = false;
                }
              });
              this.paginateData.push(paginateDataItem);
            });

            if (!this.subFeatureSearch) {
              this.paginateDataCopy = JSON.parse(JSON.stringify(this.paginateData));
            }

            this.subFeatureListForDropdown = Array.from(
              new Map(this.paginateData.map(item => [item.subfeatureId, {
                subFeatureId: item.subfeatureId,
                subFeature: item.subFeature
              }])).values()
            );
            this.subFeatureListForDropdownCopy = [...this.subFeatureListForDropdown];

            // Apply existing subfeature filter if any
            let selectedIds = this.selectedSubFeature.map(x => x.subFeatureId);
            if (selectedIds.length > 0) {
              this.paginateData = this.paginateData.filter(row =>
                selectedIds.includes(Number(row.subfeatureId))
              );
            }

            this.employeeObj.jobRoleIds = this.personaWiseJobRole.map(r => r.jobRoleId);
            this.jobRoleService.getMappedSubFeatureList(this.employeeObj)
              .pipe(first())
              .subscribe((resp: any) => {
                if (resp.serviceStatus === "Success") {
                  let mappedList = resp.serviceResponse;
                  mappedList.forEach(sub => {
                    let row = this.paginateData.find(d => d.subFeature === sub.subFeatureName);
                    if (row) row[sub.jobRoleId] = true;
                  });
                  mappedList.forEach(sub => {
                    let row = this.paginateDataCopy.find(d => d.subFeature === sub.subFeatureName);
                    if (row) row[sub.jobRoleId] = true;
                  });
                  this.updateSelectAllCheckbox();
                  if (this.subFeatureSearch) { this.updateAclData(); }
                }
              });
          }
        });
      }
    });
  }

  dropRow(event: CdkDragDrop<string[]>) {
    moveItemInArray(this.paginateData, event.previousIndex, event.currentIndex);
  }

  dropCol(event: CdkDragDrop<string[]>) {
    if (event.previousIndex != 0 && event.currentIndex !== 0) {
      moveItemInArray(this.finalColumns, event.previousIndex, event.currentIndex);
    }
  }

  dropInsideCol(event: CdkDragDrop<string[]>) {
    if (event.previousIndex != 0 && event.currentIndex !== 0) {
      this.insideCols = [];
      this.finalColumns.forEach((x) => { this.insideCols.push(...x.department); });
      moveItemInArray(this.insideCols, event.previousIndex, event.currentIndex);
    }
  }

  mouseDown(event: any, el: any = null) {
    el = el || event.target;
    this.pos = {
      x: el.getBoundingClientRect().left - event.clientX + 'px',
      y: el.getBoundingClientRect().top - event.clientY + 'px',
      width: el.getBoundingClientRect().width + 'px'
    };
  }

  onDragRelease(event: CdkDragRelease) {
    this.renderer2.setStyle(event.source.element.nativeElement, 'margin-left', '0px');
  }

  selectCellCheckbox(element: any, isAssigned: any, subFeatureId: any) {
    const alreadyUpdatedMapping = this.updatedRoleSubFeature.findIndex(
      (x) => x.subFeatureId == subFeatureId && x.jobRoleId == element
    );
    if (alreadyUpdatedMapping >= 0) {
      this.updatedRoleSubFeature.splice(alreadyUpdatedMapping, 1);
    } else {
      this.updatedRoleSubFeature.push({
        "jobRoleId": element,
        "isAssigned": isAssigned,
        "subFeatureId": subFeatureId
      });
    }
    if (this.updatedRoleSubFeature.length > 0) {
      this.disableUpdateButton = false;
    }
  }

  selectAllACLSubFeature(row: any) {
    const subId = row.subfeatureId;
    this.disableUpdateButton = false;
    const numericFields: string[] = [];
    this.finalColumns.forEach(col => {
      col.department.forEach(dep => {
        if (/^\d+$/.test(dep.field)) numericFields.push(dep.field);
      });
    });
    numericFields.forEach(field => {
      row[field] = row.selectAll;
      this.updateRoleSubFeature(field, subId, row.selectAll);
    });
  }

  updateRoleSubFeature(jobRoleId: any, subFeatureId: number, isAssigned: boolean) {
    const index = this.updatedRoleSubFeature.findIndex(
      x => x.subFeatureId === subFeatureId && x.jobRoleId === jobRoleId
    );
    if (index >= 0) {
      if (this.updatedRoleSubFeature[index].isAssigned === isAssigned) {
        this.updatedRoleSubFeature.splice(index, 1);
      } else {
        this.updatedRoleSubFeature[index].isAssigned = isAssigned;
      }
    } else {
      this.updatedRoleSubFeature.push({ jobRoleId, subFeatureId, isAssigned });
    }
  }

  updateSelectAllCheckbox() {
    if (!this.paginateData?.length) return;
    const fields = this.finalColumns.slice(1)
      .reduce((acc: any[], c: any) => acc.concat(c.department), [])
      .map((d: any) => d.field);
    this.paginateData.forEach(row => {
      row.selectAll = fields.every(f => row[f] === true);
    });
  }

  updateJobRoleSubFeatureMapping(template: TemplateRef<any>) {
    let jobRoleObj = new JobRole();
    jobRoleObj.updatedJobRoleFeatureMapping = this.updatedRoleSubFeature;
    this.jobRoleService.updateJobRoleSubFeatureMapping(jobRoleObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.updatedRoleSubFeature = [];
        this.getAllJobRoleList(this.employeeRole);
        this.selectedDepartments = [];
        this.selectedDesignations = [];
        this.selectedSubFeature = [];
        this.disableUpdateButton = true;
      } else {
        this.openAlertMod(template, response.serviceResponse);
        this.updatedRoleSubFeature = [];
      }
    });
  }

  hideColumn(column: any) {
    this.hiddenColumnObj = this.finalColumns.find(x => x.header == column);
    this.finalColumns = this.finalColumns.filter(x => x.header != column);
    this.showColumnList.push(this.hiddenColumnObj);
  }

  getDefaultMapping(template: TemplateRef<any>) {
    let payload = this.aclAdvColumns
      .filter(({ value }) => Array.isArray(value) ? value.length > 0 : !!value && String(value).trim() !== '')
      .map(({ column, value }) => ({ column, value }));

    if (payload.length === 0) {
      payload = [{ column: 'none', value: [] }];
    }

    this.jobRoleService.getDefaultMapping(payload).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.defaultMappingList = response.serviceResponse;
        this.defaultMappingList.forEach((object) => {
          const formattedPermissions = object.permissionList.reduce((permissions, permission) => {
            permission.permission = permission.permission == "N" ? false : true;
            permissions[permission.employeeRole] = permission.permission;
            return permissions;
          }, {});
          object.permissionList = formattedPermissions;
        });
        this.processData();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  selectDefaultMapCellCheckbox(subFeatureId: any, isAssigned: any, roleName: any, subFeatureName: any) {
    const alreadyUpdatedMapping = this.updateDefaultMapping.findIndex(
      (x) => x.subFeatureId == subFeatureId && x.employeeRole == roleName
    );
    if (alreadyUpdatedMapping >= 0) {
      this.updateDefaultMapping.splice(alreadyUpdatedMapping, 1);
    } else {
      this.updateDefaultMapping.push({
        "subFeatureId": subFeatureId,
        "employeeRole": roleName,
        "permission": isAssigned,
        "subFeatureName": subFeatureName
      });
    }
  }

  updateDefaultFeatureMapping(template: TemplateRef<any>) {
    let jobRoleObj = new JobRole();
    jobRoleObj.updateDefaultFeatureMapping = this.updateDefaultMapping;
    jobRoleObj.updatedBy = this.currentUser.empId;
    this.jobRoleService.updateDefaultFeatureMapping(jobRoleObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  processData() {
    const tabSeen = {};
    const featureSeen = {};
    this.defaultMappingListFilter = this.defaultMappingList
      .sort((a, b) => {
        const tabComp = (a.tabName || '').localeCompare(b.tabName || '');
        return tabComp !== 0 ? tabComp : (a.featureName || '').localeCompare(b.featureName || '');
      })
      .map(x => {
        const tabSpan = tabSeen[x.tabName] ? 0 :
          this.defaultMappingList.filter(y => y.tabName === x.tabName).length;
        tabSeen[x.tabName] = true;
        const featureSpan = featureSeen[x.tabName] && featureSeen[x.tabName][x.featureName] ? 0 :
          this.defaultMappingList.filter(y => y.tabName === x.tabName && y.featureName === x.featureName).length;
        featureSeen[x.tabName] = featureSeen[x.tabName] || {};
        featureSeen[x.tabName][x.featureName] = true;
        return { ...x, tabSpan, featureSpan };
      });

    if (!this.advSearchFlag) {
      this.aclAdvColumns[0].distinctValues = [...new Set(this.defaultMappingListFilter.map(i => i.tabName).filter(Boolean))].map(v => ({ item: v }));
      this.aclAdvColumns[1].distinctValues = [...new Set(this.defaultMappingListFilter.map(i => i.featureName).filter(Boolean))].map(v => ({ item: v }));
      this.aclAdvColumns[2].distinctValues = [...new Set(this.defaultMappingListFilter.map(i => i.subFeatureName).filter(Boolean))].map(v => ({ item: v }));
      this.aclAdvColumns.forEach(col => {
        if (!this.filteredDistinctValues[col.column]) {
          this.filteredDistinctValues[col.column] = [];
        }
        this.filteredDistinctValues[col.column].splice(
          0,
          this.filteredDistinctValues[col.column].length,
          ...col.distinctValues
        );
      });
    }
  }

  confirmAndUpdateACL(template: TemplateRef<any>): void {
    this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-sm' });
  }

  proceedAclUpdate(template: TemplateRef<any>): void {
    this.cancelRequest();
    this.updateJobRoleSubFeatureMapping(template);
  }

  updateAclData() {
    const filteredFinalColumns = this.copyFinalColumns.map(item => {
      if (item.header === undefined) return item;
      const hasSelectedDepartments = this.selectedDepartments?.length > 0;
      const hasSelectedDesignations = this.selectedDesignations?.length > 0;
      const isDeptSelected = !hasSelectedDepartments || this.selectedDepartments.includes(item.header);
      if (isDeptSelected) {
        const filteredDepartment = !hasSelectedDesignations
          ? item.department
          : item.department.filter(dep => this.selectedDesignations.includes(dep.header));
        if (hasSelectedDesignations && filteredDepartment.length === 0) return null;
        return { ...item, department: filteredDepartment };
      }
      return null;
    }).filter(item => item !== null);
    this.finalColumns = filteredFinalColumns;
  }

  resetAclAdv() {
    this.aclAdvColumns[0].value = [];
    this.aclAdvColumns[1].value = [];
    this.aclAdvColumns[2].value = [];
    this.isSearchEnabled = false;
    this.advSearchFlag = false;
    this.selectedDepartments = [];
    this.selectedDesignations = [];
    this.selectedSubFeature = [];
    this.designationDropdown = false;
    this.subFeatureSearch = false;
    this.disableUpdateButton = true;
  }

  toggleSearch() {
    this.isSearchEnabled = !this.isSearchEnabled;
    if (!this.isSearchEnabled) {
      this.filters = {};
    }
  }
  onAclAdvSearch(selectBox?: any,type?:any){ 
    this.loaderService.requestStarted();
    setTimeout(() => {
      try {
        this.performAclAdvSearch(selectBox, type);
      } finally {
        this.loaderService.requestEnded();
      }
    }, 0);
  }
  applyAclDropdownFilter(selectBox: any, type: string): void {
  this.onAclAdvSearch(selectBox, type);
}
private performAclAdvSearch(selectBox?: any,type?:any){
    if(type=='departmentSearch'){

    if(this.selectedDepartments.length>0) {
      this.designationDropdown=true;
    }else{    
      this.designationDropdown=false } 
    this.searchText = '';

    const filtered = this.mappedDesignationList
    .filter(item => item.department && this.selectedDepartments.includes(item.department))
    .map(item => item.designation);
    
  this.filteredDesignationsForDropdown = Array.from(new Set(filtered))
  .map(desig => ({
    desig: desig
  }));

    this.updateAclData()
    
    }else if(type=='designationSearch'){
        this.updateAclData()
    }else if(type=='subFeatureSearch'){
        this.subFeatureSearch=true;
        this.getAllJobRoleList(this.employeeRole);
      console.log("selectedSubFeature===>>",this.selectedSubFeature)
        this.updateAclData()
    }else{
      this.advSearchFlag=true;  
      this.getDefaultMapping(this.alertModal);
    }
    this.updateSelectAllCheckbox();
}
clearAclDropdownFilter(type: string): void {
  this.loaderService.requestStarted();
  setTimeout(() => {
    try {
      if (type === 'Dept') {
        this.selectedDepartments = [];
        this.selectedDesignations = [];
        this.designationDropdown = false;
        this.filteredDesignationsForDropdown = [];
        this.finalColumns = this.copyFinalColumns;
      } else if (type === 'Desig') {
        this.selectedDesignations = [];
      } else {
        this.selectedSubFeature = [];
        this.subFeatureSearch = false;
        this.paginateData = JSON.parse(JSON.stringify(this.paginateDataCopy));
        this.subFeatureListForDropdown = JSON.parse(JSON.stringify(this.subFeatureListForDropdownCopy));
      }
      this.updateAclData();
      this.updateSelectAllCheckbox();
    } finally {
      this.loaderService.requestEnded();
    }
  }, 0);
}

  exportToExcel(): void {
    this.excelName = `${this.employeeRole}-ACLReport.xlsx`;
    let columnsData = [];
    let fieldData = [];

    this.finalColumns.map(column => {
      columnsData.push(...column.department.map(field => field));
    });

    let columns = columnsData.map(column => column.field);
    let departments = {};
    let designations = {};

    columns.forEach(column => {
      let data = columnsData.find(cd => cd.field == column);
      if (data.department) {
        departments[column] = Object.values(departments).includes(data.department) ? "" : data.department;
      } else {
        departments[column] = "";
      }
      designations[column] = data ? data.header : "";
    });

    fieldData.push(departments, designations, ...this.paginateDataCopy);

    const onlySpecificDataArr = fieldData.map(response => {
      let data = {};
      columns.forEach((header, index) => { data[index] = "" + response[header]; });
      return data;
    });

    const worksheet: XLSX.WorkSheet = XLSX.utils.json_to_sheet(onlySpecificDataArr, { skipHeader: true });
    const book: XLSX.WorkBook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(book, worksheet, 'Sheet1');
    XLSX.writeFile(book, this.excelName);
  }

  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-sm' });
    this.alertMessage = message;
  }

  cancelRequest() {
    this.modalRef?.close();
  }

  sortData(sort: Sort) {
    if (sort.active) {
      let sortParams: any[] = sort.active?.split("|");
      this.sortColumn = sortParams[0];
      this.sortColumnType = sortParams[1];
      this.sortDirection = sort.direction;
    }
  }
}