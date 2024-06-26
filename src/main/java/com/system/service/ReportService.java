package com.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.system.entity.data.Report;
import org.springframework.web.bind.annotation.GetMapping;

public interface ReportService extends IService<Report> {
    public int getReportCount();
}
