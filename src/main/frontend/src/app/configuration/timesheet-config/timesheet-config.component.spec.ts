import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TimesheetConfigComponent } from './timesheet-config.component';

describe('TimesheetConfigComponent', () => {
  let component: TimesheetConfigComponent;
  let fixture: ComponentFixture<TimesheetConfigComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ TimesheetConfigComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TimesheetConfigComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
