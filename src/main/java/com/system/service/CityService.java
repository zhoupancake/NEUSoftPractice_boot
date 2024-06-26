package com.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.system.entity.data.City;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

public interface CityService extends IService<City> {
    public City getCityByLocation(Map<String, String> location) ;

    public List<Integer> getCitiesIdByProvince(@RequestBody String province);

    public List<Integer> getCitiesSameProvince(@RequestBody Integer cityId);

    public List<Integer> getCitiesByLikeName(@RequestBody String name);

    public List<String> getProvinceList();
}
