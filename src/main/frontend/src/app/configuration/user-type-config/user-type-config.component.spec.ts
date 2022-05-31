import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UserTypeConfigComponent } from './user-type-config.component';

describe('UserTypeConfigComponent', () => {
  let component: UserTypeConfigComponent;
  let fixture: ComponentFixture<UserTypeConfigComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ UserTypeConfigComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(UserTypeConfigComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
