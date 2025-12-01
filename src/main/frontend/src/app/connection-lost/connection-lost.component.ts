import { Component, OnInit, OnDestroy, ViewChild, ElementRef, AfterViewInit } from '@angular/core';
import { Router } from '@angular/router';
import { Location } from '@angular/common';

@Component({
  standalone: false,
  selector: 'app-connection-lost',
  templateUrl: './connection-lost.component.html',
  styleUrls: ['./connection-lost.component.css']
})
export class ConnectionLostComponent implements OnInit, OnDestroy {
  // @ViewChild('sparkSvg') sparkSvg!: ElementRef<SVGSVGElement>;
  currentTime: string = '';
  private timeInterval: any;
  private sparkTimeout: any;

  constructor(private router: Router,private location: Location) {}
  ngAfterViewInit(): void {
    throw new Error('Method not implemented.');
  }

ngOnInit(): void {
  this.updateTime();
  this.timeInterval = setInterval(() => this.updateTime(), 1000);
  this.startSparks(); 
}

ngOnDestroy(): void {
  if (this.timeInterval) clearInterval(this.timeInterval);
  if (this.sparkTimeout) clearTimeout(this.sparkTimeout); 
}

private startSparks(): void {
  const generateSpark = () => {
    this.createSpark();

    const nextDelay = Math.random() * 400 + 200;
    this.sparkTimeout = setTimeout(generateSpark, nextDelay);
  };

  generateSpark(); 
}

  updateTime(): void {
    const now = new Date();
    this.currentTime = now.toLocaleTimeString();
  }

  goHome(): void {
    this.router.navigate(['/']);
  }
goBack(): void {
  if (window.history.length > 1) {
    this.location.back();
  } else {
    window.location.href = '/';
  }
}

private createSpark(): void {
  const svg = document.querySelector('svg');
  if (!svg) return;

  const spark = document.createElementNS('http://www.w3.org/2000/svg', 'circle');
  spark.setAttribute('class', 'spark');
  spark.setAttribute('cx', '200');
  spark.setAttribute('cy', '80');
  spark.setAttribute('r', (Math.random() * 2 + 1).toString());

  const angle = Math.random() * Math.PI * 2;
  const distance = Math.random() * 40 + 30; // spread radius
  const tx = Math.cos(angle) * distance;
  const ty = Math.sin(angle) * distance;

  spark.style.setProperty('--tx', `${tx}px`);
  spark.style.setProperty('--ty', `${ty}px`);
  spark.style.animationDelay = `${(Math.random() * 0.3).toFixed(2)}s`;

  svg.appendChild(spark);

  // remove spark after animation completes
  setTimeout(() => spark.remove(), 1200);
}

}
