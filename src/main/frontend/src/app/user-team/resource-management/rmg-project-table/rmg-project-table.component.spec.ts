import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RmgProjectTableComponent } from './rmg-project-table.component';

describe('RmgProjectTableComponent', () => {
  let component: RmgProjectTableComponent;
  let fixture: ComponentFixture<RmgProjectTableComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RmgProjectTableComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RmgProjectTableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
