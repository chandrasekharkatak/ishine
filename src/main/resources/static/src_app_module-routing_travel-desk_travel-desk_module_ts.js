"use strict";
(self["webpackChunkemployee_portal_revamp"] = self["webpackChunkemployee_portal_revamp"] || []).push([["src_app_module-routing_travel-desk_travel-desk_module_ts"],{

/***/ 264:
/*!**************************************************************************!*\
  !*** ./src/app/module-routing/travel-desk/travel-desk-routing.module.ts ***!
  \**************************************************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   TravelDeskRoutingModule: () => (/* binding */ TravelDeskRoutingModule)
/* harmony export */ });
/* harmony import */ var _angular_router__WEBPACK_IMPORTED_MODULE_6__ = __webpack_require__(/*! @angular/router */ 18431);
/* harmony import */ var src_app_travel_allowance_my_travelrequest_my_travelrequest_component__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! src/app/travel-allowance/my-travelrequest/my-travelrequest.component */ 30360);
/* harmony import */ var src_app_travel_allowance_total_travelrequest_total_travelrequest_component__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! src/app/travel-allowance/total-travelrequest/total-travelrequest.component */ 98978);
/* harmony import */ var src_app_travel_allowance_travel_allowance_component__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! src/app/travel-allowance/travel-allowance.component */ 59524);
/* harmony import */ var src_app_travel_allowance_travelrequestapproval_travelrequestapproval_component__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! src/app/travel-allowance/travelrequestapproval/travelrequestapproval.component */ 73162);
/* harmony import */ var src_app_travel_allowance_view_travelrequest_view_travelrequest_component__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! src/app/travel-allowance/view-travelrequest/view-travelrequest.component */ 72470);
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_5__ = __webpack_require__(/*! @angular/core */ 37580);








const routes = [{
  path: '',
  component: src_app_travel_allowance_travel_allowance_component__WEBPACK_IMPORTED_MODULE_2__.TravelAllowanceComponent,
  children: [{
    path: '',
    redirectTo: 'my-travelrequest',
    pathMatch: 'full'
  }, {
    path: 'my-travelrequest',
    component: src_app_travel_allowance_my_travelrequest_my_travelrequest_component__WEBPACK_IMPORTED_MODULE_0__.MyTravelrequestComponent
  }, {
    path: 'view-travelrequest',
    component: src_app_travel_allowance_view_travelrequest_view_travelrequest_component__WEBPACK_IMPORTED_MODULE_4__.ViewTravelrequestComponent
  }, {
    path: 'approve-travelrequest',
    component: src_app_travel_allowance_travelrequestapproval_travelrequestapproval_component__WEBPACK_IMPORTED_MODULE_3__.TravelrequestapprovalComponent
  }, {
    path: 'total-travelrequest',
    component: src_app_travel_allowance_total_travelrequest_total_travelrequest_component__WEBPACK_IMPORTED_MODULE_1__.TotalTravelrequestComponent
  }]
}];
class TravelDeskRoutingModule {
  static {
    this.ɵfac = function TravelDeskRoutingModule_Factory(__ngFactoryType__) {
      return new (__ngFactoryType__ || TravelDeskRoutingModule)();
    };
  }
  static {
    this.ɵmod = /*@__PURE__*/_angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵdefineNgModule"]({
      type: TravelDeskRoutingModule
    });
  }
  static {
    this.ɵinj = /*@__PURE__*/_angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵdefineInjector"]({
      imports: [_angular_router__WEBPACK_IMPORTED_MODULE_6__.RouterModule.forChild(routes), _angular_router__WEBPACK_IMPORTED_MODULE_6__.RouterModule]
    });
  }
}
(function () {
  (typeof ngJitMode === "undefined" || ngJitMode) && _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵsetNgModuleScope"](TravelDeskRoutingModule, {
    imports: [_angular_router__WEBPACK_IMPORTED_MODULE_6__.RouterModule],
    exports: [_angular_router__WEBPACK_IMPORTED_MODULE_6__.RouterModule]
  });
})();

/***/ }),

/***/ 13817:
/*!******************************************************************!*\
  !*** ./src/app/module-routing/travel-desk/travel-desk.module.ts ***!
  \******************************************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   TravelDeskModule: () => (/* binding */ TravelDeskModule)
/* harmony export */ });
/* harmony import */ var _angular_common__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! @angular/common */ 84460);
/* harmony import */ var _travel_desk_routing_module__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ./travel-desk-routing.module */ 264);
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @angular/core */ 37580);



class TravelDeskModule {
  static {
    this.ɵfac = function TravelDeskModule_Factory(__ngFactoryType__) {
      return new (__ngFactoryType__ || TravelDeskModule)();
    };
  }
  static {
    this.ɵmod = /*@__PURE__*/_angular_core__WEBPACK_IMPORTED_MODULE_1__["ɵɵdefineNgModule"]({
      type: TravelDeskModule
    });
  }
  static {
    this.ɵinj = /*@__PURE__*/_angular_core__WEBPACK_IMPORTED_MODULE_1__["ɵɵdefineInjector"]({
      imports: [_angular_common__WEBPACK_IMPORTED_MODULE_2__.CommonModule, _travel_desk_routing_module__WEBPACK_IMPORTED_MODULE_0__.TravelDeskRoutingModule]
    });
  }
}
(function () {
  (typeof ngJitMode === "undefined" || ngJitMode) && _angular_core__WEBPACK_IMPORTED_MODULE_1__["ɵɵsetNgModuleScope"](TravelDeskModule, {
    imports: [_angular_common__WEBPACK_IMPORTED_MODULE_2__.CommonModule, _travel_desk_routing_module__WEBPACK_IMPORTED_MODULE_0__.TravelDeskRoutingModule]
  });
})();

/***/ })

}]);
//# sourceMappingURL=src_app_module-routing_travel-desk_travel-desk_module_ts.js.map