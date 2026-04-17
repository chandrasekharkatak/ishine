package com.apmosys.employeeportal.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class UserData {
  
    @Id
    private String mailId;    

    private String name;
    private String yourInfo;
    private String url;



	public void setFilePaths(List<FilePath> filePaths) {
		this.filePaths = filePaths;
	}

	@OneToMany(mappedBy = "userData", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<FilePath> filePaths = new ArrayList<>();


    public UserData(String mailId) {
        this.mailId = mailId;
    }


    public List<FilePath> getFilePaths() {
        return filePaths;
    }

    public void addFilePath(FilePath filePath) {
        filePaths.add(filePath);
        filePath.setUserData(this);
    }

    public void removeFilePath(FilePath filePath) {
        filePaths.remove(filePath);
        filePath.setUserData(null);
    }
}
