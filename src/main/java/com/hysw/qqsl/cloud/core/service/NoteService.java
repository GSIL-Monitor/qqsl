package com.hysw.qqsl.cloud.core.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import com.alicom.mns.tools.DefaultAlicomMessagePuller;
import com.alicom.mns.tools.MessageListener;
import com.aliyun.mns.model.Message;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.QuerySendDetailsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.QuerySendDetailsResponse;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.google.gson.Gson;
import com.hysw.qqsl.cloud.CommonAttributes;
import com.hysw.qqsl.cloud.core.dao.NoteDao;
import com.hysw.qqsl.cloud.core.entity.Filter;
import com.hysw.qqsl.cloud.core.entity.data.Note;
import com.hysw.qqsl.cloud.core.entity.Verification;
import com.hysw.qqsl.cloud.core.entity.data.ElementDB;
import com.hysw.qqsl.cloud.core.entity.element.Element;
import com.hysw.qqsl.cloud.core.entity.element.ElementGroup;
import com.hysw.qqsl.cloud.core.entity.element.Unit;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.hysw.qqsl.cloud.core.entity.data.Project;
import com.hysw.qqsl.cloud.util.SettingUtils;

import javax.servlet.http.HttpSession;

/**
 * 短信service
 *
 * @author 雪庭(flysic) qq: 119238122 github: https://github.com/flysic
 * @since 2015年12月7日
 */
@Service("noteService")
public class NoteService extends BaseService<Note, Long> {
	//Log logger = LogFactory.getLog(getClass());
	private static Log logger=LogFactory.getLog(NoteService.class);
	@Autowired
	private NoteDao noteDao;
	@Autowired
	private NoteCache noteCache;
	@Autowired
	private IntroduceService introduceService;
	@Autowired
	private UnitService unitService;
	@Autowired
	private ElementDBService elementDBService;
	@Autowired
	private AccountService accountService;
	@Autowired
	private NoteService noteService;
	private IAcsClient acsClient=null;
	@Autowired
	public void setBaseDao(NoteDao noteDao) {
		super.setBaseDao(noteDao);
	}
	private Date d = null;
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
	//短信相关要素的缓存
	List<Element> noteElements;
	List<String> noteAlias;

	public List<String> getNoteAlias() {
		if (noteAlias == null || noteAlias.size() == 0) {
			noteAlias = makeNoteAlias();
		}
		return noteAlias;
	}

	public List<Element> getNoteElements() {
		if (noteElements == null || noteElements.size() == 0) {
			noteElements = makeNoteElements();
		}
		return noteElements;
	}

    public NoteService(){
		initIAcsClient();
	}
	/**
	 * 获取和短信相关的要素
	 *
	 * @return
	 */
	private List<Element> makeNoteElements() {
		List<Element> noteElements = new ArrayList<>();
		List<Element> elements;
		Element element;
		List<Unit> units = unitService.getAgrUnits();
		List<ElementGroup> elementGroups;
		for (int i = 0; i < units.size(); i++) {
			if (units.get(i).getElementGroups() != null && units.get(i).getElementGroups().size() > 0) {
				elementGroups = units.get(i).getElementGroups();
				for (int j = 0; j < elementGroups.size(); j++) {
					elements = elementGroups.get(j).getElements();
					for (int k = 0; k < elements.size(); k++) {
						element = elements.get(k);
						if (element.getDescription() != null && !element.getDescription().equals("introduce")) {
							if (SettingUtils.stringMatcher("NOTE", element.getDescription())) {
								noteElements.add(element);
							}
						}
					}
				}
			}
		}
		logger.info(noteElements.size());
		return noteElements;
	}

	/**
	 * 获取别名
	 *
	 * @return
	 */
	private List<String> makeNoteAlias() {
		List<String> aliass = new ArrayList<>();
		List<Element> elements = getNoteElements();
		for (int i = 0; i < elements.size(); i++) {
			aliass.add(elements.get(i).getAlias());
		}
		logger.info(aliass.size());
		return aliass;
	}

	/**
	 * 生成短信对象
	 *
	 * @param projectId
	 * @return
	 */
	public JSONObject makeNote(Long projectId) {
		JSONObject noteJson = new JSONObject();
		List<Element> elements = getNoteElements();
		List<ElementDB> elementDBs = elementDBService.findNoteElementDB(projectId);
		for (int j = 0; j < elements.size(); j++) {
			for (int i = 0; i < elementDBs.size(); i++) {
				if (elements.get(j).getAlias().equals(elementDBs.get(i).getAlias())) {
					if (elementDBs.get(i).getValue() != null && StringUtils.hasText(elementDBs.get(i).getValue())) {
						getNote(elements.get(j), noteJson, elementDBs.get(i).getValue());
					}
				}

			}
		}
		return noteJson;
	}


	/**
	 * 获取短信内容
	 *
	 * @param element
	 * @param noteJson
	 */
	public void getNote(Element element, JSONObject noteJson, String value) {
		if (element.getDescription() == null) {
			return;
		}
		//判断建设单位法人
		if (SettingUtils.stringMatcher("CONTACTS_OWN_MASTER", element.getDescription())) {
			if (SettingUtils
					.stringMatcher("NOTE", element.getDescription())) {
				if (SettingUtils.stringMatcher("name",
						element.getDescription())) {
					noteJson.put("buildMaster", value);
				}
				if (SettingUtils.stringMatcher("phone", element.getDescription())) {
					noteJson.put("buildMasterPhone", value);
				}
				if (SettingUtils.stringMatcher("email", element.getDescription())) {
					noteJson.put("buildMasterEmail", value);
				}
			}
		}
		//判断业主负责人
		if (SettingUtils.stringMatcher("CONTACTS_OWN_NAME",
				element.getDescription())) {
			if (SettingUtils
					.stringMatcher("NOTE", element.getDescription())) {
				if (SettingUtils.stringMatcher("name",
						element.getDescription())) {
					noteJson.put("ownName", value);
				}
				if (SettingUtils.stringMatcher("phone",
						element.getDescription())) {
					noteJson.put("ownPhone", value);
				}
				if (SettingUtils.stringMatcher("email",
						element.getDescription())) {
					noteJson.put("ownEmail", value);
				}
			}
		}
		//设计单位相关信息
		buildDesignNoteJson(element, noteJson, value);
	}

	/**
	 * 设计单位相关短信信息
	 *
	 * @param element
	 * @param noteJson
	 */
	public void buildDesignNoteJson(Element element, JSONObject noteJson, String value) {
		if (SettingUtils.stringMatcher("CONTACTS_DESIGN",
				element.getDescription())) {
			if (SettingUtils
					.stringMatcher("NOTE", element.getDescription())) {
				if (SettingUtils.stringMatcher("company",
						element.getDescription())) {
					noteJson.put("company", value);
				}
				if (SettingUtils.stringMatcher("depart",
						element.getDescription())) {
					noteJson.put("depart", value);

				}
				if (SettingUtils.stringMatcher("master",
						element.getDescription())) {
					noteJson.put("master", value);
				}
				if (SettingUtils.stringMatcher("master_phone",
						element.getDescription())) {
					noteJson.put("masterPhone", value);
				}
				if (SettingUtils.stringMatcher("master_email", element.getDescription())) {
					noteJson.put("masterEmail", value);
				}
				if (SettingUtils.stringMatcher("name",
						element.getDescription())) {
					noteJson.put("name", value);
				}
				if (SettingUtils.stringMatcher("phone",
						element.getDescription())) {
					noteJson.put("phone", value);
				}
				if (SettingUtils.stringMatcher("email", element.getDescription())) {
					noteJson.put("email", value);
				}
			}
		}
	}

	/**
	 * 发送验证码
	 */
	private String sendCode(String phone, String code) {
		String msg = "\r\t尊敬的用户，您好！您的验证码为： " + code
				+ "，5分钟内有效！";
		Note note = new Note();
		note.setPhone(phone);
		note.setSendMsg(msg);
		//验证码短信加入缓存
		noteCache.add(phone, note);
		logger.info(phone + ":" + phone + "短信加入缓存");
		return "0";
	}

	/**
	 * 短信中添加项目负责及部门负责联系电话
	 */
	public String getNotefoot(JSONObject noteJson) {
		String noteFoot = "";
		if (noteJson.get("depart") != null) {
			noteFoot = noteFoot + "承担部门：" + noteJson.get("depart") + "\r\n";
		}
		if (noteJson.get("master") != null && noteJson.get("masterPhone") != null) {
			noteFoot = noteFoot + "部门负责人：" + noteJson.get("master") + " \r\n联系电话：" + noteJson.get("masterPhone") + "\r\n";
		}
		if (noteJson.get("name") != null && noteJson.get("phone") != null) {
			noteFoot = noteFoot + "项目负责：" + noteJson.get("name") + " \r\n联系电话：" + noteJson.get("phone") + "\r\n";
		}
		return noteFoot;
	}

	/**
	 * 构建项目开始的短信对象
	 * 可研，初设的短信通知以及可研，初设中批复阶段的短信通知，包含建设内容
	 *
	 * @return
	 */
	public JSONObject makeProjectStartNote(Project project) {
		JSONObject noteJson = makeNote(project.getId());
		if (noteJson.isEmpty()) {
			return null;
		}
		String noteHead = "尊敬的先生(女士)，您好！";
		String noteFoot = getNotefoot(noteJson);
		JSONObject introduceJson = introduceService.buildIntroduceJson(project);
		String msg = "";
		String strDate;
		if (noteJson.get("company") != null) {
			d = project.getCreateDate();
			strDate = sdf.format(new Date(d.getTime() + 15 * 24 * 60 * 60 * 1000));
			msg = msg + "您委托" + noteJson.get("company") + "完成《" + project.getName() + "》实施方案的编制工作目前已正式启动，"
					+ "预计在" + strDate + "左右可完成方案的编制工作。现将我单位项目设计工作安排信息发至你处，望我们及时沟通，合作愉快！\r\n";
		}
		//note.setMsg(noteHead+msg+noteFoot);
		noteJson.put("msg", noteHead + msg + noteFoot);
		if (introduceJson.isEmpty()) {
			return noteJson;
		}
		String studyHead = "";
		String studyFloot = "";
		String studyReplyHead = "";
		if (noteJson.get("company") != null) {
			strDate = sdf.format(new Date(d.getTime() + 3 * 24 * 60 * 60 * 1000));
			studyHead = studyHead + "您委托" + noteJson.get("company") + "完成《" + project.getName() + "》的可研的编制工作目前已正式完成，"
					+ "现将该项目的主要信息发至您处，如有不妥，请及时与我项目负责联系！如无异议，请尽早安排项目的审查事宜！\r\n";
			studyReplyHead = studyReplyHead + "您委托" + noteJson.get("company") + "完成《" + project.getName() + "》的可研的编制工作已于" + strDate +
					"完成审查，并在" + strDate + "相关专家已下达审查意见，贵单位可以正常开展下一步的工作。"
					+ "现将审查后项目的主要信息发至您处。如对项目有任何疑义，请及时与我项目负责联系！\r\n";
		}
		if (StringUtils.hasText(introduceJson.getString("studyMatter"))) {
			studyFloot = studyFloot + "建设内容：\r\t" + introduceJson.getString("studyMatter") + "\r\n";
		}
		if (StringUtils.hasText(introduceJson.getString("studyInvestment"))) {
			studyFloot = studyFloot + "工程投资：\r\t" + introduceJson.getString("studyInvestment") + "\r\n";
		}
		noteJson.put("studyMsg", noteHead + studyHead + studyFloot + noteFoot);
		noteJson.put("studyReplyMsg", noteHead + studyReplyHead + studyFloot + noteFoot);

		String earlyHead = "";
		String earlyFloot = "";
		String earlyReplyHead = "";
		if (noteJson.get("company") != null) {
			strDate = sdf.format(new Date(d.getTime() + 3 * 24 * 60 * 60 * 1000));
			earlyHead = earlyHead + "您委托" + noteJson.get("company") + "完成《" + project.getName() + "》的初设的编制工作目前已正式完成，"
					+ "现将该项目的主要信息发至您处，如有不妥，请及时与我项目负责联系！如无异议，请尽早安排项目的审查事宜！\r\n";
			earlyReplyHead = earlyReplyHead + "您委托" + noteJson.get("company") + "完成《" + project.getName() + "》的初设的编制工作已于" + strDate +
					"完成审查，并在" + strDate + "相关专家已下达审查意见，贵单位可以正常开展下一步的工作。"
					+ "现将审查后项目的主要信息发至您处。如对项目有任何疑义，请及时与我项目负责联系！\r\n";
		}
		if (StringUtils.hasText(introduceJson.getString("earlyMatter"))) {
			earlyFloot = earlyFloot + "建设内容：\r\t" + introduceJson.getString("earlyMatter") + "\r\n";
		}
		if (StringUtils.hasText(introduceJson.getString("earlyInvestment"))) {
			earlyFloot = earlyFloot + "工程投资：\r\t" + introduceJson.getString("earlyInvestment") + "\r\n";
		}
		noteJson.put("earlyMsg", noteHead + earlyHead + earlyFloot + noteFoot);
		noteJson.put("earlyReplyMsg", noteHead + earlyReplyHead + earlyFloot + noteFoot);
		return noteJson;
	}

	/**
	 * 发送短信（项目启动，项目完成，项目审查），将短信对象放入缓存池
	 *
	 * @return
	 */
	public boolean addToNoteCache(List<String> contacts, String sendMsg) {
		Note note;
		for (String contact : contacts) {
			if (!SettingUtils.phoneRegex(contact)) {
				return false;
			}
		}
		for (String contact : contacts) {
			note = new Note();
			note.setSendMsg(sendMsg);
			note.setPhone(contact);
			noteCache.add(contact, note);
		}
		logger.info(contacts.size() + "条短信加入缓存");
		return true;
	}

	/**
	 * 发送验证码短信
	 *
	 * @param phone
	 * @param session
	 * @return
	 */
	public boolean isSend(String phone,
						  HttpSession session) {
		if (!StringUtils.hasText(phone)) {
			return false;
		}
		Verification verification = new Verification();
		String code = SettingUtils.createRandomVcode();
		String result = sendCode(phone, code);
		if (result.equals("0")) {
			verification.setPhone(phone);
			verification.setCode(code);
			if (SettingUtils.getInstance().getSetting().getStatus().equals("test")) {
				verification.setCode("123456");
			}
			if (SettingUtils.getInstance().getSetting().getStatus().equals("test")) {
				verification.setCode("123456");
			}
			session.setAttribute("verification", verification);
			return true;
		}
		return false;
	}


	//阿里云短信服务
	//////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * 初始化acsClient,暂不支持region化
	 */
	public void initIAcsClient(){
		if(acsClient==null){
			//初始化acsClient,暂不支持region化
			IClientProfile profile = DefaultProfile.getProfile("cn-hangzhou", CommonAttributes.NOTE_ACCESS_KEY_ID, CommonAttributes.NOTE_ACCESS_KEY_SECRET);
			try {
				DefaultProfile.addEndpoint("cn-hangzhou", "cn-hangzhou", CommonAttributes.NOTE_PRODUCT, CommonAttributes.NOTE_DOMAIN);
			} catch (ClientException e) {
				logger.info("初始化acsClient失败！！");
				e.printStackTrace();
			}
			acsClient = new DefaultAcsClient(profile);
		}
	}

	/**
	 * 阿里云短信发送
	 * @param phone
	 * @return
	 * @throws ClientException
	 */
	public SendSmsResponse sendSms(String phone,String companyName) throws ClientException {

		//可自助调整超时时间
		System.setProperty("sun.net.client.defaultConnectTimeout", "10000");
		System.setProperty("sun.net.client.defaultReadTimeout", "10000");
		//组装请求对象-具体描述见控制台-文档部分内容
		SendSmsRequest request = new SendSmsRequest();
		//必填:待发送手机号
		request.setMethod(MethodType.POST);
		request.setPhoneNumbers(phone);
		//必填:短信签名-可在短信控制台中找到
		request.setSignName("鸿源水务青清水利");
		//必填:短信模板-可在短信控制台中找到
		request.setTemplateCode("SMS_132991461");
		//可选:模板中的变量替换JSON串,如模板内容为"亲爱的${name},您的验证码为${code}"时,此处的值为
		request.setTemplateParam("{\"company\":\"["+companyName+"]\"}");
		//选填-上行短信扩展码(无特殊需求用户请忽略此字段)
//		request.setSmsUpExtendCode("2133620");
		//可选:outId为提供给业务方扩展字段,最终在短信回执消息中将此值带回给调用者
		request.setOutId(System.currentTimeMillis()+"");
		//hint 此处可能会抛出异常，注意catch
        SendSmsResponse sendSmsResponse = acsClient.getAcsResponse(request);
        return sendSmsResponse;
	}

	/**
	 * 获得短信具体信息
	 * @param bizId
	 * @param phone
	 * @return
	 * @throws ClientException
	 */
	public QuerySendDetailsResponse querySendDetails(String bizId, String phone) throws ClientException {

		//可自助调整超时时间
		System.setProperty("sun.net.client.defaultConnectTimeout", "10000");
		System.setProperty("sun.net.client.defaultReadTimeout", "10000");

		//组装请求对象
		QuerySendDetailsRequest request = new QuerySendDetailsRequest();
		//必填-号码
		request.setPhoneNumber(phone);
		//可选-流水号
		request.setBizId(bizId);
		//必填-发送日期 支持30天内记录查询，格式yyyyMMdd
		SimpleDateFormat ft = new SimpleDateFormat("yyyyMMdd");
		request.setSendDate(ft.format(new Date()));
		//必填-页大小
		request.setPageSize(10L);
		//必填-当前页码从1开始计数
		request.setCurrentPage(1L);

		//hint 此处可能会抛出异常，注意catch
		QuerySendDetailsResponse querySendDetailsResponse = acsClient.getAcsResponse(request);

		return querySendDetailsResponse;
	}

	/**
	 * 监听阿里云短信的回执和回复信息
	 */
	class MyMessageListener implements MessageListener {
		private Gson gson=new Gson();
		@Override
		public boolean dealMessage(Message message) {

			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//			消息的几个关键值
			System.out.println("message receiver time from mns:" + format.format(new Date()));
			System.out.println("message handle: " + message.getReceiptHandle());
			System.out.println("message body: " + message.getMessageBodyAsString());
			System.out.println("message id: " + message.getMessageId());
			System.out.println("message dequeue count:" + message.getDequeueCount());
			System.out.println("Thread:" + Thread.currentThread().getName());
			try{
				Map<String,Object> contentMap=gson.fromJson(message.getMessageBodyAsString(), HashMap.class);

				//TODO 根据文档中具体的消息格式进行消息体的解析
//				String arg = (String) contentMap.get("arg");
//				System.out.println();
				//TODO 这里开始编写您的业务代码
				Object phone = contentMap.get("phone_number");
				Object content = contentMap.get("content");
				if (phone != null && content != null) {
					Note note = noteService.findByPhone(phone);
					if (note == null) {
						note = new Note();
						note.setPhone(phone.toString());
						note.setReply(content.toString());
						noteService.save(note);
					}
				}
			}catch(com.google.gson.JsonSyntaxException e){
				logger.error("error_json_format:"+message.getMessageBodyAsString(),e);
				//理论上不会出现格式错误的情况，所以遇见格式错误的消息，只能先delete,否则重新推送也会一直报错
				return true;
			} catch (Throwable e) {
				e.printStackTrace();
				//您自己的代码部分导致的异常，应该return false,这样消息不会被delete掉，而会根据策略进行重推
				return false;
			}

			//消息处理成功，返回true, SDK将调用MNS的delete方法将消息从队列中删除掉
			return true;
		}

	}

	protected Note findByPhone(Object phone) {
		List<Filter> filters = new ArrayList<>();
		filters.add(Filter.eq("phone", phone));
		List<Note> list = noteDao.findList(0, null, filters);
		if (list.size() == 1) {
			return list.get(0);
		}
		return null;
	}

	public void receiveMsg(){
		DefaultAlicomMessagePuller puller=new DefaultAlicomMessagePuller();
		//设置异步线程池大小及任务队列的大小，还有无数据线程休眠时间
		puller.setConsumeMinThreadSize(6);
		puller.setConsumeMaxThreadSize(16);
		puller.setThreadQueueSize(200);
		puller.setPullMsgThreadSize(1);
		//和服务端联调问题时开启,平时无需开启，消耗性能
        /*
         * TODO 将messageType和queueName替换成您需要的消息类型名称和对应的队列名称
         *云通信产品下所有的回执消息类型:
         *1:短信回执：SmsReport，
         *2:短息上行：SmsUp
         *3:语音呼叫：VoiceReport
         *4:流量直冲：FlowReport
         */
        puller.openDebugLog(false);

		String messageType1="SmsReport";//此处应该替换成相应产品的消息类型
		String queueName1="Alicom-Queue-30150706-SmsReport";//在云通信页面开通相应业务消息后，就能在页面上获得对应的queueName,格式类似Alicom-Queue-xxxxxx-SmsReport
		String messageType="SmsUp";//此处应该替换成相应产品的消息类型
		String queueName="Alicom-Queue-30150706-SmsUp";//在云通信页面开通相应业务消息后，就能在页面上获得对应的queueName,格式类似Alicom-Queue-xxxxxx-SmsReport
        try {
        	puller.startReceiveMsg(CommonAttributes.NOTE_ACCESS_KEY_ID, CommonAttributes.NOTE_ACCESS_KEY_SECRET, messageType, queueName, new MyMessageListener());
			puller.startReceiveMsg(CommonAttributes.NOTE_ACCESS_KEY_ID,CommonAttributes.NOTE_ACCESS_KEY_SECRET, messageType1, queueName1, new MyMessageListener());
        } catch (ClientException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

	}

	public void deleteExpiredNote() {
		List<Note> notes = noteService.findAll();
		Iterator<Note> iterator = notes.iterator();
		while (iterator.hasNext()) {
			Note note = iterator.next();
			if (note.getCreateDate().getTime() + 24 * 60 * 60 * 1000l < System.currentTimeMillis()) {
				noteService.remove(note);
				iterator.remove();
			}
		}
	}

	/**
	 * 生成note--》Json列表
	 * @return
	 */
	public JSONArray getNoteList() {
		List<Note> notes = noteService.findAll();
		JSONObject jsonObject;
		JSONArray jsonArray = new JSONArray();
		for (Note note : notes) {
			jsonObject = new JSONObject();
			jsonObject.put("phone", note.getPhone());
			jsonObject.put("reply", note.getReply());
			jsonArray.add(jsonObject);
		}
		return jsonArray;
	}

}
