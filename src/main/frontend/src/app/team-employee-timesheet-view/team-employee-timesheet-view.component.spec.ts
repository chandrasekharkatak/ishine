import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TeamEmployeeTimesheetViewComponent } from './team-employee-timesheet-view.component';

describe('TeamEmployeeTimesheetViewComponent', () => {
  let component: TeamEmployeeTimesheetViewComponent;
  let fixture: ComponentFixture<TeamEmployeeTimesheetViewComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ TeamEmployeeTimesheetViewComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TeamEmployeeTimesheetViewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
