import { Component, EventEmitter, Input, Output, TemplateRef, ViewChild } from '@angular/core';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { first } from 'rxjs/internal/operators/first';
import { ENTITY_TYPES } from 'src/app/models/EntityType';
import { FieldPalette, FieldPaletteItem } from 'src/app/models/fieldPaletteItem';
import { FormField } from 'src/app/models/formField';
import { FormNode } from 'src/app/models/formNode';
import { ProjectInsightProjectDetails } from 'src/app/models/projectInsightDetails';
import { ProjectInsightDetailsDTO } from 'src/app/models/projectInsightDetailsDTO';
import { ProjectInsightFacetCategory } from 'src/app/models/projectInsightFacetCategory';
import { ProjectInsightFormDetails } from 'src/app/models/projectInsightFormDetails';
import { ProjectInsightGroupDetails } from 'src/app/models/projectInsightGroupDetails';
import { User } from 'src/app/models/user';
import { ApiSourceService } from 'src/app/services/api-source.service';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { DepartmentService } from 'src/app/services/department.service';
import { EmployeeService } from 'src/app/services/employee.service';
import { FormBuilderService } from 'src/app/services/form-builder.service';
import { KnowledgeHubService } from 'src/app/services/knowledge-hub.service';
import { ProjectInsightDomainService } from 'src/app/services/project-insight-domain.service';
import { ProjectInsightService } from 'src/app/services/project-insight.service';
import { ProjectService } from 'src/app/services/project.service';
import { ValidationService } from 'src/app/services/validation.service';
import { LeftSideMenuComponent } from '../left-side-menu/left-side-menu.component';
import { QuestionCardsComponent } from '../question-cards/question-cards.component';
import { ProjectInsightFacetService } from 'src/app/services/project-insight-facet.service';

export interface SubmitAssignRequest {
  empId: any,
  parentType:any,
  parentId: any,
  toAllChilds : boolean
}
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
  @Input() isQuestionOverview:boolean = false;
  @Input() isApprovalTab:boolean = false;
  @Input() type = 'Project';
  @Input() searching = {value:false, query:""};
  @Input() projectId = null; // only if the type is group, it is for rendering the left side of project menu
  @Input() knowledgeHub = false;

  @Output() projectChange = new EventEmitter<void>();
  @Output() departmentChange = new EventEmitter<void>();
  @Output() clientChange = new EventEmitter<number>();
  @Output() showTable = new EventEmitter<any>();
  @Output() showQuesProjectList = new EventEmitter<any>();

  @ViewChild('alert_message_modal') alertMessageTemplate: TemplateRef<any>;
  @ViewChild('add_new_group_modal') addNewGroupModal: TemplateRef<any>;
  @ViewChild('add_field_modal') addFieldModal: TemplateRef<any>;
  @ViewChild('ask_confirmation') confirmation!: TemplateRef<any>;
  @ViewChild('ask_level_confirmation') levelConfirmation!: TemplateRef<any>;

  alertModalRef: BsModalRef = new BsModalRef();
  addConsentSaveAndAssign: BsModalRef = new BsModalRef();
  doSaveAndAssign: BsModalRef = new BsModalRef();
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
  pendingConfirmation: string = '';
  allChildConfirmation: string = '';
  allChildsRecursiv:boolean = false;
  submitAndAssignRequest: SubmitAssignRequest = {
    empId: null,
    parentType: null,
    parentId: null,
    toAllChilds: false
  };  
  isCurrentNodeGroup: boolean = false;
  isDragEnabled:boolean = false;
  isResizeEnabled:boolean = false;

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

  filterText: string = '';
  filteredCategories:ProjectInsightFacetCategory[] = [];
  facetCategoryList:ProjectInsightFacetCategory[] = [];

  constructor(
    private departmentService: DepartmentService,
    private modalService: BsModalService,
    private formBuilderService: FormBuilderService,
    private validationService: ValidationService,
    private authenticationService: AuthenticationService,
    private projectInsightService: ProjectInsightService,
    private projectInsightFacetService: ProjectInsightFacetService,
    private apiSourceService: ApiSourceService,
    private projectInsightDomainService: ProjectInsightDomainService,
    private employeeService: EmployeeService,
    public projectService: ProjectService,
    private knowledgeHubService: KnowledgeHubService,
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

  highlight(text: any): string {
    
    // if (!this.searching.value) return text;
    // if (!this.searching.query || text == null) {
    //   return typeof text === 'string' ? text : JSON.stringify(text);
    // }

    // const textStr = typeof text === 'string' ? text : JSON.stringify(text, null, 2);
    // const escapedQuery = this.searching.query.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
    // const regex = new RegExp(escapedQuery, 'gi');

    // console.log("Found in text: ", textStr.match(regex));
    
    // return textStr.replace(regex, match =>
    //   `<span class="highlight">${match}</span>`
    // );

    const highlightedText = this.knowledgeHubService.highlight(text, this.searching.query, !!this.searching.value);
    // if(text.toLowerCase().includes("i")){
    //   console.log("Highlighted Text : ", highlightedText);
    // }
    return highlightedText;
    
  }

  onSelect(event: any, selected: any, keyPath?: string) {
    if(this.viewMode === 'View'){
      return;
    }
    const value = event.value;

    if (keyPath) {
      const keys = keyPath.split('.');
      let obj = selected;

      for (let i = 0; i < keys.length - 1; i++) {
        if (!obj[keys[i]]) {
          obj[keys[i]] = {}; 
        }
        obj = obj[keys[i]];
      }

      obj[keys[keys.length - 1]] = value;
    } else {
      selected = value;
    }

    console.log("Selected after:", this.projectInsightProjectDetails);
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

    this.rootNode = new FormNode();
    this.currentNode = new FormNode();
    this.projectInsightProjectDetails = new ProjectInsightProjectDetails();
    this.projectInsightGroupDetails = new ProjectInsightGroupDetails();
    this.projectInsightDetailsDTO = new ProjectInsightDetailsDTO();

    // if (this.projectInsightDetailsId) {
    //   this.getProjectInsightDetailsByObjectId(this.projectInsightDetailsId);
    // } else {
    //   this.projectInsightProjectDetails = this.tempProjectInsightDetailsDTO?.projectInsightProjectDetails;
    //   this.selectedDeptIds = this.getDeptIds(this.projectInsightProjectDetails?.departments);
    //   this.rootNode = this.transformFormDetailsToFormNode(this.tempProjectInsightDetailsDTO?.projectInsightFormDetails, this.currentNodeType);
    //   this.currentNode = this.rootNode;
    //   this.mergeFormDataIntoFormStructure(this.currentNode, this.tempProjectInsightDetailsDTO?.projectInsightProjectDetails);
    //   this.leftSideMenuComponent.projectInsightTrees = [{
    //     id: this.projectInsightProjectDetails.id,
    //     projectId: this.projectInsightProjectDetails.projectId,
    //     projectName: this.projectInsightProjectDetails.projectName,
    //     groupList: []
    //   }];

    this.currentNodeType = this.type;
    const isProject = this.type.toLowerCase() === "project";
    if (this.projectInsightDetailsId) {
      isProject
        ? this.getProjectInsightDetailsByObjectId(this.projectInsightDetailsId)
        : this.getProjectInsightGroupDetailsByObjectId(this.projectInsightDetailsId);
    } else {
      // Load details from temp DTO
      const details: any = isProject
        ? this.tempProjectInsightDetailsDTO?.projectInsightProjectDetails
        : this.tempProjectInsightDetailsDTO?.projectInsightGroupDetails;
      let id:any;
      if (isProject) {
        this.projectInsightProjectDetails = details;
        this.selectedDeptIds = this.getDeptIds(details?.departments);
        id = this.projectInsightProjectDetails.id;
      } else {
        this.projectInsightGroupDetails = details;
        id = this.projectInsightGroupDetails.projectDetailsId;
      }

      this.rootNode = this.transformFormDetailsToFormNode(this.tempProjectInsightDetailsDTO?.projectInsightFormDetails, this.currentNodeType);
      this.currentNode = this.rootNode;
      this.mergeFormDataIntoFormStructure(this.currentNode, details);
      this.leftSideMenuComponent.loadProjectInsightTrees(id,details?.projectId,details?.projectName);
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
          item => item.name?.toLowerCase() === "domainname"
        );
        if (!hasDomain) {
          node.fields.push(this.getDomainStructure());
        }
      }
      node.layoutConfig = this.getLayoutConfig(node.fields || [])
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
    console.log("Structure for Data: ", data, " ", structure);
    
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
    if (!fields) return [];
    // Sort fields safely
    const sortedFields = [...fields]?.sort((a, b) => {
      // Fallbacks: if missing, default rowPosition=0, index=9999 (so unindexed go last), width=25
      const posA = Number.isFinite(a?.rowPosition) ? a.rowPosition : 0;
      const posB = Number.isFinite(b?.rowPosition) ? b.rowPosition : 0;
      if (posA !== posB) return posA - posB;

      const indexA = Number.isFinite(a?.index) ? a.index : 9999;
      const indexB = Number.isFinite(b?.index) ? b.index : 9999;
      if (indexA !== indexB) return indexA - indexB;

      const colA = this.getBootstrapCol(Number.isFinite(a?.width) ? a.width : 25);
      const colB = this.getBootstrapCol(Number.isFinite(b?.width) ? b.width : 25);
      if (colA !== colB) return colA - colB;
      return 0;
    });

    // Group into rows based on total width (<=100 rule)
    const rows = new Map<number, any[]>();
    let currentRow = 0;
    let currentRowWidth = 0;
    sortedFields?.forEach(field => {
      const fieldWidth = Number.isFinite(field?.width) ? Number(field.width) : 100;
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
      name: 'domainname',
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

  getBootstrapCol(widthPercent: number): number {
    const col = Math.round((widthPercent / 100) * 12)
    return Math.min(12, Math.max(3, col)) // col-3 … col-12
  }
  // Utility [End]

  // Project Insight, Group APIs [Start]
  async getProjectInsightDetailsByObjectId(projectInsightDetailsId: number, type: string = 'Project') {
    try {
      if (!this.validationService.validateNullUndefinedEmptyString(projectInsightDetailsId)) {
        return;
      }
      this.currentNodeType = type;
      this.currentNode = new FormNode();
      this.projectInsightGroupDetails = new ProjectInsightGroupDetails();
      this.projectInsightDetailsDTO = new ProjectInsightDetailsDTO();
      const response: any = await this.projectInsightService
        .getProjectInsightDetailsByObjectId(projectInsightDetailsId)
        .pipe(first())
        .toPromise();

      this.projectInsightDetailsDTO = response;
      this.projectInsightProjectDetails = this.projectInsightDetailsDTO?.projectInsightProjectDetails;
      this.selectedDeptIds = this.getDeptIds(this.projectInsightProjectDetails?.departments);
      this.rootNode = this.transformFormDetailsToFormNode(this.projectInsightDetailsDTO?.projectInsightFormDetails, this.currentNodeType);
      this.currentNode = this.rootNode;
      this.mergeFormDataIntoFormStructure(this.currentNode, this.projectInsightDetailsDTO?.projectInsightProjectDetails);
      this.leftSideMenuComponent.loadProjectInsightTrees(this.projectInsightProjectDetails.id, this.projectInsightProjectDetails?.projectId, this.projectInsightProjectDetails?.projectName);
      this.leftSideMenuComponent.loadProjectInsightGroupTrees(0, this.currentNode?.parentId, 'Project');
        if(this.isQuestionOverview && !this.isApprovalTab){  
          await this.questionCardsComponent.getAllAssignedQuestionsForUser(this.currentNode?.parentId, this.currentNode?.parentType);
          await this.leftSideMenuComponent.getAllgroupstatusdata(this.currentNode?.parentId,'Project');
        }else if(this.isQuestionOverview && this.isApprovalTab){
          await this.questionCardsComponent.getAllQuestionsForApprovalByParentIdAndParentType(this.currentNode?.parentId, this.currentNode?.parentType);
          await this.leftSideMenuComponent.getAllgroupstatusdata(this.currentNode?.parentId,'Project');
        }else{
        await this.questionCardsComponent.getProjectInsightQuestionDetailsByParentIdAndParentType(this.currentNode?.parentId, this.currentNode?.parentType);
      }

    } catch (error) {
      this.openAlertModal(error);
    }
  }

  async getProjectInsightGroupDetailsByObjectId(projectInsightGroupDetailsId: any) {
    this.projectInsightService.getProjectInsightGroupDetailsByObjectId(projectInsightGroupDetailsId)
      .pipe(first()).subscribe({
        next: async (response: any) => {
          this.currentNodeType = 'Group';
          this.currentNode = new FormNode();
          this.projectInsightGroupDetails = response?.projectInsightGroupDetails;
          this.currentNode = this.transformFormDetailsToFormNode(response?.projectInsightFormDetails, this.currentNodeType);
          console.log("Before mergeFormDataIntoFormStructure : ",this.currentNode);
          
          this.mergeFormDataIntoFormStructure(this.currentNode, response?.projectInsightGroupDetails);
          console.log("AFter mergeFormDataIntoFormStructure : ",this.currentNode);
          
          this.leftSideMenuComponent.loadProjectInsightTrees(this.projectInsightGroupDetails?.projectDetailsId, this.projectInsightGroupDetails?.projectId, this.projectInsightGroupDetails?.projectName);
          console.log("this.leftSideMenuComponent.loadProjectInsightTrees : ",this.projectInsightGroupDetails);
          
          await this.leftSideMenuComponent.rebuildAndExpandToGroup(0, this.projectInsightGroupDetails?.projectDetailsId, projectInsightGroupDetailsId);
          if (this.isQuestionOverview && !this.isApprovalTab) {
            await this.questionCardsComponent.getAllAssignedQuestionsForUser(projectInsightGroupDetailsId, 'Group');
            //await this.leftSideMenuComponent.getAllgroupstatusdata(projectInsightGroupDetailsId, 'Group');
          }else if(this.isQuestionOverview && this.isApprovalTab){
            await this.questionCardsComponent.getAllQuestionsForApprovalByParentIdAndParentType(projectInsightGroupDetailsId, 'Group');
            //await this.leftSideMenuComponent.getAllgroupstatusdata(projectInsightGroupDetailsId, 'Group');
          }else{
          await this.questionCardsComponent.getProjectInsightQuestionDetailsByParentIdAndParentType(projectInsightGroupDetailsId, 'Group');
          }
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

    let inputValidated: boolean = this.validateProjectInsightDetails(projectInsightDetailsDTO.projectInsightProjectDetails, projectInsightDetailsDTO.projectInsightFormDetails.fields);
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

    if(!this.validationService.validateNullUndefinedEmptyString(this.projectInsightGroupDetails.parentId)){
      this.projectInsightGroupDetails.parentId = this.currentNode.parentId;
    }

    if(!this.validationService.validateNullUndefinedEmptyString(this.projectInsightGroupDetails.parentType)){
      this.projectInsightGroupDetails.parentType = this.currentNode.parentType;
    }

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
    // projectInsightDetailsDTO.projectInsightGroupDetails.parentId = this.validationService.validateNullUndefinedEmptyString(this.currentNode.parentId) ? this.currentNode.parentId : this.projectInsightGroupDetails.parentId;
    // projectInsightDetailsDTO.projectInsightGroupDetails.parentType = this.validationService.validateNullUndefinedEmptyString(this.currentNode.parentType) ? this.currentNode.parentType : this.projectInsightGroupDetails.parentType;

    if(!this.validationService.validateNullUndefinedEmptyString(this.projectInsightGroupDetails.parentId)){
      this.projectInsightGroupDetails.parentId = this.currentNode.parentId;
    }

    if(!this.validationService.validateNullUndefinedEmptyString(this.projectInsightGroupDetails.parentType)){
      this.projectInsightGroupDetails.parentType = this.currentNode.parentType;
    }
    
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

  saveProjectInsightFacetCategory() {
    if (!this.validationService.validateNullUndefinedEmptyList(this.editingField?.facetCategoryList)) {
      return;
    }
    this.projectInsightFacetService.saveProjectInsightFacetCategory(this.editingField?.facetCategoryList).pipe(first()).subscribe(
      (response: any) => {
        if (!response || response?.serviceStatus == 'Failed') {
          this.openAlertModal(response?.serviceResponse || 'Something went wrong while saving Facets.');
        }
      },
      (error) => {
        console.log(error, " : error");
        this.openAlertModal('An unexpected error occurred.');
      }
    );
  }
  // Project Insight, Group APIs [End]

  // Department [Start]
  onDepartmentChange(event: any): void {
    if (this.viewMode === 'View') {
      return;
    }
    this.selectedDeptIds = event.value;
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

  sendQuestionsForApproval(node:any,nodeType:any){
    this.submitAndAssignRequest.parentId=node.id;
    this.submitAndAssignRequest.parentType=nodeType;
    this.submitAndAssignRequest.empId=this.currentUser.empId;
    let counts = this.projectService.projectMap.get(node.id);
    if(counts?.totalCount == 0){
      this.openAlertModal('No Questions Present in this Group/Project');
    }else if(counts?.pendingCount > 0){
      this.pendingConfirmation = 'Some Questions Are not answered in this group. Do you wish to submit only answered questions for review and leave remaining one ?';
        this.doSaveAndAssign = this.modalService.show(this.confirmation);
    }else{
      this.askLevelApproval();
    }
  }
  
  askLevelApproval(){
    this.doSaveAndAssign?.hide();
    this.allChildConfirmation = 'Do You Wish to Submit Group Level Questions Only or send All Questions recursively from All child groups also?';
    this.addConsentSaveAndAssign = this.modalService.show(this.levelConfirmation);
  }
  
  saveConsent(consent:boolean){
    this.addConsentSaveAndAssign?.hide();
    this.allChildsRecursiv = consent;
    this.submitAndAssignRequest.toAllChilds = consent;
    this.sendAnsweredforApproval()
  }
  
  sendAnsweredforApproval(){
    this.projectInsightService.assignQuestionsToReviewers(this.submitAndAssignRequest).pipe(first()).subscribe({
      next: (res: any) => {
        this.addConsentSaveAndAssign?.hide();
        this.doSaveAndAssign?.hide();
        this.questionCardsComponent.getAllAssignedQuestionsForUser(this.submitAndAssignRequest.parentId,this.submitAndAssignRequest.parentType);
        this.questionCardsComponent.sendForUpdate((this.currentNodeType=='Project')?this.projectInsightProjectDetails:this.projectInsightGroupDetails,(this.currentNodeType=='Project')?ENTITY_TYPES.PROJECT : ENTITY_TYPES.GROUP);
        this.openAlertModal(res);
      },
      error: (error: any) => {
        this.addConsentSaveAndAssign?.hide();
        this.doSaveAndAssign?.hide();
        this.openAlertModal(error);
      }
    });
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

  async editExistingField(field: any, index: number) {
    await this.getAllProjectInsightFacetCategory();
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
    this.saveProjectInsightFacetCategory();
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

  async addFieldFromPalette(field: any) {
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
    await this.getAllProjectInsightFacetCategory();
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
    this.saveProjectInsightFacetCategory();
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
      this.apiSourceService.loadDynamicApi(this.editingField.apiUrl).pipe(first()).subscribe({
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

  getAllProjectInsightFacetCategory(): Promise<any> {
    this.facetCategoryList = [];
    this.filteredCategories = [];
    return this.projectInsightFacetService.getAllProjectInsightFacetCategory().pipe(first())
      .toPromise()
      .then((response: any) => {
        this.facetCategoryList = response;
        this.filteredCategories = response;
      })
      .catch(error => {
        console.log(error, " : error");
        this.openAlertModal('An unexpected error occurred while fetching Project Insight Facet.');
      });
  }

  filterCategories() {
    if (!this.validationService.validateNullUndefinedEmptyString(this.filterText)) {
      return;
    }
    const filterValue = (this.filterText || '').toLowerCase();
    this.filteredCategories = this.facetCategoryList?.filter(option =>
      option.categoryName.toLowerCase().includes(filterValue) &&
      !this.editingField?.facetCategoryList?.some(
        selected => selected.categoryName.toLowerCase() === option.categoryName.toLowerCase()
      )
    );
  }

  addFacetCategoryToField(selectedValue?: string) {
    const value = (selectedValue || this.filterText || '').trim();
    if (!value) return;

    if (!this.editingField.facetCategoryList) {
      this.editingField.facetCategoryList = [];
    }

    const allCategoryList = this.facetCategoryList?.map(obj => obj.categoryName.toLowerCase()) || [];
    const categoryList = this.editingField.facetCategoryList.map(obj => obj.categoryName.toLowerCase());

    if (allCategoryList.includes(value.toLowerCase()) && !categoryList.includes(value.toLowerCase())) {
      const facet = this.facetCategoryList.find(
        obj => obj.categoryName.trim().toLowerCase() === value.toLowerCase()
      );
      if (facet) {
        this.editingField.facetCategoryList.push(facet);
      }
      this.resetFilter();
      return;
    }

    if (!categoryList.includes(value.toLowerCase())) {
      this.editingField.facetCategoryList.push({ categoryName: value } as ProjectInsightFacetCategory);
    }
    this.resetFilter();
  }

  private resetFilter() {
    this.filterText = null;
    this.filteredCategories = this.facetCategoryList?.filter(
      option =>
        !this.editingField.facetCategoryList.some(
          selected => selected.categoryName.toLowerCase() === option.categoryName.toLowerCase()
        )
    );
  }

  removeFacetCategoryFromField(index: number) {
    if (this.editingField.facetCategoryList) {
      let question = this.editingField?.facetCategoryList[index];
      if (question && question?.facetCategoryId != undefined && question?.facetCategoryId != null) {
        if (!this.editingField?.facetCategoryIds) {
          return;
        }
        this.editingField.facetCategoryIds = this.editingField?.facetCategoryIds?.filter(facetId => facetId !== question.facetCategoryId);
      }
      this.editingField.facetCategoryList.splice(index, 1);
    }
    this.filteredCategories = this.facetCategoryList?.filter(option =>
      !this.editingField?.facetCategoryList?.some(
        selected => selected.categoryName.toLowerCase() === option.categoryName.toLowerCase()
      )
    );
  }

  clearSearch() {
    this.filterText = null;
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
      const domainField = fields.find(item => item.name?.toLowerCase() === "domainname");
      if (!domainField) {
        message = "Please add a Domain field and Select a domain associated with the project. !!";
        flag = false;
      } else {
        if (projectInsightDetails?.additionalInfo["domainname"]) {
          if (!this.validationService.validateNullUndefinedEmptyList(projectInsightDetails?.additionalInfo["domainname"])) {
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
    if(!this.isQuestionOverview){
      this.showTable.emit();
    }else{
      this.showQuesProjectList.emit();
    }
  }
}