package com.apmosys.employeeportal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.TypeDocument;

@Repository
public interface TypeDocumentRepository extends JpaRepository<TypeDocument, Long>{

	Optional<TypeDocument> findByTypeName(String typeName);
	
	@Query(nativeQuery = true)
	List<Object[]> findAllType();

	@Query(nativeQuery = true , value = "select * from type_document td where td.type_id = :typeId")
	TypeDocument findByTypeId(Long typeId);

}
