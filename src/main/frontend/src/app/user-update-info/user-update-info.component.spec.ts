import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UserUpdateInfoComponent } from './user-update-info.component';

describe('UserUpdateInfoComponent', () => {
  let component: UserUpdateInfoComponent;
  let fixture: ComponentFixture<UserUpdateInfoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ UserUpdateInfoComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(UserUpdateInfoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
