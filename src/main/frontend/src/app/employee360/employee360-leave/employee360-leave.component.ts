import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-employee360-leave',
  templateUrl: './employee360-leave.component.html',
  styleUrls: ['./employee360-leave.component.css']
})
export class Employee360LeaveComponent implements OnInit {

  employeeData: any;

  constructor(private router: Router) {
    const navigation = this.router.getCurrentNavigation();
    this.employeeData = navigation?.extras.state?.['employeeData'];
    console.log("cheked",this.employeeData); // Use the received data
  }

  ngOnInit(): void {
  }

}
