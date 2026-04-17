package com.apmosys.employeeportal.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class FilePath {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String yourInfo;
    
    @Lob
    @Column(name = "file_data", columnDefinition = "LONGBLOB") 
    private byte[] fileData;

	private String path;
    @Column(name = "qr_code_path") 
    private String qrCodePath;
    @ManyToOne
    @JoinColumn(name = "user_mail_id")
    private UserData userData;
}
