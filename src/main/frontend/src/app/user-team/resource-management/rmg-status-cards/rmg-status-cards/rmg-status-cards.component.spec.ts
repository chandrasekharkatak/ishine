import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RmgStatusCardsComponent } from './rmg-status-cards.component';

describe('RmgStatusCardsComponent', () => {
  let component: RmgStatusCardsComponent;
  let fixture: ComponentFixture<RmgStatusCardsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RmgStatusCardsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RmgStatusCardsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
