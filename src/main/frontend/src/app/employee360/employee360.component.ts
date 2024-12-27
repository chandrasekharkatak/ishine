import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-employee360',
  templateUrl: './employee360.component.html',
  styleUrls: ['./employee360.component.css']
})
export class Employee360Component implements OnInit {

  currentab:String;
  leave:String;
  othertab:any;
  constructor() { }

  ngOnInit(): void {
  }
 
  leave360viewtab(tab:any){  
          this.currentab=tab;        
          console.log(this.currentab);
  }
}
