import {
  Component,
  Input,
  forwardRef
} from '@angular/core';
import {
  ControlValueAccessor,
  NG_VALUE_ACCESSOR
} from '@angular/forms';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-date-time-picker',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './date-time-picker.component.html',
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => DateTimePickerComponent),
      multi: true
    }
  ]
})
export class DateTimePickerComponent implements ControlValueAccessor {

  @Input() label = '';
  @Input() required = false;
  @Input() disabled = false;
  @Input() mode: 'date' | 'time' | 'datetime' = 'date';

  modelValue: string | null = null;

  private onChange = (_: any) => {};
  private onTouched = () => {};

  writeValue(value: string | null): void {
    this.modelValue = this.toInputValue(value);
  }

  registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    this.disabled = isDisabled;
  }

  onModelChange(val: string | null) {
    if (!val) {
      this.onChange(null);
      return;
    }

    if (this.mode === 'date') {
      const [yyyy, mm, dd] = val.split('-');
      this.onChange(`${dd}-${mm}-${yyyy}`);
    } else if (this.mode === 'time') {
      this.onChange(val);
    } else {
      this.onChange(val + ':00');
    }
  }

  clearValue() {
    this.modelValue = null;
    this.onChange(null);
  }

  private toInputValue(val: string | null): string | null {
    if (!val) return null;

    if (this.mode === 'date') {
      const [dd, mm, yyyy] = val.split('-');
      return `${yyyy}-${mm}-${dd}`;
    }

    if (this.mode === 'datetime') {
      return val.substring(0, 16);
    }

    return val;
  }
}
