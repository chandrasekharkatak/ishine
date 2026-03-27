import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ExcelDownloadHelperComponent } from './excel-download-helper.component';

describe('ExcelDownloadHelperComponent', () => {
  let component: ExcelDownloadHelperComponent;
  let fixture: ComponentFixture<ExcelDownloadHelperComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ExcelDownloadHelperComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ExcelDownloadHelperComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
