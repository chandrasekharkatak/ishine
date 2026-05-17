"use strict";
(self["webpackChunkemployee_portal_revamp"] = self["webpackChunkemployee_portal_revamp"] || []).push([["src_app_module-routing_rewards-tab_rewards-tab_module_ts"],{

/***/ 61413:
/*!******************************************************************!*\
  !*** ./src/app/module-routing/rewards-tab/rewards-tab.module.ts ***!
  \******************************************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   RewardsTabModule: () => (/* binding */ RewardsTabModule)
/* harmony export */ });
/* harmony import */ var _angular_common__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! @angular/common */ 84460);
/* harmony import */ var _rewards_tab_routing_module__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ./rewards-tab-routing.module */ 88068);
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @angular/core */ 37580);



class RewardsTabModule {
  static {
    this.ɵfac = function RewardsTabModule_Factory(__ngFactoryType__) {
      return new (__ngFactoryType__ || RewardsTabModule)();
    };
  }
  static {
    this.ɵmod = /*@__PURE__*/_angular_core__WEBPACK_IMPORTED_MODULE_1__["ɵɵdefineNgModule"]({
      type: RewardsTabModule
    });
  }
  static {
    this.ɵinj = /*@__PURE__*/_angular_core__WEBPACK_IMPORTED_MODULE_1__["ɵɵdefineInjector"]({
      imports: [_angular_common__WEBPACK_IMPORTED_MODULE_2__.CommonModule, _rewards_tab_routing_module__WEBPACK_IMPORTED_MODULE_0__.RewardsTabRoutingModule]
    });
  }
}
(function () {
  (typeof ngJitMode === "undefined" || ngJitMode) && _angular_core__WEBPACK_IMPORTED_MODULE_1__["ɵɵsetNgModuleScope"](RewardsTabModule, {
    imports: [_angular_common__WEBPACK_IMPORTED_MODULE_2__.CommonModule, _rewards_tab_routing_module__WEBPACK_IMPORTED_MODULE_0__.RewardsTabRoutingModule]
  });
})();

/***/ }),

/***/ 88068:
/*!**************************************************************************!*\
  !*** ./src/app/module-routing/rewards-tab/rewards-tab-routing.module.ts ***!
  \**************************************************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   RewardsTabRoutingModule: () => (/* binding */ RewardsTabRoutingModule)
/* harmony export */ });
/* harmony import */ var _angular_router__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! @angular/router */ 18431);
/* harmony import */ var src_app_rewards_appreciation_appreciation_component__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! src/app/rewards/appreciation/appreciation.component */ 58483);
/* harmony import */ var src_app_rewards_rewards_and_recognisation_rewards_and_recognisation_component__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! src/app/rewards/rewards-and-recognisation/rewards-and-recognisation.component */ 19979);
/* harmony import */ var src_app_rewards_rewards_component__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! src/app/rewards/rewards.component */ 2552);
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! @angular/core */ 37580);






const routes = [{
  path: '',
  component: src_app_rewards_rewards_component__WEBPACK_IMPORTED_MODULE_2__.RewardsComponent,
  children: [{
    path: 'rewards-and-recognisation',
    component: src_app_rewards_rewards_and_recognisation_rewards_and_recognisation_component__WEBPACK_IMPORTED_MODULE_1__.RewardsAndRecognisationComponent
  }, {
    path: 'rewardsappreciation',
    component: src_app_rewards_appreciation_appreciation_component__WEBPACK_IMPORTED_MODULE_0__.AppreciationComponent
  }]
}];
class RewardsTabRoutingModule {
  static {
    this.ɵfac = function RewardsTabRoutingModule_Factory(__ngFactoryType__) {
      return new (__ngFactoryType__ || RewardsTabRoutingModule)();
    };
  }
  static {
    this.ɵmod = /*@__PURE__*/_angular_core__WEBPACK_IMPORTED_MODULE_3__["ɵɵdefineNgModule"]({
      type: RewardsTabRoutingModule
    });
  }
  static {
    this.ɵinj = /*@__PURE__*/_angular_core__WEBPACK_IMPORTED_MODULE_3__["ɵɵdefineInjector"]({
      imports: [_angular_router__WEBPACK_IMPORTED_MODULE_4__.RouterModule.forChild(routes), _angular_router__WEBPACK_IMPORTED_MODULE_4__.RouterModule]
    });
  }
}
(function () {
  (typeof ngJitMode === "undefined" || ngJitMode) && _angular_core__WEBPACK_IMPORTED_MODULE_3__["ɵɵsetNgModuleScope"](RewardsTabRoutingModule, {
    imports: [_angular_router__WEBPACK_IMPORTED_MODULE_4__.RouterModule],
    exports: [_angular_router__WEBPACK_IMPORTED_MODULE_4__.RouterModule]
  });
})();

/***/ })

}]);
//# sourceMappingURL=src_app_module-routing_rewards-tab_rewards-tab_module_ts.js.map