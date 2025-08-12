import { Component, Input, Output, EventEmitter } from '@angular/core';
import { Sort } from '@angular/material/sort';
import { first } from 'rxjs/operators';
import { ProjectInsightService } from 'src/app/services/project-insight.service';

@Component({
  selector: 'app-project-table',
  templateUrl: './project-table.component.html',
  styleUrls: ['./project-table.component.scss']
})
export class ProjectTableComponent {

  @Output() openCreateProject = new EventEmitter<void>();
  @Output() editProject = new EventEmitter<number>();
  @Output() deleteProject = new EventEmitter<number>();
  @Output() viewProject = new EventEmitter<number>();
  @Output() openAlertModal = new EventEmitter<number>();

  // Variables 
  page = 1;
  limit = 10;
  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType: any;
  isSearchEnabled: boolean = false;
  filters: any = {};
  searchKeyword: any;

  filterModal: boolean = false;
  existingFilter = {};

  // Lists
  allProjectInsightProjectList: any[] = [];

  // ColumnList
  projectColumns: any[] = ['blank', '', '', '', '', ''];

  constructor(
    private projectInsightService: ProjectInsightService,
  ) { }

  ngOnInit() {
    this.getAllProjectInsightProjectList();
  }

  // APIs[Start]
  getAllProjectInsightProjectList(domain?: string | number, unique_name?: string) {
    this.allProjectInsightProjectList = [];
    this.projectInsightService.getAllProjectInsight(domain, unique_name).pipe(first()).subscribe({
      next: (response: any) => {
        this.allProjectInsightProjectList = response;
      },
      error: (error: any) => {
        this.openAlertMessageModal(error?.error?.serviceResponse || 'An unexpected error occurred.');
      }
    });
  }
  // APIs[End]

  // Searching & Sorting [Start]
  toggleSearch() {
    this.isSearchEnabled = !this.isSearchEnabled;
    if (!this.isSearchEnabled) {
      this.filters = {};
    }
  }

  onSearch(searchData) {
    this.filters = searchData;
  }

  onGlobalSearch() {
    this.projectInsightService.searchProjectInsight(this.searchKeyword, this.page - 1, this.limit).pipe(first()).subscribe({
      next: (response: any) => {
        this.allProjectInsightProjectList = response.content;
      },
      error: (error: any) => {
        this.openAlertMessageModal(error);
      }
    });
  }

  handlePageChange(event) {
    this.page = event;
  }

  sortData(sort: Sort) {
    if (sort.active) {
      let sortParams: any[] = sort.active?.split("|");
      this.sortColumn = sortParams[0];
      this.sortColumnType = sortParams[1];
      this.sortDirection = sort.direction;
    }
  }
  // Searching & Sorting [End]

  // Filter [Start]
  openFilterModal() {
    this.filterModal = true;
  }

  closeFilterModal() {
    this.filterModal = false;
  }

  applyFilterModal(filter: any) {
    this.existingFilter = null;
    this.projectInsightService.filterProjectInsight(filter).subscribe({
      next: (res: any) => {
        this.existingFilter = filter
        this.allProjectInsightProjectList = [...res.content];
        this.closeFilterModal();
      }, error: (error: any) => {
        throw error;
      }
    })
  }
  // Filter [End]

  // Modal [Start]
  onOpenCreateProject() {
    this.openCreateProject.emit();
  }

  onEditProject(projectId: number) {
    this.editProject.emit(projectId);
  }

  onDeleteProject(projectId: number) {
    this.deleteProject.emit(projectId);
  }

  onViewProject(projectId: number) {
    this.viewProject.emit(projectId);
  }

  openAlertMessageModal(error: any) {
    this.openAlertModal.emit(error);
  }
  // Modal [End]

} 