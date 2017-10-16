package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.CommonEnum;
import com.hysw.qqsl.cloud.core.controller.Message;
import com.hysw.qqsl.cloud.core.dao.StationDao;
import com.hysw.qqsl.cloud.core.entity.*;
import com.hysw.qqsl.cloud.core.entity.data.Sensor;
import com.hysw.qqsl.cloud.core.entity.data.Station;
import com.hysw.qqsl.cloud.core.entity.data.User;
import com.hysw.qqsl.cloud.core.entity.station.Camera;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
    private AuthentService authentService;

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
            stationModel.setPrice(Long.valueOf(element.attributeValue("price")));
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
        jsonObject.put("name", stationModel.getName());
        jsonObject.put("type", stationModel.getType());
        jsonObject.put("description", stationModel.getDescription());
        return jsonObject;
    }

    /**
     * 获取对于用户的水文测站
     *
     * @param user
     * @return
     */
    public List<JSONObject> getStations(User user) {
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
        //jsonObject.put("exprieDate",station.getExprieDate());
        jsonObject.put("createDate", station.getCreateDate());
        jsonObject.put("instanceId", station.getInstanceId());
        jsonObject.put("picture", station.getPicture());
        jsonObject.put("riverModel", station.getRiverModel()==null?null:JSONArray.fromObject(station.getRiverModel()));
        jsonObject.put("flowModel", station.getFlowModel()==null?null:JSONArray.fromObject(station.getFlowModel()));
        jsonObject.put("bottomElevation", station.getBottomElevation());
        jsonObject.put("name", station.getName());
        jsonObject.put("type", station.getType());
        jsonObject.put("shares", station.getShares());
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
    private List<Station> findByUser(User user) {
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
    public Message readModelFile(MultipartFile mFile, Station station) throws IOException, QQSLException {
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
        return new Message(Message.Type.OK,model);
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
        Calendar c = Calendar.getInstance();
        c.setTime(trade.getPayDate());
        c.add(Calendar.YEAR, 1);
        station.setExpireDate(new Date(c.getTimeInMillis()));
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
    public JSONArray getParamters() {
        List<Station> stations = getStationsByTransform();
        if(stations==null||stations.size()==0){
            return null;
        }
        JSONArray paramters = new JSONArray();
        JSONObject paramter,code;
        JSONArray sensorsJson;
        Station station;
        List<Sensor> sensors;
        for(int i = 0;i<stations.size();i++){
            station = stations.get(i);
            paramter = new JSONObject();
            paramter.put("instanceId",station.getInstanceId());
            paramter.put("name",station.getName());
            paramter.put("paramters",station.getParameter());
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
    public Message edit(Map<String, Object> map, Station station) {
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
            Message message = SettingUtils.checkCoordinateIsInvalid(map.get("coor").toString());
            if (!Message.Type.OK.equals(message.getType())) {
                return message;
            }
            station.setCoor(message.getData().toString());
        }
        stationDao.save(station);
        return new Message(Message.Type.OK);
    }

    /**
     * 仪表添加
     *
     * @param map
     * @return
     */
    public Message addSensor(Map<String, Object> map, Station station) {
        if(map.get("code")==null||!StringUtils.hasText(map.get("code").toString())){
            return new Message(Message.Type.FAIL);
        }
        if(map.get("ciphertext")==null||!StringUtils.hasText(map.get("ciphertext").toString())){
            return new Message(Message.Type.FAIL);
        }
        String code = map.get("code").toString();
        String ciphertext = map.get("ciphertext").toString();
        Sensor sensor = sensorService.findByCode(code);
        if (sensor != null) {
            return new Message(Message.Type.EXIST);
        }
        //return new Message(Message.Type.OK);
        //加密并验证密码是否一致
        boolean verify = monitorService.verify(code,ciphertext);
        if (verify) {
             //注册成功
            return verify(map, station);
        } else {
            return new Message(Message.Type.FAIL);
        }
    }

    /**
     * 添加摄像头
     * @param cameraMap
     * @param station
     * @return
     */
    public Message addCamera(Map<String, Object> cameraMap, Station station) {
        //判断测站类型,当前测站为水位站时,只能绑定一个仪表,一个摄像头
        Sensor sensor = new Sensor();
        sensor.setType(Sensor.Type.CAMERA);
        Message message = isHydrologyStation(station,"camera");
        if (!message.getType().equals(Message.Type.OK)){
            return message;
        }
        JSONObject infoJson = new JSONObject();
        if(cameraMap.get("phone")!=null&&StringUtils.hasText(cameraMap.get("phone").toString())){
            if(!SettingUtils.phoneRegex(cameraMap.get("phone").toString())){
                return new Message(Message.Type.FAIL);
            }
            infoJson.put("phone",cameraMap.get("phone").toString());
        }
        if(cameraMap.get("contact")!=null&&StringUtils.hasText(cameraMap.get("contact").toString())){
            infoJson.put("contact",cameraMap.get("contact").toString());
        }
        if(cameraMap.get("factory")!=null&&StringUtils.hasText(cameraMap.get("factory").toString())){
            infoJson.put("factory",cameraMap.get("factory").toString());
        }
        //视频地址:rtmp://rtmp.open.ys7.com/openlive/ba4b2fde89ab43739e3d3e74d8b08f4a.hd
        if(cameraMap.get("cameraUrl")!=null&&StringUtils.hasText(cameraMap.get("cameraUrl").toString())){
            String cameraUrl = cameraMap.get("cameraUrl").toString();
            if(!SettingUtils.rtmpRegex(cameraUrl)){
                return new Message(Message.Type.FAIL);
            }
            sensor.setCameraUrl(cameraMap.get("cameraUrl").toString());
        }
        sensor.setInfo(infoJson.isEmpty()?null:infoJson.toString());
        sensor.setStation(station);
        sensorService.save(sensor);
        JSONObject cameraJson = new JSONObject();
        cameraJson.put("id",sensor.getId());
        return new Message(Message.Type.OK,cameraJson);
    }

    /**
     * 当前测站为水位站时,只能绑定一个仪表,一个摄像头
     * @param station
     * @return
     */
    public Message isHydrologyStation(Station station,String type){
        List<Sensor> sensors = station.getSensors();
        if(sensors.size()==0){
            return new Message(Message.Type.OK);
        }
        if(type.equals("camera")){
            for(int i = 0;i<sensors.size();i++){
                if(Sensor.Type.CAMERA.equals(sensors.get(i).getType())){
                    logger.info("测站Id:" + station.getId() + ",水位站已有仪表绑定");
                    return new Message(Message.Type.EXIST);
                }
            }
        }else{
            for(int i = 0;i<sensors.size();i++){
                if(!Sensor.Type.CAMERA.equals(sensors.get(i).getType())){
                    logger.info("测站Id:" + station.getId() + ",水位站已有仪表绑定");
                    return new Message(Message.Type.EXIST);
                }
            }
        }
        return new Message(Message.Type.OK);
    }

    /**
     * 激活仪器
     * @return
     */
    public Message verify( Map<String,Object> map,Station station) {
        JSONObject infoJson = new JSONObject();
        Sensor sensor = new Sensor();
        String code = map.get("code").toString();
        if(map.get("factory")!=null&&StringUtils.hasText(map.get("factory").toString())){
            infoJson.put("factory",map.get("factory").toString());
        }
        if(map.get("contact")!=null&&StringUtils.hasText(map.get("contact").toString())){
            infoJson.put("contact",map.get("contact").toString());
        }
        if(map.get("phone")!=null&&StringUtils.hasText(map.get("phone").toString())){
            if(!SettingUtils.phoneRegex(map.get("phone").toString())){
                return new Message(Message.Type.OTHER);
            }
            infoJson.put("phone",map.get("phone").toString());
        }
        if(map.get("settingHeight")!=null&&StringUtils.hasText(map.get("settingHeight").toString())){
                Double settingHeight = Double.valueOf(map.get("settingHeight").toString());
                if (settingHeight>100.0||settingHeight<0.0) {
                    return new Message(Message.Type.FAIL);
                }
            sensor.setSettingHeight(settingHeight);
        }
        //判断测站类型,当前测站为水位站时,只能绑定一个仪表,一个摄像头
        Message message = isHydrologyStation(station,"other");
        if (!message.getType().equals(Message.Type.OK)){
            return message;
        }
        sensor.setCode(map.get("code").toString());
        sensor.setActivate(false);
        sensor.setInfo(infoJson.isEmpty()?null:infoJson.toString());
        sensor.setStation(station);
        sensorService.save(sensor);
        monitorService.add(code);
        JSONObject seneorJson = sensorService.makeSensorJson(sensor);
        return new Message(Message.Type.OK,seneorJson);
    }

    /* 测站续费
     * @param trade
     */
    public void renewStation(Trade trade) {
        Station station = findByInstanceId(trade.getInstanceId());
        Calendar c = Calendar.getInstance();
        c.setTime(station.getExpireDate());
        c.add(Calendar.YEAR, 1);
        station.setExpireDate(new Date(c.getTimeInMillis()));
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
        for (int rowNum = 1; rowNum < jsonArray.size(); rowNum++) {
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

    /**maxValue/minValue/phone/sendStatus
     * 编辑测站参数
     *
     * @param map
     * @param station
     * @return
     */
    public Message editParameter(Map<String, Object> map, Station station) {
        double maxValue,minValue;
        try{
            if(map.get("maxValue")!=null){
                maxValue = Double.valueOf(map.get("maxValue").toString());
                if(maxValue<0||maxValue>100){
                    return new Message(Message.Type.FAIL);
                }
                if(map.get("minValue")!=null){
                    minValue = Double.valueOf(map.get("minValue").toString());
                    if(minValue<0||minValue>100){
                        return new Message(Message.Type.FAIL);
                    }
                    if (minValue>maxValue){
                        return new Message(Message.Type.FAIL);
                    }
                }
            }
        }catch (NumberFormatException e){
            return new Message(Message.Type.FAIL);
        }
        if(map.get("phone")!=null){
            if(!SettingUtils.phoneRegex(map.get("phone").toString())){
                return new Message(Message.Type.FAIL);
            }
        }
        if(map.get("sendStatus")!=null){
            boolean falg = "true".equals(map.get("sendStatus").toString())||"false".equals(map.get("sendStatus"));
            if(!falg){
                return new Message(Message.Type.FAIL);
            }
        }
        JSONObject jsonObject = SettingUtils.convertMapToJson(map);
        station.setParameter(jsonObject.isEmpty()?null:jsonObject.toString());
        station.setTransform(true);
        stationDao.save(station);
        return new Message(Message.Type.OK);
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
            }
            saveShare(share);
        }
    }

    /**
     * 取消测站分享
     *
     * @param station
     * @param userIds
     * @param own
     */
    public void unShare(Station station, List<String> userIds, User own) {
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
            if (flag) {
                saveShare(share, user, own);
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

    private void saveShare(Share share, User user, User own) {
        Station station = share.getStation();
        JSONArray shareJsons = share.toJson();
        station.setShares(shareJsons.isEmpty() ? null : shareJsons.toString());
        stationDao.save(station);
        //记录仪表分享消息
        // userMessageService.sensorShareMessage(sensor,user,own,true);
    }

    private void saveShare(Share share) {
        Station station = share.getStation();
        JSONArray shareJsons = share.toJson();
        station.setShares(shareJsons.isEmpty() ? null : shareJsons.toString());
        stationDao.save(station);
    }


}
