import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RmgProjectComponent } from './rmg-project-config.component';

describe('RmgProjectComponent', () => {
  let component: RmgProjectComponent;
  let fixture: ComponentFixture<RmgProjectComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RmgProjectComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RmgProjectComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
