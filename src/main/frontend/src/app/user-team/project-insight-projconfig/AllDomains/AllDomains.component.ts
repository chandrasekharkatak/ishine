import { ApiSourceService } from 'src/app/services/api-source.service';
import { Component, EventEmitter, Input, OnInit, Output, OnChanges, SimpleChanges } from '@angular/core';
import { AllDomainService } from './AllDomain.service';

@Component({
  selector: 'app-AllDomains',
  templateUrl: './AllDomains.component.html',
  styleUrls: ['./AllDomains.component.scss'],
})
export class AllDomainsComponent implements OnInit, OnChanges {
  filteredDomains: AllDomainsI[] = [];
  searchQuery: string = '';

  @Input() domainName: string | null = null;
  @Input() childrenSelectedDomainId: number | null = null;
  @Input() incoming = false;

  selectedDomainName: string = null;
  selectedChildrenDomainId: number = null;

  @Output() selectedDomain = new EventEmitter<any>();
  @Output() selectChildrenOfDomain = new EventEmitter<{ childrenSubDomain: number, domain: string,unique_name: string }>();

  constructor(private apiSource: ApiSourceService, private allDomainService:AllDomainService) { }

  ngOnInit() {
    if (!this.incoming) {
      this.getAllDomainData(null);
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (this.incoming && (this.domainName != this.selectedDomainName || this.childrenSelectedDomainId != this.selectedChildrenDomainId) ) {
      this.selectedDomainName = this.domainName;
      this.selectedChildrenDomainId = this.childrenSelectedDomainId;
      this.filteredDomains = this.allDomainService.getAllDomains().filter(domain => this.domainName?.includes(domain.name));
    } else {
      this.selectedDomainName = null;
      this.selectedChildrenDomainId = null;
      this.filteredDomains = this.allDomainService.getAllDomains();
    }
  }

  getAllDomainData(field: AllDomainsI | null, type: string[] = ["domain"], id: number = null) {
    this.apiSource.getAllDomainData(type, id).subscribe({
      next: (res: AllDomainsI[]) => {
        const data = res.map(item => {
          item.parent = field;
          return item;
        });

        if (field) {
          field.children = data;
          field.isOpen = true;
        } else {
          this.allDomainService.setAllDomains(data);

        }

        if (this.incoming) {
          this.filteredDomains = this.allDomainService.getAllDomains().filter(domain => this.domainName?.includes(domain.name));
        } else {
          this.filteredDomains = [...this.allDomainService.getAllDomains()];
        }
      },
      error: (err: any) => {
        console.error('API call failed:', err);
      }
    });
  }

  filterProjectWithDomains(domain: AllDomainsI) {
    if(domain.type.toLowerCase() !== 'domain'.toLowerCase()) {
      const parent: AllDomainsI = this.getParent(domain);
      if(this.selectedDomainName === parent.name && this.childrenSelectedDomainId === domain.id) {
        this.selectedDomainName = null;
        this.childrenSelectedDomainId = null;
        this.selectChildrenOfDomain.emit({ childrenSubDomain: null, domain: null, unique_name: null });
        return;
      }
      this.selectedDomainName = parent.name;
      this.childrenSelectedDomainId = domain.id;
      const unique_name = domain.parent.type.toUpperCase() + '_'+domain.parent.id;
      console.log("unique_name: ", unique_name);
      this.selectChildrenOfDomain.emit({ childrenSubDomain: domain.id, domain: this.selectedDomainName, unique_name: unique_name });
    }
    else if (domain.type === 'domain') {
      this.selectedDomainName = this.selectedDomainName === domain.name ? null : domain.name;
      this.selectedDomain.emit(domain.name);
    }
  }

  addDomainData(field: AllDomainsI) {
    if (!field.isChildAvailable) return;
    this.getAllDomainData(field, ["domain", "subDomain", "service", "subService"], field.id);
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

  getName(name: string) {
    if (!name) return '-';
    return name.length > 9 ? name.substring(0, 8) + '...' : name;
  }

  searchDomains(): void {
    if (!this.searchQuery) {
      this.filteredDomains = [...this.allDomainService.getAllDomains()];
      return;
    }

    const query = this.searchQuery.toLowerCase();
    this.filteredDomains = this.allDomainService.getAllDomains().filter(domain =>
      domain.name.toLowerCase().includes(query)
    );
  }

  clearSearch(): void {
    this.searchQuery = '';
    this.searchDomains();
  }

  onToggle(domain: AllDomainsI): void {
    this.toggleDomain(domain);
  }

  getParent(node: AllDomainsI): AllDomainsI {
    let current: AllDomainsI = node;
    while (current.parent) {
      current = current.parent;
    }
    return current;
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
}