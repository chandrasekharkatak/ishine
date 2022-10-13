import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UserAppreciationComponent } from './user-appreciation.component';

describe('UserAppreciationComponent', () => {
  let component: UserAppreciationComponent;
  let fixture: ComponentFixture<UserAppreciationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ UserAppreciationComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(UserAppreciationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
