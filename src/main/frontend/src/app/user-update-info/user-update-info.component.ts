import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'app-user-update-info',
  templateUrl: './user-update-info.component.html',
  styleUrls: ['./user-update-info.component.css']
})
export class UserUpdateInfoComponent implements OnInit {

  constructor(
    private router: Router,
    private route: ActivatedRoute,  
  ) { }

  ngOnInit(): void {
    this.router.navigate(['./employee-info'], {relativeTo: this.route})
  }

}
