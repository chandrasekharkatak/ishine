import { Component, ElementRef, OnInit, TemplateRef, ViewChild } from '@angular/core';
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
import { GroupBrowserComponent } from './group-browser/group-browser.component';
import { EmployeeService } from 'src/app/services/employee.service';
import { ProjectService } from 'src/app/services/project.service';
import { FormNode } from 'src/app/models/formNode';
import { Sort } from '@angular/material/sort';
import { ActivatedRoute, Router } from '@angular/router';

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
  @ViewChild('ask_confirmation') confirmation: TemplateRef<any>;
  @ViewChild('ask_level_confirmation') levelConfirmation: TemplateRef<any>;
  @ViewChild(GroupBrowserComponent) groupbrowser!: GroupBrowserComponent;
  @ViewChild('searchInput') searchInput!: ElementRef<HTMLInputElement>;

  alertModalRef: BsModalRef = new BsModalRef();
  askConfirmation: BsModalRef = new BsModalRef();
  openCreateModalRef: BsModalRef = new BsModalRef();
  deleteProjectInsightModalRef: BsModalRef = new BsModalRef();

  // List
  searchTerm = '';
  allDeptList: any[] = [];
  allDepartmentWiseFormList: any[] = [];
  allProjects: any[] = [];
  selectedDeptList: any[] = [];
  selectedDeptIds: any[] = [];
  allProjectInsightProjectList: any[] = [];
  allProjectList: any[] = [];
  selectedProject: any;
  selectedDepartments: number[] = [];
  projectList: any[] = [];
  selectedProj: any;

  // Variable
  projectId: any;
  selectedFormId: any;
  deleteProjectInsightId: any;
  projectInsightDetailsId: any;
  alertMessage: any;

  viewType: 'Table' | 'Form' | 'Question Library' | 'Response' | 'Approval-Tab' | 'Department-Forms' | 'Domains' | 'Knowledge-Hub' = 'Table';
  viewMode: 'Edit' | 'View' = 'Edit';

  showQues: boolean = false;
  hideProjList: boolean = false;
  isProjectInsightDetailsTab = true;

  isApprovalTab: boolean = false;
  // Object 
  currentUser: User;
  projectInsightProjectDetails: ProjectInsightProjectDetails = new ProjectInsightProjectDetails();
  projectInsightDetailsDTO: ProjectInsightDetailsDTO = new ProjectInsightDetailsDTO();

  project: ProjectInsightProjectDetails = new ProjectInsightProjectDetails();
  projectInsightDto: ProjectInsightDetailsDTO = new ProjectInsightDetailsDTO;
  pendingQuestionAlertMessage: any;
  allChildConfirmation: any;
  allChildsRecursiv: boolean = false;

  // Domain
  allDomains: any[] = [];
  allDomainDataList: any[] = [];
  allDomainList: string[] = []
  allDomainWithProject: any = {};
  domainColors: any = {};
  selectedDomain: Set<string> = new Set();
  childrenSubDomain: Set<number> = new Set();
  childrenSelectedString: Set<string> = new Set();
  isDomainAlreadySelected: boolean = false;

  constructor(
    private departmentService: DepartmentService,
    public projectService: ProjectService,
    private modalService: BsModalService,
    private formBuilderService: FormBuilderService,
    private validationService: ValidationService,
    private authenticationService: AuthenticationService,
    private projectInsightService: ProjectInsightService,
    private apiSourceService: ApiSourceService,
    private projectInsightDomainService: ProjectInsightDomainService,
    private router: Router, private route: ActivatedRoute
  ) { this.authenticationService.currentUser.subscribe(x => this.currentUser = x); }

  ngOnInit(): void {
    this.showTable();
    // this.getAllProjectWithDomain();
    // this.getAllDomainsList();
    // this.getAllDomainsList();
  }

  showTable() {
    this.isProjectInsightDetailsTab = true;
    this.viewType = 'Table';
    this.selectedDeptIds = [];
    this.selectedDeptList = [];
    this.selectedFormId = null;
    this.projectInsightDetailsId = null;
    this.selectedDomain = new Set();
    this.childrenSubDomain = new Set();
    this.childrenSelectedString = new Set();
    this.projectInsightProjectDetails = new ProjectInsightProjectDetails();
    this.projectInsightDetailsDTO = new ProjectInsightDetailsDTO();
    this.getAllProjectWithDomain();
  }

  showQuestionLibrary() {
    this.isProjectInsightDetailsTab = true;
    this.viewType = 'Question Library';
  }

  navigateToChild(routeTo: any) {
    if (routeTo === 'department-forms') {
      this.viewType = 'Department-Forms';
    }
    else if (routeTo === 'knowledge-hub') {
      this.viewType = 'Knowledge-Hub';
    } else if (routeTo === 'domains') {
      this.viewType = 'Domains';
    }
    this.isProjectInsightDetailsTab = false;
    this.router.navigate([routeTo], { relativeTo: this.route });
  }

  navigateToProjectInsightDetails(routeToTab: any) {
    if (routeToTab === 'Question Library') {
      this.showQuestionLibrary();
    } else if (routeToTab === 'Response') {
      if (this.isApprovalTab) {
        this.getMyAssignedQues('approval');
      } else {
        this.getMyAssignedQues('assigned');
      }
    } else {
      this.showTable();
    }
    this.router.navigate(['/user-team/project-library']);
  }

  // Project [Start]
  openCreateProject() {
    this.projectId = null;
    this.selectedDeptIds = [];
    this.selectedFormId = null;
    this.getAllProjects();
    this.getAllDepartmentList();
    this.openCreateModalRef = this.modalService.show(this.openCreateProjectTemplate, { class: 'modal-lg' });
  }

  get sortedProjects() {
    return [...this.allProjects].sort((a, b) =>
      a.projectName.localeCompare(b.projectName)
    );
  }

  searchTermProject = '';
  searchTermDept = '';
  searchTermForm = '';

  filteredProjects: any[] = [];
  filteredDepartments: any[] = [];
  filteredForms: any[] = [];

  filterProjects() {
    const term = this.searchTermProject.trim().toLowerCase();
    this.filteredProjects = this.allProjects.filter(p =>
      p.projectName?.toLowerCase().includes(term)
    );
  }

  filterDepartments() {
    const term = this.searchTermDept.trim().toLowerCase();
    this.filteredDepartments = this.allDeptList.filter(dept =>
      dept.name?.toLowerCase().includes(term) ||
      this.selectedDeptIds.includes(dept.deptId) // keep already selected
    );
  }

  filterForms() {
    const term = this.searchTermForm.trim().toLowerCase();
    this.filteredForms = this.allDepartmentWiseFormList.filter(f =>
      f.formName?.toLowerCase().includes(term)
    );
  }

  // Reset lists when dropdown opens
  onOpenChange(open: boolean, type: string) {
    if (open) {
      if (type === 'project') {
        this.filteredProjects = [...this.allProjects];
        this.filteredDepartments = [...this.allDeptList];
        this.searchTermProject = '';
      } else if (type === 'department') {
        this.filteredDepartments = [...this.allDeptList];
        this.searchTermDept = '';
      } else if (type === 'form') {
        this.filteredForms = [...this.allDepartmentWiseFormList];
        this.searchTermForm = '';
      }
    }
  }

  getMyAssignedQues(type: string) {
    this.isProjectInsightDetailsTab = true;
    if (type == 'assigned') {
      this.projectInsightService.getProjectSummary(this.currentUser.empId).subscribe((res: any[]) => {
        this.projectList = res;
        this.projectService.projectMap.clear();
        this.projectList.forEach(item => {
          this.projectService.projectMap.set(item.projectId, item);
        });
        this.hideProjList = false;
        this.viewType = 'Response';
      });
    } else if (type == 'approval') {
      this.projectInsightService.getProjectSummaryApproval(this.currentUser.empId).subscribe((res: any[]) => {
        this.projectList = res;
        this.projectService.projectMap.clear();
        this.projectList.forEach(item => {
          this.projectService.projectMap.set(item.projectId, item);
        });
        this.hideProjList = false;
        this.viewType = 'Approval-Tab';
      });
    }
  }

  sendQuestionsForApproval(project: any) {
    if (project?.totalCount == 0) {
      this.openAlertModal('No Questions Present in this group');
    } else if (project?.pendingCount > 0) {
      this.pendingQuestionAlertMessage = 'Some Questions Are not answered in this group Do you wish to submit only answered questions for review and leave remaining one ?';
      this.askConfirmation = this.modalService.show(this.confirmation);
    } else {
      this.askLevelApproval();
    }
  }

  askLevelApproval() {
    this.cancelRequest();
    this.allChildConfirmation = 'Do You Wish to Submit Group Level Questions Only or send All Questions recursively from All child groups also?';
    this.alertModalRef = this.modalService.show(this.levelConfirmation);
  }

  saveConsent(consent: boolean) {
    this.cancelRequest();
    this.allChildsRecursiv = consent;
    this.sendAnsweredforApproval(this.selectedProj)
  }

  sendAnsweredforApproval(proj: any) {
    let request = {
      empId: this.currentUser.empId,
      parentType: 'Project',
      parentId: proj.projectId,
      toAllChilds: this.allChildsRecursiv
    }
    //Call Api to assign reviewer for all answers of all questions in that group one level or AllLevel? 
    this.projectInsightService.assignQuestionsToReviewers(request).pipe(first()).subscribe({
      next: (res: any) => {
        this.cancelRequest();
        this.groupbrowser.loadQuestionsByGroupOrProjectId(proj.projectId, 'Project');
        this.groupbrowser.sendForUpdate(this.project, 3);
        this.alertMessage = res;
        this.alertModalRef = this.modalService.show(this.alertMessageTemplate);
      },
      error: (error: any) => {
        this.cancelRequest();
        this.alertMessage = error;
        this.alertModalRef = this.modalService.show(this.alertMessageTemplate, { class: 'modal-sm' });
      }
    });
  }

  convertToArray(obj: Set<string>): string[] {
    return Array.from(obj);
  }

  selectProject(project: any) {
    this.selectedProj = project;
    this.projectService.projectMap.clear();
    this.projectService.projectMap.set(project.projectId, project);
    this.getProjectInsightDataForCards(project.projectId);
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

  getAllDynamicDepartmentsByProject(isCalledFromModal: any) {
    this.selectedDeptIds = [];
    this.selectedFormId = null;
    let departmentObject = {
      projectId: this.projectInsightProjectDetails?.projectId
    }
    this.selectedDeptList = [];
    this.departmentService.getAllDepartmentsByProjectId(departmentObject).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.selectedDeptList = response.serviceResponse;
        this.selectedDeptIds = this.selectedDeptList.map(dept => dept.deptId);
        if (isCalledFromModal) {
          this.getAllDynamicFormByDepartmentAndType();
        }
      } else {
        console.error(response.serviceResponse)
      }
    });
  }
  // Department [End]

  // Domain [Start]
  getAllDomainsList() {
    this.projectInsightDomainService.getAllDomainList().pipe(first()).subscribe({
      next: (res: any[]) => {
        this.allDomains = res;
      }, error: (error: any) => {
        console.error("Error Getting All Domains List : ", error);
        throw error;
      }
    });
  }

  // getAllProjectWithDomain() {
  //   this.apiSourceService.getAllProjectWithDomain().pipe(first()).subscribe({
  //     next: (res: any[]) => {
  //       this.allDomainList = Object.keys(res);
  //       this.allDomainList.forEach((domain, index) => {
  //         this.domainColors[domain] = res[domain].color || this.getRandomColor();
  //       });
  //       this.allDomainWithProject = res
  //     }, error: (error: any) => {
  //       throw error;
  //     }
  //   });
  // }

  getAllProjectWithDomain() {
    this.apiSourceService.getAllProjectWithDomain().pipe(first()).subscribe({
      next: (res: any) => {
        // Convert object to array and sort by data length
        const sortedEntries = Object.entries(res).sort((a: any, b: any) => {
          const lenA = a[1]?.data?.length || 0;
          const lenB = b[1]?.data?.length || 0;
          return lenB - lenA; // biggest first
        });

        // Rebuild object manually (instead of Object.fromEntries)
        const sortedRes: any = {};
        sortedEntries.forEach(([key, value]) => {
          sortedRes[key] = value;
        });

        // Assign values
        this.allDomainList = Object.keys(sortedRes);
        this.allDomainList.forEach((domain) => {
          this.domainColors[domain] = sortedRes[domain].color || this.getRandomColor();
        });
        this.allDomainWithProject = sortedRes;
      },
      error: (error: any) => {
        throw error;
      }
    });
  }



  selectDomain(domain: string) {
    domain = domain.trim();
    // if (!(this.selectedDomain.has(domain))) {
    //   this.selectedDomain.add(domain);
    //   this.isDomainAlreadySelected = true;
    //   this.projectTableComponent.getAllProjectInsightProjectList(domain);
    // } else {
    //   this.selectedDomain.delete(domain);
    //   this.childrenSubDomain = new Set();
    //   this.childrenSelectedString = new Set();
    //   this.isDomainAlreadySelected = false
    //   this.projectTableComponent.getAllProjectInsightProjectList();
    // }

    // if(!this.selectedDomain.has(domain)) {
    //   this.selectedDomain.add(domain);
    //   this.isDomainAlreadySelected = true;
    //   const domains = this.selectedDomain && [...this.selectedDomain].map(String).join(",") || null;
    //   // this.projectTableComponent.getAllProjectInsightProjectList(domains);
    // }

    // const removedDomain:boolean = this.selectedDomain.delete(domain);
    // this.childrenSubDomain = new Set();
    // this.childrenSelectedString = new Set();
    // this.isDomainAlreadySelected = false
    // const domains = this.selectedDomain && [...this.selectedDomain].map(String).join(",") || null;
    // this.projectTableComponent.getAllProjectInsightProjectList(domains);

    if (!this.selectedDomain.has(domain)) {
      this.selectedDomain.add(domain);
      this.isDomainAlreadySelected = true;
    } else {
      this.selectedDomain.delete(domain);
      this.childrenSubDomain.clear();
      this.childrenSelectedString.clear();
      this.isDomainAlreadySelected = false;
    }

    // Compute domain list once and call API
    const domains = this.selectedDomain.size > 0
      ? [...this.selectedDomain].map(String).join(",")
      : null;

    this.projectTableComponent.getAllProjectInsightProjectList(domains);

  }

  selectChildrenOfDomain(childrenSubDomain: Set<number>, domain: Set<string>, unique_name: string, childrenSelectedString: Set<string>) {
    this.childrenSelectedString = new Set(childrenSelectedString);
    this.childrenSubDomain = new Set(childrenSubDomain);

    // childrenSelectedString.forEach((value) => {
    //   this.childrenSelectedString.add(value);
    // });
    // childrenSubDomain.forEach((value) => {
    //   this.childrenSubDomain.add(value);
    // });
    // this.childrenSelectedString = childrenSelectedString;
    // this.childrenSubDomain = childrenSubDomain;

    // domain.forEach((value) => {
    //   this.selectedDomain.add(value);
    // })

    //check if all are already empty
    if(childrenSubDomain.size ==0 && this.childrenSubDomain.size == 0 && childrenSelectedString.size == 0 && this.childrenSelectedString.size == 0 && domain.size ==0 && this.selectedDomain.size == 0 && unique_name == null) {
      return;
    }

    this.selectedDomain = new Set(domain);
    this.isDomainAlreadySelected = true
    const selectedChildrenDomainString: string | null = this.childrenSelectedString.size > 0
      ? [...this.childrenSelectedString].map(String).join(",")
      : null;
    const selectedChildrenDomainId = this.childrenSubDomain.size > 0 ? [...this.childrenSubDomain].map(String).join(",") : null;
    const selectedDomain = this.selectedDomain.size > 0 ? [...this.selectedDomain].map(String).join(",") : null;
    this.projectTableComponent.getAllProjectInsightProjectList(selectedDomain, selectedChildrenDomainString, selectedChildrenDomainId);
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

  getProjectInsightDataForCards(projectId: any) {
    console.log()
    this.projectInsightService.getProjectInsightDetailsByObjectId(projectId).pipe(first()).subscribe({
      next: (response: any) => {
        this.project = response?.projectInsightProjectDetails;
        this.projectInsightDto.projectInsightProjectDetails = this.project;
        this.hideProjList = true;
      },
      error: (error: any) => {
        this.openAlertModal(error);
      }
    });
  }

  backToQuesProjectList() {
    console.log('Back to List of All Aprojects with Ques Count.');
    this.getMyAssignedQues(this.isApprovalTab?'approval':'assigned');
  }

  //Modal [Start]
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
