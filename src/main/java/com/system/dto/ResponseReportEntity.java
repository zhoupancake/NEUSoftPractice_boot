package com.system.dto;

import com.system.entity.data.City;
import com.system.entity.data.Report;
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
public class ResponseReportEntity implements Serializable {
    private String id;
    private String submitterId;
    private Integer status;
    private LocalDateTime createdTime;
    private String description;
    private String province;
    private String city;
    private String location;
    private Integer forecastAqiLevel;

    public ResponseReportEntity(Report report, City city){
        this.id = report.getId();
        this.submitterId = report.getSubmitterId();
        this.status = report.getStatus();
        this.createdTime = report.getCreatedTime();
        this.description = report.getDescription();
        this.province = city.getProvince();
        this.city = city.getName();
        this.location = report.getLocation();
        this.forecastAqiLevel = report.getForecastAqiLevel();
    }
}
