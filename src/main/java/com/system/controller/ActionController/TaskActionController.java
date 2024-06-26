package com.system.controller.ActionController;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.system.common.HttpResponseEntity;
import com.system.dto.ResponseTaskEntity;
import com.system.entity.character.GridDetector;
import com.system.entity.character.Supervisor;
import com.system.entity.data.City;
import com.system.entity.data.Report;
import com.system.entity.data.Task;
import com.system.service.*;
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
@RequestMapping("/task")
@Slf4j
@RequiredArgsConstructor
public class TaskActionController {
    private final TaskService taskService;
    private final ReportService reportService;
    private final CityService cityService;
    private final GridDetectorService gridDetectorService;
    private final AdministratorService administratorService;
    private final SupervisorService supervisorService;

    @PostMapping("/administrator/getAppointee/local")
    public HttpResponseEntity getAppointee_local(@RequestBody Map<String, Object> map) {
        if((Integer) map.get("pageNum") < 1 || (Integer) map.get("pageSize") < 1)
            return HttpResponseEntity.error("The page size and the page number should be positive");
        Map<String, String> location = Map.of("province", String.valueOf(map.get("province")),
                "city", String.valueOf(map.get("city")));
        City city = cityService.getCityByLocation((location));
        if(city == null)
            return HttpResponseEntity.error("request city is not supported");
        Map<String, Integer> paraMap = Map.of("cityId", city.getId(),
                "pageNum", (Integer)map.get("pageNum"), "pageSize", (Integer)map.get("pageSize"));
        List<GridDetector> gridDetectors = gridDetectorService.getDetectorSameCity(paraMap);
        return HttpResponseEntity.response(!gridDetectors.isEmpty(), "get appointee", gridDetectors);
    }

    @PostMapping("/administrator/getAppointee/province")
    public HttpResponseEntity getAppointee_province(@RequestBody Map<String, Object> map) {
        if((Integer) map.get("pageNum") < 1 || (Integer) map.get("pageSize") < 1)
            return HttpResponseEntity.error("The page size and the page number should be positive");
        Map<String, String> location = Map.of("province", String.valueOf(map.get("province")),
                "city", String.valueOf(map.get("city")));
        City city = cityService.getCityByLocation((location));
        if(city == null)
            return HttpResponseEntity.error("request city is not supported");
        List<Integer> cities = cityService.getCitiesSameProvince((city.getId()));
        if(cities == null || cities.isEmpty())
            return HttpResponseEntity.error("request province has no other supported city");
        List<GridDetector> result = gridDetectorService.getDetectorSameProvince(cities, (Integer)map.get("pageNum"), (Integer)map.get("pageSize"));
        return HttpResponseEntity.response(!result.isEmpty(), "get appointee ", result);
    }

    @PostMapping("/administrator/getAppointee/other")
    public HttpResponseEntity getAppointee_other(@RequestBody Map<String, Object> map) {
        if((Integer) map.get("pageNum") < 1 || (Integer) map.get("pageSize") < 1)
            return HttpResponseEntity.error("The page size and the page number should be positive");
        Map<String, String> location = Map.of("province", String.valueOf(map.get("province")),
                "city", String.valueOf(map.get("city")));
        City city = cityService.getCityByLocation((location));
        if(city == null)
            return HttpResponseEntity.error("request city is not supported");
        List<Integer> cities = cityService.getCitiesSameProvince((city.getId()));
        cities.add(city.getId());
        List<GridDetector> result = gridDetectorService.getDetectorOtherProvince(cities, (Integer)map.get("pageNum"), (Integer)map.get("pageSize"));
        return HttpResponseEntity.response(!result.isEmpty(), "get appointee ", result);
    }

    @PostMapping("/administrator/appoint")
    public HttpResponseEntity addTask(@RequestBody Task task) {
        task.setId(SnowflakeUtil.genId());
        task.setStatus(0);

        if(null == reportService.getById(task.getRelativeReportId()))
            return HttpResponseEntity.error("report not found");
        if(reportService.getById(task.getRelativeReportId()).getStatus() == 1)
            return HttpResponseEntity.error("report has been appointed");
        if(null == administratorService.getById(task.getAppointerId()))
            return HttpResponseEntity.error("administrator not found");
        if(null == gridDetectorService.getById(task.getAppointeeId()))
            return HttpResponseEntity.error("grid detector not found");

        boolean success = taskService.save(task);
        return HttpResponseEntity.response(success, "create task ", null);
    }

    @PostMapping("/gridDetector/queryTaskList")
    public HttpResponseEntity queryReportListBySubmitterId_character(@RequestBody Map<String, Object> map) throws ParseException {
        System.out.println(map);
        if((Integer)map.get("pageNum") < 1 || (Integer)map.get("pageSize") < 1)
            return HttpResponseEntity.error("The page size and the page number should be positive");
        QueryWrapper<Task> queryWrapper = new QueryWrapper<>();
        if(map.containsKey("appointeeId") && map.get("appointeeId") != null)
            queryWrapper.eq("appointee_id", map.get("appointeeId"));
        else
            return HttpResponseEntity.error("appointeeId is required");

        if(map.containsKey("id") && map.get("id") != null)
            queryWrapper.like("id", map.get("id"));
        if(map.containsKey("appointerId") && map.get("appointerId") != null)
            queryWrapper.like("appointer_id", map.get("appointerId"));
        if(map.containsKey("appointeeId") && map.get("appointeeId") != null)
            queryWrapper.like("appointee_id", map.get("appointeeId"));
        if (map.containsKey("status") && map.get("status") != null)
            queryWrapper.like("status", map.get("status"));
        if(map.containsKey("startTime") && map.get("startTime") != null && !map.get("startTime").equals("")) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date startDate = dateFormat.parse(String.valueOf(map.get("startTime")));
            Timestamp startTime = new Timestamp(startDate.getTime());
            queryWrapper.lambda().ge(Task::getCreatedTime,startTime);
        }
        if(map.containsKey("endTime") && map.get("endTime") != null && !map.get("endTime").equals("")) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date startDate = dateFormat.parse(String.valueOf(map.get("endTime")));
            Timestamp endTime = new Timestamp(startDate.getTime());
            queryWrapper.lambda().le(Task::getCreatedTime,endTime);
        }
        Page<Task> page = new Page<>((Integer)map.get("pageNum"), (Integer)map.get("pageSize"));
        Page<Task> taskPage = taskService.page(page, queryWrapper);

        List<Task> taskList = taskPage.getRecords();
        List<ResponseTaskEntity> result = new ArrayList<>();
        boolean success = !taskList.isEmpty();
        if(success)
            for(Task task: taskList) {
                Report report = reportService.getById(task.getRelativeReportId());
                Supervisor supervisor = supervisorService.getById(report.getSubmitterId());
                City city = cityService.getById(report.getCityId());
                result.add(new ResponseTaskEntity(task,report, city, supervisor));
            }
        return HttpResponseEntity.response(success, "query", result);
    }

    @PostMapping("/administrator/queryTaskList")
    public HttpResponseEntity queryReportListBySubmitterId_administrator(@RequestBody Map<String, Object> map) throws ParseException {
        if((Integer)map.get("pageNum") < 1 || (Integer)map.get("pageSize") < 1)
            return HttpResponseEntity.error("The page size and the page number should be positive");
        QueryWrapper<Task> queryWrapper = new QueryWrapper<>();
        if(map.containsKey("appointerId") && map.get("appointerId") != null)
            queryWrapper.eq("appointer_id", map.get("appointerId"));
        else
            return HttpResponseEntity.error("appointerId is required");

        if(map.containsKey("id") && map.get("id") != null)
            queryWrapper.like("id", map.get("id"));
        if(map.containsKey("appointeeId") && map.get("appointeeId") != null)
            queryWrapper.like("appointee_id", map.get("appointeeId"));
        if(map.containsKey("appointeeId") && map.get("appointeeId") != null)
            queryWrapper.like("appointee_id", map.get("appointeeId"));
        if (map.containsKey("status") && map.get("status") != null)
            queryWrapper.like("status", map.get("status"));
        if(map.containsKey("startTime") && map.get("startTime") != null && !map.get("startTime").equals("")) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date startDate = dateFormat.parse(String.valueOf(map.get("startTime")));
            Timestamp startTime = new Timestamp(startDate.getTime());
            queryWrapper.lambda().ge(Task::getCreatedTime,startTime);
        }
        if(map.containsKey("endTime") && map.get("endTime") != null && !map.get("endTime").equals("")) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date startDate = dateFormat.parse(String.valueOf(map.get("endTime")));
            Timestamp endTime = new Timestamp(startDate.getTime());
            queryWrapper.lambda().le(Task::getCreatedTime,endTime);
        }
        Page<Task> page = new Page<>((Integer)map.get("pageNum"), (Integer)map.get("pageSize"));
        Page<Task> taskPage = taskService.page(page, queryWrapper);

        List<Task> taskList = taskPage.getRecords();
        List<ResponseTaskEntity> result = new ArrayList<>();
        boolean success = !taskList.isEmpty();
        if(success)
            for(Task task: taskList) {
                Report report = reportService.getById(task.getRelativeReportId());
                Supervisor supervisor = supervisorService.getById(report.getSubmitterId());
                City city = cityService.getById(report.getCityId());
                result.add(new ResponseTaskEntity(task,report, city, supervisor));
            }
        return HttpResponseEntity.response(success, "query", result);
    }
}
