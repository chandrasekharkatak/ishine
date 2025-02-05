import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Employee360ProjectComponent } from './employee360-project.component';

describe('Employee360ProjectComponent', () => {
  let component: Employee360ProjectComponent;
  let fixture: ComponentFixture<Employee360ProjectComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ Employee360ProjectComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(Employee360ProjectComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
