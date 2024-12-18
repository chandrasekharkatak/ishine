import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Employee360AppreciationComponent } from './employee360-appreciation.component';

describe('Employee360AppreciationComponent', () => {
  let component: Employee360AppreciationComponent;
  let fixture: ComponentFixture<Employee360AppreciationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ Employee360AppreciationComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(Employee360AppreciationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
