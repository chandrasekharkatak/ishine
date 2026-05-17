import {
  Component,
  EventEmitter,
  HostBinding,
  Injectable,
  Input,
  OnChanges,
  OnInit,
  Output,
  SimpleChanges,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  DateAdapter,
  MAT_DATE_FORMATS,
  MatNativeDateModule,
  NativeDateAdapter,
} from '@angular/material/core';
import { MatDatepicker, MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';

/** Month + year text in the input only (no day). Scoped to this picker so HR dashboard can keep DD/MM for other pickers. */
@Injectable()
export class RoMonthYearAdapter extends NativeDateAdapter {
  override format(date: Date, displayFormat: object): string {
    if (String(displayFormat) === 'input') {
      return date.toLocaleDateString('en-US', { month: 'short', year: 'numeric' });
    }
    return date.toLocaleDateString('en-US', { month: 'long', year: 'numeric' });
  }
}

export const RO_MONTH_YEAR_FORMATS = {
  parse: {
    dateInput: null,
  },
  display: {
    dateInput: 'input',
    monthYearLabel: 'MMM yyyy',
    dateA11yLabel: 'LL',
    monthYearA11yLabel: 'MMMM yyyy',
  },
};

@Component({
  selector: 'app-ro-month-year-field',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatDatepickerModule,
    MatIconModule,
    MatNativeDateModule,
  ],
  providers: [
    { provide: DateAdapter, useClass: RoMonthYearAdapter },
    { provide: MAT_DATE_FORMATS, useValue: RO_MONTH_YEAR_FORMATS },
  ],
  template: `
    <mat-form-field
      appearance="outline"
      class="db-mat-field ro-month-field"
      [class.ro-month-field--unified]="hideOutline"
      subscriptSizing="dynamic"
    >
      <input
        matInput
        [matDatepicker]="picker"
        [(ngModel)]="pickerModel"
        [min]="minDate"
        [max]="maxDate"
        [name]="'roMonthYear_' + fieldId"
        readonly
        (click)="picker.open()"
        placeholder="Month, year"
      />
      <mat-datepicker-toggle matIconSuffix [for]="picker">
        <mat-icon matDatepickerToggleIcon>calendar_month</mat-icon>
      </mat-datepicker-toggle>
      <mat-datepicker
        #picker
        startView="year"
        [startAt]="pickerModel"
        (monthSelected)="onMonthSelected($event, picker)"
        panelClass="month-picker"
      >
      </mat-datepicker>
    </mat-form-field>
  `,
  styles: [
    `
      :host {
        display: block;
        flex: 0 0 182px;
        width: 182px;
        min-width: 182px;
        max-width: 182px;
        box-sizing: border-box;
      }
      :host(.ro-month-host--unified) {
        flex: 1 1 0;
        width: auto;
        min-width: 0;
        max-width: none;
      }
      .ro-month-field {
        width: 100%;
        box-sizing: border-box;
      }
      .ro-month-field ::ng-deep .mat-mdc-form-field-subscript-wrapper {
        display: none;
      }
      .ro-month-field ::ng-deep .mat-mdc-form-field-flex {
        align-items: center;
      }
      .ro-month-field ::ng-deep .mat-mdc-form-field-infix {
        box-sizing: border-box;
        padding-right: 2px !important;
        min-height: 28px !important;
        padding-top: 2px !important;
        padding-bottom: 2px !important;
      }
      .ro-month-field ::ng-deep .mat-mdc-form-field-icon-suffix {
        flex-shrink: 0;
        align-self: center;
      }
      .ro-month-field ::ng-deep input.mat-mdc-input-element {
        text-overflow: ellipsis;
        min-width: 0;
      }
      /* Inside shared Select range outline — one visual control */
      .ro-month-field--unified ::ng-deep .mdc-notched-outline,
      .ro-month-field--unified ::ng-deep .mdc-notched-outline__leading,
      .ro-month-field--unified ::ng-deep .mdc-notched-outline__notch,
      .ro-month-field--unified ::ng-deep .mdc-notched-outline__trailing {
        border: none !important;
      }
      .ro-month-field--unified ::ng-deep .mat-mdc-text-field-wrapper {
        padding-top: 0;
        padding-bottom: 0;
      }
      .ro-month-field--unified.mat-focused ::ng-deep .mat-mdc-notch-piece,
      .ro-month-field--unified ::ng-deep .mat-mdc-notch-piece {
        border-color: transparent !important;
      }
    `,
  ],
})
export class RoMonthYearFieldComponent implements OnChanges, OnInit {
  /** First day of month (from) or any day in end month (to) — parent canonical range. */
  @Input({ required: true }) value!: Date;
  /** When true, emit last day of the selected month. */
  @Input() endOfMonth = false;
  @Input() fieldId = 'ro';
  @Input() maxDate: Date | null = null;
  /** First month that may be selected (first day of that month). */
  @Input() minDate: Date | null = null;
  /** When true, outline is hidden — use inside `.ro-range-card` with a single outer border. */
  @Input() hideOutline = false;

  @HostBinding('class.ro-month-host--unified')
  get unifiedHost(): boolean {
    return this.hideOutline;
  }

  @Output() valueChange = new EventEmitter<Date>();

  pickerModel = new Date();

  ngOnInit(): void {
    this.syncPickerModelFromValue();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['value']) {
      this.syncPickerModelFromValue();
    }
  }

  private syncPickerModelFromValue(): void {
    if (!this.value) {
      return;
    }
    const d = this.value;
    this.pickerModel = new Date(d.getFullYear(), d.getMonth(), 1);
  }

  onMonthSelected(event: Date, dp: MatDatepicker<Date>): void {
    const y = event.getFullYear();
    const m = event.getMonth();
    const out = this.endOfMonth ? new Date(y, m + 1, 0) : new Date(y, m, 1);
    this.pickerModel = new Date(y, m, 1);
    this.valueChange.emit(out);
    dp.close();
  }
}
