package com.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.system.entity.character.GridDetector;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

public interface GridDetectorService extends IService<GridDetector> {
    List<GridDetector> getDetectorSameCity(Map<String, Integer> map);
    public List<GridDetector> getDetectorSameProvince(List<Integer> ids, int pageNum, int pageSize);

    public List<GridDetector> getDetectorOtherProvince(List<Integer> ids, int pageNum, int pageSize);

    public List<Integer> getDetectorCities();
}
