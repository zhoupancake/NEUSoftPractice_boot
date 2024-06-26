package com.system.service.Impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.system.entity.character.GridDetector;
import com.system.mapper.GridDetectorMapper;
import com.system.mapper.UserMapper;
import com.system.service.GridDetectorService;
import com.system.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@DS("public")
public class GridDetectorServiceImpl extends ServiceImpl<GridDetectorMapper, GridDetector> implements GridDetectorService {
    private GridDetectorMapper gridDetectorMapper;
    private UserMapper userMapper;
    @Override
    @PostMapping("/getDetectorSameCity")
    public List<GridDetector> getDetectorSameCity(Map<String, Integer> map) {
        QueryWrapper<GridDetector> gridDetectorQueryWrapper = new QueryWrapper<>();
        gridDetectorQueryWrapper.eq("city_id", map.get("cityId"));
        List<GridDetector> gridDetectorList = gridDetectorMapper.selectList(gridDetectorQueryWrapper);
        List<GridDetector> result = new ArrayList<GridDetector>();
        gridDetectorList.removeIf(gridDetector -> userMapper.selectById(gridDetector.getId()).getStatus() == 0);
        int count = (Integer) map.get("pageNum") * (Integer) map.get("pageSize");
        if(count > gridDetectorList.size() + (Integer) map.get("pageSize"))
            return result;
        else
            for(int i = count - (Integer) map.get("pageSize");i < Math.min(count, gridDetectorList.size());i++)
                result.add(gridDetectorList.get(i));
        return result;
    }

    @Override
    @PostMapping("/getDetectorSameProvince")
    public List<GridDetector> getDetectorSameProvince(List<Integer> ids, int pageNum, int pageSize) {
        QueryWrapper<GridDetector> gridDetectorQueryWrapper = new QueryWrapper<>();
        gridDetectorQueryWrapper.in("city_id", ids);
        List<GridDetector> gridDetectorList = gridDetectorMapper.selectList(gridDetectorQueryWrapper);
        List<GridDetector> result = new ArrayList<GridDetector>();
        gridDetectorList.removeIf(gridDetector -> userMapper.selectById(gridDetector.getId()).getStatus() == 0);
        int count = pageNum * pageSize;
        if(count > gridDetectorList.size() + pageSize)
            return result;
        else
            for(int i = count - pageSize;i < Math.min(count, gridDetectorList.size());i++)
                result.add(gridDetectorList.get(i));
        return result;
    }

    @Override
    @PostMapping("/getDetectorOtherProvince")
    public List<GridDetector> getDetectorOtherProvince(List<Integer> ids, int pageNum, int pageSize) {
        QueryWrapper<GridDetector> gridDetectorQueryWrapper = new QueryWrapper<>();
        gridDetectorQueryWrapper.notIn("city_id", ids);
        List<GridDetector> gridDetectorList = gridDetectorMapper.selectList(gridDetectorQueryWrapper);
        List<GridDetector> result = new ArrayList<GridDetector>();
        gridDetectorList.removeIf(gridDetector -> userMapper.selectById(gridDetector.getId()).getStatus() == 0);
        int count = pageNum * pageSize;
        if(count > gridDetectorList.size() + pageSize)
            return result;
        else
            for(int i = count - pageSize;i < Math.min(count, gridDetectorList.size());i++)
                result.add(gridDetectorList.get(i));
        return result;
    }


    @Override
    @GetMapping("/getDetectorCities")
    public List<Integer> getDetectorCities(){
        List<Integer> cities = gridDetectorMapper.selectList(new QueryWrapper<GridDetector>()).stream().map(GridDetector::getCityId).toList();
        return cities.stream().distinct().toList();
    }
}
