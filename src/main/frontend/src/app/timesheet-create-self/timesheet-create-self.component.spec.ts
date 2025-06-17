import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TimesheetCreateSelfComponent } from './timesheet-create-self.component';

describe('TimesheetCreateSelfComponent', () => {
  let component: TimesheetCreateSelfComponent;
  let fixture: ComponentFixture<TimesheetCreateSelfComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ TimesheetCreateSelfComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TimesheetCreateSelfComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
