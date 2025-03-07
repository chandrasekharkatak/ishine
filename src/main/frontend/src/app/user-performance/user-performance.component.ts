import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, Params } from '@angular/router';
@Component({
  selector: 'app-user-performance',
  templateUrl: './user-performance.component.html',
  styleUrls: ['./user-performance.component.css']
})
export class UserPerformanceComponent implements OnInit {

  tabName:any = 'Performance dashboard';
  activeTab: string = 'performance-dashboard';
  constructor(
    private router: Router,
    private route: ActivatedRoute,
  ) { }

  ngOnInit(): void {
    if (!this.route.firstChild) {
      this.router.navigate(['performance-dashboard'], { relativeTo: this.route });
    }    
    
  }

  ngOnDestroy(): void {
  }

  

 
  
}
