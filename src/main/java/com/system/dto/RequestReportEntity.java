package com.system.dto;

import com.system.entity.data.Report;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequestReportEntity implements Serializable {
    private String id;
    private String submitterId;
    private Integer status;
    private String createdTime;
    private String description;
    private String imageUrl;
    private String province;
    private String city;
    private String location;
    private Integer forecastAQILevel;

    public Map<String,String> getLocation(){
        return Map.of("province",province,"city",city);
    }

    public Report getReport_create() {
        return Report.builder()
                .location(location)
                .submitterId(submitterId)
                .description(description)
                .imageUrl(imageUrl)
                .forecastAqiLevel(forecastAQILevel)
                .build();
    }

    public Report getReport_modify() {
        LocalDateTime localDateTime = null;
        if(createdTime != null && !createdTime.isEmpty()) {
            Pattern pattern1 = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");
            Pattern pattern2 = Pattern.compile("^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$");
            Matcher matcher1 = pattern1.matcher(createdTime);
            Matcher matcher2 = pattern2.matcher(createdTime);
            if (matcher1.matches()) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate localDate = LocalDate.parse(createdTime, formatter);
                localDateTime = localDate.atStartOfDay();
            }
            else if (matcher2.matches()) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                localDateTime = LocalDateTime.parse(createdTime, formatter);
            }
            else {
                throw new RuntimeException("error time format");
            }
        }
        return Report.builder()
                .id(id)
                .submitterId(submitterId)
                .status(status)
                .createdTime(localDateTime)
                .description(description)
                .imageUrl(imageUrl)
                .location(location)
                .forecastAqiLevel(forecastAQILevel)
                .build();
    }


}
