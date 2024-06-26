package com.system.controller.ObjectController;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.system.common.HttpResponseEntity;
import com.system.dto.RequestReportEntity;
import com.system.dto.ResponseReportEntity;
import com.system.entity.data.City;
import com.system.entity.data.Report;
import com.system.service.CityService;
import com.system.service.ReportService;
import com.system.service.SupervisorService;
import com.system.util.SnowflakeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/hide/report")
@Slf4j
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;
    private final SupervisorService supervisorService;
    private final CityService cityService;
    @PostMapping("/addReport")
    public HttpResponseEntity addReport(@RequestBody RequestReportEntity requestReportEntity) {
        Report report = requestReportEntity.getReport_create();
        if (null == supervisorService.getById(report.getSubmitterId()))
            return HttpResponseEntity.error("reported submitter is not exist");
        City city = cityService.getCityByLocation(requestReportEntity.getLocation());
        if (null == city)
            return HttpResponseEntity.error("reported location is not exist");
        if (report.getForecastAqiLevel() < 1 || report.getForecastAqiLevel() > 8)
            return HttpResponseEntity.error("reported forecast AQI level is not exist");
        report.setCityId(city.getId());
        report.setId(SnowflakeUtil.genId());
        report.setStatus(0);

        boolean reportSuccess = reportService.save(report);
        return HttpResponseEntity.response(reportSuccess, "create report ", null);
    }

    @PostMapping("/modifyReport")
    public HttpResponseEntity modifyReport(@RequestBody RequestReportEntity requestReportEntity) {
        Report report = requestReportEntity.getReport_modify();
        if (null == reportService.getById(report.getId()))
            return HttpResponseEntity.error("modified report is not exist");
        if (null == supervisorService.getById(report.getSubmitterId()))
            return HttpResponseEntity.error("reported submitter is not exist");
        if (report.getForecastAqiLevel() < 1 || report.getForecastAqiLevel() > 8)
            return HttpResponseEntity.error("reported forecast AQI level is not exist");
        City city = cityService.getCityByLocation(requestReportEntity.getLocation());
        if (null == city)
            return HttpResponseEntity.error("reported location is not exist");
        report.setCityId(city.getId());
        boolean success = reportService.updateById(report);
        return HttpResponseEntity.response(success, "modify report ", report);
    }

    @PostMapping("/deleteReport")
    public HttpResponseEntity deleteReportById(@RequestBody Report report) {
        if(null == reportService.getById(report.getId()))
            return HttpResponseEntity.error("deleted report is not exist");
        boolean success = reportService.removeById(report);
        return HttpResponseEntity.response(success, "delete report ", report);
    }
    
    @PostMapping("/queryReportList")
    public HttpResponseEntity queryReportListBySubmitterId(@RequestBody Map<String, Object> map) throws ParseException {
        if((Integer) map.get("pageNum") < 1 || (Integer) map.get("pageSize") < 1)
            return HttpResponseEntity.error("pageNum or pageSize is not valid");
        QueryWrapper<Report> queryWrapper = new QueryWrapper<>();
        if (map.containsKey("province") && map.get("province") != null) {
            if (map.containsKey("city") && map.get("city") != null) {
                City city = cityService.getCityByLocation(Map.of("province", (String) map.get("province"),
                        "city", (String) map.get("city")));
                if (city == null)
                    return HttpResponseEntity.error("the selected city is not exist");
                queryWrapper.eq("city_id", city.getId());
            } else {
                List<Integer> citiesList = cityService.getCitiesIdByProvince((String) map.get("province"));
                for (Integer cityId : citiesList)
                    queryWrapper.or().eq("city_id", cityId);
            }
        }
        if (map.containsKey("id") && map.get("id") != null && !map.get("id").equals(""))
            queryWrapper.like("id", map.get("id"));
        if (map.containsKey("status") && map.get("status") != null && !map.get("status").equals(""))
            queryWrapper.like("status", map.get("status"));
        if (map.containsKey("description") && map.get("description") != null && !map.get("description").equals(""))
            queryWrapper.like("description", map.get("description"));
        if (map.containsKey("location") && map.get("location") != null && !map.get("location").equals(""))
            queryWrapper.like("location", map.get("location"));
        if (map.containsKey("forecastApiLevel") && map.get("forecastApiLevel") != null && !map.get("forecastApiLevel").equals(""))
            queryWrapper.eq("forecast_aqi_level", map.get("forecastApiLevel"));
        if(map.containsKey("startTime") && map.get("startTime") != null && !map.get("startTime").equals("")) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date startDate = dateFormat.parse(String.valueOf(map.get("startTime")));
            Timestamp startTime = new Timestamp(startDate.getTime());
            queryWrapper.lambda().ge(Report::getCreatedTime,startTime);
        }
        if(map.containsKey("endTime") && map.get("endTime") != null && !map.get("endTime").equals("")) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date startDate = dateFormat.parse(String.valueOf(map.get("endTime")));
            Timestamp endTime = new Timestamp(startDate.getTime());
            queryWrapper.lambda().le(Report::getCreatedTime,endTime);
        }

        Page<Report> page = new Page<>((Integer) map.get("pageNum"), (Integer) map.get("pageSize"));
        Page<Report> reportPage = reportService.page(page, queryWrapper);
        List<Report> reportList = reportPage.getRecords();
        List<ResponseReportEntity> result = new ArrayList<>();
        boolean success = !reportList.isEmpty();
        if (success)
            for (Report report : reportList) {
                City city = cityService.getById(report.getCityId());
                result.add(new ResponseReportEntity(report, city));
            }
        return HttpResponseEntity.response(success, "query", result);
    }
}
