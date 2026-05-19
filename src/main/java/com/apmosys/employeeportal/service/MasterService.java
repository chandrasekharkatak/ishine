package com.apmosys.employeeportal.service;

import java.util.List;

/**
 * Generic interface for master data services
 * 
 * @param <T> Entity type
 */
public interface MasterService<T> {

    List<T> getAllActive();
    T getById(Integer id);
    T create(T entity);
    T update(Integer id, T entity);
    void deactivate(Integer id);
}

