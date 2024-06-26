package com.system.controller.ObjectController;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.system.common.HttpResponseEntity;
import com.system.dto.RequestCharacterEntity;
import com.system.dto.User;
import com.system.entity.character.GridDetector;
import com.system.entity.data.City;
import com.system.service.CityService;
import com.system.service.GridDetectorService;
import com.system.service.UserService;
import com.system.util.SnowflakeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/hide/gridDetector")
@Slf4j
@RequiredArgsConstructor
public class GridDetectorController {
    private final GridDetectorService gridDetectorService;
    private final UserService userService;
    private final CityService cityService;

    @PostMapping("/addGridDetector")
    public HttpResponseEntity addGridDetector(@RequestBody RequestCharacterEntity requestCharacterEntity) {
        GridDetector gridDetector = requestCharacterEntity.getGridDetector_create();
        User user = requestCharacterEntity.getUser_create();

        if(!gridDetectorService.query().eq("id_card", gridDetector.getIdCard()).list().isEmpty())
            return HttpResponseEntity.error("this person is already exist in the system");
        Pattern pattern = Pattern.compile("^(20[1-9][0-9])([0-9]{6})$");
        Matcher matcher = pattern.matcher(gridDetector.getIdCard());
        if(!matcher.matches())
            return HttpResponseEntity.error("idCard is not correct");

        gridDetector.setId(SnowflakeUtil.genId());
        City city = cityService.getCityByLocation(requestCharacterEntity.getLocation());
        if(city == null)
            return HttpResponseEntity.error("the selected city is not exist");
        gridDetector.setCityId(city.getId());
        user.setId(gridDetector.getId());
        user.setUsername(gridDetector.getIdCard());
        user.setStatus(1);
        user.setRole("GridDetector");

        boolean gridDetectorSuccess = gridDetectorService.save(gridDetector);
        boolean userSuccess = userService.save(user);

        return HttpResponseEntity.response(gridDetectorSuccess&&userSuccess, "create grid detector", gridDetector.getId());
    }


    @PostMapping("/modifyGridDetector")
    public HttpResponseEntity modifyGridDetector(@RequestBody RequestCharacterEntity requestCharacterEntity) {
        GridDetector gridDetector = requestCharacterEntity.getGridDetector_modify();
        User user = requestCharacterEntity.getUser_modify();
        City city = cityService.getCityByLocation(requestCharacterEntity.getLocation());
        if(city == null)
            return HttpResponseEntity.error("the selected city is not exist");
        GridDetector orginalGridDetector = gridDetectorService.getById(gridDetector.getId());
        User orginalUser = userService.getById(user.getId());

        if(null == orginalGridDetector || null == orginalUser)
            return HttpResponseEntity.error("The modified gridDetector is not exist");
        if(!orginalUser.getPassword().equals(user.getPassword()))
            return HttpResponseEntity.error("The modification of password is forbidden");
        Pattern pattern = Pattern.compile("^(20[1-9][0-9])([0-9]{6})$");
        Matcher matcher = pattern.matcher(gridDetector.getIdCard());
        if(!matcher.matches())
            return HttpResponseEntity.error("idCard is not correct");

        user.setUsername(gridDetector.getIdCard());
        gridDetector.setCityId(city.getId());

        boolean gridDetectorSuccess = gridDetectorService.updateById(gridDetector);
        boolean userSuccess = userService.updateById(user);
        return HttpResponseEntity.response(gridDetectorSuccess&&userSuccess, "modify grid detector", null);
    }

    @PostMapping("/deleteGridDetector")
    public HttpResponseEntity deleteGridDetectorById(@RequestBody RequestCharacterEntity requestCharacterEntity) {
        GridDetector gridDetector = requestCharacterEntity.getGridDetector_modify();
        User user = requestCharacterEntity.getUser_modify();
        User dbUser = userService.getById(user.getId());
        if(null == dbUser)
            return HttpResponseEntity.error("The deleted grid detector is not exist");
        if(!dbUser.getPassword().equals(user.getPassword()))
            return HttpResponseEntity.error("The password is wrong");

        boolean gridDetectorSuccess = gridDetectorService.removeById(gridDetector);
        boolean userSuccess = userService.removeById(user);

        return HttpResponseEntity.response(gridDetectorSuccess&&userSuccess, "delete grid detector account", null);
    }

    @PostMapping("/queryGridDetectorList")
    public HttpResponseEntity queryGridDetectorList(@RequestBody Map<String, Object> map) {
        if((Integer)map.get("pageNum") < 1 || (Integer)map.get("pageSize") < 1)
            return HttpResponseEntity.error("pageNum and pageSize must be greater than 0");
        QueryWrapper<GridDetector> queryWrapper = new QueryWrapper<>();
        if(map.containsKey("id") && map.get("id") != null)
            queryWrapper.like("id", map.get("id"));
        if (map.containsKey("idCard") && map.get("idCard") != null)
            queryWrapper.like("id_card", map.get("idCard"));
        if (map.containsKey("name") && map.get("name") != null)
            queryWrapper.like("name", map.get("name"));

        Page<GridDetector> page = new Page<>((Integer)map.get("pageNum"), (Integer)map.get("pageSize"));
        Page<GridDetector> gridDetectorPage = gridDetectorService.page(page, queryWrapper);

        List<GridDetector> gridDetectorList = gridDetectorPage.getRecords();
        boolean success = !gridDetectorList.isEmpty();
        return HttpResponseEntity.response(success, "query", gridDetectorList);
    }
}
