import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UserReleasenotesComponent } from './user-releasenotes.component';

describe('UserReleasenotesComponent', () => {
  let component: UserReleasenotesComponent;
  let fixture: ComponentFixture<UserReleasenotesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ UserReleasenotesComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(UserReleasenotesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
