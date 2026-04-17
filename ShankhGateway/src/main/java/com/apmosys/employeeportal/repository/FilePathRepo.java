package com.apmosys.employeeportal.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apmosys.employeeportal.model.FilePath;

public interface FilePathRepo extends JpaRepository<FilePath, Long> {

}
