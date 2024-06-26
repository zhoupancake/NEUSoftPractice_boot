package com.system.service.Impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.system.entity.data.Task;
import com.system.mapper.TaskMapper;
import com.system.service.AdministratorService;
import com.system.service.TaskService;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Service
@DS("service")
public class TaskServiceImpl extends ServiceImpl<TaskMapper, Task> implements TaskService {
    private TaskMapper taskMapper;
    private AdministratorService administratorService;
    @Override
    public String[] getTaskIdByAppointerId(@RequestBody String appointerId) {
        if(null == administratorService.getById(appointerId))
            return null;
        QueryWrapper<Task> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("appointer_id", appointerId);
        List<Task> list = taskMapper.selectList(queryWrapper);
        return list.stream().map(Task::getId).toArray(String[]::new);
    }

    @Override
    @GetMapping("/getTaskCount")
    public int getTaskCount(){
        return taskMapper.selectList(new QueryWrapper<Task>()).size();
    }
}
