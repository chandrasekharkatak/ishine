package com.apmosys.employeeportal.model;


	import lombok.*;
	import javax.persistence.*;

	@Entity
	@Table(name = "question")
	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	public class Question {
	    @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;
	    
	    private String questionText;
	    
	    @ManyToOne(fetch = FetchType.LAZY)
	    @JoinColumn(name = "questionnaire_id")
	    private Questionnaire questionnaire;

		private Float response;
		//add details of creation and updation audit column
		
	    
	    // Add other fields as needed
	}

