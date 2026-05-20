import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';

export interface LegendEntry {
  code: string;
  label: string;
  color: string;
}

@Component({
  selector: 'app-calendar-legend',
  imports: [CommonModule],
  templateUrl: './calendar-legend.component.html',
  styleUrls: ['./calendar-legend.component.css']
})
export class CalendarLegendComponent {
  @Input() legendEntries: LegendEntry[] = [];

  constructor() { }
}

