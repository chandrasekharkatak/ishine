import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EdiTimesheetFormComponent } from './edi-timesheet-form.component';

describe('EdiTimesheetFormComponent', () => {
  let component: EdiTimesheetFormComponent;
  let fixture: ComponentFixture<EdiTimesheetFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ EdiTimesheetFormComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(EdiTimesheetFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
