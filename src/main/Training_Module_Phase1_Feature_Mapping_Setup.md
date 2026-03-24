# Training Module Phase-1 - Feature Mapping Setup Guide

## Overview
This document provides instructions for setting up feature mapping in the database to enable HR users to access the Training Configuration module.

## Prerequisites
- Database access to `db_emp_portal` (or your database name)
- Admin/HR role access to configure features
- Feature ID 64 should be available (or use next available feature ID)

## Step 1: Verify/Create Feature Master Entry

### Check if Feature ID 64 exists:
```sql
SELECT * FROM feature_master WHERE feature_id = 64;
```

### If Feature ID 64 doesn't exist, create it:
```sql
INSERT INTO feature_master (
    feature_id,
    feature_name,
    tab_name,
    tab_route_name,
    is_active,
    created_by,
    created_on
) VALUES (
    64,
    'Training Config',
    'Configuration',
    'configuration',
    'true',
    1, -- Replace with actual admin user ID
    NOW()
);
```

**Note:** If Feature ID 64 is already taken, use the next available feature ID and update `TrainingController.java` accordingly.

## Step 2: Create Sub-Features

### Create Sub-Feature Master entries for Training Config:

```sql
-- Sub-feature: Create Training
INSERT INTO sub_feature_master (
    sub_feature_id,
    feature_id,
    sub_feature_name,
    is_active,
    created_by,
    created_on
) VALUES (
    (SELECT COALESCE(MAX(sub_feature_id), 0) + 1 FROM sub_feature_master),
    64, -- Feature ID for Training Config
    'Create Training',
    'true',
    1, -- Replace with actual admin user ID
    NOW()
);

-- Sub-feature: Update Training
INSERT INTO sub_feature_master (
    sub_feature_id,
    feature_id,
    sub_feature_name,
    is_active,
    created_by,
    created_on
) VALUES (
    (SELECT COALESCE(MAX(sub_feature_id), 0) + 1 FROM sub_feature_master),
    64,
    'Update Training',
    'true',
    1,
    NOW()
);

-- Sub-feature: View All Trainings
INSERT INTO sub_feature_master (
    sub_feature_id,
    feature_id,
    sub_feature_name,
    is_active,
    created_by,
    created_on
) VALUES (
    (SELECT COALESCE(MAX(sub_feature_id), 0) + 1 FROM sub_feature_master),
    64,
    'View All Trainings',
    'true',
    1,
    NOW()
);

-- Sub-feature: Add Content
INSERT INTO sub_feature_master (
    sub_feature_id,
    feature_id,
    sub_feature_name,
    is_active,
    created_by,
    created_on
) VALUES (
    (SELECT COALESCE(MAX(sub_feature_id), 0) + 1 FROM sub_feature_master),
    64,
    'Add Content',
    'true',
    1,
    NOW()
);

-- Sub-feature: Update Content
INSERT INTO sub_feature_master (
    sub_feature_id,
    feature_id,
    sub_feature_name,
    is_active,
    created_by,
    created_on
) VALUES (
    (SELECT COALESCE(MAX(sub_feature_id), 0) + 1 FROM sub_feature_master),
    64,
    'Update Content',
    'true',
    1,
    NOW()
);

-- Sub-feature: View Content
INSERT INTO sub_feature_master (
    sub_feature_id,
    feature_id,
    sub_feature_name,
    is_active,
    created_by,
    created_on
) VALUES (
    (SELECT COALESCE(MAX(sub_feature_id), 0) + 1 FROM sub_feature_master),
    64,
    'View Content',
    'true',
    1,
    NOW()
);

-- Sub-feature: Deactivate Training
INSERT INTO sub_feature_master (
    sub_feature_id,
    feature_id,
    sub_feature_name,
    is_active,
    created_by,
    created_on
) VALUES (
    (SELECT COALESCE(MAX(sub_feature_id), 0) + 1 FROM sub_feature_master),
    64,
    'Deactivate Training',
    'true',
    1,
    NOW()
);

-- Sub-feature: View Training History
INSERT INTO sub_feature_master (
    sub_feature_id,
    feature_id,
    sub_feature_name,
    is_active,
    created_by,
    created_on
) VALUES (
    (SELECT COALESCE(MAX(sub_feature_id), 0) + 1 FROM sub_feature_master),
    64,
    'View Training History',
    'true',
    1,
    NOW()
);

-- Sub-feature: Compliance Report
INSERT INTO sub_feature_master (
    sub_feature_id,
    feature_id,
    sub_feature_name,
    is_active,
    created_by,
    created_on
) VALUES (
    (SELECT COALESCE(MAX(sub_feature_id), 0) + 1 FROM sub_feature_master),
    64,
    'Compliance Report',
    'true',
    1,
    NOW()
);
```

## Step 3: Map Features to Job Roles

### Map Training Config feature to HR Manager role (or appropriate role):

```sql
-- Get Job Role ID for HR Manager (adjust role name as needed)
SET @hr_manager_role_id = (SELECT job_role_id FROM job_role WHERE job_role_name = 'HR Manager' LIMIT 1);

-- Map feature to role
INSERT INTO role_feature_map (
    role_feature_map_id,
    job_role_id,
    feature_id,
    is_active,
    created_by,
    created_on
) VALUES (
    (SELECT COALESCE(MAX(role_feature_map_id), 0) + 1 FROM role_feature_map),
    @hr_manager_role_id,
    64, -- Feature ID for Training Config
    'true',
    1, -- Replace with actual admin user ID
    NOW()
);

-- Map all sub-features to the role
INSERT INTO role_sub_feature_map (
    role_sub_feature_map_id,
    job_role_id,
    sub_feature_id,
    is_active,
    created_by,
    created_on
)
SELECT 
    (SELECT COALESCE(MAX(role_sub_feature_map_id), 0) + 1 FROM role_sub_feature_map) + ROW_NUMBER() OVER (ORDER BY sf.sub_feature_id),
    @hr_manager_role_id,
    sf.sub_feature_id,
    'true',
    1,
    NOW()
FROM sub_feature_master sf
WHERE sf.feature_id = 64;
```

## Step 4: Verify Setup

### Check Feature Master:
```sql
SELECT * FROM feature_master WHERE feature_id = 64;
```

### Check Sub-Features:
```sql
SELECT * FROM sub_feature_master WHERE feature_id = 64;
```

### Check Role Feature Mapping:
```sql
SELECT rfm.*, jr.job_role_name, fm.feature_name
FROM role_feature_map rfm
JOIN job_role jr ON rfm.job_role_id = jr.job_role_id
JOIN feature_master fm ON rfm.feature_id = fm.feature_id
WHERE rfm.feature_id = 64;
```

### Check Role Sub-Feature Mapping:
```sql
SELECT rsfm.*, jr.job_role_name, sfm.sub_feature_name
FROM role_sub_feature_map rsfm
JOIN job_role jr ON rsfm.job_role_id = jr.job_role_id
JOIN sub_feature_master sfm ON rsfm.sub_feature_id = sfm.sub_feature_id
WHERE sfm.feature_id = 64;
```

## Step 5: Update User Sessions

After mapping features to roles, existing logged-in users need to log out and log back in to get the updated feature mappings.

Alternatively, you can clear user sessions:
```sql
-- Clear all user sessions (users will need to log in again)
DELETE FROM user_session;
```

## Frontend Feature Mapping

The frontend component expects the following user mapping keys:
- `training_config` - Main feature access
- `create_training` - Create Training sub-feature
- `update_training` - Update Training sub-feature
- `view_all_trainings` - View All Trainings sub-feature
- `add_content` - Add Content sub-feature
- `update_content` - Update Content sub-feature
- `view_content` - View Content sub-feature
- `deactivate_training` - Deactivate Training sub-feature
- `view_training_history` - View Training History sub-feature
- `compliance_report` - Compliance Report sub-feature

These are automatically generated from sub-feature names by replacing spaces with underscores and converting to lowercase.

## Troubleshooting

1. **Feature not showing in Configuration tab:**
   - Verify feature_master entry exists
   - Check role_feature_map has entry for user's role
   - Ensure user has logged out and logged back in

2. **Sub-features not accessible:**
   - Verify sub_feature_master entries exist
   - Check role_sub_feature_map has entries for user's role
   - Verify sub-feature names match exactly (case-sensitive)

3. **403 Forbidden errors:**
   - Check @JobRoleAccess annotations in TrainingController.java
   - Verify feature ID matches database
   - Check user's role has feature mapped

## Notes

- Feature ID 64 is used as a placeholder. Verify this ID is available in your database.
- If using a different feature ID, update all `@JobRoleAccess(featureIds = {64})` annotations in `TrainingController.java`
- Sub-feature names must match exactly (case-sensitive) with what the frontend expects
- After database changes, users must log out and log back in to refresh their feature mappings
