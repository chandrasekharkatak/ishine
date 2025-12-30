import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TimesheetTimeEntryComponent } from './timesheet-time-entry.component';

describe('TimesheetTimeEntryComponent', () => {
  let component: TimesheetTimeEntryComponent;
  let fixture: ComponentFixture<TimesheetTimeEntryComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ TimesheetTimeEntryComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TimesheetTimeEntryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

