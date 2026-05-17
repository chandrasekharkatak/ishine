"use strict";
(self["webpackChunkemployee_portal_revamp"] = self["webpackChunkemployee_portal_revamp"] || []).push([["src_app_module-routing_update-info_update-info_module_ts"],{

/***/ 600:
/*!**************************************************************************!*\
  !*** ./src/app/module-routing/update-info/update-info-routing.module.ts ***!
  \**************************************************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   UpdateInfoRoutingModule: () => (/* binding */ UpdateInfoRoutingModule)
/* harmony export */ });
/* harmony import */ var _angular_router__WEBPACK_IMPORTED_MODULE_5__ = __webpack_require__(/*! @angular/router */ 18431);
/* harmony import */ var src_app_user_update_info_document_upload_document_upload_component__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! src/app/user-update-info/document-upload/document-upload.component */ 97527);
/* harmony import */ var src_app_user_update_info_employee_info_employee_info_component__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! src/app/user-update-info/employee-info/employee-info.component */ 94719);
/* harmony import */ var src_app_user_update_info_information_preview_information_preview_component__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! src/app/user-update-info/information-preview/information-preview.component */ 319);
/* harmony import */ var src_app_user_update_info_user_update_info_component__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! src/app/user-update-info/user-update-info.component */ 88530);
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! @angular/core */ 37580);







const routes = [{
  path: '',
  component: src_app_user_update_info_user_update_info_component__WEBPACK_IMPORTED_MODULE_3__.UserUpdateInfoComponent,
  children: [{
    path: 'employee-info',
    component: src_app_user_update_info_employee_info_employee_info_component__WEBPACK_IMPORTED_MODULE_1__.EmployeeInfoComponent
  }, {
    path: 'document-upload',
    component: src_app_user_update_info_document_upload_document_upload_component__WEBPACK_IMPORTED_MODULE_0__.DocumentUploadComponent
  }, {
    path: 'info-preview',
    component: src_app_user_update_info_information_preview_information_preview_component__WEBPACK_IMPORTED_MODULE_2__.InformationPreviewComponent
  }]
}];
class UpdateInfoRoutingModule {
  static {
    this.ɵfac = function UpdateInfoRoutingModule_Factory(__ngFactoryType__) {
      return new (__ngFactoryType__ || UpdateInfoRoutingModule)();
    };
  }
  static {
    this.ɵmod = /*@__PURE__*/_angular_core__WEBPACK_IMPORTED_MODULE_4__["ɵɵdefineNgModule"]({
      type: UpdateInfoRoutingModule
    });
  }
  static {
    this.ɵinj = /*@__PURE__*/_angular_core__WEBPACK_IMPORTED_MODULE_4__["ɵɵdefineInjector"]({
      imports: [_angular_router__WEBPACK_IMPORTED_MODULE_5__.RouterModule.forChild(routes), _angular_router__WEBPACK_IMPORTED_MODULE_5__.RouterModule]
    });
  }
}
(function () {
  (typeof ngJitMode === "undefined" || ngJitMode) && _angular_core__WEBPACK_IMPORTED_MODULE_4__["ɵɵsetNgModuleScope"](UpdateInfoRoutingModule, {
    imports: [_angular_router__WEBPACK_IMPORTED_MODULE_5__.RouterModule],
    exports: [_angular_router__WEBPACK_IMPORTED_MODULE_5__.RouterModule]
  });
})();

/***/ }),

/***/ 44809:
/*!******************************************************************!*\
  !*** ./src/app/module-routing/update-info/update-info.module.ts ***!
  \******************************************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   UpdateInfoModule: () => (/* binding */ UpdateInfoModule)
/* harmony export */ });
/* harmony import */ var _angular_common__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! @angular/common */ 84460);
/* harmony import */ var _update_info_routing_module__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ./update-info-routing.module */ 600);
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @angular/core */ 37580);



class UpdateInfoModule {
  static {
    this.ɵfac = function UpdateInfoModule_Factory(__ngFactoryType__) {
      return new (__ngFactoryType__ || UpdateInfoModule)();
    };
  }
  static {
    this.ɵmod = /*@__PURE__*/_angular_core__WEBPACK_IMPORTED_MODULE_1__["ɵɵdefineNgModule"]({
      type: UpdateInfoModule
    });
  }
  static {
    this.ɵinj = /*@__PURE__*/_angular_core__WEBPACK_IMPORTED_MODULE_1__["ɵɵdefineInjector"]({
      imports: [_angular_common__WEBPACK_IMPORTED_MODULE_2__.CommonModule, _update_info_routing_module__WEBPACK_IMPORTED_MODULE_0__.UpdateInfoRoutingModule]
    });
  }
}
(function () {
  (typeof ngJitMode === "undefined" || ngJitMode) && _angular_core__WEBPACK_IMPORTED_MODULE_1__["ɵɵsetNgModuleScope"](UpdateInfoModule, {
    imports: [_angular_common__WEBPACK_IMPORTED_MODULE_2__.CommonModule, _update_info_routing_module__WEBPACK_IMPORTED_MODULE_0__.UpdateInfoRoutingModule]
  });
})();

/***/ })

}]);
//# sourceMappingURL=src_app_module-routing_update-info_update-info_module_ts.js.map