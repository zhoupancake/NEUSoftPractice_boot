package com.system.service.Impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.system.entity.data.Submission;
import com.system.mapper.SubmissionMapper;
import com.system.service.SubmissionService;
import org.springframework.stereotype.Service;

@Service
@DS("service")
public class SubmissionServiceImpl extends ServiceImpl<SubmissionMapper, Submission> implements SubmissionService {
}
