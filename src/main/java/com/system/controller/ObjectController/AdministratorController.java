package com.system.controller.ObjectController;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.system.common.HttpResponseEntity;
import com.system.dto.RequestCharacterEntity;
import com.system.dto.User;
import com.system.entity.character.Administrator;
import com.system.service.AdministratorService;
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
@RequestMapping("/hide/administrator")
@Slf4j
@RequiredArgsConstructor
public class AdministratorController {
    private final AdministratorService administratorService;
    private final UserService userService;

    @PostMapping("/addAdministrator")
    public HttpResponseEntity addAdministrator(@RequestBody RequestCharacterEntity requestCharacterEntity) {
        Administrator administrator = requestCharacterEntity.getAdministrator_create();
        User user = requestCharacterEntity.getUser_create();

        if(!administratorService.query().eq("id_card", administrator.getIdCard()).list().isEmpty())
            return HttpResponseEntity.error("this person is already exist in the system");
        Pattern pattern = Pattern.compile("^(20[1-9][0-9])([0-9]{6})$");
        Matcher matcher = pattern.matcher(administrator.getIdCard());
        if(!matcher.matches())
            return HttpResponseEntity.error("idCard is not correct");
        administrator.setId(SnowflakeUtil.genId());
        user.setId(administrator.getId());
        user.setUsername(administrator.getIdCard());
        user.setStatus(1);
        user.setRole("Administrator");

        boolean administratorSuccess = administratorService.save(administrator);
        boolean userSuccess = userService.save(user);

        return HttpResponseEntity.response(administratorSuccess&&userSuccess, "add administrator ", administrator.getId());
    }


    @PostMapping("/modifyAdministrator")
    public HttpResponseEntity modifyAdministrator(@RequestBody RequestCharacterEntity requestCharacterEntity) {
        Administrator administrator = requestCharacterEntity.getAdministrator_modify();
        User user = requestCharacterEntity.getUser_modify();

        Administrator orginalAdministrator = administratorService.getById(administrator.getId());
        User orginalUser = userService.getById(user.getId());
        if(null == orginalAdministrator || null == orginalUser)
            return HttpResponseEntity.error("The modified administrator is not exist");
        if(!orginalUser.getPassword().equals(user.getPassword()))
            return HttpResponseEntity.error("The modification of password is forbidden");
        Pattern pattern = Pattern.compile("^(20[1-9][0-9])([0-9]{6})$");
        Matcher matcher = pattern.matcher(administrator.getIdCard());
        if(!matcher.matches())
            return HttpResponseEntity.error("idCard is not correct");
        user.setUsername(administrator.getIdCard());

        boolean administratorSuccess = administratorService.updateById(administrator);
        boolean userSuccess = userService.updateById(user);
        return HttpResponseEntity.response(administratorSuccess&&userSuccess, "modify administrator ", null);
    }

    @PostMapping("/deleteAdministrator")
    public HttpResponseEntity deleteAdministratorById(@RequestBody RequestCharacterEntity requestCharacterEntity) {
        Administrator administrator = requestCharacterEntity.getAdministrator_modify();
        User user = requestCharacterEntity.getUser_modify();
        User dbUser = userService.getById(user.getId());
        if(null == dbUser)
            return HttpResponseEntity.error("The deleted administrator is not exist");
        if(!dbUser.getPassword().equals(user.getPassword()))
            return HttpResponseEntity.error("The password is wrong");

        boolean administratorSuccess = administratorService.removeById(administrator);
        boolean userSuccess = userService.removeById(user);

        return HttpResponseEntity.response(administratorSuccess&&userSuccess, "delete administrator account", null);
    }

    @PostMapping("/queryAdministratorList")
    public HttpResponseEntity queryAdministratorList(@RequestBody Map<String, Object> map) {
        if((Integer)map.get("pageNum") < 1 || (Integer)map.get("pageSize") < 1)
            return HttpResponseEntity.error("pageNum and pageSize must be greater than 0");
        QueryWrapper<Administrator> queryWrapper = new QueryWrapper<>();
        if(map.containsKey("id") && map.get("id") != null)
            queryWrapper.like("id", map.get("id"));
        if (map.containsKey("idCard") && map.get("idCard") != null)
            queryWrapper.like("id_card", map.get("idCard"));
        if (map.containsKey("name") && map.get("name") != null)
            queryWrapper.like("name", map.get("name"));

        Page<Administrator> page = new Page<>((Integer)map.get("pageNum"), (Integer)map.get("pageSize"));
        Page<Administrator> administratorPage = administratorService.page(page, queryWrapper);

        List<Administrator> administratorList = administratorPage.getRecords();
        boolean success = !administratorList.isEmpty();
        return HttpResponseEntity.response(success, "query grid detector", administratorList);
    }
}
