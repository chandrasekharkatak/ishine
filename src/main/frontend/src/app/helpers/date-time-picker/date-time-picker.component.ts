import {
  Component,
  Input,
  forwardRef,
  ViewChild
} from '@angular/core';
import {
  ControlValueAccessor,
  NG_VALUE_ACCESSOR
} from '@angular/forms';

import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatDatepickerModule, MatDatepickerInputEvent } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import * as moment from 'moment';

@Component({
  selector: 'app-date-time-picker',
  standalone: true,
  imports: [
    CommonModule, 
    FormsModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatFormFieldModule,
    MatInputModule
  ],
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
  materialDateValue: Date | null = null; // For Material datepicker (Date object)

  @ViewChild('picker') picker: any; // Reference to Material datepicker

  private onChange = (_: any) => {};
  private onTouched = () => {};

  /* ---------------- CVA ---------------- */

  writeValue(value: string | null): void {
    if (this.mode === 'date') {
      // For date mode, handle Material datepicker value
      if (value) {
        // Value comes in as dd-MM-yyyy format from parent component
        try {
          const parts = value.split('-');
          if (parts.length === 3) {
            // Check if format is dd-MM-yyyy or yyyy-MM-dd
            const isYYYYMMDD = parts[0].length === 4;
            if (isYYYYMMDD) {
              // Format is yyyy-MM-dd
              const [yyyy, mm, dd] = parts;
              this.materialDateValue = new Date(parseInt(yyyy), parseInt(mm) - 1, parseInt(dd));
            } else {
              // Format is dd-MM-yyyy
              const [dd, mm, yyyy] = parts;
              this.materialDateValue = new Date(parseInt(yyyy), parseInt(mm) - 1, parseInt(dd));
            }
            // Also set modelValue for consistency
            this.modelValue = this.toInputValue(value);
          } else {
            this.materialDateValue = null;
            this.modelValue = null;
          }
        } catch (e) {
          console.error('Error parsing date value:', value, e);
          this.materialDateValue = null;
          this.modelValue = null;
        }
      } else {
        this.materialDateValue = null;
        this.modelValue = null;
      }
    } else {
      // For time/datetime modes, use regular modelValue
      this.modelValue = this.toInputValue(value);
      this.materialDateValue = null;
    }
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

      // Check if date is disabled (including disabledDates array)
      if (this.isDateDisabled(formatted)) {
        // Reset to previous value or null
        this.modelValue = null;
        this.onChange(null);
        // Optionally show an alert or message to user
        console.warn(`Date ${formatted} is disabled. MinDate: ${this.minDate}, MaxDate: ${this.maxDate}, DisabledDates count: ${this.disabledDates?.length || 0}`);
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
    this.materialDateValue = null;
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

  /** 🚫 Disable date logic - returns true if date should be disabled */
  private isDateDisabled(date: string): boolean {
    // Check disabled dates array (format: dd-MM-yyyy)
    if (this.disabledDates && this.disabledDates.length > 0) {
      // Normalize dates for comparison (handle leading zeros)
      const normalizeDate = (d: string) => {
        const parts = d.split('-');
        if (parts.length === 3) {
          // Pad with zeros to ensure consistent format
          const dd = parts[0].padStart(2, '0');
          const mm = parts[1].padStart(2, '0');
          const yyyy = parts[2];
          return `${dd}-${mm}-${yyyy}`;
        }
        return d;
      };
      
      const normalizedDate = normalizeDate(date);
      const normalizedDisabledDates = this.disabledDates.map(normalizeDate);
      
      if (normalizedDisabledDates.includes(normalizedDate)) {
        return true; // Date is in disabled list
      }
    }

    // Check min/max date range
    const toNum = (d: string) => {
      // Convert dd-MM-yyyy to yyyyMMdd for comparison
      const parts = d.split('-');
      if (parts.length === 3) {
        // Ensure 2-digit day and month, 4-digit year
        const dd = parts[0].padStart(2, '0');
        const mm = parts[1].padStart(2, '0');
        const yyyy = parts[2];
        return Number(yyyy + mm + dd);
      }
      return 0;
    };

    const current = toNum(date);

    if (this.minDate) {
      const minNum = toNum(this.minDate);
      if (current < minNum) {
        return true; // Date is before minDate
      }
    }
    
    if (this.maxDate) {
      const maxNum = toNum(this.maxDate);
      if (current > maxNum) {
        return true; // Date is after maxDate
      }
    }

    return false; // Date is enabled
  }

  /** Convert dd-MM-yyyy to yyyy-MM-dd for HTML5 date input */
  get minDateForInput(): string | undefined {
    if (!this.minDate) return undefined;
    try {
      const [dd, mm, yyyy] = this.minDate.split('-');
      if (dd && mm && yyyy) {
        return `${yyyy}-${mm}-${dd}`;
      }
    } catch (e) {
      console.error('Error parsing minDate:', this.minDate, e);
    }
    return undefined;
  }

  /** Convert dd-MM-yyyy to yyyy-MM-dd for HTML5 date input */
  get maxDateForInput(): string | undefined {
    if (!this.maxDate) return undefined;
    try {
      const [dd, mm, yyyy] = this.maxDate.split('-');
      if (dd && mm && yyyy) {
        return `${yyyy}-${mm}-${dd}`;
      }
    } catch (e) {
      console.error('Error parsing maxDate:', this.maxDate, e);
    }
    return undefined;
  }

  /** Handle Material datepicker date change */
  onMaterialDateChange(event: MatDatepickerInputEvent<Date>): void {
    const date: Date | null = event.value;
    
    if (!date) {
      this.materialDateValue = null;
      this.modelValue = null;
      this.onChange(null);
      return;
    }

    // Convert Date object to dd-MM-yyyy format
    const dd = date.getDate().toString().padStart(2, '0');
    const mm = (date.getMonth() + 1).toString().padStart(2, '0');
    const yyyy = date.getFullYear().toString();
    const formatted = `${dd}-${mm}-${yyyy}`;

    // Check if date is disabled
    if (this.isDateDisabled(formatted)) {
      this.materialDateValue = null;
      this.modelValue = null;
      this.onChange(null);
      console.warn(`Date ${formatted} is disabled`);
      return;
    }

    // Update both Material datepicker value and model value
    this.materialDateValue = date;
    this.modelValue = this.toInputValue(formatted);
    this.onChange(formatted);
  }

  /** Date filter function for Material datepicker */
  dateFilter = (date: Date | null): boolean => {
    if (!date) return false;

    // Convert Date to dd-MM-yyyy format for comparison
    const dd = date.getDate().toString().padStart(2, '0');
    const mm = (date.getMonth() + 1).toString().padStart(2, '0');
    const yyyy = date.getFullYear().toString();
    const dateStr = `${dd}-${mm}-${yyyy}`;

    // Return true if date is NOT disabled (Material datepicker shows enabled dates)
    return !this.isDateDisabled(dateStr);
  };

  /** Get minimum date as Date object for Material datepicker */
  get minDateForMaterial(): Date | null {
    if (!this.minDate) return null;
    try {
      const [dd, mm, yyyy] = this.minDate.split('-');
      return new Date(parseInt(yyyy), parseInt(mm) - 1, parseInt(dd));
    } catch (e) {
      console.error('Error parsing minDate for Material:', this.minDate, e);
      return null;
    }
  }

  /** Get maximum date as Date object for Material datepicker */
  get maxDateForMaterial(): Date | null {
    if (!this.maxDate) return null;
    try {
      const [dd, mm, yyyy] = this.maxDate.split('-');
      return new Date(parseInt(yyyy), parseInt(mm) - 1, parseInt(dd));
    } catch (e) {
      console.error('Error parsing maxDate for Material:', this.maxDate, e);
      return null;
    }
  }
}
