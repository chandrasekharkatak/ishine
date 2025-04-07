import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Employee360LeaveComponent } from './employee360-leave.component';

describe('Employee360LeaveComponent', () => {
  let component: Employee360LeaveComponent;
  let fixture: ComponentFixture<Employee360LeaveComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ Employee360LeaveComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(Employee360LeaveComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
