package com.system.service.Impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.system.entity.data.Report;
import com.system.mapper.ReportMapper;
import com.system.service.ReportService;
import org.springframework.stereotype.Service;

@Service
@DS("service")
public class ReportServiceImpl extends ServiceImpl<ReportMapper, Report> implements ReportService {
    private ReportMapper reportMapper;
    @Override
    public int getReportCount(){
        return reportMapper.selectList(new QueryWrapper<Report>()).size();
    }
}
