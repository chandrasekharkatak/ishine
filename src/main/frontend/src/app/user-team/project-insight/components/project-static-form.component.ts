import { Component, Input, Output, EventEmitter, TemplateRef, ViewChild } from '@angular/core';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { first } from 'rxjs/internal/operators/first';
import { FormNode } from 'src/app/models/formNode';
import { ProjectInsightProjectDetails } from 'src/app/models/projectInsightDetails';
import { User } from 'src/app/models/user';
import { ApiSourceService } from 'src/app/services/api-source.service';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { DepartmentService } from 'src/app/services/department.service';
import { EmployeeService } from 'src/app/services/employee.service';
import { FormBuilderService } from 'src/app/services/form-builder.service';
import { ProjectInsightDomainService } from 'src/app/services/project-insight-domain.service';
import { ProjectInsightService } from 'src/app/services/project-insight.service';
import { ProjectService } from 'src/app/services/project.service';
import { ValidationService } from 'src/app/services/validation.service';
import { LeftSideMenuComponent } from './left-side-menu.component';
import { ProjectInsightDetailsDTO } from 'src/app/models/projectInsightDetailsDTO';
import { ProjectInsightFormDetails } from 'src/app/models/projectInsightFormDetails';
import { ProjectInsightGroupDetails } from 'src/app/models/projectInsightGroupDetails';
import { FormField } from 'src/app/models/formField';
import { FieldPaletteItem, FieldPalette } from 'src/app/models/fieldPaletteItem';
import { QuestionCardsComponent } from './question-cards.component';

@Component({
  selector: 'app-project-static-form',
  templateUrl: './project-static-form.component.html',
  styleUrls: ['./project-static-form.component.scss']
})

export class ProjectStaticFormComponent {

  @ViewChild(QuestionCardsComponent) questionCardsComponent!: QuestionCardsComponent;
  @ViewChild(LeftSideMenuComponent) leftSideMenuComponent!: LeftSideMenuComponent;
  
  @Input() viewMode!: any;
  @Input() projectInsightDetailsId!: any;
  @Input() tempProjectInsightDetailsDTO!: ProjectInsightDetailsDTO;

  @Output() projectChange = new EventEmitter<void>();
  @Output() departmentChange = new EventEmitter<void>();
  @Output() clientChange = new EventEmitter<number>();
  @Output() showTable = new EventEmitter<any>();

  @ViewChild('alert_message_modal') alertMessageTemplate: TemplateRef<any>;
  @ViewChild('add_new_group_modal') addNewGroupModal: TemplateRef<any>;
  @ViewChild('add_field_modal') addFieldModal: TemplateRef<any>;

  alertModalRef: BsModalRef = new BsModalRef();
  addGroupDetailsModalRef: BsModalRef = new BsModalRef();
  addFieldModalRef: BsModalRef = new BsModalRef();

  allProjects: any[] = [];
  allDeptList: any[] = [];
  selectedDeptList: any[] = [];
  selectedDeptIds: any[] = [];
  allEmployeeList: any[] = [];
  managerList: any[] = [];
  allClientList: any[] = [];
  filteredClientList: any[] = [];
  apiList: any[] = [];
  allDomainDataList: any[] = [];

  alertMessage: any;
  isCurrentNodeGroup: boolean = false;

  // Object 
  currentUser: User;
  currentNodeType: any = '';
  currentNode: FormNode;
  rootNode: FormNode;
  projectInsightDetailsDTO: ProjectInsightDetailsDTO;
  projectInsightProjectDetails: ProjectInsightProjectDetails;
  projectInsightGroupDetails: ProjectInsightGroupDetails;

  //Add new Field to Form
  newFieldType: any = null;
  newFieldConfig: any = {};
  addFieldTargetNode: FormNode;
  showFieldConfig = false;
  editingField: FormField | null = null;
  editingIndex: number = -1;
  editingFieldIndex: number = -1;
  originalFieldData: any = null;
  showExistingFieldsList: boolean = false;
  searchKeyword: any;
  currentVersion: any;
  selectedDomainId: number | null = null;
  fieldPalette: FieldPaletteItem[] = FieldPalette.getAll();

  constructor(
    private departmentService: DepartmentService,
    private modalService: BsModalService,
    private formBuilderService: FormBuilderService,
    private validationService: ValidationService,
    private authenticationService: AuthenticationService,
    private projectInsightService: ProjectInsightService,
    private apiSourceService: ApiSourceService,
    private projectInsightDomainService: ProjectInsightDomainService,
    private employeeService: EmployeeService,
    private projectService: ProjectService
  ) { this.authenticationService.currentUser.subscribe(x => this.currentUser = x); }

  ngOnInit() {
    this.getAllApiSourceList();
    this.renderInitialForm();
  }

  async ngAfterViewInit(): Promise<void> {
    await this.questionCardsComponent.getProjectInsightQuestionDetailsByParentIdAndParentType(this.currentNode?.parentId, this.currentNode?.parentType);
  }

  // Fetch APIs [Start]
  getAllProjects(): Promise<any> {
    this.allProjects = [];
    return this.apiSourceService.getAllProject().pipe(first())
      .toPromise().then(
        (response: any) => {
          this.allProjects = response;
        }
      ).catch(error => {
        console.log(error, " : error");
        this.openAlertModal(error?.error?.serviceResponse || 'An unexpected error occurred.');
      });
  }

  getAllDepartmentList(): Promise<any> {
    this.allDeptList = [];
    return this.departmentService.getAllDeptsList().pipe(first())
      .toPromise().then(
        (response: any) => {
          if (response.serviceStatus == "Success") {
            this.allDeptList = response.serviceResponse;
          } else {
            this.openAlertModal(response.serviceResponse)
          }
        }
      ).catch(error => {
        console.log(error, " : error");
        this.openAlertModal(error?.error?.serviceResponse || 'An unexpected error occurred.');
      });
  }

  getManagerList(): Promise<any> {
    this.managerList = [];
    let employeeObj: any = {
      role: "Manager",
    }
    return this.employeeService.getAllEmployeesByRole(employeeObj).pipe(first())
      .toPromise().then(
        (response: any) => {
          if (response.serviceStatus == "Success") {
            this.managerList = response.serviceResponse;
          } else {
            this.openAlertModal(response.serviceResponse)
          }
        }
      ).catch(error => {
        console.log(error, " : error");
        this.openAlertModal(error?.error?.serviceResponse || 'An unexpected error occurred.');
      });
  }

  getAllClientList(): Promise<any> {
    this.allClientList = [];
    return this.projectService.getAllClients().pipe(first())
      .toPromise().then(
        (response: any) => {
          if (response.serviceStatus == "Success") {
            this.allClientList = response.serviceResponse;
            //remove duplicate clients
            this.filteredClientList = this.allClientList.filter((value, index, self) =>
              index === self.findIndex((t) => (
                t.clientId === value.clientId
              ))
            )
          } else {
            this.openAlertModal(response.serviceResponse)
          }
        }
      ).catch(error => {
        console.log(error, " : error");
        this.openAlertModal(error?.error?.serviceResponse || 'An unexpected error occurred.');
      });

  }

  getAllEmployeeList(): Promise<any> {
    this.allEmployeeList = [];
    return this.formBuilderService.getAllEmployeeList().pipe(first())
      .toPromise()
      .then((response: any) => {
        this.allEmployeeList = response;
      })
      .catch(error => {
        console.log(error, " : error");
        this.openAlertModal(error?.error?.serviceResponse || 'An unexpected error occurred.');
      })
      ;

  }
  // Fetch APIs [End]

  // Form Render [Start]
  async renderInitialForm() {

    await Promise.all([
      this.getAllProjects(),
      this.getAllDepartmentList(),
      this.getManagerList(),
      this.getAllClientList(),
      this.getAllEmployeeList()
    ]);

    this.currentNodeType = 'Project';
    this.rootNode = new FormNode();
    this.currentNode = new FormNode();
    this.projectInsightProjectDetails = new ProjectInsightProjectDetails();
    this.projectInsightGroupDetails = new ProjectInsightGroupDetails();
    this.projectInsightDetailsDTO = new ProjectInsightDetailsDTO();

    if (this.projectInsightDetailsId) {
      this.getProjectInsightDetailsByObjectId(this.projectInsightDetailsId);
    } else {
      this.projectInsightProjectDetails = this.tempProjectInsightDetailsDTO?.projectInsightProjectDetails;
      this.selectedDeptIds = this.getDeptIds(this.projectInsightProjectDetails?.departments);
      this.rootNode = this.transformFormDetailsToFormNode(this.tempProjectInsightDetailsDTO?.projectInsightFormDetails, this.currentNodeType);
      this.currentNode = this.rootNode;
      this.mergeFormDataIntoFormStructure(this.currentNode, this.tempProjectInsightDetailsDTO?.projectInsightProjectDetails);
      this.leftSideMenuComponent.loadProjectInsightTrees(this.projectInsightProjectDetails);
    }
  }

  // Utility [Start]
  updateProjectDetails() {
    if (this.allProjects && this.allProjects?.length > 0) {
      this.allProjects.forEach((project) => {
        if (project.projectId == this.projectInsightProjectDetails?.projectId) {
          this.projectInsightProjectDetails.projectId = project?.projectId;
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

  transformFormDetailsToFormNode(formDetails: any, nodeType: any): FormNode {
    let node: FormNode = new FormNode();
    if (!formDetails || formDetails == undefined || formDetails == null) {
      node.fields = [];
      node.formData = {};
      node.layoutConfig = [];
    } else {
      node = {
        id: formDetails.id,
        formName: formDetails.formName,
        parentId: formDetails.parentId,
        parentType: formDetails.parentType,
        fields: formDetails.fields || [],
        formData: {},
        layoutConfig: this.getLayoutConfig(formDetails.fields || [])
      };
    }
    if (nodeType == 'Project') {
      if (!this.validationService.validateNullUndefinedEmptyList(node?.fields)) {
        node.fields = [];
        node.fields.push(this.getDomainStructure());
      } else {
        const hasDomain = node?.fields.some(
          item => item.name?.toLowerCase() === "domain"
        );
        if (!hasDomain) {
          node.fields.push(this.getDomainStructure());
        }
      }
      node.layoutConfig = this.getLayoutConfig(formDetails.fields || [])
    }
    return node;
  }

  mergeFormDataIntoFormStructure(structure: any, data: any) {
    if (!structure || structure == undefined || structure == null) {

    }
    if (structure?.fields && Array.isArray(structure?.fields) && data?.additionalInfo) {
      structure.fields.forEach(field => {
        field.value = data.additionalInfo[field.name];
      });
      structure.formData = data.additionalInfo;
    }
    if (data?.questions) {
      structure.questionList = data.questions;
    }
    if (structure?.children && data?.child) {
      for (let i = 0; i < structure.children.length; i++) {
        this.mergeFormDataIntoFormStructure(structure.children[i], data.child[i]);
      }
    }
  }

  transformFormNodeToFormDetails(formNode: FormNode): any {
    const formDetails: ProjectInsightFormDetails = {
      id: formNode.id,
      formName: formNode.formName,
      parentId: formNode.parentId,
      parentType: formNode.parentType,
      fields: formNode.fields
    };
    return formDetails;
  }

  getLayoutConfig(fields: any[]): any[][] {
    const rows = new Map<number, any[]>();
    let currentRow = 0;
    let currentRowWidth = 0;
    (fields || []).forEach(field => {
      const fieldWidth = Number(field.width) || 100;
      if (currentRowWidth + fieldWidth > 100) {
        currentRow++;
        currentRowWidth = fieldWidth;
      } else {
        currentRowWidth += fieldWidth;
      }
      if (!rows.has(currentRow)) {
        rows.set(currentRow, []);
      }
      rows.get(currentRow)?.push(field);
    });
    return Array.from(rows.values());
  }

  resetOptionsForOptionTypeAPI(fields: any) {
    if (fields) {
      fields.forEach(row => {
        if (row.optionSource?.toLowerCase() === 'api') {
          row.options = [];
        }
      });
    }
    return fields;
  }

  onSelectedDomainIds(domainIds: any) {
    this.leftSideMenuComponent.loadAllProjectInsightDomain(domainIds);
  }

  onGetProjectInsightGroupDetailsByObjectId(groupId: any) {
    this.getProjectInsightGroupDetailsByObjectId(groupId);
  }

  onGetProjectInsightDetailsByObjectId() {
    this.getProjectInsightDetailsByObjectId(this.rootNode?.parentId);
  }

  getDomainStructure(): any {
    const masterDomainId = this.generateUniqueId();
    return {
      id: masterDomainId,
      type: 'select',
      label: 'Domain',
      name: 'domain',
      required: true,
      placeholder: '',
      defaultValue: '',
      options: [],
      optionSource: 'api',
      width: 25,
      rowPosition: 3,
      multiple: true,
      apiUrl: 'api/getAllProjectInsightDomain',
      apiLabelKey: 'name',
      apiValueKey: 'id',
      parentField: '',
      dependentApiUrl: '',
      dependentLabelKey: '',
      dependentValueKey: '',
      dependentParamName: '',
      parentDynamicId: null,
      hierarchyType: null,
      tableConfig: null,
      isDynamicallyCreated: null,
      value: []
    }
  }
  // Utility [End]

  // Project Insight, Group APIs [Start]
  getProjectInsightDetailsByObjectId(projectInsightDetailsId: any) {
    if (!this.validationService.validateNullUndefinedEmptyString(projectInsightDetailsId)) {
      return;
    }
    this.currentNodeType = 'Project';
    this.currentNode = new FormNode();
    this.projectInsightGroupDetails = new ProjectInsightGroupDetails();
    this.projectInsightDetailsDTO = new ProjectInsightDetailsDTO();
    this.projectInsightService.getProjectInsightDetailsByObjectId(projectInsightDetailsId).pipe(first()).subscribe({
      next: async (response: any) => {
        this.projectInsightDetailsDTO = response;
        this.projectInsightProjectDetails = this.projectInsightDetailsDTO?.projectInsightProjectDetails;
        this.selectedDeptIds = this.getDeptIds(this.projectInsightProjectDetails?.departments);
        this.rootNode = this.transformFormDetailsToFormNode(this.projectInsightDetailsDTO?.projectInsightFormDetails, this.currentNodeType);
        this.currentNode = this.rootNode;
        this.mergeFormDataIntoFormStructure(this.currentNode, this.projectInsightDetailsDTO?.projectInsightProjectDetails);
        this.leftSideMenuComponent.loadProjectInsightGroupTrees(0, this.currentNode?.parentId, this.currentNode?.parentType);
        await this.questionCardsComponent.getProjectInsightQuestionDetailsByParentIdAndParentType(this.currentNode?.parentId, this.currentNode?.parentType);
      },
      error: (error: any) => {
        this.openAlertModal(error);
      }
    });
  }

  async getProjectInsightGroupDetailsByObjectId(projectInsightGroupDetailsId: any) {
    this.projectInsightService.getProjectInsightGroupDetailsByObjectId(projectInsightGroupDetailsId)
      .pipe(first()).subscribe({
        next: async (response: any) => {
          this.currentNodeType = 'Group';
          this.currentNode = new FormNode();
          this.projectInsightGroupDetails = response?.projectInsightGroupDetails;
          this.currentNode = this.transformFormDetailsToFormNode(response?.projectInsightFormDetails, this.currentNodeType);
          this.leftSideMenuComponent.loadProjectInsightGroupTrees(0, projectInsightGroupDetailsId, 'Group');
          this.mergeFormDataIntoFormStructure(this.currentNode, response?.projectInsightGroupDetails);
          await this.questionCardsComponent.getProjectInsightQuestionDetailsByParentIdAndParentType(projectInsightGroupDetailsId, 'Group');
        },
        error: (error: any) => {
          this.openAlertModal(error);
        }
      });
  }

  getProjectInsightGroupDetailsByParentIdAndParentType(parentId: any, parentType: any) {
    let temp = [];
    this.projectInsightService.getProjectInsightGroupDetailsByParentIdAndParentType(parentId, parentType).pipe(first()).subscribe({
      next: (response: any) => {
        temp = response.serviceResponse;
      },
      error: (error: any) => {
        this.openAlertModal(error);
      }
    });
  }

  saveProjectInsightDetails(isDraft: any) {
    this.cancelRequest();

    let projectInsightDetailsDTO: ProjectInsightDetailsDTO = new ProjectInsightDetailsDTO();
    projectInsightDetailsDTO.projectInsightGroupDetails = null;
    projectInsightDetailsDTO.projectInsightQuestionDetails = null;
    projectInsightDetailsDTO.projectInsightProjectDetails = this.projectInsightProjectDetails;
    projectInsightDetailsDTO.projectInsightFormDetails = this.transformFormNodeToFormDetails(this.rootNode);
    projectInsightDetailsDTO.projectInsightProjectDetails.departments = this.getDepartmentObjFromList(projectInsightDetailsDTO.projectInsightProjectDetails.departments);
    projectInsightDetailsDTO.projectInsightProjectDetails.isDraft = isDraft;
    projectInsightDetailsDTO.projectInsightProjectDetails.additionalInfo = this.rootNode.formData;
    let fields = JSON.parse(JSON.stringify(projectInsightDetailsDTO.projectInsightFormDetails.fields));
    projectInsightDetailsDTO.projectInsightFormDetails.fields = this.resetOptionsForOptionTypeAPI(fields);

    if (!projectInsightDetailsDTO.projectInsightProjectDetails.id || projectInsightDetailsDTO.projectInsightProjectDetails.id == undefined || projectInsightDetailsDTO.projectInsightProjectDetails.id == null) {
      projectInsightDetailsDTO.projectInsightProjectDetails.createdBy = this.currentUser.empId;
    } else {
      projectInsightDetailsDTO.projectInsightProjectDetails.updatedBy = this.currentUser.empId;
    }

    let inputValidated: boolean = this.validateProjectInsightDetails(projectInsightDetailsDTO.projectInsightProjectDetails,projectInsightDetailsDTO.projectInsightFormDetails.fields);
    if (!inputValidated) return;

    this.projectInsightService.saveProjectInsightDetails(projectInsightDetailsDTO).pipe(first()).subscribe(
      (response: any) => {
        this.openAlertModal(response.serviceStatus);
        this.getProjectInsightDetailsByObjectId(response?.serviceResponse?.id);
      },
      (error) => {
        console.log(error, " : error");
        this.openAlertModal(error?.error?.serviceResponse || 'An unexpected error occurred.');
      }
    );
  }

  saveProjectInsightStaticGroupDetails(isDraft: any) {
    this.cancelRequest();
    let inputValidated: boolean = this.validateGroupDetails();
    if (!inputValidated) return;

    this.projectInsightGroupDetails.isDraft = isDraft;
    this.projectInsightGroupDetails.createdBy = this.currentUser.empId;
    this.projectInsightGroupDetails.parentId = this.validationService.validateNullUndefinedEmptyString(this.currentNode.parentId) ? this.currentNode.parentId : this.projectInsightGroupDetails.parentId;
    this.projectInsightGroupDetails.parentType = this.validationService.validateNullUndefinedEmptyString(this.currentNode.parentType) ? this.currentNode.parentType : this.projectInsightGroupDetails.parentType;
    this.projectInsightService.saveProjectInsightStaticGroupDetails(this.projectInsightGroupDetails).pipe(first()).subscribe(
      (response: any) => {
        this.closeAddGroupDetailsModal();
        this.getProjectInsightGroupDetailsByObjectId(response?.serviceResponse?.id);
        this.openAlertModal(response.serviceStatus);
      },
      (error) => {
        console.log(error, " : error");
        this.openAlertModal(error?.serviceStatus || 'An unexpected error occurred.');
      }
    );
  }

  saveProjectInsightGroupDetails(isDraft: any) {
    this.cancelRequest();

    let projectInsightDetailsDTO: ProjectInsightDetailsDTO = new ProjectInsightDetailsDTO();
    projectInsightDetailsDTO.projectInsightProjectDetails = null;
    projectInsightDetailsDTO.projectInsightQuestionDetails = null;
    projectInsightDetailsDTO.projectInsightGroupDetails = this.projectInsightGroupDetails;
    projectInsightDetailsDTO.projectInsightFormDetails = this.transformFormNodeToFormDetails(this.currentNode);
    projectInsightDetailsDTO.projectInsightGroupDetails.isDraft = isDraft;
    projectInsightDetailsDTO.projectInsightGroupDetails.additionalInfo = this.currentNode.formData;
    projectInsightDetailsDTO.projectInsightGroupDetails.parentId = this.validationService.validateNullUndefinedEmptyString(this.currentNode.parentId) ? this.currentNode.parentId : this.projectInsightGroupDetails.parentId;
    projectInsightDetailsDTO.projectInsightGroupDetails.parentType = this.validationService.validateNullUndefinedEmptyString(this.currentNode.parentType) ? this.currentNode.parentType : this.projectInsightGroupDetails.parentType;
    let fields = JSON.parse(JSON.stringify(projectInsightDetailsDTO.projectInsightFormDetails.fields));
    projectInsightDetailsDTO.projectInsightFormDetails.fields = this.resetOptionsForOptionTypeAPI(fields);

    if (!projectInsightDetailsDTO.projectInsightGroupDetails.id || projectInsightDetailsDTO.projectInsightGroupDetails.id == undefined || projectInsightDetailsDTO.projectInsightGroupDetails.id == null) {
      projectInsightDetailsDTO.projectInsightGroupDetails.createdBy = this.currentUser.empId;
    } else {
      projectInsightDetailsDTO.projectInsightGroupDetails.updatedBy = this.currentUser.empId;
    }

    let inputValidated: boolean = this.validateGroupDetails();
    if (!inputValidated) return;

    if (isDraft && isDraft === 'Y') {
      // validations 

    }

    this.projectInsightService.saveProjectInsightGroupDetails(projectInsightDetailsDTO).pipe(first()).subscribe(
      (response: any) => {
        this.getProjectInsightGroupDetailsByObjectId(response?.serviceResponse?.id);
        this.openAlertModal(response.serviceStatus);
      },
      (error) => {
        console.log(error, " : error");
        this.openAlertModal(error?.error?.serviceResponse || 'An unexpected error occurred.');
      }
    );
  }

  deleteProjectInsightGroupDetails(currentNode: any) {

  }
  // Project Insight, Group APIs [End]

  // Department [Start]
  onDepartmentChange(): void {
    this.projectInsightProjectDetails.departments = this.allDeptList.filter(dept =>
      this.selectedDeptIds.includes(dept.deptId)
    );
  }

  getAllDepartmentsByProjectId() {
    this.selectedDeptIds = [];
    this.selectedDeptList = [];
    let departmentObject = { projectId: this.projectInsightProjectDetails?.projectId };

    this.departmentService.getAllDepartmentsByProjectId(departmentObject).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.selectedDeptList = response?.serviceResponse;
        this.selectedDeptIds = this.selectedDeptList?.map(dept => dept.deptId);
        if (!this.validationService.validateNullUndefinedEmptyList(this.selectedDeptIds)) {
          this.getAllDepartmentList();
          this.openAlertModal('No departments found for the selected project. Please choose a department.');
          return;
        }
      } else {
        this.openAlertModal(response?.serviceResponse || 'Something went wrong!!');
      }
    });
  }

  getDepartmentObjFromList(departments: any[]) {
    if (departments && departments?.length > 0) {
      return departments.map(dept => ({
        deptId: dept.deptId,
        name: dept.name
      }));
    }
  }

  getDeptIds(departments: any) {
    if (departments && departments?.length > 0) {
      return departments.map(dept => dept.deptId);
    }
  }
  // Department [End]


  // Add Field to Form [Start]
  generateId() {
    return Math.random().toString(36).substr(2, 9);
  }

  generateUniqueId(): string {
    return Date.now() + '_' + Math.random().toString(36).substr(2, 9);
  }

  getFieldIcon(type: string): string {
    const icons: { [key: string]: string } = {
      text: 'fa fa-font',
      textarea: 'fa fa-align-left',
      select: 'fa fa-caret-square-down',
      checkbox: 'fa fa-check-square',
      radio: 'fa fa-dot-circle',
      date: 'fa fa-calendar',
      number: 'fa fa-hashtag',
      email: 'fa fa-envelope',
      file: 'fa fa-file'
    };
    return icons[type] || 'fa fa-question';
  }

  showExistingFields() {
    this.showExistingFieldsList = true;
    this.newFieldType = null;
    this.editingField = null;
    this.editingFieldIndex = -1;
  }

  addNewField(node: FormNode) {
    this.addFieldTargetNode = node;
    this.editingField = {
      id: this.generateUniqueId(),
      type: 'text',
      label: '',
      name: '',
      required: false,
      placeholder: '',
      defaultValue: '',
      options: [],
      optionSource: 'static',
      width: 100,
      rowPosition: 0,
      multiple: false
    };

    this.editingIndex = -1;
    this.showFieldConfig = true;
    this.showFieldTypePalette();
  }

  showFieldTypePalette() {
    this.addFieldModalRef = this.modalService.show(this.addFieldModal);
  }

  editExsitingFields() {
    this.showExistingFieldsList = true;
    this.newFieldType = null;
    this.editingField = null;
    this.editingFieldIndex = -1;
  }

  editExistingField(field: any, index: number) {
    this.editingField = JSON.parse(JSON.stringify(field));
    this.editingFieldIndex = index;
    this.originalFieldData = JSON.parse(JSON.stringify(field));
    this.newFieldType = null;
    this.showExistingFieldsList = false;

    if (['select', 'checkbox', 'radio'].includes(this.editingField.type) && !this.editingField.options) {
      this.editingField.options = [];
    }

    if (this.editingField.type === 'table' && !this.editingField.tableConfig) {
      this.editingField.tableConfig = {
        columns: [{ name: 'col1', label: 'Column 1', type: 'text' }],
        rows: 1
      };
    }
  }

  saveEditedField() {
    if (!this.editingField || this.editingFieldIndex === -1) return;
    if (!this.editingField.label || !this.editingField.name) {
      alert('Label and Name are required fields');
      return;
    }
    this.currentNode.fields[this.editingFieldIndex] = { ...this.editingField };
    this.currentNode.layoutConfig = this.getLayoutConfig(this.currentNode.fields);
    this.editingField = null;
    this.editingFieldIndex = -1;
    this.originalFieldData = null;
    this.showExistingFieldsList = false;
    this.newFieldType = null;
    this.closeAddFieldModal();
  }

  deleteField() {
    if (this.editingFieldIndex === -1) return;
    this.currentNode.fields.splice(this.editingFieldIndex, 1);
    this.currentNode.layoutConfig = this.getLayoutConfig(this.currentNode.fields);
    this.currentNode.fields = [...this.currentNode.fields];
    this.currentNode.layoutConfig = [...this.currentNode.layoutConfig];
    this.editingField = null;
    this.editingFieldIndex = -1;
    this.originalFieldData = null;
    this.showExistingFieldsList = false;
    this.newFieldType = null;
    this.closeAddFieldModal();
  }

  deleteFieldFromList(index: number) {
    if (index == null || index < 0) return;
    this.currentNode.fields.splice(index, 1);
    this.currentNode.layoutConfig = this.getLayoutConfig(this.currentNode.fields);
    this.currentNode.fields = [...this.currentNode.fields];
    this.currentNode.layoutConfig = [...this.currentNode.layoutConfig];
  }

  startFieldConfig(type: any) {
    this.editingField = {
      id: this.generateUniqueId(),
      type: type.type,
      label: '',
      name: '',
      required: false,
      placeholder: '',
      defaultValue: '',
      options: ['select', 'checkbox', 'radio'].includes(type.type) ? [] : undefined,
      optionSource: 'static',
      width: 100,
      rowPosition: 0,
      multiple: false
    };
    this.showFieldConfig = true;
  }

  saveFieldConfig() {
    if (!this.editingField) return;
    this.editingField.width = Number(this.editingField.width);
    if (['select', 'checkbox', 'radio'].includes(this.editingField.type) && this.editingField.optionSource === 'static') {
      if (typeof this.editingField.options === 'string') {
        this.editingField.options = this.editingField.options.split(',').map((opt: string, i: number) => ({
          label: opt.trim(),
          value: opt.trim() || i
        }));
      }
    }
    if (this.editingField.type === 'table' && this.editingField.tableConfig) {
      this.editingField.tableConfig.rows = Number(this.editingField.tableConfig.rows) || 1;
    }

    // Add the field to the current node
    this.addFieldTargetNode.fields.push(this.editingField);
    this.addFieldTargetNode.layoutConfig = this.getLayoutConfig(this.addFieldTargetNode.fields);
    this.editingField = null;
    this.showFieldConfig = false;
  }

  removeOptionFormBuilder(i: number) {
    if (this.editingField && this.editingField.options) {
      this.editingField.options.splice(i, 1);
    }
  }

  addOptionFormBuilder() {
    if (this.editingField) {
      if (!this.editingField.options) this.editingField.options = [];
      this.editingField.options.push({ label: '', value: '' });
    }
  }

  onOptionSourceChange() {
    if (this.editingField) {
      if (this.editingField.optionSource === 'static') {
        if (!this.editingField.options) {
          this.editingField.options = [];
        }
      } else if (this.editingField.optionSource === 'api') {
        this.editingField.options = [];
      } else if (this.editingField.optionSource === 'dependent') {
        this.editingField.parentField = '';
        this.editingField.dependentApiUrl = '';
        this.editingField.dependentLabelKey = 'name';
        this.editingField.dependentValueKey = 'id';
        this.editingField.dependentParamName = '';
        this.editingField.options = [];
      }
    }
  }

  onApiSelect(event: any) {
    if (this.editingField && event.target.value) {
      const selectedApi = this.apiList.find(api => api.url === event.target.value);
      if (selectedApi) {
        this.editingField.apiUrl = selectedApi.url;
        this.editingField.apiLabelKey = selectedApi.labelKey;
        this.editingField.apiValueKey = selectedApi.valueKey;
        this.loadApiOptionsFormBuilder();
      }
    }
  }

  getNonDependentApis() {
    return this.apiList.filter(api => api.isdependent === 'N');
  }

  getDependentApis() {
    return this.apiList.filter(api => api.isdependent === 'Y');
  }

  onDependentApiSelect(event: any) {
    if (this.editingField && event.target.value) {
      const selectedApi = this.apiList.find(api => api.url === event.target.value);
      if (selectedApi) {
        const parentFieldName = this.editingField.parentField;
        const parentField = this.currentNode.fields.find(f => f.name === parentFieldName);
        const parentValueKey = parentField?.apiValueKey || parentFieldName;
        this.editingField.dependentApiUrl = `${selectedApi.url}/{${parentValueKey}}`;
        this.editingField.dependentLabelKey = selectedApi.labelKey;
        this.editingField.dependentValueKey = selectedApi.valueKey;
        this.editingField.dependentParamName = parentValueKey;
      }
    }
  }

  closeAddFieldModal() {
    if (this.addFieldModalRef) {
      this.addFieldModalRef.hide();
    }
    this.editingField = null;
    this.editingFieldIndex = -1;
    this.originalFieldData = null;
    this.showExistingFieldsList = false;
    this.newFieldType = null;
  }

  addFieldFromPalette(field: any) {
    const newField: FormField = {
      id: this.generateId(),
      type: field.type,
      label: field.label,
      name: '',
      required: false,
      placeholder: '',
      defaultValue: '',
      options: field.type === 'select' || field.type === 'checkbox' || field.type === 'radio' ? [] : undefined,
      optionSource: 'static',
      width: 100,
      rowPosition: 0,
      multiple: false,
      ...(field.type === 'table' ? {
        tableConfig: {
          columns: [
            { name: 'col1', label: 'Column 1', type: 'text' },
            { name: 'col2', label: 'Column 2', type: 'text' }
          ],
          rows: 2
        }
      } : {})
    };

    this.editingField = newField;
    this.editingIndex = -1;
    this.showFieldConfig = true;
    this.newFieldType = field;
    this.editingFieldIndex = -1;
    this.showExistingFieldsList = false;
  }

  backToFieldPalette() {
    this.showExistingFieldsList = false;
    this.newFieldType = null;
    this.editingField = null;
    this.editingFieldIndex = -1;
  }

  addFieldToCurrentNode() {
    if (!this.editingField) return;
    this.editingField.width = Number(this.editingField.width);
    if (['select', 'checkbox', 'radio'].includes(this.editingField.type) && this.editingField.optionSource === 'static') {
      if (this.editingField.options && typeof this.editingField.options === 'string') {
        this.editingField.options = this.editingField.options.split(',').map((opt: string, i: number) => ({
          label: opt.trim(),
          value: opt.trim() || i
        }));
      }
    }

    this.currentNode.fields.push(this.editingField);
    this.currentNode.layoutConfig = this.getLayoutConfig(this.currentNode.fields);
    this.currentNode.fields = [...this.currentNode.fields];
    this.editingField = null;
    this.editingFieldIndex = -1;
    this.showExistingFieldsList = false;
    this.newFieldType = null;
    this.closeAddFieldModal();
  }

  getAvailableParentFields(currentField: any): any[] {
    if (!this.currentNode || !this.currentNode.fields) return [];
    return this.currentNode.fields.filter(field =>
      field.name !== currentField.name &&
      ['select', 'radio'].includes(field.type) &&
      field.optionSource !== 'dependent'
    );
  }

  onParentFieldChange() {
    if (this.editingField && this.editingField.parentField) {
      const parentField = this.currentNode.fields.find(f => f.name === this.editingField.parentField);
      const parentValueKey = parentField?.apiValueKey || parentField?.name;
      if (parentField) {
        if (!this.editingField.dependentParamName) {
          this.editingField.dependentParamName = parentValueKey;
        }

        if (this.editingField.dependentApiUrl) {
          const baseUrl = this.editingField.dependentApiUrl.split('/{')[0];
          this.editingField.dependentApiUrl = `${baseUrl}/{${parentValueKey}}`;
        }

        if (!this.currentNode.fieldDependencies) {
          this.currentNode.fieldDependencies = {};
        }
        if (!this.currentNode.dependentFieldsMap) {
          this.currentNode.dependentFieldsMap = {};
        }

        this.currentNode.fieldDependencies[this.editingField.name] = this.editingField.parentField;
        if (!this.currentNode.dependentFieldsMap[this.editingField.parentField]) {
          this.currentNode.dependentFieldsMap[this.editingField.parentField] = [];
        }
        this.currentNode.dependentFieldsMap[this.editingField.parentField].push(this.editingField.name);
      }
    }
  }

  setTableRows(val: number) {
    if (this.editingField && this.editingField.tableConfig) {
      this.editingField.tableConfig.rows = Number(val);
    }
  }

  getAllApiSourceList() {
    this.apiSourceService.getAllApiSourceList().pipe(first()).subscribe({
      next: (response: any) => {
        this.apiList = response;
      },
      error: (error: any) => {
        this.openAlertModal(error);
      }
    });
  }

  loadApiOptionsFormBuilder() {
    if (this.editingField && this.editingField.apiUrl) {
      this.apiSourceService.loadDynamicApi(this.editingField.apiUrl).subscribe({
        next: (data: any) => {
          this.editingField!.options = this.mapApiOptions(data, this.editingField!.apiLabelKey!, this.editingField!.apiValueKey!);
        },
        error: (error) => {
          console.error('Error loading API options:', error);
        }
      });
    }
  }

  mapApiOptions(data: any[], labelKey: string, valueKey: string): any[] {
    const getValue = (obj: any, path: string) =>
      path.split('.').reduce((acc, part) => acc && acc[part], obj);
    return data.map(item => ({
      label: getValue(item, labelKey),
      value: getValue(item, valueKey)
    }));
  }
  // Add Field to Form [End]


  // Dynamic Form & Fields [Start]
  onFieldsUpdated(event: { fields: any[], layoutConfig: any[][] }) {
    this.currentNode.fields = event.fields;
    this.currentNode.layoutConfig = event.layoutConfig;
    this.currentNode.fields = [...this.currentNode.fields];
    this.currentNode.layoutConfig = [...this.currentNode.layoutConfig];
  }

  onFormValueChange(node: FormNode, value: any) {
    node.formData = value;
    Object.assign(node.fields, value);
  }
  // Dynamic Form & Fields [End]

  // Validations [Start]
  validateGroupDetails() {
    let flag = true;
    if (!this.validationService.validateNullUndefinedEmptyString(this.projectInsightGroupDetails.groupTitle)) {
      this.openAlertModal("Please Provide Group Title !!");
      return false;
    }
    if (!this.validationService.validateNullUndefinedEmptyString(this.projectInsightGroupDetails.groupType)) {
      this.openAlertModal("Please select Group Type !!");
      return false;
    }
    return flag;
  }

  validateProjectInsightDetails(projectInsightDetails: any, fields: any) {
    let flag = true;
    let message = '';
    if (!this.validationService.validateNullUndefinedEmptyString(projectInsightDetails.projectId)) {
      message = "Please Select Project Name !!";
      flag = false;
    }
    if (!this.validationService.validateNullUndefinedEmptyString(projectInsightDetails.departments)) {
      message = "Please select atleast one Department !!";
      flag = false;
    }
    if (!this.validationService.validateNullUndefinedEmptyString(projectInsightDetails.projectManagerId)) {
      message = "Please select Project Manager !!";
      flag = false;
    }
    if (!this.validationService.validateNullUndefinedEmptyString(projectInsightDetails?.client?.clientId)) {
      message = "Please select Client !!";
      flag = false;
    }
    if (!this.validationService.validateNullUndefinedEmptyList(fields)) {
      message = "Please add a Domain field and Select a domain associated with the project. !!";
      flag = false;
    }
    if (this.validationService.validateNullUndefinedEmptyList(fields)) {
      const domainField = fields.find(item => item.name?.toLowerCase() === "domain");
      if (!domainField) {
        message = "Please add a Domain field and Select a domain associated with the project. !!";
        flag = false;
      } else {
        if (projectInsightDetails?.additionalInfo["Domain"]) {
          if (!this.validationService.validateNullUndefinedEmptyList(projectInsightDetails?.additionalInfo["Domain"])) {
            message = "Kindly Select atleast one domain associated with the project. !!";
            flag = false;
          }
        } else {
          message = "Please add a Domain field and Select a domain associated with the project. !!";
          flag = false;
        }
      }
    }
    if (!flag) {
      this.openAlertModal(message);
    }
    return flag;
  }
  // Validations [End]

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

  openAddGroupDetailsModal(addGroupFlag: any) {
    console.log(this.rootNode);
    if (this.projectInsightProjectDetails && !this.validationService.validateNullUndefinedEmptyString(this.projectInsightProjectDetails?.id)) {
      this.openAlertModal('Kindly Save Project Insight Details before adding a Group.');
      return;
    }
    let projectInsightGroupDetails: ProjectInsightGroupDetails = this.projectInsightGroupDetails;
    this.projectInsightGroupDetails = new ProjectInsightGroupDetails();
    this.projectInsightGroupDetails.parentId = addGroupFlag ? projectInsightGroupDetails?.parentId : projectInsightGroupDetails?.id;
    this.projectInsightGroupDetails.parentType = addGroupFlag ? projectInsightGroupDetails?.parentType : 'Group';
    this.isCurrentNodeGroup = addGroupFlag ? true : false;
    this.addGroupDetailsModalRef = this.modalService.show(this.addNewGroupModal, { class: 'modal-lg modal-dialog-centered' });
  }

  closeAddGroupDetailsModal() {
    if (this.addGroupDetailsModalRef) {
      this.addGroupDetailsModalRef.hide();
    }
  }
  // Modals [End]}}

  // Close Form
  onCloseForm() {
    this.showTable.emit();
  }
}