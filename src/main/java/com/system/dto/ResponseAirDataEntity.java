package com.system.dto;

import com.system.entity.data.AirData;
import com.system.entity.data.City;
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
public class ResponseAirDataEntity implements Serializable{
    private String id;
    private String province;
    private String city;
    private String location;
    private LocalDateTime date;

    private Double pm25;
    private Double pm10;
    private Double so2;
    private Double no2;
    private Double co;
    private Double o3;
    private Integer aqiLevel;
    private Integer aqi;

    public ResponseAirDataEntity(AirData airData, City city){
        this.id = airData.getId();
        this.province = city.getProvince();
        this.city = city.getName();
        this.location = airData.getLocation();
        this.date = airData.getDate();
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
