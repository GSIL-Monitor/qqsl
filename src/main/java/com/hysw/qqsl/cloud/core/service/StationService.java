package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.CommonEnum;
import com.hysw.qqsl.cloud.core.dao.StationDao;
import com.hysw.qqsl.cloud.core.entity.*;
import com.hysw.qqsl.cloud.core.entity.data.Account;
import com.hysw.qqsl.cloud.core.entity.data.Sensor;
import com.hysw.qqsl.cloud.core.entity.data.Station;
import com.hysw.qqsl.cloud.core.entity.data.User;
import com.hysw.qqsl.cloud.core.entity.station.Camera;
import com.hysw.qqsl.cloud.core.entity.station.Cooperate;
import com.hysw.qqsl.cloud.core.entity.station.Share;
import com.hysw.qqsl.cloud.util.SettingUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.ss.usermodel.*;
import com.hysw.qqsl.cloud.pay.entity.data.Trade;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * 测站服务类
 */
@Service("stationService")
public class StationService extends BaseService<Station, Long> {

    Log logger = LogFactory.getLog(getClass());
    @Autowired
    private StationDao stationDao;
    @Autowired
    private UserService userService;
    @Autowired
    private SensorService sensorService;
    @Autowired
    private CacheManager cacheManager;
    @Autowired
    private MonitorService monitorService;
    @Autowired
    private UserMessageService userMessageService;
    @Autowired
    private AccountMessageService accountMessageService;

    @Autowired
    public void setBaseDao(StationDao stationDao) {
        super.setBaseDao(stationDao);
    }

    Setting setting = SettingUtils.getInstance().getSetting();

    /**
     * 读取xml--station
     *
     * @param xml
     */
    private List<StationModel> readStationItem(String xml) {
        Element root = null;
        try {
            root = SettingUtils.getInstance().getRootElement(xml);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        List<Element> elements = root.elements();
        Element element;
        List<StationModel> stationModels = new ArrayList<>();
        StationModel stationModel;
        for (int i = 0; i < elements.size(); i++) {
            element = elements.get(i);
            stationModel = new StationModel();
            stationModel.setName(element.attributeValue("name"));
            stationModel.setType(CommonEnum.StationType.valueOf(element.attributeValue("type").toUpperCase()));
            stationModel.setDescription(element.attributeValue("description"));
            stationModel.setPrice(Double.valueOf(element.attributeValue("price")));
            stationModel.setServicePrice(Double.valueOf(element.attributeValue("servicePrice")));
            stationModels.add(stationModel);
        }
        return stationModels;
    }

    /**
     * 将数据服务模板放入缓存
     */
    public void putStationModelInCache() {
        Cache cache = cacheManager.getCache("stationModelCache");
        net.sf.ehcache.Element element = new net.sf.ehcache.Element("station", readStationItem(setting.getStation()));
        cache.put(element);
    }

    /**
     * 从缓存取出数据服务模板
     *
     * @return
     */
    public List<StationModel> getStationModelFromCache() {
        Cache cache = cacheManager.getCache("stationModelCache");
        net.sf.ehcache.Element element = cache.get("station");
        return (List<StationModel>) element.getValue();
    }

    /**
     * 向前台返回数据服务列表
     *
     * @return
     */
    public JSONArray getStationList() {
        List<StationModel> stationModels = (List<StationModel>) SettingUtils.objectCopy(getStationModelFromCache());
        JSONArray jsonArray = new JSONArray();
        for (StationModel stationModel : stationModels) {
            JSONObject jsonObject = stationModelToJson(stationModel);
            jsonArray.add(jsonObject);
        }
        return jsonArray;
    }

    public JSONObject stationModelToJson(StationModel stationModel) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("price", stationModel.getPrice());
        jsonObject.put("servicePrice", stationModel.getServicePrice());
        jsonObject.put("name", stationModel.getName());
        jsonObject.put("type", stationModel.getType());
        jsonObject.put("description", stationModel.getDescription());
        return jsonObject;
    }

    /**
     * 获取对于用户的水文测站
     *
     * @param object
     * @return
     */
    public List<JSONObject> getStations(Object object) {
        if(object instanceof User)
            return getStationsFromUser((User) object);
        if(object instanceof Account)
            return getStationsFromAccount((Account) object);
        return new ArrayList<>();
    }

    /**
     * 获取子账户可查看的测站
     * @param account
     * @return
     */
    private List<JSONObject> getStationsFromAccount(Account  account) {
        List<Station> stations = findAll();
        Cooperate cooperate;
        List<JSONObject> jsonObjects = new ArrayList<>();
        for (Station station : stations) {
            cooperate = new Cooperate(station);
            makeCooperateVisits(station, cooperate);
            for (Account account1 : cooperate.getVisits()) {
                if (account1.getId().toString().equals(account.getId().toString())) {
                    jsonObjects.add(makeStationJson(station));
                }
            }
        }
        return jsonObjects;
    }

    /**
     * 获取用户的水文测站
     *
     * @param user
     * @return
     */
    private List<JSONObject> getStationsFromUser(User user) {
        List<Station> stations = findAll();
        List<JSONObject> jsonObjects = new ArrayList<>();
        Station station;
        JSONObject jsonObject;
        for (int i = 0; i < stations.size(); i++) {
            station = stations.get(i);
            if(station.getUser().getId().equals(user.getId())||isShare(user,station)){
                jsonObject = makeStationJson(stations.get(i));
                jsonObjects.add(jsonObject);
            }
        }
        return jsonObjects;
    }

    /**
     * json 转化
     *
     * @param station
     * @return
     */
    private JSONObject makeStationJson(Station station) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", station.getId());
        jsonObject.put("parameter", StringUtils.hasText(station.getParameter()) ? JSONObject.fromObject(station.getParameter()) : null);
        jsonObject.put("address", station.getAddress());
        jsonObject.put("coor", station.getCoor());
        jsonObject.put("description", station.getDescription());
        jsonObject.put("exprieDate",station.getExpireDate()==null?null:station.getExpireDate().getTime());
        jsonObject.put("createDate", station.getCreateDate());
        jsonObject.put("instanceId", station.getInstanceId());
        jsonObject.put("picture", station.getPicture());
        jsonObject.put("riverModel", station.getRiverModel()==null?null:JSONArray.fromObject(station.getRiverModel()));
        jsonObject.put("flowModel", station.getFlowModel()==null?null:JSONArray.fromObject(station.getFlowModel()));
        jsonObject.put("bottomElevation", station.getBottomElevation());
        jsonObject.put("name", station.getName());
        jsonObject.put("type", station.getType());
        jsonObject.put("shares", station.getShares());
        jsonObject.put("cooperate", station.getCooperate());
        Camera camera = getCameraFromStation(station);
        Sensor sensor = getSensorFromStation(station);
        JSONObject cameraJson = makeCameraJson(camera);
        jsonObject.put("camera",cameraJson.isEmpty()?null:cameraJson);
       // List<JSONObject> sensorJsons = sensorService.makeSensorJsons(station.getSensors());
        JSONObject sensorJson = sensorService.makeSensorJson(sensor);
        jsonObject.put("sensor", sensorJson.isEmpty()?null:sensorJson);
        jsonObject.put("userId", station.getUser().getId());
        return jsonObject;

    }

    private Sensor getSensorFromStation(Station station) {
        List<Sensor> sensors = station.getSensors();
        if(sensors.size()==0){
            return null;
        }
        for(int i = 0;i<sensors.size();i++){
            if(!Sensor.Type.CAMERA.equals(sensors.get(i).getType())){
                return sensors.get(i);
            }
        }
        return null;
    }

    private Camera getCameraFromStation(Station station) {
        List<Sensor> sensors = station.getSensors();
        Camera camera = null;
        if(sensors.size()==0){
            return camera;
        }
        Sensor sensor;
        for(int i = 0;i<sensors.size();i++){
            sensor = sensors.get(i);
            if(Sensor.Type.CAMERA.equals(sensor.getType())){
                camera = new Camera();
                camera.setCameraUrl(sensor.getCameraUrl());
                camera.setInfo(sensor.getInfo());
                camera.setId(sensor.getId());
                camera.setStation(station);
                return camera;
            }
        }
        return camera;
    }


    private JSONObject makeCameraJson(Camera camera) {
        JSONObject jsonObject = new JSONObject();
        if(camera==null){
            return jsonObject;
        }
        jsonObject.put("id",camera.getId());
        jsonObject.put("info",camera.getInfo());
        jsonObject.put("station",camera.getStation().getId());
        jsonObject.put("cameraUrl",camera.getCameraUrl());
        return jsonObject;
    }



    /**
     * 根据用户查找水文测站
     *
     * @param user
     * @return
     */
    public List<Station> findByUser(User user) {
        List<Filter> filters = new ArrayList<>();
        filters.add(Filter.eq("user", user.getId()));
        List<Station> stations = stationDao.findList(0, null, filters);
        return stations;
    }

    /**
     * 读取河道模型及水位流量excle文件,并将文件内容转换为json数据
     *
     * @param mFile
     * @return
     */
    public JSONObject readModelFile(MultipartFile mFile, Station station) throws IOException, QQSLException {
        String fileName = mFile.getOriginalFilename();
        String prefix = fileName.substring(fileName.lastIndexOf(".") + 1,
                fileName.length());
        InputStream is = mFile.getInputStream();
        Workbook wb = SettingUtils.readExcel(is, prefix);
        if (null == wb) {
            throw new QQSLException("文件格式有误!");
        }
        int number = wb.getNumberOfSheets();
        if (number != 2) {
            throw new QQSLException();
        }
        //读取河道模型excle文件,并将文件内容转换为json数据
        JSONArray riverJsons = null;
        JSONArray flowJsons = null;
        Sheet sheet;
        double bottomElevation = 0;
        for (int i = 0; i < number; i++) {
            sheet = wb.getSheetAt(i);
            if ("河道模型".equals(sheet.getSheetName())) {
                riverJsons = readSheet(sheet);
                //挑选河底高程
                bottomElevation = selectBottomElevation(sheet);
            }
            if ("水位流量曲线".equals(sheet.getSheetName())) {
                flowJsons = readSheet(sheet);
            }
        }
        station.setRiverModel(riverJsons == null || riverJsons.isEmpty() ? null : riverJsons.toString());
        station.setFlowModel(flowJsons == null || flowJsons.isEmpty() ? null : flowJsons.toString());
        station.setBottomElevation(bottomElevation);
        stationDao.save(station);
        JSONObject model = new JSONObject();
        model.put("flowModel",station.getFlowModel());
        model.put("riverModel",station.getRiverModel());
        return model;
    }

    /**
     * 挑选河底高程
     *
     * @param sheet
     * @return
     */
    private double selectBottomElevation(Sheet sheet) {
        double data;
        Row row;
        double tmp = sheet.getRow(1).getCell(1).getNumericCellValue();
        int rows = sheet.getLastRowNum() + 1;
        for (int i = 1; i < rows; i++) {
            row = sheet.getRow(i);
            data = row.getCell(1).getNumericCellValue();
            if (tmp > data) {
                tmp = data;
            }
        }
        return tmp;
    }

    /**
     * 读取sheet
     *
     * @param sheet
     * @return
     */
    private JSONArray readSheet(Sheet sheet) {
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject;
        Row row;
        int rows = sheet.getLastRowNum() + 1;
        for (int i = 0; i < rows; i++) {
            jsonObject = new JSONObject();
            row = sheet.getRow(i);
            if (i == 0) {
                continue;
            }
            jsonObject.put("x", row.getCell(0).getNumericCellValue());
            jsonObject.put("y", row.getCell(1).getNumericCellValue());
            jsonArray.add(jsonObject);
        }
        logger.info(jsonArray);
        return jsonArray;
    }

    /**
     * 激活测站
     */
    public void activateStation(Trade trade) {
        Station station = findByInstanceId(trade.getInstanceId());
        if (station == null) {
            station = new Station();
        }
        JSONObject remarkJson = JSONObject.fromObject(trade.getRemark());
        Long times = remarkJson.getLong("expireDate");
        station.setExpireDate(new Date(times));
        station.setInstanceId(trade.getInstanceId());
        station.setType(CommonEnum.StationType.valueOf(trade.getBaseType().toString()));
        station.setUser(trade.getUser());
        save(station);
    }

    public Station findByInstanceId(String instanceId) {
        List<Filter> filters = new ArrayList<>();
        filters.add(Filter.eq("instanceId", instanceId));
//        filters.add(Filter.eq("user", user));
        List<Station> list = stationDao.findList(0, null, filters);
        if (list != null && list.size() == 1) {
            return list.get(0);
        }
        return null;
    }

    /**
     * 监测系统取得所有已改变的参数列表
     * @return
     */
    public JSONArray getParameters() {
        JSONArray paramters = new JSONArray();
        List<Station> stations = getStationsByTransform();
        if(stations==null||stations.size()==0){
            return paramters;
        }
        JSONObject paramter,code;
        JSONArray sensorsJson;
        Station station;
        List<Sensor> sensors;
        for(int i = 0;i<stations.size();i++){
            station = stations.get(i);
            paramter = new JSONObject();
            paramter.put("instanceId",station.getInstanceId());
            paramter.put("name",station.getName());
            paramter.put("parameters",StringUtils.hasText(station.getParameter())?station.getParameter():"");
            sensors = station.getSensors();
            sensorsJson = new JSONArray();
            for(int k =0;k<sensors.size();k++){
                code = new JSONObject();
                code.put("code",sensors.get(k).getCode());
                sensorsJson.add(code);
            }
            paramter.put("sensors",sensorsJson);
            paramters.add(paramter);
        }
        return paramters;
    }

    /**
     * 监测系统取得所有已改变的测站列表
     */
    private List<Station> getStationsByTransform() {
        List<Filter> filters = new ArrayList<>();
        filters.add(Filter.eq("transform",true));
        List<Station> stations = stationDao.findList(0,null,filters);
        return stations;
    }


    /**
     * 测站编辑
     *
     * @param map
     * @return
     */
    public boolean edit(Map<String, Object> map, Station station) {
        if (map.get("type") != null && StringUtils.hasText(map.get("type").toString())) {
            station.setType(CommonEnum.StationType.valueOf(map.get("type").toString()));
        }
        if (map.get("name") != null && StringUtils.hasText(map.get("name").toString())) {
            station.setName(map.get("name").toString());
        }
        if (map.get("description") != null && StringUtils.hasText(map.get("description").toString())) {
            station.setDescription(map.get("description").toString());
        }
        if (map.get("address") != null && StringUtils.hasText(map.get("address").toString())) {
            station.setAddress(map.get("address").toString());
        }
        if (map.get("coor") != null && StringUtils.hasText(map.get("coor").toString())) {
            JSONObject jsonObject = SettingUtils.checkCoordinateIsInvalid(map.get("coor").toString());
            if (jsonObject == null) {
                return false;
            }
            station.setCoor(jsonObject.toString());
        }
        stationDao.save(station);
        return true;
    }

    /**
     * 保存仪表归属并添加至缓存激活
     * @param code
     * @param infoJson
     * @param station
     * @param sensor
     * @return
     */
    public JSONObject saveAndAddCache(String code, JSONObject infoJson, Station station, Sensor sensor) {
        sensor.setCode(code);
        sensor.setActivate(false);
        sensor.setInfo(infoJson.isEmpty()?null:infoJson.toString());
        sensor.setStation(station);
        sensorService.save(sensor);
        monitorService.add(code);
        return sensorService.makeSensorJson(sensor);
    }

    public boolean cameraVerify(Map<String, Object> cameraMap, JSONObject infoJson, Station station, Sensor sensor) {
        if (cameraMap.get("phone") != null && StringUtils.hasText(cameraMap.get("phone").toString())) {
            if (!SettingUtils.phoneRegex(cameraMap.get("phone").toString())) {
                return false;
            }
            infoJson.put("phone", cameraMap.get("phone").toString());
        }
        if (cameraMap.get("contact") != null && StringUtils.hasText(cameraMap.get("contact").toString())) {
            infoJson.put("contact", cameraMap.get("contact").toString());
        }
        if (cameraMap.get("factory") != null && StringUtils.hasText(cameraMap.get("factory").toString())) {
            infoJson.put("factory", cameraMap.get("factory").toString());
        }
        //视频地址:rtmp://rtmp.open.ys7.com/openlive/ba4b2fde89ab43739e3d3e74d8b08f4a.hd
        if (cameraMap.get("cameraUrl") != null && StringUtils.hasText(cameraMap.get("cameraUrl").toString())) {
            String cameraUrl = cameraMap.get("cameraUrl").toString();
            if (!SettingUtils.parameterRegex(cameraUrl)) {
                return false;
            }
            sensor.setCameraUrl(cameraMap.get("cameraUrl").toString());
        }
        return true;
    }

    /**
     * sensor参数效验
     * @return
     */
    public boolean sensorVerify(Map<String, Object> map, Sensor sensor, JSONObject infoJson) {
        if(map.get("factory")!=null&&StringUtils.hasText(map.get("factory").toString())){
            infoJson.put("factory",map.get("factory").toString());
        }
        if(map.get("contact")!=null&&StringUtils.hasText(map.get("contact").toString())){
            infoJson.put("contact",map.get("contact").toString());
        }
        if(map.get("phone")!=null&&StringUtils.hasText(map.get("phone").toString())){
            if(!SettingUtils.phoneRegex(map.get("phone").toString())){
                return false;
            }
            infoJson.put("phone",map.get("phone").toString());
        }
        if(map.get("settingHeight")!=null&&StringUtils.hasText(map.get("settingHeight").toString())){
            Double settingHeight = Double.valueOf(map.get("settingHeight").toString());
            if (settingHeight>100.0||settingHeight<0.0) {
                return false;
            }
            sensor.setSettingHeight(settingHeight);
        }
        return true;
    }

    /* 测站续费
     * @param trade
     */
    public void renewStation(Trade trade) {
        Station station = findByInstanceId(trade.getInstanceId());
        JSONObject remarkJson = JSONObject.fromObject(trade.getRemark());
        Long times = remarkJson.getLong("expireDate");
        station.setExpireDate(new Date(times));
        save(station);
    }

    /**
     * 下载测站河道模型及水位流量excle文件
     *
     * @param station
     * @return
     */
    public Workbook makeStationModelData(Station station) {
        Workbook workbook = null;
        try {
            workbook = new XSSFWorkbook();
            // workbook = new HSSFWorkbook();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (workbook == null) {
            return workbook;
        }
        Sheet sheet;
        JSONArray jsonArray;
        CellStyle style = getStyle(workbook);
        if (StringUtils.hasText(station.getRiverModel())) {
            sheet = workbook.createSheet("河道模型");
            jsonArray = JSONArray.fromObject(station.getRiverModel());
            makeExcle(sheet, jsonArray, style, "riverModel");
        }
        if (StringUtils.hasText(station.getFlowModel())) {
            sheet = workbook.createSheet("水位流量曲线");
            jsonArray = JSONArray.fromObject(station.getFlowModel());
            makeExcle(sheet, jsonArray, style, "flowModel");
        }
        return workbook;
    }

    /**
     * 将json数据写入sheet
     *
     * @param sheet
     * @param jsonArray
     */
    private void makeExcle(Sheet sheet, JSONArray jsonArray, CellStyle style, String type) {
        Row row;
        row = sheet.createRow(0);
        Cell cell;
        cell = row.createCell(0, Cell.CELL_TYPE_STRING);
        cell.setCellStyle(style);
        cell.setCellValue(type.equals("riverModel") ? "桩号" : "水位（m）");
        cell = row.createCell(1, Cell.CELL_TYPE_STRING);
        cell.setCellStyle(style);
        cell.setCellValue(type.equals("riverModel") ? "高程" : "流量（m3/s）");
        sheet.autoSizeColumn(2);
        JSONObject jsonObject;
        for (int rowNum = 1; rowNum <= jsonArray.size(); rowNum++) {
            jsonObject = jsonArray.getJSONObject(rowNum - 1);
            row = sheet.createRow(rowNum);
            for (int i = 0; i < 2; i++) {
                cell = row.createCell(i, Cell.CELL_TYPE_STRING);
                cell.setCellStyle(style);
                cell.setCellValue(i == 0 ? jsonObject.getDouble("x") : jsonObject.getDouble("y"));
            }
        }
    }

    /**
     * 设置样式
     *
     * @param workbook
     * @return
     */
    private CellStyle getStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        Font font = workbook.createFont();
        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        font.setFontName("宋体");//设置字体名称
        style.setFont(font);
        style.setBorderTop(HSSFCellStyle.BORDER_THIN);//上边框
        style.setBorderBottom(HSSFCellStyle.BORDER_THIN);//下边框
        style.setBorderLeft(HSSFCellStyle.BORDER_THIN);//左边框
        style.setBorderRight(HSSFCellStyle.BORDER_THIN);//右边框
        return style;
    }

    /**
     * 测站分享
     *
     * @param stationIds
     * @param userIds
     * @param own
     */
    public void shares(List<String> stationIds, List<String> userIds, User own) {
        Share share;
        User user;
        Station station;
        for (int i = 0; i < stationIds.size(); i++) {
            station = stationDao.find(Long.valueOf(stationIds.get(i)));
            if (station == null || !station.getUser().getId().equals(own.getId())) {
                continue;
            }
            share = makeShare(station);
            for (int k = 0; k < userIds.size(); k++) {
                user = userService.find(Long.valueOf(userIds.get(k)));
                if (user.getId().equals(own.getId())) {
                    continue;
                }
                share.register(user);
                userMessageService.stationShareMessage(station,user,true);
            }
            saveShare(share);
        }
    }

    /**
     * 取消测站分享
     *
     * @param station
     * @param userIds
     */
    public void unShare(Station station, List<String> userIds) {
        Long userId;
        User user;
        boolean flag;
        Share share = makeShare(station);
        for (int i = 0; i < userIds.size(); i++) {
            userId = Long.valueOf(userIds.get(i));
            user = userService.find(userId);
            if (user == null) {
                continue;
            }
            flag = share.unRegister(user);
            userMessageService.stationShareMessage(station,user,false);
            if (flag) {
                saveShare(share);
            }
        }
    }

    /**
     * 构建测站分享对象
     *
     * @param station
     * @return
     */
    private Share makeShare(Station station) {
        Share share = new Share(station);
        JSONArray jsonArray;
        if (StringUtils.hasText(station.getShares())) {
            jsonArray = JSONArray.fromObject(station.getShares());
        } else {
            return share;
        }
        User user;
        JSONObject jsonObject;
        for (int i = 0; i < jsonArray.size(); i++) {
            jsonObject = (JSONObject) jsonArray.get(i);
            user = new User();
            user.setId(jsonObject.getLong("id"));
            user.setName(jsonObject.get("name")==null?null:jsonObject.getString("name"));
            user.setPhone(jsonObject.getString("phone"));
            share.register(user);
        }
        return share;
    }

    /**
     * 判断是否分享于该用户
     *
     * @param user
     * @param station
     * @return
     */
    private boolean isShare(User user, Station station) {
        if (user.getId().equals(station.getUser().getId())) {
            return true;
        }
        Share share = makeShare(station);
        List<User> users = share.getShareUsers();
        for (int i = 0; i < users.size(); i++) {
            if (user.getId().equals(users.get(i).getId())) {
                return true;
            }
        }
        return false;
    }


    private void saveShare(Share share) {
        Station station = share.getStation();
        JSONArray shareJsons = share.toJson();
        station.setShares(shareJsons.isEmpty() ? null : shareJsons.toString());
        stationDao.save(station);
    }


    /**
     * 根据ids字符串获取station列表
     * @param stationIds
     * @return
     */
    public List<Station> findByIdList(Object stationIds) {
        String[] split = stationIds.toString().split(",");
        Station station;
        List<Station> stations = new ArrayList<>();
        for (String s : split) {
            station = find(Long.valueOf(s));
            stations.add(station);
        }
        return stations;
    }

    /**
     * 测站用有不属于当前用户的测站，返回true
     * @param stations
     * @param user
     * @return
     */
    public boolean stationIsBelongtoCurrentUser(List<Station> stations, User user) {
        for (Station station : stations) {
            if (!station.getUser().getId().equals(user.getId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 注册访问权限
     * @param stations
     * @param accounts
     */
    public void cooperateMul(List<Station> stations, List<Account> accounts) {
        Cooperate cooperate;
        for (Station station : stations) {
            cooperate = new Cooperate(station);
            makeCooperateVisits(station,cooperate);
            for (Account account : accounts) {
                cooperate.register(account);
                accountMessageService.cooperateStationMessage(station,account,true);
            }
            cooperate.addToStation();
            save(station);
        }
    }

    /**
     * 注销测站查看权限
     * @param station
     * @param accounts
     */
    public void unCooperate(Station station, List<Account> accounts) {
        Cooperate cooperate = new Cooperate(station);
        makeCooperateVisits(station,cooperate);
        for (Account account : accounts) {
            Iterator<Account> iterator = cooperate.getVisits().iterator();
            while (iterator.hasNext()) {
                Account account1 = iterator.next();
                if (account.getId().equals(account1.getId())) {
                    cooperate.unRegister(account);
                    accountMessageService.cooperateStationMessage(station, account, false);
                    iterator = cooperate.getVisits().iterator();
                }
            }
        }
        cooperate.addToStation();
        save(station);
        flush();
    }

    /**
     * 构建visits
     * @param station
     * @param cooperate
     */
    private void makeCooperateVisits(Station station, Cooperate cooperate) {
        Account account1;
        if (station.getCooperate() != null) {
            JSONArray jsonArray = JSONArray.fromObject(station.getCooperate());
            List<Account> visits = new ArrayList<>();
            for (Object o : jsonArray) {
                JSONObject jsonObject = JSONObject.fromObject(o);
                account1 = new Account();
                account1.setId(Long.valueOf(jsonObject.get("id").toString()));
                account1.setName(jsonObject.get("name").toString());
                account1.setPhone(jsonObject.get("phone").toString());
                visits.add(account1);
            }
            cooperate.setVisits(visits);
        }
    }

    /**
     * 解绑子账号
     * @param user
     * @param account
     */
    public void unCooperate(User user, Account account) {
        List<Station> stations = findByUser(user);
        for (int i = 0; i < stations.size(); i++) {
            List<Account> accounts = new ArrayList<>();
            accounts.add(account);
            unCooperate(stations.get(i),accounts);
        }
    }
}
