package com.apmosys.employeeportal.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Data
@Table(name = "travel_desk_ticket_day_seq")
public class TravelDeskTicketDaySeq {

	@Id
	@Column(name = "day_key", length = 8)
	private String dayKey;

	@Column(name = "last_seq")
	private Integer lastSeq;
}
