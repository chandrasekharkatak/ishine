import { Component, OnInit, HostListener, TemplateRef, ViewChild } from '@angular/core';
import { KnowledgeHubService } from '../../services/KnowledgeHub.service';
import { ProjectInsightDetailsDTO } from 'src/app/models/projectInsightDetailsDTO';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';

interface SearchResultItem {
  path: string;
  value: string;
  parentIds : string[],
  type:string
}

@Component({
  selector: 'app-knowledge-hub',
  templateUrl: './KnowledgeHub.component.html',
  styleUrls: ['./KnowledgeHub.component.scss']
})
export class KnowledgeHubComponent implements OnInit {
  filteredData: SearchResultItem[] = [];
  query: string = '';
  currentPage: number = 1;
  pageSize: number = 1;
  loading: boolean = false;
  searchPerformed = false;
  totalResults: number = 0;
  hasMore: boolean = true;
  projectId:string = null

  @ViewChild('open_project_static_form_modal') openProjectStaticFormModal: TemplateRef<any>;
  projectInsightDetailsDTO: ProjectInsightDetailsDTO = new ProjectInsightDetailsDTO();
  openProjectStaticFormModalRef: BsModalRef = new BsModalRef();

  constructor(private knowledgeHubService: KnowledgeHubService, private modalService: BsModalService) {}

  ngOnInit(): void {}

  search(): void {
    if (!this.query.trim()) {
      this.filteredData = [];
      this.searchPerformed = false;
      this.totalResults = 0;
      this.hasMore = false;
      return;
    }

    this.projectInsightDetailsDTO = new ProjectInsightDetailsDTO();

    this.loading = true;
    this.searchPerformed = true;
    this.currentPage = 1;
    this.filteredData = [];
    this.hasMore = true;

    this.loadSearches(this.query.toLowerCase());
  }

  loadSearches(query: string): void {
    if (!this.hasMore) return;
    this.projectInsightDetailsDTO = new ProjectInsightDetailsDTO();

    const limit = this.pageSize;
    const skip = (this.currentPage - 1) * limit;

    this.knowledgeHubService.search(query, limit, skip).subscribe({
      next: (res) => {
        if (res.data?.length) {
          this.filteredData = [...this.filteredData, ...res.data];
          this.totalResults = res.size;
          this.currentPage++;
        }

        if (this.pageSize >= this.totalResults) {
          this.hasMore = false;
        }

        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  // highlight(text: any): string {
  //   if (!this.searchPerformed) return text;
  //   if (!this.query || text == null) {
  //     return typeof text === 'string' ? text : JSON.stringify(text);
  //   }

  //   const textStr = typeof text === 'string' ? text : JSON.stringify(text, null, 2);
  //   const escapedQuery = this.query.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
  //   const regex = new RegExp(escapedQuery, 'gi');

  //   return textStr.replace(regex, match =>
  //     `<span class="highlight">${match}</span>`
  //   );
  // }

  highlight(text: any): string {
    return this.knowledgeHubService.highlight(text, this.query, this.searchPerformed);
  }

  // getProjectDetails(){
  //   this.knowledgeHubService.getProjectDetails(this.projectId).subscribe({
  //     next: (res) => {
  //       this.projectInsightDetailsDTO = res;
  //     },
  //     error: () => {
  //     }
  //   });
  // }

  onClickPath(id:string){
    this.projectId = id;
    // this.openProjectStaticFormModalRef = this.modalService.show(this.openProjectStaticFormModal, { class: 'modal-xl' });
  }

  onClose(){
    this.projectId = null
    // this.openProjectStaticFormModalRef.hide();
  }

  @HostListener('window:scroll', [])
  onScroll(): void {
    if (this.loading || !this.hasMore) return;

    const scrollTop = window.scrollY;
    const windowHeight = window.innerHeight;
    const docHeight = document.documentElement.scrollHeight;

    if (scrollTop + windowHeight >= docHeight - 200) {
      this.loadSearches(this.query.toLowerCase());
    }
  }
}

