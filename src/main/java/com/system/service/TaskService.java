package com.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.system.entity.data.Task;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface TaskService extends IService<Task> {
    public String[] getTaskIdByAppointerId(@RequestBody String appointerId);

    public int getTaskCount();
}
