package com.system.controller.ActionController;

import com.system.common.HttpResponseEntity;
import com.system.dto.RequestCharacterEntity;
import com.system.dto.User;
import com.system.entity.character.Administrator;
import com.system.entity.character.GridDetector;
import com.system.entity.character.Supervisor;
import com.system.service.*;
import com.system.util.JWTUtil;
import com.system.util.SnowflakeUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/user")
@Slf4j
@RequiredArgsConstructor
public class UserActionController {
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final int LOCK_DURATION_MINUTES = 900;
    private final RedisTemplate<String, Integer> countRedisTemplate;
    private final UserService userService;
    private final AdministratorService administratorService;
    private final GridDetectorService gridManagerService;
    private final SupervisorService supervisorService;
    private final CityService cityService;

    @PostMapping("/login")
    public HttpResponseEntity login(@RequestBody User user, HttpServletResponse response) {
        System.out.println(user);
        List<User> users = userService.query().eq("username", user.getUsername()).list();
        System.out.println(users);
        if (users.isEmpty())
            return HttpResponseEntity.response(false, "login for error username or password", null);
        User loginUser = users.get(0);
        if (loginUser == null)
            return HttpResponseEntity.response(false, "login for error username or password", null);
        if (loginUser.getStatus() == 0)
            return HttpResponseEntity.response(false, "login, The account is banned", null);

        // check whether the account is locked because of too many login attempts
        boolean boos = Boolean.TRUE.equals(countRedisTemplate.hasKey(loginUser.getId()));
        if (boos) {
            Integer attempts = countRedisTemplate.opsForValue().get(loginUser.getId());
            if (attempts == null) {
                attempts = 0;
            }
            if (attempts >= MAX_LOGIN_ATTEMPTS) {
                countRedisTemplate.expire(user.getUsername(), LOCK_DURATION_MINUTES, TimeUnit.SECONDS);
                return HttpResponseEntity.response(false, "The account is locked with 15 minutes", null);
            }
        }
        else {
            countRedisTemplate.opsForValue().set(loginUser.getId(), 0);
        }
        if(user.getPassword().equals(loginUser.getPassword())) {
            String token = JWTUtil.getToken(Map.of("id", loginUser.getId(), "name", user.getUsername(), "password", loginUser.getPassword(), "role", loginUser.getRole()));
            countRedisTemplate.opsForValue().set(loginUser.getId(), 0);
            switch (loginUser.getRole()) {
                case "Administrator" -> {
                    Administrator administrator = administratorService.getById(loginUser.getId());
//                    Cookie cookie = new Cookie("administratorId", administrator.getId());
//                    cookie.setSecure(true);
//                    cookie.setHttpOnly(true);
//                    response.addCookie(cookie);
                    Map<String, String> result = Map.of("id", administrator.getId(), "token", token);
                    return HttpResponseEntity.response(true, "Login successfully", result);
                }
                case "GridDetector" -> {
                    GridDetector gridDetector = gridManagerService.getById(loginUser.getId());
//                    Cookie cookie = new Cookie("gridDetectorId", gridDetector.getId());
//                    cookie.setSecure(true);
//                    cookie.setHttpOnly(true);
//                    response.addCookie(cookie);
                    Map<String, String> result = Map.of("id", gridDetector.getId(), "token", token);
                    return HttpResponseEntity.response(true, "Login successfully", result);
                }
                case "Supervisor" -> {
                    Supervisor supervisor = supervisorService.getById(loginUser.getId());
//                    Cookie cookie = new Cookie("supervisorId", supervisor.getId());
//                    cookie.setSecure(true);
//                    cookie.setHttpOnly(true);
//                    response.addCookie(cookie);
                    Map<String, String> result = Map.of("id", supervisor.getId(), "token", token);
                    return HttpResponseEntity.response(true, "Login successfully", result);
                }
                default -> {
                    return HttpResponseEntity.response(false, "Access Deny", null);
                }
            }
        }
        else {
            countRedisTemplate.opsForValue().increment(loginUser.getId());
            return HttpResponseEntity.response(false, "login, for Error username or password", null);
        }
    }

    @PostMapping("/changePassword")
    public HttpResponseEntity changePassword(@RequestBody RequestCharacterEntity requestCharacterEntity) {
        Map<String, String> map = requestCharacterEntity.getUser_modifyPassword();
        User requestUser = userService.getById(map.get("id"));
        if (requestUser == null)
            return HttpResponseEntity.response(false, "change password because the modify user is not exist", null);
        if (requestUser.getStatus() == 0)
            return HttpResponseEntity.response(false, "change password because the account is banned", null);
        if(Boolean.TRUE.equals(countRedisTemplate.hasKey(map.get("id"))))
            if((Integer)countRedisTemplate.opsForValue().get(map.get("id")) >= MAX_LOGIN_ATTEMPTS) {
                countRedisTemplate.expire(map.get("id"), LOCK_DURATION_MINUTES, TimeUnit.SECONDS);
                return HttpResponseEntity.response(false, "change password because the account is locked with 15 minutes", null);
            }
        if (requestUser.getPassword().equals(map.get("password"))) {
            countRedisTemplate.opsForValue().set(map.get("id"), 0);
            requestUser.setPassword(map.get("newPassword"));
            userService.updateById(requestUser);
            return HttpResponseEntity.response(true, "modify ", null);
        }
        else {
            countRedisTemplate.opsForValue().increment(map.get("id"));
            return HttpResponseEntity.response(false, "change password because the original password is wrong", null);
        }
    }

    @PostMapping("/register")
    public HttpResponseEntity register(@RequestBody RequestCharacterEntity requestCharacterEntity) {
        Supervisor supervisor = requestCharacterEntity.getSupervisor_create();
        User user = requestCharacterEntity.getUser_create();

        String telRegex = "^(?:(?:(?:\\+|00)86)?\\s*)?1(?:(?:3[\\d])|(?:4[5-79])|(?:5[0-35-9])|(?:6[5-7])|(?:7[0-8])|(?:8[\\d])|(?:9[189]))\\d{8}$";
        Pattern telPattern = Pattern.compile(telRegex);
        Matcher telMatcher = telPattern.matcher(supervisor.getTel());
        if(!telMatcher.matches())
            return HttpResponseEntity.response(false, "register because the tel is not valid", null);
        if(Calendar.getInstance().get(Calendar.YEAR) - supervisor.getBirthYear() > 150 ||
                Calendar.getInstance().get(Calendar.YEAR) - supervisor.getBirthYear() < 14)
            return HttpResponseEntity.response(false, "register because the age is not valid", null);
        Integer cityId = cityService.getCityByLocation(requestCharacterEntity.getLocation()).getId();

        if(!userService.query().eq("username", supervisor.getTel()).list().isEmpty())
            return HttpResponseEntity.response(false, "register because the username is exist", null);
        supervisor.setId(SnowflakeUtil.genId());
        supervisor.setCityId(cityId);
        user.setId(SnowflakeUtil.genId());
        user.setUsername(supervisor.getTel());
        user.setStatus(1);
        user.setRole("Supervisor");

        boolean supervisorSuccess = supervisorService.save(supervisor);
        boolean userSuccess = userService.save(user);

        return HttpResponseEntity.response(supervisorSuccess&&userSuccess, "create supervisor", supervisor.getId());
    }
}
