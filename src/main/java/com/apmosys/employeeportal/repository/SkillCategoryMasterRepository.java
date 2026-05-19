package com.apmosys.employeeportal.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.SkillCategoryMaster;

@Repository
public interface SkillCategoryMasterRepository
		extends JpaRepository<SkillCategoryMaster, Integer>, JpaSpecificationExecutor<SkillCategoryMaster> {

	boolean existsByCategoryNameIgnoreCase(String categoryName);

	boolean existsByCategoryNameIgnoreCaseAndCategoryIdNot(String categoryName, Integer categoryId);

	Optional<SkillCategoryMaster> findFirstByCategoryNameIgnoreCaseOrderByCategoryIdAsc(String categoryName);
}
