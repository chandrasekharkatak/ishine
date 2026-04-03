package com.apmosys.employeeportal.model;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@Table(name="client_locations",
		indexes = { @Index(name = "idx_cli_loc_cli_id", columnList = "clientId"),
        @Index(name = "idx_cli_loc_cli_location", columnList = "clientLocation")
})
public class ClientLocation {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "client_location_id")
	private Integer clientLocationId;
	
	private String clientLocation;
	
	private String clientState;
	
	private Long clientAddressId;
	
	private Integer clientId;
	
	@Column(columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP" , insertable = false ,updatable = false)
	private Timestamp createdOn;
	
	
	private boolean activeInPo;
	
	
//	private Long poId;
	
	
	
	
	
	public ClientLocation(Integer clientId,String clientLocation){
		this.clientId = clientId;
		this.clientLocation = clientLocation;
	}
}
