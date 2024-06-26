package com.system.dto;

import com.system.entity.data.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseSubmissionEntity implements Serializable {
    private String submissionId;
    private String submissionSubmitterId;
    private String submissionDescription;
    private LocalDateTime submissionSubmittedTime;

    private String taskAppointerName;
    private String taskAppointee;
    private LocalDateTime taskCreatedTime;

    private String reportId;
    private String reportSubmitterName;
    private String reportSubmitterTel;
    private LocalDateTime reportCreatedTime;
    private String reportDescription;
    private String Province;
    private String City;
    private String reportLocation;
    private Integer reportForecastAqiLevel;

    private String submissionLocation;
    private LocalDateTime airDataDate;
    private Double pm25;
    private Double pm10;
    private Double so2;
    private Double no2;
    private Double co;
    private Double o3;
    private Integer aqiLevel;
    private Integer aqi;

    public ResponseSubmissionEntity(Submission submission, Task task, Report report, AirData airData, City city, String reportSubmitterTel) {
        this.submissionId = submission.getId();
        this.submissionSubmitterId = submission.getSubmitterId();
        this.submissionDescription = submission.getDescription();
        this.submissionSubmittedTime = submission.getSubmittedTime();
        this.taskAppointerName = task.getAppointerId();
        this.taskAppointee = task.getAppointeeId();
        this.taskCreatedTime = task.getCreatedTime();
        this.reportId = report.getId();
        this.reportSubmitterName = report.getSubmitterId();
        this.reportSubmitterTel = reportSubmitterTel;
        this.reportCreatedTime = report.getCreatedTime();
        this.reportDescription = report.getDescription();
        this.Province = city.getProvince();
        this.City = city.getName();
        this.reportLocation = report.getLocation();
        this.reportForecastAqiLevel = report.getForecastAqiLevel();
        this.submissionLocation = airData.getLocation();
        this.airDataDate = airData.getDate();
        this.pm25 = airData.getPm25();
        this.pm10 = airData.getPm10();
        this.so2 = airData.getSo2();
        this.no2 = airData.getNo2();
        this.co = airData.getCo();
        this.o3 = airData.getO3();
        this.aqiLevel = airData.getAqiLevel();
        this.aqi = airData.getAqi();
    }
}
