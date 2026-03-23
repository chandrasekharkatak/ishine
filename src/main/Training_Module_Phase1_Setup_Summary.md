# Training Module Phase-1 - Setup Summary

## ✅ Completed Tasks

### 1. Feature IDs Configuration
- **Status**: ✅ Complete
- **File**: `java/com/apmosys/employeeportal/controller/TrainingController.java`
- **Feature ID Used**: `64` (Training Config)
- **Note**: Feature ID 64 is used as a placeholder. Please verify this ID exists in your `feature_master` table. If not, either:
  - Create feature ID 64 using the SQL script in `Training_Module_Phase1_Feature_Mapping_Setup.md`
  - OR use the next available feature ID and update all `@JobRoleAccess(featureIds = {64})` annotations in `TrainingController.java`

### 2. File Storage Configuration
- **Status**: ✅ Complete
- **Files Updated**:
  - `resources/application-local.properties`
  - `resources/application-dev.properties`
  - `resources/application-uat.properties`
  - `resources/application-prod.properties`
- **Property Added**: `file.location.documents.training`
- **Default Paths**:
  - Local: `D:/documents/training/`
  - Dev: `/home/apmosys/Prasad/Employee_Portal_Workspace/documents/training/`
  - UAT: `/home/apmosys/tomcat-app/tomcat-emp/webapps/documents/training`
  - Prod: `/data/tomcat-app/tomcat-emp/webapps/documents/training`

**⚠️ Action Required**: 
- Create the training directory on the server if it doesn't exist
- Ensure the application user has read/write permissions on the directory

### 3. Feature Mapping Setup
- **Status**: ✅ Documentation Created
- **File**: `Training_Module_Phase1_Feature_Mapping_Setup.md`
- **Contains**: Complete SQL scripts for:
  - Creating feature master entry
  - Creating sub-feature master entries
  - Mapping features to job roles
  - Verification queries

**⚠️ Action Required**: 
- Execute SQL scripts from `Training_Module_Phase1_Feature_Mapping_Setup.md` in your database
- Map Training Config feature to appropriate HR roles (e.g., HR Manager)

### 4. Service Implementation
- **Status**: ✅ Complete
- **Files**:
  - `java/com/apmosys/employeeportal/serviceInterface/TrainingService.java` - Added `downloadContent` method
  - `java/com/apmosys/employeeportal/service/TrainingServiceImpl.java` - Implemented `downloadContent` method with file security checks

### 5. Controller Updates
- **Status**: ✅ Complete
- **File**: `java/com/apmosys/employeeportal/controller/TrainingController.java`
- **Changes**:
  - All `@JobRoleAccess` annotations updated with feature ID 64
  - `downloadContent` method fixed to properly use service response
  - File upload helper method implemented

## 📋 Pre-Deployment Checklist

### Database Setup
- [ ] Verify/Create Feature ID 64 in `feature_master` table
- [ ] Create sub-feature master entries (9 sub-features)
- [ ] Map Training Config feature to HR Manager role (or appropriate role)
- [ ] Map all sub-features to the role
- [ ] Verify mappings using provided SQL queries

### File System Setup
- [ ] Create training directory on server:
  - Local: `D:/documents/training/`
  - Dev: `/home/apmosys/Prasad/Employee_Portal_Workspace/documents/training/`
  - UAT: `/home/apmosys/tomcat-app/tomcat-emp/webapps/documents/training`
  - Prod: `/data/tomcat-app/tomcat-emp/webapps/documents/training`
- [ ] Set appropriate permissions (read/write for application user)
- [ ] Test directory creation (application will auto-create subdirectories per training)

### Application Configuration
- [ ] Verify `file.location.documents.training` property in all environment properties files
- [ ] Restart application after configuration changes
- [ ] Clear user sessions or have users log out/in to refresh feature mappings

### Testing Checklist
- [ ] **HR Configuration**:
  - [ ] Create a training
  - [ ] Update a training
  - [ ] View all trainings
  - [ ] Add training content (PPT/Video/Audio/Link)
  - [ ] Update training content
  - [ ] View training content
  - [ ] Deactivate a training
  - [ ] View training history
  - [ ] Generate compliance report

- [ ] **Employee Training**:
  - [ ] Login as employee with pending mandatory training
  - [ ] Verify lock screen appears
  - [ ] View training content
  - [ ] Timer functionality (if min time configured)
  - [ ] Submit consent
  - [ ] Skip training (if allowed)
  - [ ] Verify lock is released after consent
  - [ ] Verify next training appears after completing current one

- [ ] **Lock Enforcement**:
  - [ ] Verify API calls are blocked when locked (except training APIs)
  - [ ] Verify redirect to training page when locked
  - [ ] Verify lock status on login

## 🔧 Troubleshooting

### Issue: Feature not showing in Configuration tab
**Solution**: 
1. Verify feature_master entry exists: `SELECT * FROM feature_master WHERE feature_id = 64;`
2. Check role_feature_map: `SELECT * FROM role_feature_map WHERE feature_id = 64;`
3. Have user log out and log back in

### Issue: 403 Forbidden on Training APIs
**Solution**:
1. Verify feature ID in database matches controller annotations
2. Check user's role has feature mapped
3. Verify sub-features are mapped to role

### Issue: File upload fails
**Solution**:
1. Verify directory exists and has write permissions
2. Check `file.location.documents.training` property value
3. Check application logs for detailed error

### Issue: Lock not working
**Solution**:
1. Verify training has `lock_enabled = 'true'`
2. Verify training has `mandatory_flag = 'true'`
3. Verify training is active (`active_status = 'true'`)
4. Check `EmployeePortalInterceptor` whitelist includes training APIs
5. Verify `getLockStatus` is called in `EmployeeService.getEmployeeInfoOnLogin`

## 📝 Notes

1. **Feature ID**: Feature ID 64 is used as a placeholder. Verify availability before deployment.
2. **File Storage**: Files are stored in `{trainingFileLocation}/{trainingId}/content_{timestamp}_{originalFilename}` format
3. **Lock Mechanism**: Lock is enforced at API level via `EmployeePortalInterceptor` and at route level via `AuthGuard`
4. **Content Management**: Only one active content per training at a time (based on effective dates)
5. **Consent Tracking**: Consent is tracked per content per cycle, ensuring employees complete new content when it changes

## 🚀 Deployment Steps

1. **Backend**:
   - Deploy updated Java code
   - Run database migration scripts (if any)
   - Execute feature mapping SQL scripts
   - Create training file storage directory
   - Update application properties
   - Restart application

2. **Frontend**:
   - Build Angular application
   - Deploy to web server
   - Clear browser cache

3. **Post-Deployment**:
   - Test HR configuration features
   - Test employee training flow
   - Verify lock enforcement
   - Monitor application logs for errors

## 📞 Support

For issues or questions:
1. Check application logs for detailed error messages
2. Verify database entries using SQL queries in feature mapping document
3. Test API endpoints directly using Postman/curl
4. Check browser console for frontend errors
