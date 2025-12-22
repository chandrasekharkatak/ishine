import { HttpClient } from '@angular/common/http';
import { Component, EventEmitter, Input, Output, OnInit } from '@angular/core';
import { ProjectInsightDomainService } from 'src/app/services/project-insight-domain.service';
import { environment } from 'src/environments/environment';

@Component({
  standalone: false,
  selector: 'app-filter-project-insight',
  templateUrl: './filter-project-insight.component.html',
  styleUrls: ['./filter-project-insight.component.scss']
})
export class FilterProjectInsightComponent implements OnInit {

  @Input() isVisible: boolean = false;
  @Input() existingFilters: any = {};
  @Output() onClose = new EventEmitter<void>();
  @Output() onApplyFilter = new EventEmitter<any>();

  filterOptions: any = {};
  actualFilter: any = {};
  filtersLoaded = false;

  constructor(private projectInsightDomainService: ProjectInsightDomainService) { }

  ngOnInit(): void {
    this.getAllValues();
  }

  ngOnChanges() {
    if (this.filtersLoaded) {
      this.initializeFilters();
    }
  }

  initializeFilters(clearing: boolean = false) {
    this.actualFilter = !clearing ? { ...this.existingFilters } : {};

    Object.keys(this.filterOptions).forEach(key => {
      const filterName = key;

      if (!clearing && this.actualFilter[filterName]) {
        const value = this.actualFilter[filterName];

        if (this.filterOptions[key].type === 'select') {
          this.filterOptions[key].selectedValues = Array.isArray(value) ? value : [value];
        } else if (this.filterOptions[key].type === 'date') {
          if (typeof value === 'string') {
            const dateParts = value.split('-');
            this.filterOptions[key].selectedValue = new Date(
              parseInt(dateParts[0], 10),
              parseInt(dateParts[1], 10) - 1,
              parseInt(dateParts[2], 10)
            );
          } else {
            this.filterOptions[key].selectedValue = value;
          }
        } else {
          this.filterOptions[key].selectedValue = value;
        }

      } else {
        if (this.filterOptions[key].type === 'select') {
          this.filterOptions[key].selectedValues = [];
        } else {
          this.filterOptions[key].selectedValue = null;
        }
      }
    });
  }

  clearFilter() {
    this.actualFilter = {};
    this.initializeFilters(true);
  }

  async getAllValues() {
    this.projectInsightDomainService.loadAllFilters().subscribe({
      next: (res) => {
        this.filterOptions = res;
        this.filtersLoaded = true;
        this.initializeFilters();
      },
      error: (error) => {
        this.filtersLoaded = true;
      }
    });
  }

  inputFilter(event: any, name: string) {
    const value = event.value || event.target.value;;
    if (name.toLowerCase() === 'created_At'.toLowerCase()) {
      const year = value.getFullYear();
      const month = (value.getMonth() + 1).toString().padStart(2, '0');
      const day = value.getDate().toString().padStart(2, '0');
      this.actualFilter[name] = `${year}-${month}-${day}`;
      this.filterOptions[name].selectedValue = value;
      return;
    }

    if (typeof value === 'number') {
      this.actualFilter[name] = Number(value);
    } else {
      this.actualFilter[name] = value;
    }

    const filterKey = this.getFilterKeyByName(name);
    if (filterKey && this.filterOptions[filterKey].type === 'select') {
      this.filterOptions[filterKey].selectedValues = Array.isArray(value) ? value : [value];
    }
  }

  applyFilter() {
    this.onApplyFilter.emit(this.actualFilter);
  }

  closeModal() {
    this.onClose.emit();
  }

  getAllKeys(filter: any): string[] {
    return Object.keys(filter);
  }

  private getFilterKeyByName(name: string): string | undefined {
    return Object.keys(this.filterOptions).find(key =>
      this.filterOptions[key].name === name
    );
  }
}
