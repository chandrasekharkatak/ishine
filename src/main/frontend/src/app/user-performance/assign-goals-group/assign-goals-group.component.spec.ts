import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AssignGoalsGroupComponent } from './assign-goals-group.component';

describe('AssignGoalsGroupComponent', () => {
  let component: AssignGoalsGroupComponent;
  let fixture: ComponentFixture<AssignGoalsGroupComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AssignGoalsGroupComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AssignGoalsGroupComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
