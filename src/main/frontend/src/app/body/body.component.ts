import { Component, Input, OnInit } from '@angular/core';
import { User } from '../models/user';
import { AuthenticationService } from '../services/authentication.service';

@Component({
  selector: 'app-body',
  templateUrl: './body.component.html',
  styleUrls: ['./body.component.css']
})
export class BodyComponent implements OnInit {

  @Input() collapsed = false;
  @Input() screenWidth = 0;
  currentUser:User = new User();

  constructor(private authenticationService: AuthenticationService){
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  ngOnInit(): void {
  }

  getBodyClass(): string{
    let styleClass = '';

    if(this.collapsed && this.screenWidth > 768){
      if(this.currentUser) styleClass= 'body-trimmed body--active';
      else styleClass= 'body-trimmed';
    }else if(this.collapsed && this.screenWidth <= 768 && this.screenWidth > 0){
      if(this.currentUser) styleClass= 'body-md-screen body--active';
      else styleClass= 'body-md-screen';
    }
    else{
      if(this.currentUser) styleClass= 'body--active';
    }
    return styleClass;
  }

  getNavClass(): string{
    let styleClass = '';
    if(this.collapsed && this.screenWidth > 768){
      styleClass= 'navbar-trimmed'
    }else if(this.collapsed && this.screenWidth <= 768 && this.screenWidth > 0){
      styleClass= 'navbar-md-screen'
    }
    return styleClass;
  }

}
