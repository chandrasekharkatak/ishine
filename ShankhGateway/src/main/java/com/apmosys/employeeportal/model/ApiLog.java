package com.apmosys.employeeportal.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "API_LOG_TABLE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "TRACE_ID", length = 100)
    private String traceId;

    @Column(name = "API_ENDPOINT_NAME", length = 255)
    private String apiEndpointName;

    @Column(name = "PORTAL", length = 20)
    private String portal;

    @Column(name = "INITIATED_FROM_API", length = 500)
    private String initiatedFromApi;

    @Column(name = "API_METHOD_TYPE", length = 10)
    private String apiMethodType;

    @Column(name = "ENDPOINT_URL", length = 1000)
    private String endpointUrl;

    @Column(name = "API_STATUS", length = 20)
    private String apiStatus;

    @Column(name = "EXCEPTION_DETAILS", length = 2000)
    private String exceptionDetails;

    @Column(name = "REQUESTED_BY_USER_ID")
    private Long requestedByUserId;

    @Column(name = "REQUEST_TIMESTAMP")
    private LocalDateTime requestTimestamp;

    @Column(name = "RESPONSE_TIMESTAMP")
    private LocalDateTime responseTimestamp;

    @Column(name = "API_STATUS_CODE")
    private Integer apiStatusCode;
}
