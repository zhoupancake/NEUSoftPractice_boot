package com.system.controller.ObjectController;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.system.common.HttpResponseEntity;
import com.system.entity.data.City;
import com.system.service.CityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/hide/city")
@Slf4j
@RequiredArgsConstructor
public class CityController {
    private final CityService cityService;
    @PostMapping("/addCity")
    public HttpResponseEntity addCity(@RequestBody City city) {
        boolean success = cityService.save(city);
        return HttpResponseEntity.response(success, "create city ", null);
    }

    @PostMapping("/modifyCity")
    public HttpResponseEntity modifyCity(@RequestBody City city) {
        boolean success = cityService.updateById(city);
        return HttpResponseEntity.response(success, "modify city", null);
    }

    @PostMapping("/deleteCity")
    public HttpResponseEntity deleteCityById(@RequestBody City city) {
        boolean success = cityService.removeById(city);
        return HttpResponseEntity.response(success, "delete city ", null);
    }

    @PostMapping("/getCityById")
    public City getCityById(String id) {
        return cityService.getById(id);
    }

    @PostMapping("/getCityByLocation")
    public City getCityByLocation(@RequestBody Map<String, String> location) {
        City result;
        List<City> administratorList = cityService.query()
                .eq("province", location.get("province"))
                .eq("name", location.get("city"))
                .list();
        System.out.println(administratorList);
        if (administratorList.isEmpty())
            result = null;
        else
            result = administratorList.get(0);
        return result;
    }

    @PostMapping("/queryCityList")
    public HttpResponseEntity queryCityList(@RequestBody Map<String, Object> map) {
        if((Integer)map.get("pageNum") < 1 || (Integer)map.get("pageSize") < 1)
            return HttpResponseEntity.error("pageNum or pageSize is invalid");
        QueryWrapper<City> queryWrapper = new QueryWrapper<>();
        if (map.containsKey("cityId") && map.get("cityId") == null)
            queryWrapper.eq("id", map.get("cityId"));
        if (map.containsKey("name") && map.get("name") != null)
            queryWrapper.like("name", map.get("name"));
        if (map.containsKey("province") && map.get("province") != null)
            queryWrapper.eq("province", map.get("province"));
        if (map.containsKey("level") && map.get("level") != null)
            queryWrapper.eq("level", map.get("level"));

        Page<City> page = new Page<>((Integer)map.get("pageNum"), (Integer)map.get("pageSize"));
        Page<City> cityPage = cityService.page(page, queryWrapper);
        List<City> cityList = cityPage.getRecords();
        return HttpResponseEntity.response(!cityList.isEmpty(),"query city", cityList);
    }
}
