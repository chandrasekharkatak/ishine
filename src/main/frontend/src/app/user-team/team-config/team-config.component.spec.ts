import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TeamConfigComponent } from './team-config.component';

describe('TeamConfigComponent', () => {
  let component: TeamConfigComponent;
  let fixture: ComponentFixture<TeamConfigComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ TeamConfigComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TeamConfigComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
