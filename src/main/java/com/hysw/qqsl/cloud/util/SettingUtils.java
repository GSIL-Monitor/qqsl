package com.hysw.qqsl.cloud.util;

import com.aliyun.oss.common.utils.IOUtils;
import com.hysw.qqsl.cloud.CommonAttributes;
import com.hysw.qqsl.cloud.core.entity.Setting;
import com.hysw.qqsl.cloud.core.entity.buildModel.GeoCoordinate;
import net.sf.json.JSONObject;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StringUtils;
import org.xml.sax.SAXException;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 系统设置
 *
 * @since 2015年8月10日
 * @author 雪庭(flysic) qq: 119238122 github: https://github.com/flysic
 *
 */
public class SettingUtils {

	private Setting setting = new Setting();
	/** 单例 */
	private static SettingUtils instance;

	public SettingUtils() {
		try {
			File qqslXmlFile = new ClassPathResource(
					CommonAttributes.QQSL_XML_PATH).getFile();
			SAXReader reader = new SAXReader();
			try {
				reader.setFeature(
						"http://apache.org/xml/features/nonvalidating/load-external-dtd",
						false);
			} catch (SAXException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			Document document = reader.read(qqslXmlFile);
			@SuppressWarnings("unchecked")
			List<Element> elements = document.selectNodes("/qqsl/setting");
			for (Element element : elements) {
				String name = element.attributeValue("name");
				String value = element.attributeValue("value");
				try {
					BeanUtilsBean.getInstance().setProperty(setting, name,
							value);
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * 获取根元素
	 * 
	 * @return
	 * @throws DocumentException
	 */
	public Element getRootElement(String path) throws DocumentException {
		File file = null;
		try {
			file = new ClassPathResource(path).getFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		SAXReader reader = new SAXReader();
		try {
			reader.setFeature(
					"http://apache.org/xml/features/nonvalidating/load-external-dtd",
					false);
		} catch (SAXException e1) {
			e1.printStackTrace();
		}
		Document doc = reader.read(file);
		Element elem = doc.getRootElement();
		return elem;
	}

	/**
	 * 获取对应的复合要素xml的节点
	 * 
	 * @param element
	 * @return
	 */
	public List<Element> getElementGroupList(Element element) {
		@SuppressWarnings("unchecked")
		List<Element> elements = element.elements();
		return elements;
	}

	/**
	 * 递归遍历方法
	 * 
	 * @param element
	 */
	public List<Element> getElementList(Element element) {
		@SuppressWarnings("unchecked")
		List<Element> elements = element.elements();
		if (elements.size() == 0) {
		} else {
			// 有子元素
			for (Iterator<Element> it = elements.iterator(); it.hasNext();) {
				Element elem = (Element) it.next();
				// 递归遍历
				getElementList(elem);
			}
		}
		return elements;
	}

	/**
	 * 单例
	 * 
	 * @return
	 */
	public static SettingUtils getInstance() {
		if (instance == null) {
			instance = new SettingUtils();
		}
		return instance;
	}

	/**
	 * 取得系统参数
	 * 
	 * @return
	 */
	public Setting getSetting() {
		return this.setting;
	}

	/**
	 * 匹配字符串中是否存在要匹配的字符串对象(全单词匹配)
	 * 
	 * @param regex
	 *            要匹配的字符串
	 * @param describtion
	 *            从数据库查询到的字符串
	 * @return
	 */
	public static boolean stringMatcher(String regex, String describtion) {
		if(describtion==null){
			return false;
		}
		String s = "\\b" + regex + "\\b";
		Pattern p = Pattern.compile(s);
		Matcher m = p.matcher(describtion);
		boolean b = m.find();
		return b;

	}

	/**
	 * 验证项目编号以及用户名为非汉字且不能包含特殊字符
	 * 
	 * @param parameter
	 * @return
	 */
	public static boolean parameterRegex(String parameter) {
		if(!StringUtils.hasText(parameter)){
			return false;
		}
		String regex = "^[a-zA-Z0-9\\-]+$";
		return regexResult(parameter,regex);
	}

	/**
	 * 匹配中文
	 * @param parameter
	 * @return
     */
	public static boolean chineseRegex(String parameter){
		if(!StringUtils.hasText(parameter)){
			return false;
		}
		//String regex = "[\\u4e00-\\u9fa5]*";
		String regex = "^[\\u4E00-\\u9FA5]+$";
		return regexResult(parameter,regex);
	}
	/**
	 * 手机号码正则验证
	 * @param phone
	 * @return
	 */
	public static boolean phoneRegex(String phone){
		if(!StringUtils.hasText(phone)){
			return false;
		}
		String regex = "^(166|13[0-9]|17[0-9]|14[5|7]|15[0|1|2|3|5|6|7|8|9]|18[0|1|2|3|5|6|7|8|9])\\d{8}$";
		return regexResult(phone,regex);
	}

	/**
	 * 邮箱正则验证
	 * @param email
	 * @return
	 */
	public static boolean emailRegex(String email){
		if(!StringUtils.hasText(email)){
			return false;
		}
		String regex = "^\\w+((-\\w+)|(\\.\\w+))*\\@[A-Za-z0-9]+((\\.|-)[A-Za-z0-9]+)*\\.[A-Za-z0-9]+$";
		return regexResult(email,regex);
	}

	/**
	 * userName正则验证(字母数字)
	 * @param userName
	 * @return
	 */
	public static boolean userNameRegexNumber(String userName){
		if(!StringUtils.hasText(userName)){
			return false;
		}
		if (userName.length() > 36) {
			return false;
		}
		String regex = "^[a-zA-Z0-9]+$";
		return regexResult(userName,regex);
	}

	/**
	 * userName正则验证(字母中文)
	 * @param userName
	 * @return
	 */
	public static boolean userNameRegexChinese(String userName){
		if(!StringUtils.hasText(userName)){
			return false;
		}
		String regex = "^[a-zA-Z0-9\u4E00-\u9FA5]+$";
		return regexResult(userName,regex);
	}

	/**
	 * httpUrl验证
	 * @param url
	 * @return
	 */
	public static boolean httpUrlRegex(String url){
		if(!StringUtils.hasText(url)){
			return false;
		}
		String regex = "^(http|www|ftp|)?(://)?(\\w+(-\\w+)*)(\\.(\\w+(-\\w+)*))*((:\\d+)?)(/(\\w+(-\\w+)*))*(\\.?(\\w)*)(\\?)?(((\\w*%)*(\\w*\\?)*(\\w*:)*(\\w*\\+)*(\\w*\\.)*(\\w*&)*(\\w*-)*(\\w*=)*(\\w*%)*(\\w*\\?)*(\\w*:)*(\\w*\\+)*(\\w*\\.)*(\\w*&)*(\\w*-)*(\\w*=)*)*(\\w*)*)$";
		return regexResult(url,regex);
	}

	public static boolean rtmpRegex(String url){
		if(!StringUtils.hasText(url)){
			return false;
		}
		if(!url.startsWith("rtmp://rtmp.open.ys7.com/openlive/")){
			return false;
		}
		String end = url.substring(url.lastIndexOf("/")+1);
		return Pattern.compile("[A-z0-9]+\\.(?!\\.)").matcher(end).find();
	}

	private static boolean regexResult(String parameter, String regex) {
		Pattern pattern = Pattern.compile(regex);
		Matcher match = pattern.matcher(parameter);
		return match.matches();
	}

	/**
	 * 深度克隆
	 * 
	 * @param oldObj
	 * @return
	 */
	public static Object objectCopy(Object oldObj) {
		Object newObj = null;
		ByteArrayOutputStream bo = null;
		ObjectOutputStream oo = null;
		ByteArrayInputStream bi = null;
		ObjectInputStream oi = null;
		try {
			bo = new ByteArrayOutputStream();
			oo = new ObjectOutputStream(bo);
			oo.writeObject(oldObj);// 源对象
			bi = new ByteArrayInputStream(bo.toByteArray());
			oi = new ObjectInputStream(bi);
			newObj = oi.readObject();// 目标对象
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}finally {
			IOUtils.safeClose(bo);
			IOUtils.safeClose(oo);
			IOUtils.safeClose(bi);
			IOUtils.safeClose(oi);
		}
		return newObj;
	}


	/**
	 * 计算地球上任意两点(经纬度)距离
	 * @param lon1
	 * @param lat1
	 * @param lon2
	 * @param lat2
	 * @return
	 */
	public static double distance(double lon1, double lat1, double lon2, double lat2) {
		double a, b, R;
		R = 6378137; // 地球半径
		lat1 = lat1 * Math.PI / 180.0;
		lat2 = lat2 * Math.PI / 180.0;
		a = lat1 - lat2;
		b = (lon1 - lon2) * Math.PI / 180.0;
		double d;
		double sa2, sb2;
		sa2 = Math.sin(a / 2.0);
		sb2 = Math.sin(b / 2.0);
		d = 2* R* Math.asin(Math.sqrt(sa2 * sa2 + Math.cos(lat1)* Math.cos(lat2) * sb2 * sb2));
		return d;
	}

	public static boolean coordinateParameterCheck(Object longitude, Object latitude, Object elevation) {
		if (Double.valueOf(longitude.toString()) > 180 || Double.valueOf(longitude.toString()) < 0) {
			return false;
		}
		if (Double.valueOf(latitude.toString()) > 90 || Double.valueOf(latitude.toString()) < 0) {
			return false;
		}
		if (Double.valueOf(elevation.toString()) < 0) {
			return false;
		}
		return true;
	}

	/**
	 * 检查坐标格式有效性ing转换为json格式
	 *
	 * @return
	 */
	public static JSONObject checkCoordinateIsInvalid(String coordinate) {
		String[] coordinates = coordinate.split(",");
		if (coordinates.length != 3) {
			return null;
		}
		if (Double.valueOf(coordinates[0]) > 180 || Double.valueOf(coordinates[0]) < 0) {
			return null;
		}
		if (Double.valueOf(coordinates[1]) > 90 || Double.valueOf(coordinates[1]) < 0) {
			return null;
		}
		if (Double.valueOf(coordinates[2]) < 0) {
			return null;
		}
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("longitude", coordinates[0]);
		jsonObject.put("latitude", coordinates[1]);
		jsonObject.put("elevation", coordinates[2]);
		return jsonObject;
	}

	/**
	 * excle 文件的读取
	 * @param is
	 * @param str
	 * @return
	 * @throws IOException
	 */
	public static Workbook readExcel(InputStream is,  String str) throws IOException {
		Workbook wb=null;
		if (str.trim().toLowerCase().equals("xls")) {
			wb = new HSSFWorkbook(is);
		} else if (str.trim().toLowerCase().equals("xlsx")) {
			wb = new XSSFWorkbook(is);
		}
		return wb;
	}

	/**
	 * excle 文件的读取
	 * @param filePath
	 * @return
	 * @throws IOException
	 */
	public static Workbook readExcel(String filePath) throws IOException {
		File file = null;
		try {
			file = new ClassPathResource(filePath).getFile();
		}catch (Exception e){
			e.printStackTrace();
		}
		Workbook wb=null;
		InputStream is  = new FileInputStream(file);
		String str = filePath.substring(filePath.lastIndexOf(".")+1);
		if (str.trim().toLowerCase().equals("xls")) {
			wb = new HSSFWorkbook(is);
		} else if (str.trim().toLowerCase().equals("xlsx")) {
			wb = new XSSFWorkbook(is);
		}
		return wb;
	}

	/**
	 * Map转Json
	 * @param map
	 * @return
	 */
	public static JSONObject convertMapToJson(Map<String, Object> map){
		JSONObject jsonObject = new JSONObject();
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			jsonObject.put(entry.getKey(),entry.getValue());
		}
		return jsonObject;
	}

	/**
	 * 手机获取的验证码
	 *
	 * @return
	 */
	public static String createRandomVcode() {
		// 验证码
		String vcode = "";
		for (int i = 0; i < 6; i++) {
			vcode = vcode + (int) (Math.random() * 10);
		}
		return vcode;
	}

	/**
	 * 过滤枚举被废弃的值
	 * @param enumm
	 * @param name
	 * @return
	 */
	public static boolean changeDeprecatedEnum(Object enumm,String name){
		Field field = null;
		try {
			field = enumm.getClass().getField(name);
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		Deprecated support = field.getAnnotation(Deprecated.class);
		if (support != null&&support.annotationType().getName().equalsIgnoreCase("java.lang.Deprecated")) {
			return true;
		}
		return false;
	}

	/**
	 * 根据输入的地点坐标计算中心点
	 * @param geoCoordinateList
	 * @return
	 */
	public static GeoCoordinate getCenterPointFromListOfCoordinates(List<GeoCoordinate> geoCoordinateList) {
		int total = geoCoordinateList.size();
		double X = 0, Y = 0, Z = 0;
		for (GeoCoordinate g : geoCoordinateList) {
			double lat, lon, x, y, z;
			lat = g.getLatitude() * Math.PI / 180;
			lon = g.getLongitude() * Math.PI / 180;
			x = Math.cos(lat) * Math.cos(lon);
			y = Math.cos(lat) * Math.sin(lon);
			z = Math.sin(lat);
			X += x;
			Y += y;
			Z += z;
		}
		X = X / total;
		Y = Y / total;
		Z = Z / total;
		double Lon = Math.atan2(Y, X);
		double Hyp = Math.sqrt(X * X + Y * Y);
		double Lat = Math.atan2(Z, Hyp);
		return new GeoCoordinate(Lat * 180 / Math.PI, Lon * 180 / Math.PI);
	}

}
