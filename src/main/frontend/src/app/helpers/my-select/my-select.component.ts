import {
  Component,
  Input,
  Output,
  EventEmitter,
  forwardRef,
  OnInit,
  ViewChild,
  OnChanges,
  SimpleChanges,
} from '@angular/core';
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
      multi: true,
    },
  ],
})
export class MySelectComponent implements ControlValueAccessor, OnInit, OnChanges {
  @Input() disable = false;
  @Input() readonly = false;
  @Input() placeholder = 'Select';
  @Input() multiple = false;
  @Input() options: any[] = [];
  @Input() displayKey: string | string[] = '';
  @Input() displaySeparator: string = ' ';
  @Input() valueKey;
  @Input() optionDisabledKey?: string;
  @Input() isOptionDisabled?: (option: any) => boolean;
  @Input() showFilterAction: boolean = false;
  @Input() filterActionLabel: string = 'Filter List';
  @Input() wrapOptions = false;
  @Input() showSelectAll = true;
  @Input() wrapOptionLines = false;
  @Input() title = '';
  @Output() selectionChange = new EventEmitter<any>();
  @Output() change = new EventEmitter<any>();
  @Output() dropdownClosed = new EventEmitter<void>();
  @Output() filterAction = new EventEmitter<void>();

  @ViewChild(MatSelect) matSelect!: MatSelect;

  searchText = '';
  filteredOptions: any[] = [];
  selectedValue: any = null;
  prevSelectedValues: any;
  selectedValuesToEmit: any;

  ngOnInit(): void {
    this.filteredOptions = this.options || [];
  }

  private equalValue(a: any, b: any): boolean {
    if (a == null && b == null) {
      return true;
    }
    if (a == null || b == null) {
      return false;
    }
    // When consuming API data, ids may be numbers in options but strings in ngModel.
    // Compare by string for primitives to keep labels stable.
    const aType = typeof a;
    const bType = typeof b;
    const aPrim = aType === 'string' || aType === 'number' || aType === 'boolean';
    const bPrim = bType === 'string' || bType === 'number' || bType === 'boolean';
    if (aPrim && bPrim) {
      return String(a) === String(b);
    }
    return a === b;
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['options']) {
      this.onSearchChange();
    }
  }

  /** Optional panel classes (feat branch: no default custom-select-panel — avoids RMG overlay CSS clash). */
  get selectPanelClass(): string {
    const classes: string[] = [];
    if (this.wrapOptionLines) {
      classes.push('my-select-panel--wrap');
    }
    if (this.wrapOptions) {
      classes.push('wrapped-select-panel');
    }
    return classes.join(' ');
  }

  openWithDynamicPosition(_triggerElement: HTMLElement) {
    if (!this.matSelect) {
      return;
    }
    setTimeout(() => this.matSelect.open());
  }

  onSelectionChange(value: any): void {
    if (this.disable || this.readonly) {
      this.writeValue(this.selectedValue);
      return;
    }

    if (this.multiple) {
      const invalid = (value || []).some((v) =>
        this.isDisabledOption(
          this.options.find((o) =>
            this.valueKey ? this.equalValue(o[this.valueKey], v) : o === v
          )
        )
      );

      if (invalid) {
        this.writeValue(this.selectedValue);
        return;
      }
    } else {
      const opt = this.options.find((o) =>
        this.valueKey ? this.equalValue(o[this.valueKey], value) : o === value
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
    if (!Array.isArray(value)) {
      return value ?? undefined;
    }
    const copy = [...value];
    return copy.sort((a, b) => this.sortIndexForItem(a) - this.sortIndexForItem(b));
  }

  private sortIndexForItem(item: any): number {
    if (!this.options?.length) {
      return 0;
    }
    if (this.valueKey) {
      return this.options.findIndex((o) => this.equalValue(o[this.valueKey], item));
    }
    return this.getIndex(this.options, item);
  }

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

  onDropdownOpened(opened: boolean): void {
    if (!opened) {
      this.dropdownClosed.emit(this.selectedValue);
      return;
    }
    setTimeout(() => this.focusPanelSearch(), 0);
  }

  private focusPanelSearch(): void {
    const input = document.querySelector(
      '.cdk-overlay-container .mat-select-search-input'
    ) as HTMLInputElement | null;
    input?.focus();
  }

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
        ? this.filteredOptions.map((o) => o[this.valueKey])
        : [...this.filteredOptions];
    }

    this.onChange(this.selectedValue);
    this.onTouched();
    this.selectionChange?.emit(this.selectedValue);
    this.change?.emit(this.sort(this.selectedValue));
  }

  isAllSelected(): boolean {
    if (!this.selectedValue || !Array.isArray(this.selectedValue)) return false;
    if (!this.filteredOptions || this.filteredOptions.length === 0) return false;

    return this.filteredOptions.every((opt) =>
      this.selectedValue.includes(this.valueKey ? opt[this.valueKey] : opt)
    );
  }

  onSearchChange(): void {
    if (!Array.isArray(this.options)) {
      this.filteredOptions = [];
      return;
    }

    const text = (this.searchText || '').toLowerCase();
    this.filteredOptions = this.options.filter((opt) =>
      this.getDisplayText(opt)?.toLowerCase().includes(text)
    );
  }

  getIndex(list: any[], item: any): number {
    return list.findIndex((element) => this.compareObjects(element, item));
  }

  compareObjects = (o1: any, o2: any): boolean => {
    if (!o1 || !o2) return false;
    if (o1 == o2) {
      return true;
    }
    return this.deepEqual(o1, o2);
  };

  deepEqual(obj1: any, obj2: any): boolean {
    if (obj1 === obj2) return true;

    if (typeof obj1 !== typeof obj2) return false;

    if (typeof obj1 !== 'object' || obj1 === null || obj2 === null) return false;

    const keys1 = Object.keys(obj1);
    const keys2 = Object.keys(obj2);

    if (keys1.length !== keys2.length) return false;

    return keys1.every((key) => {
      if (typeof obj1[key] === 'object' && obj1[key] !== null) {
        return this.deepEqual(obj1[key], obj2[key]);
      }
      return obj1[key] === obj2[key];
    });
  }

  multiTriggerLabel(): string {
    if (!this.multiple) {
      return '';
    }
    const selected = this.selectedValue;
    if (!Array.isArray(selected) || selected.length === 0) {
      return this.placeholder || 'Select';
    }
    const labels = selected
      .map((item) => this.labelForSelectedItem(item))
      .filter((label) => label !== '');
    if (labels.length === 0) {
      return this.placeholder || 'Select';
    }
    return labels.join(', ');
  }

  selectHoverTitle(): string {
    if (this.title) {
      return this.title;
    }
    if (this.multiple) {
      const label = this.multiTriggerLabel();
      return label === (this.placeholder || 'Select') ? '' : label;
    }
    if (this.selectedValue == null || this.selectedValue === '') {
      return '';
    }
    return this.labelForSelectedItem(this.selectedValue);
  }

  private labelForSelectedItem(item: any): string {
    if (item == null) {
      return '';
    }
    if (this.options?.length) {
      const opt = this.options.find((o) =>
        this.valueKey
          ? this.equalValue(o?.[this.valueKey], item)
          : o === item || this.compareObjects(o, item)
      );
      if (opt != null) {
        return this.getDisplayText(opt);
      }
    }
    if (typeof item === 'string' || typeof item === 'number') {
      return String(item);
    }
    return this.getDisplayText(item);
  }

  getDisplayText(option: any): string {
    if (!option) return '';

    if (Array.isArray(this.displayKey)) {
      return this.displayKey
        .map((key) => option[key] ?? '')
        .filter((val) => val)
        .join(this.displaySeparator);
    }

    return this.displayKey ? (option[this.displayKey] ?? '') : option;
  }

  setDisabledState(isDisabled: boolean): void {
    this.disable = isDisabled;
  }

  isDisabledOption(option: any): boolean {
    if (this.disable || this.readonly) {
      return true;
    }

    if (this.isOptionDisabled) {
      return this.isOptionDisabled(option);
    }

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

  trackByOption = (_: number, opt: any): any =>
    this.valueKey ? opt?.[this.valueKey] : this.getDisplayText(opt);
}
