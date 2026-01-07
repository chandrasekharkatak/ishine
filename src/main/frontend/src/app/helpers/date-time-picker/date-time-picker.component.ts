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

  /** 🔒 Date disable support */
  @Input() minDate?: string; // dd-MM-yyyy
  @Input() maxDate?: string; // dd-MM-yyyy
  @Input() disabledDates: string[] = []; // dd-MM-yyyy[]

  modelValue: string | null = null;

  private onChange = (_: any) => {};
  private onTouched = () => {};

  /* ---------------- CVA ---------------- */

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

  /* ---------------- UI Change ---------------- */

  onModelChange(val: string | null) {
    if (!val) {
      this.onChange(null);
      return;
    }

    if (this.mode === 'date') {
      const [yyyy, mm, dd] = val.split('-');
      const formatted = `${dd}-${mm}-${yyyy}`;

      if (this.isDateDisabled(formatted)) {
        this.modelValue = null;
        this.onChange(null);
        return;
      }

      this.onChange(formatted);
    }

    if (this.mode === 'time') {
      this.onChange(this.to12HourFormat(val));
    }

    if (this.mode === 'datetime') {
      this.onChange(val);
    }
  }

  clearValue() {
    this.modelValue = null;
    this.onChange(null);
  }

  /* ---------------- Helpers ---------------- */

  private toInputValue(val: string | null): string | null {
    if (!val) return null;

    if (this.mode === 'date') {
      const [dd, mm, yyyy] = val.split('-');
      return `${yyyy}-${mm}-${dd}`;
    }

    if (this.mode === 'time') {
      return this.to24HourFormat(val);
    }

    if (this.mode === 'datetime') {
      return val.substring(0, 16);
    }

    return val;
  }

  /** 🕒 24 → 12 hour */
  private to12HourFormat(time: string): string {
    let [hour, minute] = time.split(':').map(Number);
    const ampm = hour >= 12 ? 'PM' : 'AM';
    hour = hour % 12 || 12;
    return `${hour.toString().padStart(2, '0')}:${minute
      .toString()
      .padStart(2, '0')} ${ampm}`;
  }

  /** 🕒 12 → 24 hour */
  private to24HourFormat(time: string): string {
    if (!time.includes('AM') && !time.includes('PM')) return time;

    let [t, meridian] = time.split(' ');
    let [hour, minute] = t.split(':').map(Number);

    if (meridian === 'PM' && hour < 12) hour += 12;
    if (meridian === 'AM' && hour === 12) hour = 0;

    return `${hour.toString().padStart(2, '0')}:${minute
      .toString()
      .padStart(2, '0')}`;
  }

  /** 🚫 Disable date logic */
  private isDateDisabled(date: string): boolean {
    if (this.disabledDates.includes(date)) return true;

    const toNum = (d: string) => Number(d.split('-').reverse().join(''));

    const current = toNum(date);

    if (this.minDate && current < toNum(this.minDate)) return true;
    if (this.maxDate && current > toNum(this.maxDate)) return true;

    return false;
  }
}
