package com.system.dto;

import com.system.entity.character.Supervisor;
import com.system.entity.data.City;
import com.system.entity.data.Report;
import com.system.entity.data.Task;
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
public class ResponseTaskEntity implements Serializable {
    private String id;
    private String appointerId;
    private String appointee;
    private LocalDateTime createdTime;
    private Integer status;

    private String province;
    private String city;
    private String location;
    private String description;
    private String reportName;
    private String tel;
    private Integer reportForecastAqiLevel;

    public ResponseTaskEntity(Task task, Report report, City city, Supervisor supervisor) {
        this.id = task.getId();
        this.appointerId = task.getAppointerId();
        this.appointee = task.getAppointeeId();
        this.createdTime = task.getCreatedTime();

        this.status = task.getStatus();
        this.province = city.getProvince();
        this.city = city.getName();
        this.location = report.getLocation();
        this.reportForecastAqiLevel = report.getForecastAqiLevel();
        this.description = report.getDescription();

        this.reportName = supervisor.getName();
        this.tel = supervisor.getTel();
    }


}
