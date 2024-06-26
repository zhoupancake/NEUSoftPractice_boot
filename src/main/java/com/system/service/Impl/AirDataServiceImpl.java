package com.system.service.Impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.system.entity.data.AirData;
import com.system.mapper.AirDataMapper;
import com.system.service.AirDataService;
import org.springframework.stereotype.Service;

@Service
@DS("data")
public class AirDataServiceImpl extends ServiceImpl<AirDataMapper, AirData> implements AirDataService {
}
