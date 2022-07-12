import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'app-user-team',
  templateUrl: './user-team.component.html',
  styleUrls: ['./user-team.component.css']
})
export class UserTeamComponent implements OnInit {


  constructor(
    private router: Router,
    private route: ActivatedRoute,
  ) { }

  ngOnInit(): void {
  }

  ngAfterViewInit(): void {
    this.setActiveTab();
  }

  ngOnDestroy(): void {
    this.removeActiveTab();
  }

  setActiveTab(){
    const tab = document.getElementById('teamTab').querySelector('.nav-link');
    console.log(tab);

    tab.classList.add('active');
    let activeRouteLink = tab.getAttribute('routerLink');
    console.log("activeRouteLink :", activeRouteLink);
    console.log("Router :",  this.router);
    
    this.router.navigate(['./'+activeRouteLink], {relativeTo: this.route});
  }

  removeActiveTab(){
    const tab = document.getElementById('teamTab').querySelector('.nav-link.active');
    console.log("active tab :", tab);
    tab?.classList.remove('active');
  }

}


