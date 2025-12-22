import { Component, EventEmitter, forwardRef, Input, OnInit, Output } from '@angular/core';
import { NG_VALUE_ACCESSOR, ControlValueAccessor, FormControl } from '@angular/forms';
import { debounceTime, distinctUntilChanged } from 'rxjs';

@Component({
  standalone: false,
  selector: 'app-my-autocomplete',
  templateUrl: './my-autocomplete.component.html',
  styleUrl: './my-autocomplete.component.css',
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => MyAutocompleteComponent),
      multi: true
    }
  ]
})

export class MyAutocompleteComponent implements OnInit, ControlValueAccessor {

  control = new FormControl('');
  searchText = '';

  @Input() placeholder: string = 'Search...';
  @Input() label: string | undefined;
  @Input() disabled = false;
  @Input() loading = false;
  @Input() displayKey: string = '';
  @Input() clearable = true;

  private _options: any[] = [];
  filteredOptions: any[] = [];

  @Input() set options(value: any[]) {
    this._options = value ?? [];
    this.filteredOptions = [...this._options]; // refresh UI when options input changes
  }

  @Output() optionSelected = new EventEmitter<any>();
  @Output() cleared = new EventEmitter<void>();

  onTouched: any = () => { };
  onChange: any = () => { };

  ngOnInit(): void {
    this.control.valueChanges
      .pipe(distinctUntilChanged())
      .subscribe(value => {
        if (typeof value === 'string') {
          this.searchText = value;
          this.filterLocal(value);
        }
      });
  }

  private filterLocal(searchTerm: string) {
    if (!searchTerm) {
      this.filteredOptions = [...this._options];
      return;
    }

    const lower = searchTerm.toLowerCase();
    this.filteredOptions = this._options.filter(opt =>
      (opt[this.displayKey] ?? '').toLowerCase().includes(lower)
    );
  }

  writeValue(value: any): void {
    this.control.setValue(value, { emitEvent: false });
  }

  registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    isDisabled ? this.control.disable() : this.control.enable();
  }

  displayFn(option: any): string {
    if (!option) return '';
    return this.displayKey ? option[this.displayKey] : option?.toString() || '';
  }

  selectOption(option: any) {
    let opt = this.displayKey ? option[this.displayKey] : option || '';
    this.control.setValue(opt);
    this.onChange(opt);
    this.optionSelected.emit(opt);
  }

  onManualInput() {
    const value = this.control.value;
    if (typeof value === 'string') {
      this.onChange(value);
    }
  }

  clear() {
    this.control.setValue('');
    this.filteredOptions = [...this._options];
    this.onChange('');
    this.cleared.emit();
  }

  highlightText(text: string): string {
    if (!this.searchText) {
      return text;
    }
    const escaped = this.searchText.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
    const regex = new RegExp(escaped, 'gi');
    return text.replace(regex, (match: string) => `<mark>${match}</mark>`);
  }
}

