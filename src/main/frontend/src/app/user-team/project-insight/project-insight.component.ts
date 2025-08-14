import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { first } from 'rxjs/operators';
import { ApiSourceService } from 'src/app/services/api-source.service';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { DepartmentService } from 'src/app/services/department.service';
import { FormBuilderService } from 'src/app/services/form-builder.service';
import { ProjectInsightService } from 'src/app/services/project-insight.service';
import { ProjectInsightDomainService } from 'src/app/services/project-insight-domain.service';
import { ValidationService } from 'src/app/services/validation.service';
import { ProjectInsightProjectDetails } from 'src/app/models/projectInsightDetails';
import { User } from 'src/app/models/user';
import { LeftSideMenuComponent, ProjectTableComponent } from './components';
import { ProjectInsightDetailsDTO } from 'src/app/models/projectInsightDetailsDTO';

@Component({
  selector: 'app-project-insight',
  templateUrl: './project-insight.component.html',
  styleUrls: ['./project-insight.component.scss', './project-insight.component.css']
})

export class ProjectInsightComponent implements OnInit {

  @ViewChild(ProjectTableComponent) projectTableComponent!: ProjectTableComponent;

  @ViewChild('alert_message_modal') alertMessageTemplate: TemplateRef<any>;
  @ViewChild('delete_project_insight_modal') deleteProjectInsightTemplate: TemplateRef<any>;
  @ViewChild('open_create_project_modal') openCreateProjectTemplate: TemplateRef<any>;

  alertModalRef: BsModalRef = new BsModalRef();
  openCreateModalRef: BsModalRef = new BsModalRef();
  deleteProjectInsightModalRef: BsModalRef = new BsModalRef();

  // List
  allDeptList: any[] = [];
  allDepartmentWiseFormList: any[] = [];
  allProjects: any[] = [];
  selectedDeptList: any[] = [];
  selectedDeptIds: any[] = [];
  allProjectInsightProjectList: any[] = [];

  // Variable
  projectId: any;
  selectedFormId: any;
  deleteProjectInsightId: any;
  projectInsightDetailsId: any;
  alertMessage: any;

  viewType : 'Table' | 'Form' | 'Question Library'= 'Table';
  viewMode: 'Edit' | 'View' = 'Edit';

  // Object 
  currentUser: User;
  projectInsightProjectDetails: ProjectInsightProjectDetails = new ProjectInsightProjectDetails();
  projectInsightDetailsDTO: ProjectInsightDetailsDTO = new ProjectInsightDetailsDTO();

  // Domain
  allDomains: any[] = [];
  allDomainDataList: any[] = [];
  allDomainList: string[] = []
  allDomainWithProject: any = {};
  domainColors: any = {};
  selectedDomain: string = null;
  childrenSubDomain: number = null;
  isDomainAlreadySelected: boolean = false;

  constructor(
    private departmentService: DepartmentService,
    private modalService: BsModalService,
    private formBuilderService: FormBuilderService,
    private validationService: ValidationService,
    private authenticationService: AuthenticationService,
    private projectInsightService: ProjectInsightService,
    private apiSourceService: ApiSourceService,
    private projectInsightDomainService: ProjectInsightDomainService,
  ) { this.authenticationService.currentUser.subscribe(x => this.currentUser = x); }

  ngOnInit(): void {
    this.showTable();
    this.getAllProjectWithDomain();
  }

  showTable() {
    this.viewType = 'Table';
    this.selectedDeptIds = [];
    this.selectedDeptList = [];
    this.selectedFormId = null;
    this.projectInsightDetailsId = null;
    this.projectInsightProjectDetails = new ProjectInsightProjectDetails();
    this.projectInsightDetailsDTO = new ProjectInsightDetailsDTO();
  }

  showQuestionLibrary(){
    this.viewType = 'Question Library';
  }

  //----------------------------------------------------------------------------------------------------------------------------------------------------------------

  // Project [Start]
  openCreateProject() {
    this.projectId = null;
    this.selectedDeptIds = [];
    this.selectedFormId = null;
    this.getAllProjects();
    this.getAllDepartmentList();
    this.openCreateModalRef = this.modalService.show(this.openCreateProjectTemplate, { class: 'modal-lg' });
  }

  closeCreateProject() {
    if (this.openCreateModalRef) {
      this.openCreateModalRef.hide();
    }
  }

  getAllProjects() {
    this.allProjects = [];
    this.apiSourceService.getAllProject().pipe(first()).subscribe(
      (response: any) => {
        this.allProjects = response;
      },
      (error) => {
        console.log(error, " : error");
        this.openAlertModal(error?.error?.serviceResponse || 'An unexpected error occurred.');
      }
    );
  }

  async showCreateProject() {
    if (!this.allDepartmentWiseFormList || this.allDepartmentWiseFormList?.length == 0) {
      this.openAlertModal('No Form(s) found for Selected Department(s).');
      return;
    }
    this.cancelRequest();
    let response = await this.getFormByFormId(this.selectedFormId);
    this.projectInsightDetailsDTO = new ProjectInsightDetailsDTO();
    this.projectInsightDetailsDTO.projectInsightProjectDetails = this.projectInsightProjectDetails;
    this.updateProjectDetails();
    this.onDepartmentChange();
    this.projectInsightDetailsDTO.projectInsightFormDetails = response;
    this.viewType = 'Form';
    this.viewMode = 'Edit';
    this.closeCreateProject();
  }

  async openViewProjectInsight(projectInsightDetailsId: any) {
    this.viewType = 'Form';
    this.viewMode = 'View';
    this.projectInsightDetailsId = projectInsightDetailsId;
  }

  async openEditProjectInsight(projectInsightDetailsId: any) {
    this.viewMode = 'Edit';
    this.viewType = 'Form';
    this.selectedDeptIds = [];
    this.projectInsightDetailsId = projectInsightDetailsId;
  }

  openDeleteProjectInsight(projectInsightId: any) {
    this.deleteProjectInsightId = projectInsightId;
    this.alertModalRef = this.modalService.show(this.deleteProjectInsightTemplate, { class: 'modal-sm' });
  }

  updateProjectDetails() {
    if (this.allProjects && this.allProjects?.length > 0) {
      this.allProjects.forEach((project) => {
        if (project.projectId == this.projectId) {
          this.projectInsightProjectDetails.projectId = Number(this.projectId);
          this.projectInsightProjectDetails.projectManagerId = project?.projectManagerId;
          this.projectInsightProjectDetails.projectManagerName = project?.projectManagerName;
          this.projectInsightProjectDetails.projectName = project?.projectName;
          this.projectInsightProjectDetails.client.clientId = project?.clientId;
          this.projectInsightProjectDetails.client.clientName = project?.clientName;
          this.projectInsightProjectDetails.apmosysRM = project?.apmosysRM;
          this.projectInsightProjectDetails.clientRM = project?.clientRM;
        }
      });
    }
  }

  deleteProjectInsightById() {
    this.cancelRequest();
    this.projectInsightService.deleteProjectInsightById(this.deleteProjectInsightId).pipe(first()).subscribe({
      next: (response: any) => {
        this.openAlertModal(response.serviceMessage);
        this.projectTableComponent.getAllProjectInsightProjectList();
      },
      error: (error: any) => {
        this.openAlertModal(error);
      }
    });
  }

  async getFormByFormId(formId: string): Promise<any> {
    this.cancelRequest();
    try {
      const response: any = await this.formBuilderService.getByDynamicFormById(formId)
        .pipe(first())
        .toPromise();
      response.id = null;
      return response;
    } catch (error) {
      this.openAlertModal(error);
      throw error;
    }
  }
  // Project [End]

  // Department [Start]
  getAllDepartmentList() {
    this.allDeptList = [];
    this.selectedDeptList = [];
    this.departmentService.getAllDeptsList().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allDeptList = response.serviceResponse;
      } else {
        console.error(response.serviceResponse)
      }
    });
  }

  getAllDepartmentsByProjectId(isCalledFromModal: any) {
    this.selectedDeptIds = [];
    this.selectedDeptList = [];
    this.selectedFormId = null;
    let departmentObject = { projectId: this.projectId };

    this.departmentService.getAllDepartmentsByProjectId(departmentObject).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.selectedDeptList = response?.serviceResponse;
        this.selectedDeptIds = this.selectedDeptList?.map(dept => dept.deptId);
        if (!this.validationService.validateNullUndefinedEmptyList(this.selectedDeptIds)) {
          this.getAllDepartmentList();
          this.openAlertModal('No departments found for the selected project. Please choose a department.');
          return;
        }
        if (isCalledFromModal) {
          this.getAllDynamicFormByDepartmentAndType();
        }
      } else {
        this.openAlertModal(response?.serviceResponse || 'Something went wrong!!');
      }
    });
  }

  getAllDynamicFormByDepartmentAndType() {
    this.selectedFormId = null;
    if (this.selectedDeptIds && this.selectedDeptIds?.length > 0) {
      this.allDepartmentWiseFormList = [];
      this.formBuilderService.getAllDynamicFormByDepartmentAndType(this.selectedDeptIds).pipe(first()).subscribe({
        next: (response: any) => {
          this.allDepartmentWiseFormList = response;
          if (!this.allDepartmentWiseFormList || this.allDepartmentWiseFormList?.length == 0) {
            this.openAlertModal('No Form(s) found for Selected Department.');
            return;
          }
        },
        error: (error: any) => {
          this.openAlertModal(error || 'Something went wrong!!');
          return;
        }
      });
    }
  }

  onDepartmentChange(): void {
    this.projectInsightProjectDetails.departments = this.allDeptList.filter(dept =>
      this.selectedDeptIds.includes(dept.deptId)
    );
  }
  // Department [End]

  // Domain [Start]
  getAllDomainsList() {
    this.projectInsightDomainService.getAllDomainList().subscribe({
      next: (res: any[]) => {
        this.allDomains = res;
      }, error: (error: any) => {
        console.error("Error Getting All Domains List : ", error);
        throw error;
      }
    });
  }

  getAllProjectWithDomain() {
    this.apiSourceService.getAllProjectWithDomain().subscribe({
      next: (res: any[]) => {
        this.allDomainList = Object.keys(res);
        this.allDomainList.forEach((domain, index) => {
          this.domainColors[domain] = this.getRandomColor();
        });
        this.allDomainWithProject = res
      }, error: (error: any) => {
        throw error;
      }
    });
  }

  selectDomain(domain: string) {
    if (!this.isDomainAlreadySelected || !(domain == this.selectedDomain)) {
      this.selectedDomain = domain;
      this.isDomainAlreadySelected = true;
      this.projectTableComponent.getAllProjectInsightProjectList(domain);
    } else {
      this.selectedDomain = null;
      this.isDomainAlreadySelected = false
      this.projectTableComponent.getAllProjectInsightProjectList();
    }
  }

  selectChildrenOfDomain(childrenSubDomain: number, domain: string, unique_name: string) {
    this.childrenSubDomain = childrenSubDomain
    this.selectedDomain = domain
    this.isDomainAlreadySelected = true
    this.projectTableComponent.getAllProjectInsightProjectList(childrenSubDomain, unique_name);
  }

  getRandomColor(): string {
    const colors = [
      '#1e3a8a', '#4338ca', '#5b21b6', '#7c3aed', '#9333ea',
      '#a21caf', '#be123c', '#b91c1c', '#dc2626', '#c2410c',
      '#b45309', '#92400e', '#0f766e', '#065f46', '#047857',
      '#064e3b', '#0c4a6e', '#1e40af', '#1d4ed8', '#2563eb',
      '#3b82f6', '#312e81', '#422006', '#78350f', '#3f6212',
      '#365314', '#14532d', '#166534', '#115e59', '#134e4a'
    ];
    return colors[Math.floor(Math.random() * colors.length)];
  }
  // Domain [End]

  // Modals [Start]
  openAlertModal(message: any) {
    this.alertMessage = message;
    this.alertModalRef = this.modalService.show(this.alertMessageTemplate, { class: 'modal-sm' });
  }

  cancelRequest() {
    if (this.alertModalRef) {
      this.alertModalRef.hide();
    }
  }
  // Modals [End]

}
