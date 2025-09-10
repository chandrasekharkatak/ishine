import { Component, OnInit, HostListener, TemplateRef, ViewChild, ElementRef } from '@angular/core';
import { KnowledgeHubService } from '../../services/knowledge-hub.service';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { first } from 'rxjs/operators';
import { KnowledgeHubSearch } from 'src/app/models/knowledgeHubSearch';
import { ValidationService } from 'src/app/services/validation.service';
import { KnowledgeHubSearchResultProject, KnowledgeHubSearchResultWrapper } from 'src/app/models/KnowledgeHubSearchResultProject';
import { KnowledgeHubSearchResultObject } from 'src/app/models/KnowledgeHubSearchResultObject';

@Component({
  selector: 'app-knowledge-hub',
  templateUrl: './KnowledgeHub.component.html',
  styleUrls: ['./KnowledgeHub.component.scss']
})

export class KnowledgeHubComponent implements OnInit {

  @ViewChild('project_static_form_modal') projectStaticFormModal: TemplateRef<any>;
  @ViewChild('infinite_scroll_anchor') infiniteScrollAnchor!: ElementRef;

  private observer!: IntersectionObserver;
  projectStaticFormModalRef: BsModalRef = new BsModalRef();

  type = 'Project';
  query: string = '';
  projectId: string = null;
  parentProjectId: string = null

  searchPerformed = false;
  isLoading: boolean = false;
  hasMore: boolean = true;
  syncDisabled: boolean = false;

  projectPage: number = 1;
  projectPageSize: number = 5;
  totalProjects: number = 0;
  totalProjectPages: number = 0;

  searchResponse: KnowledgeHubSearchResultWrapper = new KnowledgeHubSearchResultWrapper();
  allProjects: KnowledgeHubSearchResultProject[] = [];
  private loadTimeout: any;

  projectColors = ['#fff8e1', '#e3f2fd', '#e8eaf6', '#fce4ec', '#ede7f6', '#e1f5fe', '#e0f7fa', '#e0f2f1', '#f1f8e9', '#f9fbe7', '#fffde7', '#fff3e0', '#fbe9e7', '#f9f9f9', '#f0f4c3', '#c8e6c9', '#d1c4e9'];

  constructor(private knowledgeHubService: KnowledgeHubService, private modalService: BsModalService, private validationService: ValidationService) { }

  ngOnInit(): void {
    this.hasMore = false;
    this.isLoading = false;
    this.searchPerformed = false;
    this.allProjects = [];
  }

  ngAfterViewInit(): void {
    if (this.infiniteScrollAnchor) {
      this.observer = new IntersectionObserver(
        entries => {
          if (entries[0].isIntersecting && this.hasMore && !this.isLoading) {
            this.debouncedLoadSearch();
          }
        }
      );
      this.observer.observe(this.infiniteScrollAnchor.nativeElement);
    }
  }

  ngOnDestroy(): void {
    if (this.observer) {
      this.observer.disconnect();
    }
  }

  debouncedLoadSearch() {
    clearTimeout(this.loadTimeout);
    this.loadTimeout = setTimeout(() => this.loadSearch(), 150);
  }

  getType(path: string, type: string): string {
    const parts = path.split('/').filter(p => p.trim() !== '');
    if (type.toLocaleLowerCase() === 'project' || type.toLowerCase() === 'group') return type;
    if (parts.length == 1) return 'Project'
    return 'Group';
  }

  getParentId(parentIds: string[], type: string): string {
    if (type.toLowerCase() === 'project' || type.toLowerCase() === 'group') return parentIds[parentIds.length - 1];
    return parentIds[parentIds.length - 2];
  }

  openProjectInsightStaticFormModal(id: string, type: string) {
    this.projectId = id;
    this.type = type;
    this.projectStaticFormModalRef = this.modalService.show(this.projectStaticFormModal, { class: 'modal-xl', backdrop: 'static', keyboard: false });
  }

  closeProjectInsightStaticFormModal() {
    this.projectId = null;
    this.type = null;
    this.projectStaticFormModalRef.hide();
  }

  clearSearch() {
    this.query = '';
  }

  clearSerachInputAndResult() {
    this.query = null;
    this.allProjects = [];
    this.searchResponse = new KnowledgeHubSearchResultWrapper();
  }

  search(): void {
    if (!this.validationService.validateNullUndefinedEmptyString(this.query)) {
      this.searchPerformed = false;
      this.totalProjects = 0;
      this.hasMore = false;
      return;
    }
    this.searchPerformed = true;
    this.projectPage = 1;
    this.allProjects = [];
    this.hasMore = true;
    this.loadSearch();
  }

  loadSearch(): void {
    if (!this.hasMore || this.isLoading) return;

    this.isLoading = true;
    const limit = this.projectPageSize;
    const skip = (this.projectPage - 1) * limit;

    const knowledgeObj: KnowledgeHubSearch = {
      keyword: this.query,
      limit,
      skip,
      exactMatch: false,
      matchCase: false,
      regexPattern: null,
      options: '',
      projectId: null
    };

    this.knowledgeHubService.onSearchTerm(knowledgeObj).pipe(first()).subscribe({
      next: (res) => {
        this.isLoading = false;
        this.searchResponse = res;
        const newProjects = this.searchResponse?.knowledgeHubSearchResultProjectList ?? [];

        // Add pagination props for inner objects
        newProjects.forEach((p: any) => {
          p.currentObjectPage = 0;
          p.objectPageSize = 10;
          p.totalObjectPageSize = Math.ceil(p?.totalGroupCount / p?.objectPageSize) || 1;
        });

        if (newProjects.length > 0) {
          if (!this.validationService.validateNullUndefinedEmptyList(this.allProjects)) {
            this.allProjects = [];
          }
          // append to master list
          this.allProjects = [...this.allProjects, ...newProjects];

          // update pagination view
          this.totalProjectPages = Math.ceil(this.allProjects?.length / this.projectPageSize);
          this.projectPage++;
        }

        // if no more results
        if (newProjects.length < limit) {
          this.hasMore = false;
        }
      },
      error: () => {
        this.hasMore = false;
        this.isLoading = false;
      }
    });
  }

  loadNextProjectSearchObjectPage(project: KnowledgeHubSearchResultProject) {
    const limit = project.objectPageSize;
    console.log(project.currentObjectPage);
    const skip = project?.knowledgeHubSearchResultObjectList?.length || (project.currentObjectPage + 1) * limit;

    const knowledgeObj: KnowledgeHubSearch = {
      keyword: this.query,
      limit,
      skip,
      exactMatch: false,
      matchCase: false,
      regexPattern: null,
      options: '',
      projectId: project.projectId
    };

    this.knowledgeHubService.loadProjectSearchObject(knowledgeObj).pipe(first()).subscribe({
      next: (res) => {
        const newObjects: KnowledgeHubSearchResultObject[] = res?.knowledgeHubSearchResultObjectList ?? [];

        if (newObjects.length > 0) {
          // Append new objects to this project's list
          if (!project.knowledgeHubSearchResultObjectList) {
            project.knowledgeHubSearchResultObjectList = [];
          }
          project.knowledgeHubSearchResultObjectList = [
            ...project.knowledgeHubSearchResultObjectList,
            ...newObjects
          ];

          // Update pagination props only for this project
          project.totalObjectPageSize = Math.ceil(project?.totalGroupCount / project.objectPageSize) || 1;
          project.currentObjectPage++;
        }
      },
      error: () => {
        this.isLoading = false;
      }
    });
  }

  syncAllDataWithFlatSearch() {
    this.syncDisabled = true;
    this.knowledgeHubService.syncAllDataWithFlatSearch().pipe(first()).subscribe({
      next: (res) => {
        this.syncDisabled = false;
        console.error('Sync Successfuly!');
      },
      error: () => {
        this.syncDisabled = false;
        console.error('Sync Failed!');
      }
    });
  }

  onPageSizeChange(project: any): void {
    project.currentObjectPage = 0;
    project.totalObjectPageSize = Math.ceil(project?.totalGroupCount / project?.objectPageSize) || 1;
  }

  getPaginatedObjects(project: any) {
    const start = project?.currentObjectPage * project?.objectPageSize;
    const end = start + project?.objectPageSize;
    // if (end > project?.knowledgeHubSearchResultObjectList?.length &&
    //   project?.knowledgeHubSearchResultObjectList?.length < project?.totalGroupCount) {
    //   this.loadNextProjectSearchObjectPage(project);
    // }
    return project?.knowledgeHubSearchResultObjectList?.slice(start, end);
  }


  prevObjectPage(project: any) {
    if (project?.currentObjectPage > 0) {
      project.currentObjectPage--;
    }
  }

  nextObjectPage(project: any) {
    const startIndex = (project.currentObjectPage + 1) * project.objectPageSize;
    if (startIndex < project.knowledgeHubSearchResultObjectList.length) {
      project.currentObjectPage++;
    }
    else {
      this.loadNextProjectSearchObjectPage(project);
    }
  }

  highlight(text: any): string {
    return this.knowledgeHubService.highlight(text, this.query, this.searchPerformed);
  }

  highlightValue(value: any): string {
    let text = '';
    if (Array.isArray(value)) {
      text = value.join(', ');
    }
    text = value ?? '';
    return this.knowledgeHubService.highlight(text, this.query, this.searchPerformed);
  }
}

