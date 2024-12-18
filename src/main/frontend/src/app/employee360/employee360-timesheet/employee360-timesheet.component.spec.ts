import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Employee360TimesheetComponent } from './employee360-timesheet.component';

describe('Employee360TimesheetComponent', () => {
  let component: Employee360TimesheetComponent;
  let fixture: ComponentFixture<Employee360TimesheetComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ Employee360TimesheetComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(Employee360TimesheetComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
