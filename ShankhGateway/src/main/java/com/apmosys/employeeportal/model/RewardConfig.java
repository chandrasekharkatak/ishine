package com.apmosys.employeeportal.model;

import javax.persistence.*;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity
@Table(name = "reward_config")
public class RewardConfig {
	
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reward_name")
    private String rewardName;

    @Column(name = "reward_type")
    private String rewardType;

    @Column(name = "reward_condition")
    private String rewardCondition;

    @Column(name = "catagory_Id")
    private Integer categoryId;
    
//    private Long createdBy;	
//	private Long updatedBy;
//	
//	@Column(columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP" , insertable = false ,updatable = false)
//	private Timestamp createdOn;
//	
//	@JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
//	private LocalDateTime updatedOn;
	
	private String filterConditions;
	
    private int isTeam;
    
    @Embedded
	public CommonProperties commonProperty = new CommonProperties();
	
}
