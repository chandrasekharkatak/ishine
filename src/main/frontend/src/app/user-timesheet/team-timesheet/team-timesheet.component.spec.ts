import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TeamTimesheetComponent } from './team-timesheet.component';

describe('TeamTimesheetComponent', () => {
  let component: TeamTimesheetComponent;
  let fixture: ComponentFixture<TeamTimesheetComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ TeamTimesheetComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TeamTimesheetComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
