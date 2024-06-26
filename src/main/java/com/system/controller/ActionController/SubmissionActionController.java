package com.system.controller.ActionController;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.system.common.HttpResponseEntity;
import com.system.dto.RequestSubmissionEntity;
import com.system.entity.data.AirData;
import com.system.entity.data.City;
import com.system.entity.data.Submission;
import com.system.entity.data.Task;
import com.system.service.*;
import com.system.util.SnowflakeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/submission")
@Slf4j
@RequiredArgsConstructor
public class SubmissionActionController {
    private final SubmissionService submissionService;
    private final ReportService reportService;
    private final TaskService taskService;
    private final AirDataService airDataService;
    private final CityService cityService;
    private final GridDetectorService gridDetectorService;
    @PostMapping("/gridDetector/submit")
    public HttpResponseEntity submit(@RequestBody RequestSubmissionEntity requestSubmissionEntity) {
        Submission submission = requestSubmissionEntity.getSubmission_create();
        AirData airData = requestSubmissionEntity.getAirData_create();
        Task task = taskService.getById(requestSubmissionEntity.getTaskId());
        if(null == task)
            return HttpResponseEntity.error("task not exist");
        if(task.getStatus() == 1)
            return HttpResponseEntity.error("task is finished");
        if(null == gridDetectorService.getById(submission.getSubmitterId()))
            return HttpResponseEntity.error("submitter is not exist");
        String airDataId = SnowflakeUtil.genId();

        airData.setId(airDataId);
        City city = cityService.getCityByLocation(requestSubmissionEntity.getLocation());
        if(null == city)
            return HttpResponseEntity.error("city not exist");
        airData.setCityId(city.getId());
        task.setStatus(1);
        submission.setRelatedAirDataId(airDataId);

        boolean airDataSuccess = airDataService.save(airData);
        boolean taskSuccess = taskService.updateById(task);
        boolean submissionSuccess = submissionService.save(submission);

        return HttpResponseEntity.response(airDataSuccess&&taskSuccess&&submissionSuccess, "create submission ", null);
    }

//    @PostMapping("/selectAll/gridDetector")
//    public HttpResponseEntity selectAll_gridDetector(@RequestBody Map<String, Object> map) {
//        QueryWrapper<Submission> queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq("submitter_id", map.get("submitterId"));
//        Page<Submission> page = new Page<>((Integer)map.get("pageNum"), (Integer)map.get("pageSize"));
//        Page<Submission> submissionPage = submissionService.page(page, queryWrapper);
//
//        List<Submission> submissionList = submissionPage.getRecords();
//        return HttpResponseEntity.success("select all submission", submissionList);
//    }

    @PostMapping("/gridDetector/querySubmissionList")
    public HttpResponseEntity querySubmissionList_gridDetector(@RequestBody Map<String, Object> map) throws ParseException {
        if((Integer)map.get("pageNum") < 1 || (Integer)map.get("pageSize") < 1)
            return HttpResponseEntity.error("pageNum or pageSize is invalid");
        QueryWrapper<Submission> queryWrapper = new QueryWrapper<>();
        if (map.containsKey("submitterId") && map.get("submitterId") != null)
            queryWrapper.eq("submitter_id", map.get("submitterId"));
        else
            return HttpResponseEntity.error("submitter id is required");
        if(map.containsKey("city") && map.get("city") != null && !map.get("city").equals("")){
            if(map.containsKey("province") && map.get("province") != null && !map.get("province").equals("")){
                City city = cityService.getCityByLocation(Map.of("province", (String) map.get("province"),
                        "city", (String) map.get("city")));
                if(city == null)
                    return HttpResponseEntity.error("the selected city is not exist");
                queryWrapper.eq("city_id", city.getId());
            }
            else{
                List<Integer> citiesList = cityService.getCitiesByLikeName((String) map.get("city"));
                if(citiesList == null || citiesList.isEmpty())
                    return HttpResponseEntity.error("the selected city is not exist in the list");
                for (Integer cityId : citiesList)
                    queryWrapper.or().eq("city_id", cityId);
            }
        }
        else{
            if(map.containsKey("province") && map.get("province") != null && !map.get("province").equals("")){
                List<Integer> citiesList = cityService.getCitiesIdByProvince((String) map.get("province"));
                for (Integer cityId : citiesList)
                    queryWrapper.or().eq("city_id", cityId);
            }
        }
        if(map.containsKey("id") && map.get("id") != null)
            queryWrapper.like("id", map.get("id"));
        if(map.containsKey("taskId") && map.get("taskId") != null)
            queryWrapper.like("task_id", map.get("taskId"));
        if (map.containsKey("description") && map.get("description") != null)
            queryWrapper.like("description", map.get("description"));
        if(map.containsKey("startTime") && map.get("startTime") != null && !map.get("startTime").equals("")) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date startDate = dateFormat.parse(String.valueOf(map.get("startTime")));
            Timestamp startTime = new Timestamp(startDate.getTime());
            queryWrapper.lambda().ge(Submission::getSubmittedTime,startTime);
        }
        if(map.containsKey("endTime") && map.get("endTime") != null && !map.get("endTime").equals("")) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date startDate = dateFormat.parse(String.valueOf(map.get("endTime")));
            Timestamp endTime = new Timestamp(startDate.getTime());
            queryWrapper.lambda().le(Submission::getSubmittedTime,endTime);
        }

        Page<Submission> page = new Page<>((Integer)map.get("pageNum"), (Integer)map.get("pageSize"));
        Page<Submission> submissionPage = submissionService.page(page, queryWrapper);

        List<Submission> submissionList = submissionPage.getRecords();
        return HttpResponseEntity.response(!submissionList.isEmpty(), "query submission ", submissionList);
    }

    @PostMapping("/administrator/querySubmissionList")
    public HttpResponseEntity querySubmissionList_administrator(@RequestBody Map<String, Object> map) throws ParseException {
        if((Integer)map.get("pageNum") < 1 || (Integer)map.get("pageSize") < 1)
            return HttpResponseEntity.error("pageNum or pageSize is invalid");
        QueryWrapper<Submission> queryWrapper = new QueryWrapper<>();
        if (map.containsKey("submitterId") && map.get("submitterId") != null)
            queryWrapper.eq("submitter_id", map.get("submitterId"));
        if(map.containsKey("id") && map.get("id") != null)
            queryWrapper.like("id", map.get("id"));
        if(map.containsKey("taskId") && map.get("taskId") != null)
            queryWrapper.like("task_id", map.get("taskId"));
        if (map.containsKey("description") && map.get("description") != null)
            queryWrapper.like("description", map.get("description"));
        if(map.containsKey("city") && map.get("city") != null && !map.get("city").equals("")){
            if(map.containsKey("province") && map.get("province") != null && !map.get("province").equals("")){
                City city = cityService.getCityByLocation(Map.of("province", (String) map.get("province"),
                        "city", (String) map.get("city")));
                if(city == null)
                    return HttpResponseEntity.error("the selected city is not exist");
                queryWrapper.eq("city_id", city.getId());
            }
            else{
                List<Integer> citiesList = cityService.getCitiesByLikeName((String) map.get("city"));
                if(citiesList == null || citiesList.isEmpty())
                    return HttpResponseEntity.error("the selected city is not exist in the list");
                for (Integer cityId : citiesList)
                    queryWrapper.or().eq("city_id", cityId);
            }
        }
        else{
            if(map.containsKey("province") && map.get("province") != null && !map.get("province").equals("")){
                List<Integer> citiesList = cityService.getCitiesIdByProvince((String) map.get("province"));
                for (Integer cityId : citiesList)
                    queryWrapper.or().eq("city_id", cityId);
            }
        }
        if(map.containsKey("startTime") && map.get("startTime") != null && !map.get("startTime").equals("")) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date startDate = dateFormat.parse(String.valueOf(map.get("startTime")));
            Timestamp startTime = new Timestamp(startDate.getTime());
            queryWrapper.lambda().ge(Submission::getSubmittedTime,startTime);
        }
        if(map.containsKey("endTime") && map.get("endTime") != null && !map.get("endTime").equals("")) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date startDate = dateFormat.parse(String.valueOf(map.get("endTime")));
            Timestamp endTime = new Timestamp(startDate.getTime());
            queryWrapper.lambda().le(Submission::getSubmittedTime,endTime);
        }

        Page<Submission> page = new Page<>((Integer)map.get("pageNum"), (Integer)map.get("pageSize"));
        Page<Submission> submissionPage = submissionService.page(page, queryWrapper);

        List<Submission> submissionList = submissionPage.getRecords();
        if(map.containsKey("administratorId") && map.get("administratorId") != null)
            submissionList.removeIf(submission -> !taskService.getById(submission.getTaskId()).getAppointerId().equals(map.get("administratorId")));
        return HttpResponseEntity.response(!submissionList.isEmpty(), "query submission ", submissionList);
    }

    @GetMapping("/digitalScreen/getProcession")
    public HttpResponseEntity getProcession() {
        System.out.println("fuck");
        Map<String,Integer> result = Map.of("report", reportService.getReportCount(),
                                            "task", taskService.getTaskCount(),
                                            "submission", submissionService.query().list().size());
        System.out.println(result);
        return HttpResponseEntity.response(true, "get procession", result);
    }

}
