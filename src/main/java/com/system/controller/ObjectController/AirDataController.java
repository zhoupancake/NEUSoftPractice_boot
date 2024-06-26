package com.system.controller.ObjectController;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.system.common.HttpResponseEntity;
import com.system.entity.data.AirData;
import com.system.service.AirDataService;
import com.system.util.SnowflakeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import static com.system.util.AQIUtil.getAQI;
import static com.system.util.AQIUtil.getAQILevel;

@RestController
@RequestMapping("/hide/airData")
@Slf4j
@RequiredArgsConstructor
public class AirDataController {
    private final AirDataService airDataService;
    @PostMapping("/addAirData")
    public HttpResponseEntity addAirData(@RequestBody AirData airData) {
        if(airData.getPm25() < 0 || airData.getPm10() < 0 || airData.getSo2() < 0 || airData.getCo() < 0 || airData.getO3() < 0 || airData.getNo2() < 0)
            return HttpResponseEntity.error("airData value must be positive");
        if(airData.getPm25() > 500)
            airData.setPm25(500.0);
        if(airData.getSo2() > 2620)
            airData.setSo2(2620.0);
        if(airData.getCo() > 60)
            airData.setCo(60.0);
        airData.setId(SnowflakeUtil.genId());
        airData.setAqiLevel(getAQILevel(airData.getPm25(), airData.getSo2(), airData.getCo()));
        airData.setAqi(getAQI(airData.getPm25(), airData.getSo2(), airData.getCo()));

        boolean success = airDataService.save(airData);
        return HttpResponseEntity.response(success, "create airData ", null);
    }

    @PostMapping("/modifyAirData")
    public HttpResponseEntity modifyAirData(@RequestBody AirData airData) {
        if(airData.getPm25() < 0 || airData.getPm10() < 0 || airData.getSo2() < 0 || airData.getCo() < 0 || airData.getO3() < 0 || airData.getNo2() < 0)
            return HttpResponseEntity.error("airData value must be positive");
        if(airData.getPm25() > 500)
            airData.setPm25(500.0);
        if(airData.getSo2() > 2620)
            airData.setSo2(2620.0);
        if(airData.getCo() > 60)
            airData.setCo(60.0);
        System.out.println(airDataService.getById(airData.getId()));
        if(null == airDataService.getById(airData.getId()))
            return HttpResponseEntity.error("airData not exist");
        boolean success = airDataService.updateById(airData);
        return HttpResponseEntity.response(success, "modify airData", null);
    }

    @PostMapping("/deleteAirData")
    public HttpResponseEntity deleteAirDataById(@RequestBody AirData airData) {
        boolean success = airDataService.removeById(airData);
        return HttpResponseEntity.response(success, "delete airData ", null);
    }

    @PostMapping("/queryAirDataList")
    public HttpResponseEntity queryAirDataList(@RequestBody Map<String, Object> map) {
        if((Integer)map.get("pageNum") < 1 || (Integer)map.get("pageSize") < 1)
            return HttpResponseEntity.error("pageNum or pageSize is invalid");

        QueryWrapper<AirData> queryWrapper = new QueryWrapper<>();
//        if (map.containsKey("cityId") && map.get("cityId") == null)
//            queryWrapper.eq("id", map.get("cityId"));
//        if (map.containsKey("name") && map.get("name") != null)
//            queryWrapper.like("name", map.get("name"));
//        if (map.containsKey("province") && map.get("province") != null)
//            queryWrapper.eq("province", map.get("province"));
//        if (map.containsKey("level") && map.get("level") != null)
//            queryWrapper.eq("level", map.get("level"));

        Page<AirData> page = new Page<>((Integer)map.get("pageNum"), (Integer)map.get("pageSize"));
        Page<AirData> cityPage = airDataService.page(page, queryWrapper);
        List<AirData> cityList = cityPage.getRecords();
        return HttpResponseEntity.response(!cityList.isEmpty(),"query city", cityList);
    }
}
