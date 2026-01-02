import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-date-time-picker',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './date-time-picker.component.html'
})
export class DateTimePickerComponent {

  @Input() label = '';
  @Input() required = false;
  @Input() disabled = false;
  @Input() mode: 'date' | 'time' | 'datetime' = 'date';

  /** emitted value */
  @Output() valueChange = new EventEmitter<string>();

  /* ---------------- HANDLERS ---------------- */

  onDateChange(val: string) {
    if (!val) return;

    // val = yyyy-MM-dd
    const [yyyy, mm, dd] = val.split('-');

    // Emit dd-MM-yyyy
    this.valueChange.emit(`${dd}-${mm}-${yyyy}`);
  }

  onTimeChange(val: string) {
    if (!val) return;

    // val = HH:mm
    this.valueChange.emit(val);
  }

  onDateTimeChange(val: string) {
    if (!val) return;

    // val = yyyy-MM-ddTHH:mm
    // Java LocalDateTime compatible
    this.valueChange.emit(val + ':00');
  }
}
