package com.system.service.Impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.system.entity.data.City;
import com.system.mapper.CityMapper;
import com.system.service.CityService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@DS("public")
public class CityServiceImpl extends ServiceImpl<CityMapper, City> implements CityService {
    @Resource
    private CityMapper cityMapper;
    public City getCityByLocation(Map<String, String> location) {
        City result;
        QueryWrapper<City> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("province", location.get("province"));
        queryWrapper.eq("name", location.get("city"));
        List<City> administratorList = cityMapper.selectList(queryWrapper);
        if (administratorList.isEmpty())
            result = null;
        else
            result = administratorList.get(0);
        return result;
    }

    public List<Integer> getCitiesIdByProvince(String province) {
        QueryWrapper<City> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("province", province);
        List<City> cityList = cityMapper.selectList(queryWrapper);
        return cityList.stream()
                .map(City::getId)
                .toList();
    }

    public List<Integer> getCitiesSameProvince(Integer cityId) {
        City city = cityMapper.selectById(cityId);
        if(city == null)
            return null;
        QueryWrapper<City> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("province", city.getProvince());
        queryWrapper.ne("id", cityId);
        List<City> cityList = cityMapper.selectList(queryWrapper);
        return cityList.stream()
                .map(City::getId)
                .toList();
    }

    public List<Integer> getCitiesByLikeName(String name) {
        QueryWrapper<City> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("name", name);
        List<City> cityList = cityMapper.selectList(queryWrapper);
        return cityList.stream().map(City::getId).toList();
    }

    public List<String> getProvinceList() {
        List<String> result = new ArrayList<>();
        List<City> cityList = cityMapper.selectList(new QueryWrapper<City>());
        result = cityList.stream()
                .map(City::getProvince)
                .distinct()
                .collect(Collectors.toList());
        return result;
    }
}
