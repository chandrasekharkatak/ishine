import { Component, Input, Output, EventEmitter } from '@angular/core';
import { first } from 'rxjs/operators';
import { ProjectInsightProjectDetails } from 'src/app/models/projectInsightDetails';
import { ApiSourceService } from 'src/app/services/api-source.service';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { ProjectInsightDomainService } from 'src/app/services/project-insight-domain.service';
import { ProjectInsightService } from 'src/app/services/project-insight.service';
import { ProjectService } from 'src/app/services/project.service';
import { ValidationService } from 'src/app/services/validation.service';

export interface ProjectInsightTree {
  id: any,
  projectId: string;
  projectName: string;
  groupList: GroupNode[];
}

export interface GroupNode {
  id: string;
  groupTitle: string;
  parentId?: string;
  parentType?: string;
  groupList?: GroupNode[];
  isExpanded?: boolean;
}

@Component({
  selector: 'app-left-side-menu',
  templateUrl: './left-side-menu.component.html',
  styleUrls: ['./left-side-menu.component.scss']
})

export class LeftSideMenuComponent {

  @Input() projectInsightProjectDetails: ProjectInsightProjectDetails;
  @Input() isQuestionOverview: boolean = false;

  @Output() openAlertModal = new EventEmitter<any>();
  @Output() getProjectInsightDetailsByObjectId = new EventEmitter<any>();
  @Output() getProjectInsightGroupDetailsByObjectId = new EventEmitter<any>();


  isDomainStructure: boolean = false;
  isAddDomainModalVisible: boolean = false;
  isCreateDomainModalVisible: boolean = false;

  allDomains: any[] = [];
  allDomainList: string[] = []
  allDomainDataList: any[] = [];
  projectInsightTrees: ProjectInsightTree[] = [];
  expandedPaths: { [key: string]: boolean } = {};

  title = "";
  addedDomainId = -1;
  currentItem: any = null;
  parentItem: string = null;
  childType: string = '';
  currentUser:any;

  modalTitleMap = {
    'domain': 'Add Sub-Domain',
    'subDomain': 'Add Sub-Domain',
    'service': 'Add Service',
    'subService': 'Add Sub-Service'
  };

  constructor(private projectInsightDomainService: ProjectInsightDomainService, private validationService: ValidationService
    , private apiSourceService: ApiSourceService, private projectInsightService: ProjectInsightService, public projectService:ProjectService,
    private authenticationService:AuthenticationService){ 
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  ngOnInit(): void {
    console.log('isQuestionOverview : ',this.isQuestionOverview);
  }

  // Modal [Start]
  openCreateDomainModal() {
    this.isCreateDomainModalVisible = true;
  }

  closeCreateDomainModal() {
    this.isCreateDomainModalVisible = false;
  }

  openAddDomainModal(item: any, type: string, parent: string) {
    this.currentItem = item;
    this.childType = type;
    this.parentItem = parent;
    this.isAddDomainModalVisible = true;
    this.title = "Add new Item in " + item.name;
  }

  closeAddDomainModal() {
    this.isAddDomainModalVisible = false;
    this.currentItem = null;
    this.childType = '';
  }

  onOpenAlertModal(message: any) {
    this.openAlertModal.emit();
  }
  // Modal [End]

  // Domain [Start]
  toggleDomain(item: any) {
    item.isOpen = !item.isOpen;
  }

  getDomainOrServiceName(name: string): string {
    return name || 'Unnamed';
  }

  changeChildType(childType: string) {
    this.childType = childType;
  }

  loadAllProjectInsightDomain(ids: any[]) {
    if (ids.length === 0) {
      this.allDomainDataList = [];
      return;
    }

    let changed = false;
    for (const id of ids) {
      if (!this.allDomainDataList.some(domain => domain.id === id)) {
        changed = true;
        break;
      }
    }

    if (!changed) {
      return;
    }
    this.allDomainDataList = [];
    this.projectInsightDomainService.getAllProjectInsightDomain(ids).subscribe({
      next: (res: any[]) => { 
        this.allDomainDataList = res.filter(domain => domain.isActive).map(domain => ({
          ...domain,
          isOpen: false,
          ...this.processChildren(domain.children || [])
        }));

      }, error: (error: any) => {
        throw error;
      }
    });
  }

  processChildren(children: any[]): { subDomains: any[]; services: any[] } {
    const subDomains = [];
    const services = [];
    for (const child of children) {
      if (!child.isActive) continue;

      const base = {
        ...child,
        isOpen: false
      };

      if (child.type === 'subDomain') {
        const { subDomains: subSubDomains, services: subServices } = this.processChildren(child.children || []);
        subDomains.push({
          ...base,
          children: [], // Optional: keep if backend uses it
          subDomains: subSubDomains,
          services: subServices
        });
      } else if (child.type === 'service') {
        const { subServices } = this.processSubServices(child.children || []);
        services.push({
          ...base,
          subServices
        });
      }
    }
    return { subDomains, services };
  }

  processSubServices(children: any[]): { subServices: any[] } {
    const subServices = [];
    for (const child of children) {
      if (!child.isActive) continue;
      if (child.type === 'subService') {
        const { subServices: nestedSubServices } = this.processSubServices(child.children || []);
        subServices.push({
          ...child,
          isOpen: false,
          subServices: nestedSubServices
        });
      }
    }
    return { subServices };
  }
  // Domain [End]


  //Breadcrumb [Start]
  async loadProjectInsightGroupTrees(projectIndex: number, parentId: any, parentType: any): Promise<void> {
    const project = this.projectInsightTrees[projectIndex];

    if (parentType === 'Project') {
      const groups = await this.getAllProjectInsightGroupsByParentId(parentId, parentType);
      project.groupList = groups;
      if (this.isQuestionOverview) {
        this.getAllgroupstatusdata(project?.id, 'Project');
      }
    }
    else if (parentType === 'Group') {
      const groupNode = this.findGroupNode(project.groupList, parentId);
      if (groupNode && (!groupNode.groupList || groupNode.groupList.length === 0)) {
        const children = await this.getAllProjectInsightGroupsByParentId(parentId, parentType);
        groupNode.groupList = children;
        if (this.isQuestionOverview) {
          this.getAllgroupstatusdata(parentId, parentType);
        }
      }
    }
  }

  loadProjectInsightTrees(id: any, projectId: any, projectName: any) {
    this.projectInsightTrees = [{
      id: id,
      projectId: projectId,
      projectName: projectName,
      groupList: []
    }];
  }

  toggleProjectRoot(projectIndex: number, project: any) {
    console.log('Map List : ',this.projectService.projectMap);
    const key = `${projectIndex}`;
    this.expandedPaths[key] = !this.expandedPaths[key];
    if (this.expandedPaths[key] && (!project.groupList || project.groupList.length === 0)) {
      this.loadProjectInsightGroupTrees(projectIndex, project.id, 'Project');
    }
    this.getProjectInsightDetailsByObjectId.emit();
  }

  toggleGroup(projectIndex: number, path: number[], group: any) {
    const key = [projectIndex, ...path].join('-');
    this.expandedPaths[key] = !this.expandedPaths[key];
    if (this.expandedPaths[key] && (!group.groupList || group.groupList.length === 0)) {
      this.loadProjectInsightGroupTrees(projectIndex, group.id, 'Group');
    }
  }

  navigateToGroupNode(group: any) {
    this.getProjectInsightGroupDetailsByObjectId.emit(group?.id);
  }

  isExpanded(projectIndex: number, path?: number[]) {
    const key = path ? [projectIndex, ...path].join('-') : `${projectIndex}`;
    return this.expandedPaths[key];
  }

  private findGroupNode(groupList: any[], id: any): any {
    for (const group of groupList) {
      if (group.id === id) return group;
      if (group.groupList) {
        const found = this.findGroupNode(group.groupList, id);
        if (found) return found;
      }
    }
    return null;
  }

  async getAllProjectInsightGroupsByParentId(parentId: string, parentType: string): Promise<any> {
    const res: any = await this.projectInsightService
      .getAllProjectInsightGroupsByParentId(parentId, parentType)
      .pipe(first()) // ensure it completes after first emission
      .toPromise();

    return res?.serviceResponse || [];
  }

  toggleProjectGroup(projectIndex: number) {
    if(this.isQuestionOverview){
      this.loadProjectInsightGroupTrees(0,this.projectInsightProjectDetails.id,'Project');
    }else{
      this.getProjectInsightDetailsByObjectId.emit();
    }
  }

  getAllgroupstatusdata(parentId:any,parentType:any){
    let payload = {
      parentId:parentId,
      parentType:parentType,
      empId:this.currentUser.empId
    }
    this.projectInsightService.getAllGroupsStatusInfo(payload).pipe(first()).subscribe({
        next: (response: any) => {
          response.forEach((item)=>{
            this.projectService.projectMap.set(item.projectId,item);
          });
        },
        error: (error: any) => {
          this.openAlertModal.emit(error);
        }
      });
    }
  //Breadcrumb [End]

  // APIs [Start]
  saveDomain(value: string) {
    if (!this.validationService.validateNullUndefinedEmptyString(value)) {
      this.onOpenAlertModal("Please enter a valid value");
      return;
    }
    this.addData(this.currentItem, this.childType, this.parentItem, value);
    this.closeAddDomainModal();
  }

  addData(item: any, child: string, parent: string, value?: string) {
    const regex = /^[a-zA-Z0-9 ]+$/
    if (!regex.test(value)) {
      this.onOpenAlertModal("Please enter a valid value");
      return;
    }

    let parentIdName = parent;
    if (parent === 'domain') parentIdName = 'domain';
    if (parent !== 'domain' && child === 'subDomain') parentIdName = 'subDomain';

    const payload = {
      parent_id: item.id,
      parent_id_name: parentIdName,
      name: value,
      type: child
    };

    this.projectInsightDomainService.editDomain(payload).subscribe({
      next: (res: any) => {
        // Decide which array to push into
        switch (child) {
          case 'subDomain':
            if (parent === 'domain') {
              item.subDomains.push({ name: value, id: res, isOpen: false, subDomains: [], services: [] });
            } else {
              item.children.push({ name: value, id: res, isOpen: false, subDomains: [], services: [] });
            }
            break;

          case 'service':
            if (parent === 'domain') {
              item.services.push({ service: value, serviceId: res, isOpen: false, subServices: [] });
            } else {
              item.services.push({ name: value, id: res, isOpen: false, subServices: [] });
            }
            break;

          case 'subService':
            item.subServices.push({ name: value, id: res, isOpen: false, subServices: [] });
            break;
        }
      },
      error: (error: any) => { throw error; }
    });
    this.apiSourceService.setIdToRemove(this.addedDomainId);
    this.title = "";
  }
  // APIs [End]

  async rebuildAndExpandToGroup(projectIndex: number, projectId: any, targetGroupId: any) {
    // 1. Always load the root groups fresh
    await this.loadProjectInsightGroupTrees(projectIndex, projectId, 'Project');
    this.expandedPaths[`${projectIndex}`] = true;

    const expandPath = async (groupList: any[], path: number[]): Promise<boolean> => {
      for (let i = 0; i < groupList.length; i++) {
        const group = groupList[i];

        // Expand this group first to load its children
        const key = [projectIndex, ...path, i].join('-');
        this.expandedPaths[key] = true;
        // If it's the target group, stop here
        if (group.id === targetGroupId) {
          return true;
        }
        // Ensure children are loaded
        if (!group.groupList || group.groupList.length === 0) {
          await this.loadProjectInsightGroupTrees(projectIndex, group.id, 'Group');
        }
        // Recurse deeper
        if (group.groupList && group.groupList.length > 0) {
          const found = await expandPath(group.groupList, [...path, i]);
          if (found) return true;
        }
        // Collapse back if this branch didn't lead to the target
        // (optional – remove if you want everything expanded)
        this.expandedPaths[key] = false;
      }
      return false;
    };

    const project = this.projectInsightTrees[projectIndex];
    if (project && project.groupList) {
      await expandPath(project.groupList, []);
    }
  }
} 