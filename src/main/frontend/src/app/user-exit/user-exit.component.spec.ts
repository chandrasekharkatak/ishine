import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UserExitComponent } from './user-exit.component';

describe('UserExitComponent', () => {
  let component: UserExitComponent;
  let fixture: ComponentFixture<UserExitComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ UserExitComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(UserExitComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
