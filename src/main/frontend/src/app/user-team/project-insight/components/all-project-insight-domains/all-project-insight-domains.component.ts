import { ApiSourceService } from 'src/app/services/api-source.service';
import { Component, EventEmitter, Input, OnInit, Output, OnChanges, SimpleChanges, HostListener } from '@angular/core';
import { ProjectInsightDomainService } from 'src/app/services/project-insight-domain.service';
import { KnowledgeHubService } from 'src/app/services/knowledge-hub.service';
import { Subject } from 'rxjs';
import { debounceTime } from 'rxjs/operators';

@Component({
  standalone: false,
  selector: 'app-all-project-insight-domains',
  templateUrl: './all-project-insight-domains.component.html',
  styleUrls: ['./all-project-insight-domains.component.scss'],
})

export class AllProjectInsightDomainsComponent implements OnInit, OnChanges {
  filteredDomains: AllDomainsI[] = [];
  searchQuery: string = '';
  previousQuery:string = "";

  @Input() domainName: string[] = [];
  @Input() childrenSelectedDomainId: Set<number>  = new Set();
  @Input() childrenSelectedString: Set<string> = new Set();
  @Input() incoming = false;

  selectedDomainName: Set<string> = new Set();
  isLoadingRecommendations = false;
  selectedChildrenDomainId: Set<number> = new Set();

  private searchSubject = new Subject<string>();

  recommendedDomains: string[] = [];
  showRecommendations = false;
  selectedIndex = -1; 

  @Output() selectedDomain = new EventEmitter<any>();
  @Output() selectChildrenOfDomain = new EventEmitter<{ childrenSubDomain: Set<number>, domain: Set<string>, unique_name: string, childrenSelectedString: Set<string> }>();

  constructor(private apiSource: ApiSourceService, private projectInsightDomainService: ProjectInsightDomainService, private knowledgeHubService: KnowledgeHubService) { }

  ngOnInit() {
    if (!this.incoming) {
      this.getAllDomainData(null);
    }

    this.searchSubject.pipe(debounceTime(500)).subscribe(query => {
      if (query) {
        if(query.toLowerCase() == this.previousQuery.toLowerCase()) {
          return;
        }
        this.getDomainSearchRecommendation(query);
      } else {
        this.recommendedDomains = [];
      }
    });
  }

  ngOnChanges(changes: SimpleChanges): void {
    // if (this.incoming) {
      this.selectedDomainName = new Set(this.domainName);
      this.selectedChildrenDomainId = new Set(this.childrenSelectedDomainId);
    // } else {
    //   this.selectedDomainName = new Set();
    //   this.selectedChildrenDomainId = new Set();
    // }

    const newDomains = this.projectInsightDomainService.getNewDomains();
    const allDomains = this.projectInsightDomainService.getAllDomains();

    this.filteredDomains = this.searchQuery ? newDomains : allDomains;
  }

  selectDomain(domain: AllDomainsI) {
    console.log('Selected:', domain);
    this.searchQuery = domain.name;
    this.showRecommendations = false;
  }

  getAllDomainData(field: AllDomainsI | null, type: string[] = ["domain"], id: number = null) {
    // if((field.children && field.children.length > 0)) {
    //   field.isOpen = true;
    //   return;
    // }
    this.apiSource.getAllDomainData(type, id).subscribe({
      next: (res: AllDomainsI[]) => {
        const data = res.filter(domain => domain.isActive).map(item => {
          item.parent = field;
          return item;
        });

        if (field) {
          field.children = data;
          field.isOpen = true;
        } else if (!this.searchQuery) {
          this.projectInsightDomainService.setAllDomains(data);
          this.projectInsightDomainService.setNewDomains(data);
        }
        this.filteredDomains = !this.searchQuery ? this.projectInsightDomainService.getNewDomains() : [...this.projectInsightDomainService.getAllDomains()];
      },
      error: (err: any) => {
        console.error('API call failed:', err);
      }
    });
  }

  // filterProjectWithDomains(domain: AllDomainsI) {
  //   // domain.isOpen = !domain.isOpen;
  //   if (domain.type.toLowerCase() !== 'domain') {
  //     const parent: AllDomainsI = this.getParent(domain);
  //     if (this.selectedDomainName.has(parent.name) && this.childrenSelectedDomainId.has(domain.id)) {
  //       // this.selectedDomainName.delete(parent.name);
  //       this.childrenSelectedDomainId.delete(domain.id);
  //       const unique_name = domain.parent.type.toUpperCase() + '_' + domain.parent.id;
  //       this.childrenSelectedString.delete(unique_name);
  //       this.removeChildren(domain)
  //       this.selectChildrenOfDomain.emit({ childrenSubDomain: this.childrenSelectedDomainId, domain: this.selectedDomainName, unique_name: null, childrenSelectedString: this.childrenSelectedString });
  //       return;
  //     }

  //     this.selectedDomainName?.add(parent.name);
  //     this.childrenSelectedDomainId?.add(domain.id);
  //     const unique_name = domain.parent.type.toUpperCase() + '_' + domain.parent.id;
  //     this.childrenSelectedString.add(unique_name);
  //     !this.searchQuery && this.addDomainData(domain);
  //     this.selectChildrenOfDomain.emit({ childrenSubDomain: this.childrenSelectedDomainId, domain: this.selectedDomainName, unique_name: unique_name, childrenSelectedString: this.childrenSelectedString });
  //   }
  //   else if (domain.type === 'domain') {
  //     // this.selectedDomainName.has(domain.name) ? this.selectedDomainName.delete(domain.name) : this.selectedDomainName.add(domain.name);
  //     // this.childrenSelectedDomainId = new Set();
  //     // this.childrenSelectedString = new Set();
  //     // this.selectedDomain.emit(domain.name);
  //     if(this.selectedDomainName.has(domain.name)) {
  //       this.selectedDomainName.delete(domain.name);
  //       this.childrenSelectedDomainId = new Set();
  //       this.childrenSelectedString = new Set();
  //       this.selectedDomain.emit(domain.name);
  //     } else {
  //       this.selectedDomainName.add(domain.name);
  //       !this.searchQuery && this.addDomainData(domain);
  //       // this.childrenSelectedDomainId = new Set();
  //       // this.childrenSelectedString = new Set();
  //       // this.selectedDomain.emit(domain.name);
  //       this.selectChildrenOfDomain.emit({ childrenSubDomain: this.childrenSelectedDomainId, domain: this.selectedDomainName, unique_name: null, childrenSelectedString: this.childrenSelectedString });
  //     }
  //   }
  // }

  filterProjectWithDomains(domain: AllDomainsI) {
    // domain.isOpen = !domain.isOpen;
    // If the selected item is not a top-level domain
    if (domain.type.toLowerCase() !== 'domain') {
      const parent: AllDomainsI = this.searchQuery? this.findRoot(domain.id) : this.getParent(domain);

      if (this.selectedDomainName.has(parent.name) && this.childrenSelectedDomainId.has(domain.id)) {
        // Deselect child and all its children recursively
        this.removeChildren(domain);

        // Emit updated state
        this.selectChildrenOfDomain.emit({
          childrenSubDomain: this.childrenSelectedDomainId,
          domain: this.selectedDomainName,
          unique_name: null,
          childrenSelectedString: this.childrenSelectedString
        });
        return;
      }

      // Select parent and child
      this.selectedDomainName.add(parent.name);
      // this.childrenSelectedDomainId.add(domain.id);
      const unique_name = domain.parent.type.toUpperCase() + '_' + domain.parent.id;
      // this.childrenSelectedString.add(unique_name);

      this.addParent(domain);

      // Optionally load child data if not in search
      !this.searchQuery && this.addDomainData(domain);

      this.selectChildrenOfDomain.emit({
        childrenSubDomain: this.childrenSelectedDomainId,
        domain: this.selectedDomainName,
        unique_name: unique_name,
        childrenSelectedString: this.childrenSelectedString
      });

    } else {
      // Top-level domain selection
      if (this.selectedDomainName.has(domain.name)) {
        // Deselect domain and clear children
        this.selectedDomainName.delete(domain.name);
        this.childrenSelectedDomainId = new Set();
        this.childrenSelectedString = new Set();

        this.selectedDomain.emit(domain.name);
      } else {
        // Select domain
        this.selectedDomainName.add(domain.name);

        // Optionally load child data if not in search
        !this.searchQuery && this.addDomainData(domain);

        this.selectChildrenOfDomain.emit({
          childrenSubDomain: this.selectedChildrenDomainId,
          domain: this.selectedDomainName,
          unique_name: null,
          childrenSelectedString: this.childrenSelectedString
        });
      }
    }
  }


  addDomainData(field: AllDomainsI) {
    if(!field.isChildAvailable) return;
    this.getAllDomainData(field, ["domain", "subDomain", "service", "subService"], field.id);
  }

  getType(type: string) {
    switch (type.toLowerCase()) {
      case 'domain': return 'D';
      case 'subdomain': return 'SD';
      case 'service': return 'S';
      case 'subservice': return 'SS';
    }
  }

  getHighlightedName(name: string) {
    if (this.searchQuery !== "" || this.previousQuery == this.searchQuery ) return name;
    return this.knowledgeHubService.highlight(name, this.searchQuery, true);
  }

  toggleDomain(domain: AllDomainsI): void {
    domain.isOpen = !domain.isOpen;

    if (domain.isOpen && domain.isChildAvailable && !domain.children?.length) {
      this.addDomainData(domain);
    }
  }

  getColour(name: string) {
    switch (name.toLowerCase()) {
      case 'domain': return 'light-blue';
      case 'subdomain': return 'blue';
      case 'service': return 'yellow';
      case 'subservice': return 'green';
    }
  }

  searchDomains(event:any): void {
    const value:string = event.target.value;
    // this.searchQuery = value;
    if (!value) {
      this.searchQuery = '';
      this.previousQuery = '';
      this.filteredDomains = [...this.projectInsightDomainService.getAllDomains()];
      return;
    }

    const query = value.trim();
    this.searchSubject.next(query); 

  }

  searchWholeDomain(query:string){
    this.searchQuery = query.trim();
    this.projectInsightDomainService.searchDomain(query).subscribe({
      next: (res: AllDomainsI[]) => {
        const data = this.markDomains(res, query);
        console.log("data: ", data);
        
        this.filteredDomains = data;
        this.recommendedDomains = [];
        this.isLoadingRecommendations = false;
        this.previousQuery = query;
        this.projectInsightDomainService.setNewDomains(data);
      },
      error: (err: any) => {
        console.error('API call failed:', err);
      }
    })
  }

  private markDomains(domains: AllDomainsI[], query: string, parent?: AllDomainsI): AllDomainsI[] {
    return domains.map(domain => {
      const hasChildren = domain.children && domain.children.length > 0;

      // Recursively mark children, passing the current domain as their parent
      const children = hasChildren ? this.markDomains(domain.children!, query, domain) : [];

      const isDirectMatch = domain.name.trim().toLowerCase().includes(query.trim().toLowerCase());
      const isChildMatch = children.some(child => child.isOpen);

      return {
        ...domain,
        parent,                 
        children,               
        isOpen: isDirectMatch || isChildMatch,
        isChildAvailable: hasChildren
      };
    });
  }

  getDomainSearchRecommendation(query:string){
    query = query.replace(/[^a-zA-Z0-9 ]/g, '');
    if(!query || query == '' || query == this.previousQuery) {
      this.searchQuery = '';
      this.previousQuery = '';
      this.recommendedDomains = [];
      this.isLoadingRecommendations = false;
      return;
    }
    this.isLoadingRecommendations = true;
    this.projectInsightDomainService.getDomainSearchRecommendation(query).subscribe({
      next: (res: any) => {
        this.recommendedDomains = res;
        this.isLoadingRecommendations = false;
        this.searchQuery = query;
      },
      error: (err: any) => {
        console.error('API call failed:', err);
      }
    })
  }

  clearSearch(): void {
    this.searchQuery = '';
    this.searchDomains(null);
  }

  onToggle(domain: AllDomainsI): void {
    this.toggleDomain(domain);
  }

  getParent(node: AllDomainsI, removal?: boolean): AllDomainsI {
    let current: AllDomainsI = node;
    while (current.parent) {
      current = current.parent;
    }
    return current;
  }

  removeChildren(node: AllDomainsI) {
    if(node.type.toLowerCase() === 'domain') {
      this.selectedDomainName.delete(node.name);
    }
    else {
      this.childrenSelectedDomainId.delete(node.id);
      // this.childrenSelectedString.delete();
      const unique_name = node?.parent?.type.toUpperCase() + '_' + node?.parent?.id
      if(!this.checkParent(unique_name)) {
        this.childrenSelectedString.delete(unique_name);
      }
    }


    if(node.isChildAvailable && node.children?.length) {
      for(const child of node?.children){
        this.removeChildren(child);
      }
    }
  }

  checkParent(unique_name: string): boolean {
    for (const domain of this.filteredDomains) {
      if (this.checkParentRecursive(domain, unique_name)) {
        return true;
      }
    }
    return false;
  }

  private checkParentRecursive(node: AllDomainsI, unique_name: string): boolean {
    // Check current node's parent
    if (this.childrenSelectedDomainId.has(node.id) && node.parent && (node.parent.type.toUpperCase() + '_' + node.parent.id === unique_name)) {
      return true;
    }

    // Recurse into children
    if (node.children && node.children.length > 0) {
      for (const child of node.children) {
        if (this.checkParentRecursive(child, unique_name)) {
          return true;
        }
      }
    }

    return false;
  }


  addParent(node: AllDomainsI) {
    
    if(node.type.toLowerCase() === 'domain') {
      this.selectedDomainName.add(node.name);
      return;
    }
    
    this.childrenSelectedString.add(node?.parent?.type.toUpperCase() + '_' + node?.parent?.id);
    this.childrenSelectedDomainId.add(node.id);

    // if(node.parent){
    //   this.addParent(node.parent)
    // }
  }

  findRoot(id: number): AllDomainsI | null {
    for (const domain of this.filteredDomains) {
      const parent = this.findRootNode(domain, id);
      if (parent) {
        return parent;
      }
    }
    return null;
  }

  private findRootNode(node: AllDomainsI, id: number): AllDomainsI | null {
    if(node.id === id) {
      return this.findRootNode(node.parent!, id);
    }

    if(!node.parent) {
      return node;
    }

    return node;
  }

  @HostListener('document:keydown.escape', ['$event'])
  onEscKey(event: KeyboardEvent) {
    if (this.showRecommendations) {
      event.preventDefault();
      this.showRecommendations = false;
      this.selectedIndex = -1;
      
      const searchInput = document.querySelector('.search-input') as HTMLInputElement;
      if (searchInput) {
        searchInput.blur();
      }
    }
  }

  @HostListener('document:click', ['$event'])
  onClickOutside(event: MouseEvent) {
    const searchInput = document.querySelector('.search-input');
    const recommendations = document.querySelector('.recommendations');
    
    // Check if the click was outside both search input and recommendations
    if (searchInput && !searchInput.contains(event.target as Node) &&
        recommendations && !recommendations.contains(event.target as Node)) {
      this.showRecommendations = false;
    }
  }

}


export interface AllDomainsI {
  id: number;
  name: string;
  type: string;
  isChildAvailable: boolean;
  isOpen?: boolean;
  children?: AllDomainsI[];
  parent?: AllDomainsI;
  isActive?: boolean
}