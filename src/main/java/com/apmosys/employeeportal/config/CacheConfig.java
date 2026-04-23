package com.apmosys.employeeportal.config;

import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;

import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.EntryUnit;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.jsr107.Eh107Configuration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Cache configuration using Ehcache 3 (JSR-107).
 * Defines caches for Timesheet Dashboard API and existing Employee cache.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    public static final String CACHE_TIMESHEET_DASHBOARD_COUNT = "timesheetDashboardCountForEmployee";
    public static final String CACHE_EMPLOYEE = "Employee";
    public static final String CACHE_EMPLOYEE_VIEW_CLIENT_ATTENDANCE = "employeeViewForClientAttendanceStatus";
    public static final String CACHE_DEPARTMENT_STATUS_SUMMARY = "departmentStatusSummary";

    @Bean
    public org.springframework.cache.CacheManager cacheManager(javax.cache.CacheManager jcacheCacheManager) {
        return new JCacheCacheManager(jcacheCacheManager);
    }

    @Bean(destroyMethod = "close")
    public javax.cache.CacheManager jcacheCacheManagerBean() {

        javax.cache.spi.CachingProvider provider =
                Caching.getCachingProvider("org.ehcache.jsr107.EhcacheCachingProvider");

        javax.cache.CacheManager cacheManager = provider.getCacheManager();

        // Timesheet Dashboard Cache
       CacheConfiguration<Object, Object> timesheetConfig =
                CacheConfigurationBuilder
                        .newCacheConfigurationBuilder(
                                Object.class,
                                Object.class,
                                ResourcePoolsBuilder.heap(500))
                        .withExpiry(
                                         ExpiryPolicyBuilder
                                        .timeToLiveExpiration(java.time.Duration.ofMinutes(5)))
                        .build();

        if (cacheManager.getCache(CACHE_TIMESHEET_DASHBOARD_COUNT) == null) {
            cacheManager.createCache(
                    CACHE_TIMESHEET_DASHBOARD_COUNT,
                    Eh107Configuration.fromEhcacheCacheConfiguration(timesheetConfig));
        }

        // Employee Cache
        MutableConfiguration<Object, Object> employeeConfig =
                new MutableConfiguration<>()
                        .setTypes(Object.class, Object.class)
                        .setStoreByValue(false)
                        .setExpiryPolicyFactory(
                                CreatedExpiryPolicy.factoryOf(Duration.ONE_HOUR));

        if (cacheManager.getCache(CACHE_EMPLOYEE) == null) {
            cacheManager.createCache(CACHE_EMPLOYEE, employeeConfig);
        }

        // Employee View for Client Attendance Status: heap 30 MB, TTL 5 minutes (large payloads)
         CacheConfiguration<Object, Object> employeeViewConfig =
                CacheConfigurationBuilder
                        .newCacheConfigurationBuilder(
                                Object.class,
                                Object.class,
                                ResourcePoolsBuilder.heap(500).build())
                        .withExpiry(
                                ExpiryPolicyBuilder.timeToLiveExpiration(java.time.Duration.ofMinutes(5)))
                        .build();
        if (cacheManager.getCache(CACHE_EMPLOYEE_VIEW_CLIENT_ATTENDANCE) == null) {
            cacheManager.createCache(
                    CACHE_EMPLOYEE_VIEW_CLIENT_ATTENDANCE,
                    Eh107Configuration.fromEhcacheCacheConfiguration(employeeViewConfig));
        }

        // Department Status Summary: heap 500 entries, TTL 5 minutes (small payload)
        CacheConfiguration<Object, Object> deptSummaryConfig =
                CacheConfigurationBuilder
                        .newCacheConfigurationBuilder(
                                Object.class,
                                Object.class,
                                ResourcePoolsBuilder.heap(500))
                        .withExpiry(
                                ExpiryPolicyBuilder.timeToLiveExpiration(java.time.Duration.ofMinutes(5)))
                        .build();
        if (cacheManager.getCache(CACHE_DEPARTMENT_STATUS_SUMMARY) == null) {
            cacheManager.createCache(
                    CACHE_DEPARTMENT_STATUS_SUMMARY,
                    Eh107Configuration.fromEhcacheCacheConfiguration(deptSummaryConfig));
        }

        return cacheManager;
    }
}