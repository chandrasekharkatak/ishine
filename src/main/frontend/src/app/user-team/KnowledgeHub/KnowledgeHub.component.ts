import { Component, OnInit, HostListener, TemplateRef, ViewChild, ElementRef } from '@angular/core';
import { KnowledgeHubService } from '../../services/KnowledgeHub.service';
import { ProjectInsightDetailsDTO } from 'src/app/models/projectInsightDetailsDTO';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { first } from 'rxjs/operators';

interface SearchResultItem {
  path: string;
  value: string;
  key:string;
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
  pageSize: number = 10;
  loading: boolean = false;
  searchPerformed = false;
  totalResults: number = 0;
  hasMore: boolean = true;
  projectId:string = null;
  type = 'Project';
  parentProjectId = null

  @ViewChild('open_project_static_form_modal') openProjectStaticFormModal: TemplateRef<any>;
  projectInsightDetailsDTO: ProjectInsightDetailsDTO = new ProjectInsightDetailsDTO();
  openProjectStaticFormModalRef: BsModalRef = new BsModalRef();

  @ViewChild('infiniteScrollAnchor', { static: false }) infiniteScrollAnchor!: ElementRef;
  private observer!: IntersectionObserver;

  constructor(private knowledgeHubService: KnowledgeHubService, private modalService: BsModalService) {}

  ngOnInit(): void {}

  ngAfterViewInit(): void {
    if (this.infiniteScrollAnchor) {
      this.observer = new IntersectionObserver(entries => {
        if (entries[0].isIntersecting && this.hasMore && !this.loading) {
          this.loadSearches(this.query.toLowerCase());
        }
      });
      this.observer.observe(this.infiniteScrollAnchor.nativeElement);
    }
  }
  ngOnDestroy(): void {
    if (this.observer) {
      this.observer.disconnect();
    }
  }

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

    this.knowledgeHubService.search(query, limit, skip).pipe(first()).subscribe({
      next: (res) => {
        if (res.data?.length) {
          this.filteredData = [...this.filteredData, ...res.data];
          this.totalResults = res.size;
          this.currentPage++;
        }

        if (this.currentPage >= this.totalResults || res?.data?.length == 0 ) {
          this.hasMore = false;
        }
        this.loading = false;

        if (window.innerHeight >= document.documentElement.scrollHeight && this.hasMore) {
          this.loadSearches(this.query.toLowerCase());
        }
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

  getType(path:string, type:string):string{
    const parts = path.split('/').filter(p => p.trim() !== '');
    console.log("Parts: ", parts);
    console.log("Type: ", type);
    
    if(type.toLocaleLowerCase() === 'project' || type.toLowerCase() === 'group') return type;
    if(parts.length == 1) return 'Project'
    return 'Group';

  }

  getParentId(parentIds:string[], type:string):string{
    if(type.toLowerCase() === 'project' || type.toLowerCase() === 'group') return parentIds[parentIds.length - 1];
    return parentIds[parentIds.length - 2];
  }

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

  onClickPath(id:string, path:string, parentProjectId:string, type:string){
    console.log("Id: ", id);
    this.parentProjectId = parentProjectId
    this.projectId = id;
    // captitilize the first letter
    this.type = this.getType(path, type);
    this.type = this.type.charAt(0).toUpperCase() + this.type.slice(1);
    console.log("Type: ", this.type);
    
    
    // this.openProjectStaticFormModalRef = this.modalService.show(this.openProjectStaticFormModal, { class: 'modal-xl' });
  }

  onClose(){
    this.projectId = null
    this.type = null
    // this.openProjectStaticFormModalRef.hide();
  }
}

