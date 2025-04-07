import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AttendanceReconciliationComponent } from './attendance-reconciliation.component';

describe('AttendanceReconciliationComponent', () => {
  let component: AttendanceReconciliationComponent;
  let fixture: ComponentFixture<AttendanceReconciliationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AttendanceReconciliationComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AttendanceReconciliationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
