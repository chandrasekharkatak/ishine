import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AclConfigComponent } from './acl-config.component';

describe('AclConfigComponent', () => {
  let component: AclConfigComponent;
  let fixture: ComponentFixture<AclConfigComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AclConfigComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AclConfigComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});