package com.apmosys.employeeportal;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface JobRoleAccess {
//    String[] roles() default {};   // Optional: Role names like "ADMIN", "HOD"
    long[] featureIds() default {};   // Optional: Role IDs like 1, 2, 3
}