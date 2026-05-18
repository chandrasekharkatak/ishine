package com.apmosys.employeeportal.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "doc_mime_type_master_new")
public class DocMimeTypeMasterNew {

	@Id
    @Column(name = "mime_type_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer mimeTypeId;  

    @Column(name = "mime_type")
    private String mimeType;

    @Column(name = "description")
    private String description;
	
}
