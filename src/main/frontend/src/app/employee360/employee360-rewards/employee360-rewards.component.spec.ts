import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Employee360RewardsComponent } from './employee360-rewards.component';

describe('Employee360RewardsComponent', () => {
  let component: Employee360RewardsComponent;
  let fixture: ComponentFixture<Employee360RewardsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ Employee360RewardsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(Employee360RewardsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
