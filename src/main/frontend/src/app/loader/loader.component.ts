import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { LoaderService } from '../services/loader.service';

@Component({
  selector: 'app-loader',
  templateUrl: './loader.component.html',
  styleUrls: ['./loader.component.css']
})
export class LoaderComponent implements OnInit {

  showLoader = false;



  constructor(private loaderService: LoaderService, private cdRef: ChangeDetectorRef) { }

  ngOnInit(): void {
    this.init();
  }

  init() {

    this.loaderService.getSpinnerObserver().subscribe((status) => {
      this.showLoader = (status === 'start');
      this.cdRef.detectChanges();
    });
  }


}
