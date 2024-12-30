import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { BreadcrumbService } from 'src/app/services/breadcrumb.service';

@Component({
  selector: 'app-breadcrumb',
  templateUrl: './breadcrumb.component.html',
  styleUrls: ['./breadcrumb.component.css']
})
export class BreadcrumbComponent implements OnInit {

  //Boolean 
  isEmployee360Module: boolean = false;

  breadcrumbList:any[] = [
    {
      "title": "Home",
      "url": "/home",
      "subtab": "",
      "object": {
        }
    }
  ];
  displayedBreadcrumbs:any[] = [];

  constructor(
    private router: Router,
    private breadcrumbService: BreadcrumbService
  ) { }

  ngOnInit(): void {
    this.updateDisplayedBreadcrumbs();
    this.breadcrumbService.setBreadcrumbSubject(this.breadcrumbList);
    if(this.breadcrumbList.length > 1){
      this.isEmployee360Module = true;
    }
  }

  updateDisplayedBreadcrumbs() {
    console.log(JSON.parse(sessionStorage.getItem('breadcrumb')) + " :==========")
    this.breadcrumbList = JSON.parse(sessionStorage.getItem('breadcrumb')) != undefined ? JSON.parse(sessionStorage.getItem('breadcrumb')) : this.breadcrumbList;
    const maxVisible = 4;

    if (this.breadcrumbList.length > 10) {
      const lastItems = this.breadcrumbList.slice(-5);
      this.displayedBreadcrumbs = [
        this.breadcrumbList[0],
        { title: '...', url: '', subtab: '', object: {} },
        ...lastItems
      ];
    } else {
      this.displayedBreadcrumbs = [...this.breadcrumbList];
    }

    console.log(this.displayedBreadcrumbs, " : this.displayedBreadcrumbs");
  }

  navigateToSelectedTab(module: any, index: any){
    this.breadcrumbList.splice(index + 1);
    this.breadcrumbService.setBreadcrumbSubject(this.breadcrumbList);

    this.displayedBreadcrumbs = [...this.breadcrumbList];
    this.router.navigate([module.url], { queryParams: { }});

    if(index == 0 && module.title == "Exit Employee 360"){
      this.isEmployee360Module = false;
    }
  }

}
