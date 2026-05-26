import { Component, EventEmitter, HostListener, OnDestroy, OnInit, Output,ElementRef } from '@angular/core';
import { Subscription } from 'rxjs';
import { User } from '../models/user';
import { AuthenticationService } from '../services/authentication.service';
import { navbarData,navbarItemData  } from './nav-data';
import { enableAppreciation } from '../models/enableAppreciation';
import * as moment from 'moment';
import { BreadcrumbService } from '../services/breadcrumb.service';
import { Router } from '@angular/router';

interface SideNavToggle{
  screenWidth: number;
  collapsed: boolean;
}


export interface NavGroup {
  groupLabel: string;
  groupIcon: string;
  items: any[];
  isExpanded: boolean;
}
@Component({
  standalone: false,
  selector: 'app-sidenav',
  templateUrl: './sidenav.component.html',
  styleUrls: ['./sidenav.component.css']
})
export class SidenavComponent implements OnInit, OnDestroy {

  @Output() onToggleSideNav: EventEmitter<SideNavToggle> = new EventEmitter();
  collapsed=false;
  screenWidth = 0;
  navData=navbarData;
  currentUser:User;
  firstTimeLogin="false";
  menuItems:any;
  menuItems1:any;
  appreciationEventInfo:enableAppreciation;

  private activatedSubscriptions:Subscription;

   groupedMenuItems: NavGroup[] = [];// The primary processed array that our HTML template loops through
   hoveredGroup: NavGroup | null = null;//Remembers which folder icon is currently hovered over in small mode
    hoveredGroupTop: number = 0;//Tracks the exact vertical coordinate (height) of the hovered icon
private closeTimer: any = null;//Tiny stopwatch to prevent menus from vanishing instantly on accidental mouse slips

  @HostListener('window:resize', ['$event'])
  onResize(event:any){
    this.screenWidth = window.innerWidth;
    if(this.screenWidth <= 768){
      this.collapsed = false;
      this.onToggleSideNav.emit({collapsed: this.collapsed, screenWidth: this.screenWidth});
    }
  }
  // @HostListener('document:mousemove', ['$event'])
  // onGlobalMouseMove(event: MouseEvent): void {
  //   // Only run this execution logic if a popup panel is currently hovered/active and sidebar is collapsed
  //   if (!this.hoveredGroup || this.collapsed) {
  //     return;
  //   }

  //   const target = event.target as HTMLElement;
  //   const nativeEl = this.elementRef.nativeElement;

  //   // Check if the moving cursor is inside the core sidebar element OR over the position:fixed floating popup panel
  //   const isInsideComponent = nativeEl.contains(target) || target.closest('.floating-group-panel');

  //   if (!isInsideComponent) {
  //     // If the cursor is completely outside, trigger the close timer sequence if it's not already running
  //     if (!this.closeTimer) {
  //       this.closeTimer = setTimeout(() => {
  //         this.hoveredGroup = null;
  //         this.closeTimer = null;
  //       }, 120); // Aligns with your standard mouseleave delay
  //     }
  //   } else {
  //     // If the cursor moves back inside the safe zones, clear any active close countdowns immediately
  //     if (this.closeTimer) {
  //       clearTimeout(this.closeTimer);
  //       this.closeTimer = null;
  //     }
  //   }
  // }

  // CHANGED: Consolidated onPanelMouseLeave, onGroupMouseLeave, and onGlobalMouseMove into a single clean engine
@HostListener('document:mousemove', ['$event'])
  onGlobalMouseMove(event: MouseEvent): void {
    // Check if any panel is visible via hover or an explicit accordion expansion flag
    const hasActivePanel = this.hoveredGroup || this.groupedMenuItems.some(g => g.isExpanded);
    if (!hasActivePanel || this.collapsed) {
      return;
    }

    const target = event.target as HTMLElement;
    const nativeEl = this.elementRef.nativeElement;
    const isInsideComponent = nativeEl.contains(target) || target.closest('.floating-group-panel');

    if (!isInsideComponent) {
      if (!this.closeTimer) {
        this.closeTimer = setTimeout(() => {
          this.closeAllFloatingPanels();
        }, 120);
      }
    } else {
      if (this.closeTimer) {
        clearTimeout(this.closeTimer);
        this.closeTimer = null;
      }
    }
  }

  // CHANGED: Closes panels immediately if clicking anywhere outside the navbar area
  @HostListener('document:click', ['$event'])
  onGlobalClick(event: MouseEvent): void {
    const hasActivePanel = this.hoveredGroup || this.groupedMenuItems.some(g => g.isExpanded);
    if (!hasActivePanel || this.collapsed) {
      return;
    }

    const target = event.target as HTMLElement;
    const nativeEl = this.elementRef.nativeElement;
    const isInsideComponent = nativeEl.contains(target) || target.closest('.floating-group-panel');

    if (!isInsideComponent) {
      if (this.closeTimer) {
        clearTimeout(this.closeTimer);
        this.closeTimer = null;
      }
      this.closeAllFloatingPanels();
    }
  }

  // CHANGED: Added helper function to completely clear out popup states uniformly
  private closeAllFloatingPanels(): void {
    this.hoveredGroup = null;
    this.groupedMenuItems.forEach(g => g.isExpanded = false);
  }

  constructor(private authenticationService: AuthenticationService,
    private breadcrumbService: BreadcrumbService,
  private router: Router,
   private elementRef: ElementRef){
  }

   ngOnInit(): void {
    this.screenWidth = window.innerWidth;
    this.firstTimeLogin = sessionStorage.getItem('FirstTimeLogin');

    this.activatedSubscriptions = this.authenticationService.currentUser.subscribe(x => {
      this.currentUser = x;
      if (!x?.tabList?.length) {
        this.menuItems = [];
        this.appreciationEventInfo = x?.appreciationEventInfo;
        return;
      }
      this.menuItems = [...x.tabList];
      this.appreciationEventInfo = x.appreciationEventInfo;
      this.filterAppreciationTab();
       this.buildGroupedMenu();
    });
  }

  /** Hide Appreciation tab when disabled (same rules as before; runs whenever user / tabList updates). */
  private filterAppreciationTab(): void {
    if (!this.menuItems?.length || !this.currentUser) {
      return;
    }
    if (!this.appreciationEventInfo?.fromDate || !this.appreciationEventInfo?.toDate) {
      return;
    }
    const dateFormat = 'YYYY-MM-DD';
    const currentDate = moment(new Date()).format(dateFormat);
    const fromDate = this.appreciationEventInfo?.fromDate;
    const toDate = this.appreciationEventInfo?.toDate;
    const dateCheck = this.dateCheck(currentDate, fromDate, toDate);

    this.menuItems = this.menuItems.filter((item) => {
      if (item.tabName !== 'Appreciation') {
        return true;
      }
      if (this.currentUser.isAppreciationEnable !== true && dateCheck === false) {
        return false;
      }
      if (this.currentUser.isAppreciationEnable === false && dateCheck === true) {
        return false;
      }
      if (this.currentUser.isAppreciationEnable === true && dateCheck === false) {
        return false;
      }
      return true;
    });
  }

  private buildGroupedMenu(): void {
  const groupMap: { [label: string]: { items: any[], sequence: number, icon: string } } = {};

  console.log('menuItems from API:', this.menuItems);

  this.menuItems.forEach(item => {
    // tabGroup and groupSequence now come directly from API response
    const groupLabel    = item.tabGroup      || 'OTHER';
    const groupSequence = item.groupSequence ?? 999;
    const groupIcon = item.groupIcon || 'fa-solid fa-folder';

    if (!groupMap[groupLabel]) {
      groupMap[groupLabel] = { items: [], sequence: groupSequence, icon: groupIcon };
    }
    groupMap[groupLabel].items.push(item);
  });

  // Sort by groupSequence from DB — no hardcoded ORDER array needed
  this.groupedMenuItems = Object.entries(groupMap)
    .sort(([, a], [, b]) => a.sequence - b.sequence)
    .map(([groupLabel, { items, icon }]) => ({
      groupLabel,
      groupIcon: icon,
      items,
      isExpanded: false,

    }));
}

  // setHoveredGroup(group: NavGroup | null): void {
  //   this.hoveredGroup = group;
  // }
//   onGroupMouseEnter(group: NavGroup, el: HTMLElement): void {
//     if (this.closeTimer) { clearTimeout(this.closeTimer); this.closeTimer = null; }
//     this.groupedMenuItems.forEach(g => g.isExpanded = false);
//     this.hoveredGroup = group;
//     this.hoveredGroupTop = el.getBoundingClientRect().top;
// }

onGroupMouseEnter(group: NavGroup, el: HTMLElement): void {
    if (this.closeTimer) { clearTimeout(this.closeTimer); this.closeTimer = null; }
    this.groupedMenuItems.forEach(g => g.isExpanded = false);
    this.hoveredGroup = group;

    const rect = el.getBoundingClientRect();
    const windowHeight = window.innerHeight;

    // Calculate space needed based on item count (approx 45px per link + header padding)
    const estimatedPanelHeight = (group.items.length * 45) + 50;

    // If the panel will bleed off the bottom of the screen, push it up
    if (rect.top + estimatedPanelHeight > windowHeight) {
        const calculatedTop = windowHeight - estimatedPanelHeight - 16;
        // Ensure it doesn't push past the top of the viewport (0) if there are too many items
        this.hoveredGroupTop = calculatedTop < 10 ? 10 : calculatedTop;
    } else {
        // Standard position: aligns perfectly with the hovered sidebar icon
        this.hoveredGroupTop = rect.top;
    }
}

// onGroupMouseLeave(): void {
//     this.closeTimer = setTimeout(() => { this.hoveredGroup = null; }, 120);
// }

onPanelMouseEnter(): void {
    if (this.closeTimer) { clearTimeout(this.closeTimer); this.closeTimer = null; }
}

// onPanelMouseLeave(): void {
//     this.closeTimer = setTimeout(() => { this.hoveredGroup = null; }, 120);
// }

  //   toggleGroup(group: NavGroup): void {
  //      if (group.items.length === 1) return;
  //   group.isExpanded = !group.isExpanded;
  // }

 toggleGroup(group: NavGroup, el?: HTMLElement): void {
    group.isExpanded = !group.isExpanded;

    // Apply the exact same positioning math when clicked on mobile
    if (group.isExpanded && el) {
        const rect = el.getBoundingClientRect();
        const windowHeight = window.innerHeight;

        // Calculate space needed (approx 45px per link + mobile padding/gap)
        const estimatedPanelHeight = (group.items.length * 48) + 16;

        if (rect.top + estimatedPanelHeight > windowHeight) {
            const calculatedTop = windowHeight - estimatedPanelHeight - 12;
            this.hoveredGroupTop = calculatedTop < 10 ? 10 : calculatedTop;
        } else {
            this.hoveredGroupTop = rect.top;
        }
    }
}

  ngOnDestroy(): void {
    this.activatedSubscriptions?.unsubscribe();

  }


  toggleCollapse(){
    this.collapsed = !this.collapsed;
    this.onToggleSideNav.emit({collapsed: this.collapsed, screenWidth: this.screenWidth});
  }

  isGroupActive(group: NavGroup): boolean {
    if (!group || !group.items) {
      return false;
    }
    // Loop through nested items and check if the current active URL includes the item's tabRouteName
    return group.items.some(item => this.router.url.includes(item.tabRouteName));
  }

  closeSidenav(){
    this.collapsed = false;
    this.onToggleSideNav.emit({collapsed: this.collapsed, screenWidth: this.screenWidth});
  }
  dateCheck(currentDate,fromDate,toDate) {

    var fDate,tDate,cDate;
    fDate = Date.parse(fromDate);
    tDate = Date.parse(toDate);
    cDate = Date.parse(currentDate);

    if((cDate <= tDate && cDate >= fDate)) {
      //console.log("true date");
        return true;
    }
    else{
    //console.log("false date");
    return false;
    }
  }

  // onTabClick(){
  //   this.breadcrumbService.setBreadcrumbSubject(null)
  // }
  onTabClick(event: Event, targetGroup?: NavGroup): void {
    if (this.firstTimeLogin === 'true') {
      event.preventDefault();  // Prevent the click from triggering navigation
      event.stopPropagation();  // Prevent further propagation of the event
      console.log('Routing is disabled because firstTimeLogin is false');
    }else{
      this.breadcrumbService.setBreadcrumbSubject(null);

      //this.closeAllFloatingPanels();

      this.groupedMenuItems.forEach((group) => {
        if (group !== targetGroup) {
          group.isExpanded = false;
        }
      });

    }
  }
}
