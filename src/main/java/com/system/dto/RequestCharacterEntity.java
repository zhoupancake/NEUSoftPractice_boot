package com.system.dto;

import com.system.entity.character.Administrator;
import com.system.entity.character.GridDetector;
import com.system.entity.character.Supervisor;
import com.system.entity.data.City;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequestCharacterEntity implements Serializable {
    private String id;
    private String username;
    private String password;
    private String newPassword;
    private Integer status;
    private String role;
    private String tel;
    private String name;
    private Integer age;
    private String sex;
    private String idCard;
    private String province;
    private String city;
    private Integer cityId;

    public User getUser_create(){
        return User.builder()
                .password(password)
                .role(role)
                .build();
    }

    public User getUser_modify(){
        return User.builder()
                .id(id)
                .username(username)
                .password(password)
                .status(status)
                .role(role)
                .build();
    }

    public Map<String,String> getUser_modifyPassword(){
        return Map.of("id",id,"newPassword",newPassword, "password", password);
    }

    public Map<String,String> getLocation(){
        return Map.of("province",province,"city",city);
    }

    public Administrator getAdministrator_create(){
        return Administrator.builder()
                .idCard(idCard)
                .name(name)
                .build();
    }

    public Administrator getAdministrator_modify(){
        return Administrator.builder()
                .id(id)
                .idCard(idCard)
                .name(name)
                .build();
    }

    public Supervisor getSupervisor_create(){
        int digitalSex = 0;
        if(sex.equals("male"))
            digitalSex = 1;
        else if(sex.equals("female"))
            digitalSex = 0;
        return Supervisor.builder()
                .tel(tel)
                .name(name)
                .birthYear(Calendar.getInstance().get(Calendar.YEAR) - age)
                .sex(digitalSex)
                .build();
    }

    public Supervisor getSupervisor_modify(){
        int digitalSex = 0;
        if(sex.equals("male"))
            digitalSex = 1;
        else if(sex.equals("female")) {
            digitalSex = 0;
        }
        return Supervisor.builder()
                .id(id)
                .tel(tel)
                .name(name)
                .birthYear(Calendar.getInstance().get(Calendar.YEAR) - age)
                .sex(digitalSex)
                .build();
    }

    public GridDetector getGridDetector_create(){
        return GridDetector.builder()
                .idCard(idCard)
                .name(name)
                .build();
    }

    public GridDetector getGridDetector_modify(){
        return GridDetector.builder()
                .id(id)
                .idCard(idCard)
                .name(name)
                .cityId(cityId)
                .build();
    }

    public City getCity(){
        return City.builder()
                .province(province)
                .name(name)
                .build();
    }
}
