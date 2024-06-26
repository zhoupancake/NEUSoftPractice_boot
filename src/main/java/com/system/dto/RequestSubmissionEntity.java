package com.system.dto;

import com.system.entity.data.AirData;
import com.system.entity.data.Submission;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequestSubmissionEntity implements Serializable {
    private String id;
    private String taskId;
    private String description;
    private String submitterId;
    private String relatedAirDataId;
    private String imageUrl;
    private LocalDateTime submittedTime;
    private String province;
    private String city;
    private String location;
    private String airDataId;
    private LocalDateTime date;
    private Double pm25;
    private Double pm10;
    private Double so2;
    private Double no2;
    private Double co;
    private Double o3;

    public Map<String,String> getLocation(){
        return Map.of("province",province,"city",city);
    }

    public Submission getSubmission_create() {
        return Submission.builder()
                .taskId(taskId)
                .submitterId(submitterId)
                .description(description)
                .imageUrl(imageUrl)
                .build();
    }

    public Submission getSubmission_modify() {
        return Submission.builder()
                .id(id)
                .taskId(taskId)
                .description(description)
                .relatedAirDataId(relatedAirDataId)
                .imageUrl(imageUrl)
                .submittedTime(submittedTime)
                .build();
    }

    public AirData getAirData_create() {
        return AirData.builder()
                .location(location)
                .date(date)
                .pm25(pm25)
                .pm10(pm10)
                .so2(so2)
                .no2(no2)
                .co(co)
                .o3(o3)
                .build();
    }

    public AirData getAirData_modify() {
        return AirData.builder()
                .id(airDataId)
                .location(location)
                .date(date)
                .pm25(pm25)
                .pm10(pm10)
                .so2(so2)
                .no2(no2)
                .co(co)
                .o3(o3)
                .build();
    }
}
