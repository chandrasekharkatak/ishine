import { ConnectedPosition, Overlay, OverlayRef, ViewportRuler } from '@angular/cdk/overlay';
import { ComponentPortal } from '@angular/cdk/portal';
import { Component, Input, Output, EventEmitter, forwardRef, OnInit, ViewChild, ElementRef, OnChanges, SimpleChanges } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import { MatSelect } from '@angular/material/select';

@Component({
  standalone: false,
  selector: 'app-my-select',
  templateUrl: './my-select.component.html',
  styleUrls: ['./my-select.component.css'],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => MySelectComponent),
      multi: true
    }
  ]
})
export class MySelectComponent implements ControlValueAccessor, OnInit {
  
  @Input() disable = false;
  @Input() readonly = false;
  @Input() placeholder = 'Select';
  @Input() multiple = false;
  @Input() options: any[] = [];
  @Input() displayKey: string | string[] = '';
  @Input() displaySeparator: string = ' ';
  @Input() valueKey;
  @Input() optionDisabledKey?: string; // e.g. 'disabled'
  @Input() isOptionDisabled?: (option: any) => boolean;
  @Input() showFilterAction: boolean = false;
  @Input() filterActionLabel: string = 'Filter List';

  @Input() wrapOptions = false;
  @Input() showSelectAll = true;
  @Output() selectionChange = new EventEmitter<any>();
  @Output() change = new EventEmitter<any>();
  @Output() dropdownClosed = new EventEmitter<void>();
  @Output() filterAction = new EventEmitter<void>();  
  
  @ViewChild(MatSelect) matSelect!: MatSelect;
  @ViewChild('search') searchInputBox!: ElementRef<HTMLInputElement>;
  
  searchText = '';
  filteredOptions: any[] = [];
  selectedValue: any=null;
  prevSelectedValues:any;
  selectedValuesToEmit:any;

  ngOnInit(): void {
    this.filteredOptions = this.options || [];
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['options']) {
      this.onSearchChange();
    }
  }

  ngDoCheck() {
    // manually trigger filtering when searchText changes (ngModel doesn't auto-pipe)
    this.onSearchChange();
  }

  openWithDynamicPosition(triggerElement: HTMLElement) {
    if (!this.matSelect) return;
    setTimeout(() => this.matSelect.open());
  }

  // Called when selection changes
 onSelectionChange(value: any): void {

  // Block readonly / disabled component
  if (this.disable || this.readonly) {
    this.writeValue(this.selectedValue);
    return;
  }

  // Block disabled options (single & multi)
  if (this.multiple) {
    const invalid = (value || []).some(v =>
      this.isDisabledOption(
        this.options.find(o =>
          this.valueKey ? o[this.valueKey] === v : o === v
        )
      )
    );

    if (invalid) {
      this.writeValue(this.selectedValue);
      return;
    }
  } else {
    const opt = this.options.find(o =>
      this.valueKey ? o[this.valueKey] === value : o === value
    );

    if (this.isDisabledOption(opt)) {
      this.writeValue(this.selectedValue);
      return;
    }
  }

  this.selectedValue = value;
  this.onChange(value);
  this.onTouched();
  this.selectionChange.emit(this.sort(value));
  this.change.emit(this.sort(value));
}



  sort(value: any) {
    return Array.isArray(value)
      ? value.sort((x, y) => this.getIndex(this.options, x) - this.getIndex(this.options, y))
      : value ?? undefined;
  }


  // ControlValueAccessor interface methods
  onChange = (_: any) => {};
  onTouched = () => {};

  writeValue(value: any): void {
    this.selectedValue = value;
  }

  registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }

  onDropdownOpened(opened){
    if(!opened){
      this.dropdownClosed.emit(this.selectedValue);
    }else{
      this.searchInputBox?.nativeElement.focus();
    }
    
  }
  // resetFilter(){
  //   this.searchText='';
  //   this.onSearchChange();
  // }

 toggleSelectAll(event: Event): void {
  if (this.readonly || this.disable) {
    event.stopPropagation();
    return;
  }

  event.stopPropagation();

  if (this.isAllSelected()) {
    this.selectedValue = [];
  } else {
    this.selectedValue = this.valueKey
      ? this.filteredOptions.map(o => o[this.valueKey])
      : [...this.filteredOptions];
  }

  // Emit selection change
  this.onChange(this.selectedValue);
  this.onTouched();
  this.selectionChange?.emit(this.selectedValue);
  this.change?.emit(this.sort(this.selectedValue));
}


isAllSelected(): boolean {
  if (!this.selectedValue || !Array.isArray(this.selectedValue)) return false;
  if (!this.filteredOptions || this.filteredOptions.length === 0) return false;

  return this.filteredOptions.every(opt =>
    this.selectedValue.includes(this.valueKey ? opt[this.valueKey] : opt )
  );
}

// Called when search input changes
  onSearchChange(): void {
   if (!Array.isArray(this.options)) {
    this.filteredOptions = [];
    return;
  }

    let text = (this.searchText || '').toLowerCase();
    this.filteredOptions = this.options?.filter(opt =>
      this.getDisplayText(opt)?.toLowerCase().includes(text)
    );
  }

  getIndex(list: any[], item: any): number {
  return list.findIndex((element) => this.compareObjects(element, item));
}

compareObjects = (o1: any, o2: any): boolean => {
  if (!o1 || !o2) return false;
  if(o1==o2){return true;}
  // if(this.valueKey){
  // return o1[this.valueKey] === o2[this.valueKey];
  // }
  return this.deepEqual(o1,o2);
};

deepEqual(obj1: any, obj2: any): boolean {
  if (obj1 === obj2) return true;

  if (typeof obj1 !== typeof obj2) return false;

  if (typeof obj1 !== 'object' || obj1 === null || obj2 === null) return false;

  const keys1 = Object.keys(obj1);
  const keys2 = Object.keys(obj2);

  if (keys1.length !== keys2.length) return false;

  return keys1.every(key => {
    if (typeof obj1[key] === 'object' && obj1[key] !== null) {
      return this.deepEqual(obj1[key], obj2[key]);
    } else {
      return obj1[key] === obj2[key];
    }
  });
}

getDisplayText(option: any): string {
  if (!option) return '';

  // If displayKey is array → join multiple keys
  if (Array.isArray(this.displayKey)) {
    return this.displayKey
      .map(key => option[key] ?? '') // safely handle missing keys
      .filter(val => val)            // remove empty values
      .join(this.displaySeparator);  // join using separator
  }

  // If it's a single key
  return this.displayKey ? option[this.displayKey] ?? '' : option;
}
setDisabledState(isDisabled: boolean): void {
  this.disable = isDisabled;
}

isDisabledOption(option: any): boolean {
  if (this.disable || this.readonly) {
    return true;
  }

  // Function-based disabling (highest priority)
  if (this.isOptionDisabled) {
    return this.isOptionDisabled(option);
  }

  // Key-based disabling
  if (this.optionDisabledKey) {
    return !!option?.[this.optionDisabledKey];
  }

  return false;
}

  triggerFilter(event: Event) {
    event.stopPropagation();
    event.preventDefault();
    this.filterAction.emit();
  }


trackByOption = (_: number, opt: any): any => this.valueKey ? opt?.[this.valueKey] : this.getDisplayText(opt);

getPanelClass(): string[] {
  return this.wrapOptions ? ['custom-select-panel', 'wrapped-select-panel'] : ['custom-select-panel'];
}

  
}