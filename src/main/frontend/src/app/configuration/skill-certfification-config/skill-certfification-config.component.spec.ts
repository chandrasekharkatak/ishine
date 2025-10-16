import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SkillCertfificationConfigComponent } from './skill-certfification-config.component';

describe('SkillCertfificationConfigComponent', () => {
  let component: SkillCertfificationConfigComponent;
  let fixture: ComponentFixture<SkillCertfificationConfigComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SkillCertfificationConfigComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SkillCertfificationConfigComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
