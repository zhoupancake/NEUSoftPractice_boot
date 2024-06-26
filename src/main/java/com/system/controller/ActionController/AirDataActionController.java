package com.system.controller.ActionController;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.system.common.HttpResponseEntity;
import com.system.dto.ResponseAirDataEntity;
import com.system.entity.data.AirData;
import com.system.entity.data.City;
import com.system.service.AirDataService;
import com.system.service.CityService;
import com.system.util.AQIUtil;
import com.system.util.Base64Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

;

@RestController
@RequestMapping("/airData")
@Slf4j
@RequiredArgsConstructor
public class AirDataActionController {
    private final AirDataService airDataService;
    private final CityService cityService;

    @PostMapping("/administrator/selectAll/page")
    public HttpResponseEntity selectAll(@RequestBody Map<String, Object> map) {
        if((Integer)map.get("pageNum") < 1 || (Integer)map.get("pageSize") < 1)
            return HttpResponseEntity.error("pageNum and pageSize must be positive");
        QueryWrapper<AirData> queryWrapper = new QueryWrapper<>();
        Page<AirData> page = new Page<>((Integer)map.get("pageNum"), (Integer)map.get("pageSize"));
        Page<AirData> airDataPage = airDataService.page(page, queryWrapper);

        List<AirData> airDataList = airDataPage.getRecords();
        List<ResponseAirDataEntity> result = new ArrayList<>();
        boolean success = !airDataList.isEmpty();
        if(success)
            for(AirData airData: airDataList) {
                City city = cityService.getById(airData.getCityId());
                result.add(new ResponseAirDataEntity(airData, city));
            }
        return HttpResponseEntity.success("query successfully", result);
    }

    @GetMapping("/administrator/selectAll")
    public HttpResponseEntity selectAll() {
        QueryWrapper<AirData> queryWrapper = new QueryWrapper<>();
        List<AirData> airDataList = airDataService.list(queryWrapper);
        List<ResponseAirDataEntity> result = new ArrayList<>();
        boolean success = !airDataList.isEmpty();
        if(success)
            for(AirData airData: airDataList) {
                City city = cityService.getById(airData.getCityId());
                result.add(new ResponseAirDataEntity(airData, city));
            }
        return HttpResponseEntity.response(success,"query ", result);
    }

    @PostMapping("/administrator/selectByProvince")
    public HttpResponseEntity selectByProvince(@RequestBody Map<String, Object> map) {
        QueryWrapper<AirData> queryWrapper = new QueryWrapper<>();
        List<Integer> cityIdList = cityService.getCitiesIdByProvince((String)map.get("province"));
        queryWrapper.in("city_id", cityIdList);

        Page<AirData> page = new Page<>((Integer)map.get("pageNum"), (Integer)map.get("pageSize"));
        Page<AirData> airDataPage = airDataService.page(page, queryWrapper);

        List<AirData> airDataList = airDataPage.getRecords();
        List<ResponseAirDataEntity> result = new ArrayList<>();
        boolean success = !airDataList.isEmpty();
        if(success)
            for(AirData airData: airDataList) {
                City city = cityService.getById(airData.getCityId());
                result.add(new ResponseAirDataEntity(airData, city));
            }
        return HttpResponseEntity.success("query ", result);
    }

    @PostMapping("/administrator/queryAirDataList")
    public HttpResponseEntity queryAirDataList(@RequestBody Map<String, Object> map) throws ParseException {
        if((Integer) map.get("pageNum") < 1 || (Integer) map.get("pageSize") < 1)
            return HttpResponseEntity.error("pageNum and pageSize must be positive");
        QueryWrapper<AirData> queryWrapper = new QueryWrapper<>();
        if(map.containsKey("id") && map.get("id") != null&& !map.get("id").equals(""))
            queryWrapper.like("id", map.get("id"));
        if(map.containsKey("city") && map.get("city") != null && !map.get("city").equals("")){
            if(map.containsKey("province") && map.get("province") != null && !map.get("province").equals("")){
                City city = cityService.getCityByLocation(Map.of("province", (String) map.get("province"),
                        "city", (String) map.get("city")));
                if(city == null)
                    return HttpResponseEntity.error("the selected city is not exist");
                queryWrapper.eq("city_id", city.getId());
            }
            else{
                List<Integer> citiesList = cityService.getCitiesByLikeName((String) map.get("city"));
                if(citiesList.isEmpty())
                    return HttpResponseEntity.error("the selected city is not exist in the air data list");
                for (Integer cityId : citiesList)
                    queryWrapper.or().eq("city_id", cityId);
            }
        }
        else{
            if(map.containsKey("province") && map.get("province") != null && !map.get("province").equals("")){
                List<Integer> citiesList = cityService.getCitiesIdByProvince((String) map.get("province"));
                for (Integer cityId : citiesList)
                    queryWrapper.or().eq("city_id", cityId);
            }
        }
        if(map.containsKey("location") && map.get("location") != null && !map.get("location").equals(""))
            queryWrapper.like("location", map.get("location"));
        if(map.containsKey("startTime") && map.get("startTime") != null && !map.get("startTime").equals("")) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date startDate = dateFormat.parse(String.valueOf(map.get("startTime")));
            Timestamp startTime = new Timestamp(startDate.getTime());
            queryWrapper.lambda().ge(AirData::getDate,startTime);
        }
        if(map.containsKey("endTime") && map.get("endTime") != null && !map.get("endTime").equals("")) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date startDate = dateFormat.parse(String.valueOf(map.get("endTime")));
            Timestamp endTime = new Timestamp(startDate.getTime());
            queryWrapper.lambda().le(AirData::getDate,endTime);
        }
        if(map.containsKey("aqiLevel") && map.get("aqiLevel") != null && !map.get("aqiLevel").equals("")) {
            if((Integer) map.get("aqiLevel") < 1)
                return HttpResponseEntity.error("aqiLevel must be positive");
            queryWrapper.ge("aqi_level", (Integer) map.get("aqiLevel"));
        }
        if(map.containsKey("pm25Level") && map.get("pm25Level") != null && !map.get("pm25Level").equals("")) {
            if((Integer) map.get("aqiLevel") < 1)
                return HttpResponseEntity.error("aqiLevel must be positive");
            queryWrapper.ge("pm25", AQIUtil.AQILevel2value_pm25((Integer) map.get("pm25Level")));
        }
        if(map.containsKey("so2Level") && map.get("so2Level") != null && !map.get("so2Level").equals("")) {
            if((Integer) map.get("aqiLevel") < 1)
                return HttpResponseEntity.error("aqiLevel must be positive");
            queryWrapper.ge("so2", AQIUtil.AQILevel2value_so2((Integer) map.get("so2Level")));
        }
        if(map.containsKey("coLevel") && map.get("coLevel") != null && !map.get("coLevel").equals("")) {
            if((Integer) map.get("aqiLevel") < 1)
                return HttpResponseEntity.error("aqiLevel must be positive");
            queryWrapper.ge("co", AQIUtil.AQILevel2value_co((Integer) map.get("coLevel")));
        }
        Page<AirData> page = new Page<>((Integer) map.get("pageNum"), (Integer) map.get("pageSize"));
        Page<AirData> airDataPage = airDataService.page(page, queryWrapper);
        List<AirData> airDataList = airDataPage.getRecords();
        List<ResponseAirDataEntity> result = new ArrayList<ResponseAirDataEntity>();
        boolean success = !airDataList.isEmpty();
        if(success)
            for(AirData airData: airDataList) {
                City city = cityService.getById(airData.getCityId());
                result.add(new ResponseAirDataEntity(airData, city));
            }
        return HttpResponseEntity.response(!airDataList.isEmpty(), "query air data ", result);
    }

    @PostMapping("/administrator/getProvinceCount")
    public HttpResponseEntity getProvinceCount(@RequestBody Map<String, Object> map) {
        if((Integer) map.get("pageNum") < 1 || (Integer) map.get("pageSize") < 1)
            return HttpResponseEntity.error("pageNum and pageSize must be positive");
        Integer aqiLevel = 3, so2Level = 3, coLevel = 3, pm25Level = 3;
        if(map.containsKey("aqi") && map.get("aqi") != null && !map.get("aqi").equals("")) {
            aqiLevel = (Integer) map.get("aqi");
            if(aqiLevel < 1 || aqiLevel > 7)
                return HttpResponseEntity.error("aqiLevel must be between 1 and 7");
        }
        if(map.containsKey("so2") && map.get("so2") != null && !map.get("so2").equals("")) {
            so2Level = (Integer) map.get("so2");
            if(so2Level < 1 || so2Level > 7)
                return HttpResponseEntity.error("so2Level must be between 1 and 7");
        }
        if(map.containsKey("co") && map.get("co") != null && !map.get("co").equals("")) {
            coLevel = (Integer) map.get("co");
            if (coLevel < 1 || coLevel > 7)
                return HttpResponseEntity.error("coLevel must be between 1 and 7");
        }
        if(map.containsKey("pm25") && map.get("pm25") != null && !map.get("pm25").equals("")) {
            pm25Level = (Integer) map.get("pm25");
            if(pm25Level < 1 || pm25Level > 7)
                return HttpResponseEntity.error("pm25Level must be between 1 and 7");
        }
        Map<String, Map<String, Integer>> result  = new HashMap<>();
        List<String> provinces = cityService.getProvinceList();
        for(String province : provinces) {
            Map<String, Integer> count = new HashMap<>();
            List<Integer> citiesList = cityService.getCitiesIdByProvince(province);
            QueryWrapper<AirData> AQIQueryWrapper = new QueryWrapper<>();
            AQIQueryWrapper.in("city_id", citiesList).ge("aqi_level", aqiLevel);
            count.put("aqi", airDataService.list(AQIQueryWrapper).size());
            QueryWrapper<AirData> Pm25QueryWrapper = new QueryWrapper<>();
            Pm25QueryWrapper.in("city_id", citiesList).ge("pm25", AQIUtil.AQILevel2value_pm25(pm25Level));
            count.put("pm25", airDataService.list(Pm25QueryWrapper).size());
            QueryWrapper<AirData> So2QueryWrapper = new QueryWrapper<>();
            So2QueryWrapper.in("city_id", citiesList).ge("so2", AQIUtil.AQILevel2value_so2(so2Level));
            count.put("so2", airDataService.list(So2QueryWrapper).size());
            QueryWrapper<AirData> CoQueryWrapper = new QueryWrapper<>();
            CoQueryWrapper.in("city_id", citiesList).ge("co", AQIUtil.AQILevel2value_co(coLevel));
            count.put("co", airDataService.list(CoQueryWrapper).size());
            result.put(province, count);
        }
        List<String> keys = result.keySet().stream().sorted().toList();
        int start = ((Integer) map.get("pageNum") - 1) * (Integer) map.get("pageSize");
        Map<String, Map<String, Integer>> resultList = new HashMap<>();
        for(int i = start; i < start + (Integer) map.get("pageSize") && i < keys.size(); i++)
            resultList.put(keys.get(i), result.get(keys.get(i)));
        Map<String, Object> resultMap = Map.of("count", keys.size(), "result", resultList);
        return HttpResponseEntity.success("get province count", resultMap);
    }

    @GetMapping("/digitalScreen/queryAirDataByLevel")
    public HttpResponseEntity queryAirDataByLevel() throws ParseException {
        int[] count = new int[6];
        for(int i = 1; i <= 6; i++)
            count[i-1] = (int)airDataService.count(new QueryWrapper<AirData>().ge("aqi_level", i));
        Map<String, Integer> result = Map.of("one", count[0],"two", count[1], "three", count[2],
                                            "four", count[3], "five", count[4], "six", count[5]);
        return HttpResponseEntity.success("query air data by level", result);
    }

    @GetMapping("/digitalScreen/getProvinceCount")
    public HttpResponseEntity getProvinceCount() {
        int level = 3;
        Object[] provinces = cityService.getProvinceList().toArray();
        String[] provinceList = Arrays.copyOf(provinces, provinces.length, String[].class);
        Integer[] pm25Count = new Integer[provinces.length];
        Integer[] so2Count = new Integer[provinces.length];
        Integer[] coCount = new Integer[provinces.length];
        Integer[] AQICount = new Integer[provinces.length];
        for(int i = 0;i < provinces.length;i++) {
            List<Integer> citiesList = cityService.getCitiesIdByProvince(provinceList[i]);
            QueryWrapper<AirData> AQIQueryWrapper = new QueryWrapper<>();
            AQIQueryWrapper.in("city_id", citiesList).ge("aqi_level", level);
            AQICount[i] = airDataService.list(AQIQueryWrapper).size();
            QueryWrapper<AirData> Pm25QueryWrapper = new QueryWrapper<>();
            Pm25QueryWrapper.in("city_id", citiesList).ge("pm25", AQIUtil.AQILevel2value_pm25(level));
            pm25Count[i] = airDataService.list(Pm25QueryWrapper).size();
            QueryWrapper<AirData> so2QueryWrapper = new QueryWrapper<>();
            so2QueryWrapper.in("city_id", citiesList).ge("so2", AQIUtil.AQILevel2value_so2(level));
            so2Count[i] = airDataService.list(so2QueryWrapper).size();
            QueryWrapper<AirData> coQueryWrapper = new QueryWrapper<>();
            coQueryWrapper.in("city_id", citiesList).ge("co", AQIUtil.AQILevel2value_co(level));
            coCount[i] = airDataService.list(coQueryWrapper).size();
        }
        Map<String,  Object[]> result = new HashMap<>();
        result.put("category", provinceList);
        result.put("pm25", pm25Count);
        result.put("so2", so2Count);
        result.put("co", coCount);
        result.put("aqi", AQICount);
        return HttpResponseEntity.success("get province count", result);
    }

    @GetMapping("/digitalScreen/selectAll")
    public HttpResponseEntity selectAll_digitalScreen(@RequestParam("limitNum") Integer limitNum) {
        QueryWrapper<AirData> queryWrapper = new QueryWrapper<>();
        List<AirData> airDataList = airDataService.list(queryWrapper.orderByDesc("date"));
        List<ResponseAirDataEntity> result = new ArrayList<>();
        boolean success = !airDataList.isEmpty();
        if(success) {
            int count = 0;
            for (AirData airData : airDataList) {
                if(count >= limitNum)
                    break;
                City city = cityService.getById(airData.getCityId());
                result.add(new ResponseAirDataEntity(airData, city));
                count++;
            }
        }
        return HttpResponseEntity.response(success,"query ", result);
    }

    @GetMapping("/digitalScreen/selectOrderList")
    public HttpResponseEntity selectOrderList_digitalScreen(@RequestParam("limitNum") Integer limitNum) {
        QueryWrapper<AirData> queryWrapper = new QueryWrapper<>();
        List<AirData> airDataList = airDataService.list(queryWrapper.orderByDesc("aqi"));
        List<Map<String, Object>> result = new ArrayList<>();
        for(int i = 0; i < limitNum; i++){
            City city = cityService.getById(airDataList.get(i).getCityId());
            Map<String, Object> map = Map.of("value", airDataList.get(i).getAqi(), "name", city.getProvince()+"/"+city.getName());
            result.add(map);
        }
        return HttpResponseEntity.success("query ", result);
    }


    @GetMapping("/digitalScreen/WeeklyAirData")
    public HttpResponseEntity getWeeklyAirData(@RequestParam("province") String encodedProvince) {
        String province = "";
        if(encodedProvince != null)
            province = Base64Util.decodeBase64ToString(encodedProvince);
        Map<String, Integer> data = null;
        if(province.equals("china"))
            data = getWeeklyAirData_China();
        else
            data = getWeeklyAirData_Province(province);
        List<Map<String, Object>> result = new ArrayList<>();
        for(String key : data.keySet())
            result.add(Map.of("name", key, "value", data.get(key)));
        return HttpResponseEntity.success("get weekly air data", result);
    }

    private Map<String, Integer> getWeeklyAirData_China(){
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneWeekAgo = now.minusWeeks(1);

        QueryWrapper<AirData> queryWrapper = new QueryWrapper<>();
        queryWrapper.between("date", oneWeekAgo, now);
        List<AirData> airDataList = airDataService.list(queryWrapper.orderByDesc("aqi"));

        Map<String, Integer> maxAqiByProvince = new HashMap<>();
        for(AirData airData : airDataList){
            String province = cityService.getById(airData.getCityId()).getProvince();
            if(maxAqiByProvince.containsKey(province)) {
                if (airData.getAqi() > maxAqiByProvince.get(province))
                    maxAqiByProvince.put(province, airData.getAqi());
            }
            else
                maxAqiByProvince.put(province, airData.getAqi());
        }
        return maxAqiByProvince;
    }

    private Map<String, Integer> getWeeklyAirData_Province(String province) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneWeekAgo = now.minusWeeks(1);

        List<Integer> citiesId = cityService.getCitiesIdByProvince(province);
        QueryWrapper<AirData> queryWrapper = new QueryWrapper<>();
        queryWrapper.between("date", oneWeekAgo, now);
        queryWrapper.in("city_id", citiesId);
        List<AirData> airDataList = airDataService.list(queryWrapper.orderByDesc("aqi"));

        Map<String, Integer> maxAqiByCity = new HashMap<>();
        for(AirData airData : airDataList){
            String city = cityService.getById(airData.getCityId()).getName();
            if(maxAqiByCity.containsKey(city)){
                if(airData.getAqi() > maxAqiByCity.get(city))
                    maxAqiByCity.put(city, airData.getAqi());
            }
            else
                maxAqiByCity.put(city, airData.getAqi());
        }
        return maxAqiByCity;
    }
}
