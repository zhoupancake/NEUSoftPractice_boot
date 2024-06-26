package com.system.controller.ObjectController;

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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/hide/submission")
@Slf4j
@RequiredArgsConstructor
public class SubmissionController {
    private final SubmissionService submissionService;
    private final TaskService taskService;
    private final AirDataService airDataService;
    private final CityService cityService;
    private final GridDetectorService gridDetectorService;

    @PostMapping("/addSubmission")
    public HttpResponseEntity addSubmission(@RequestBody RequestSubmissionEntity requestSubmissionEntity) {
        System.out.println(requestSubmissionEntity);
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

    @PostMapping("/modifySubmission")
    public HttpResponseEntity modifySubmission(@RequestBody Submission submission) {
        if(null == submissionService.getById(submission.getId()))
            return HttpResponseEntity.error("modified submission not exist");
        boolean success = submissionService.updateById(submission);
        return HttpResponseEntity.response(success, "modify ", null);
    }

    @PostMapping("/deleteSubmission")
    public HttpResponseEntity deleteSubmissionById(@RequestBody Submission submission) {
        if(null == submissionService.getById(submission.getId()))
            return HttpResponseEntity.error("deleted submission not exist");
        boolean success = submissionService.removeById(submission);
        return HttpResponseEntity.response(success, "delete ", null);
    }

    @PostMapping("/querySubmissionList")
    public HttpResponseEntity querySubmissionList(@RequestBody Map<String, Object> map) throws ParseException {
        if((Integer)map.get("pageNum") < 1 && (Integer)map.get("pageSize") < 1)
            return HttpResponseEntity.error("pageNum and pageSize must be positive");
        QueryWrapper<Submission> queryWrapper = new QueryWrapper<>();
        if(map.containsKey("submitterId") && map.get("submitterId") != null)
            queryWrapper.like("submitter_id", map.get("submitterId"));
        if(map.containsKey("id") && map.get("id") != null)
            queryWrapper.like("id", map.get("id"));
        if(map.containsKey("taskId") && map.get("taskId") != null)
            queryWrapper.like("task_id", map.get("taskId"));
        if(map.containsKey("description") && map.get("description") != null)
            queryWrapper.like("description", map.get("description"));
        if(map.containsKey("relatedAirDataId") && map.get("relatedAirDataId") != null)
            queryWrapper.like("related_air_data_id", map.get("relatedAirDataId"));
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
        Page<Submission> taskPage = submissionService.page(page, queryWrapper);

        List<Submission> submissionList = taskPage.getRecords();
        return HttpResponseEntity.response(!submissionList.isEmpty(), "query", submissionList);
    }
}
