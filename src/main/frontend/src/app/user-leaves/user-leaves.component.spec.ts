import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UserLeavesComponent } from './user-leaves.component';

describe('UserLeavesComponent', () => {
  let component: UserLeavesComponent;
  let fixture: ComponentFixture<UserLeavesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ UserLeavesComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(UserLeavesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
