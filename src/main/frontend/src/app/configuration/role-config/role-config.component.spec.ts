import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RoleConfigComponent } from './role-config.component';

describe('RoleConfigComponent', () => {
  let component: RoleConfigComponent;
  let fixture: ComponentFixture<RoleConfigComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RoleConfigComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(RoleConfigComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
