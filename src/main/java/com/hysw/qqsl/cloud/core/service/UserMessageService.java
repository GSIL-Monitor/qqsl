package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.CommonAttributes;
import com.hysw.qqsl.cloud.core.dao.UserMessageDao;
import com.hysw.qqsl.cloud.core.entity.Filter;
import com.hysw.qqsl.cloud.core.entity.data.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service("userMessageService")
public class UserMessageService extends BaseService<UserMessage, Long>{

	@Autowired
	private UserMessageDao userMessageDao;
	
	@Autowired
	public void setBaseDao(UserMessageDao userMessageDao) {
		super.setBaseDao(userMessageDao);
	}
	
	public List<UserMessage> findByUser(User user) {
		List<Filter> filters = new ArrayList<Filter>();
		filters.add(Filter.eq("user", user.getId()));
		List<UserMessage> userMessages = userMessageDao.findList(0, null, filters);
			return userMessages;
	}

	public List<UserMessage> findByUserAndType(User user) {
		Date newDate=new Date(); 
		Calendar calendar = Calendar.getInstance();  //得到日历
		calendar.setTime(newDate);//把当前时间赋给日历
		calendar.add(Calendar.DAY_OF_MONTH, -30);  //设置为前一天
		Date dBefore = calendar.getTime();   //得到前一天的时间
		List<Filter> filters1 = new ArrayList<Filter>();
		List<Filter> filters2 = new ArrayList<Filter>();
		filters1.add(Filter.eq("user", user.getId()));
		filters2.add(Filter.eq("user", user.getId()));
		filters1.add(Filter.in("status", UserMessage.Status.UNREAD));
		filters2.add(Filter.between("createDate", dBefore, newDate));
		List<UserMessage> userMessages= userMessageDao.findList(0, null, filters1,filters2);
		for (int i = 0; i < userMessages.size(); i++) {
			userMessages.get(i).setContent(userMessages.get(i).getContent().replaceAll("PROJECTURL", CommonAttributes.PROJECTURL).replaceAll("SENSORURL", CommonAttributes.SENSORURL));
		}
		return userMessages;
	}

	/**
	 * 记录子账号解绑企业消息
	 * @param account
	 * @param user
	 */
	public void unbindMessage(Account account, User user) {
		UserMessage userMessage = new UserMessage();
		String content = "尊敬的用户，您好！手机号为: "+account.getPhone()+" 的子账号已与企业解绑。";
		userMessage.setUser(user);
		userMessage.setStatus(UserMessage.Status.UNREAD);
		userMessage.setContent(content);
		userMessage.setType(UserMessage.Type.INVITE__ACCOUNT);
		userMessageDao.save(userMessage);
	}

	/**
	 * 记录分享和取消分享的消息
	 * @param project
	 * @param user
	 */
    public void shareMessage(Project project, User user,boolean flag) {
		UserMessage userMessage = new UserMessage();
		userMessage.setUser(user);
		String content;
		//分享权限
		if(flag){
			content = "尊敬的用户，您好！"+project.getUser().getUserName()+"企业已将《<a href=\"PROJECTURL"+project.getId()+"\">"+project.getName()+"</a>》项目的查看权限分配给贵公司，已获得该项目查看权限。";
		}else{
			content="尊敬的用户，您好！"+project.getUser().getUserName()+"已将《"+project.getName()+"》项目的查看权限收回，您已失去查看权限。";
		}
		userMessage.setContent(content);
		userMessage.setStatus(UserMessage.Status.UNREAD);
		userMessage.setType(UserMessage.Type.SHARE_PROJECT);
		userMessageDao.save(userMessage);
    }

	/**
	 * 记录仪表分享和取消分享的消息
	 * @param sensor
	 * @param user
	 * @param flag
	 */
    public void sensorShareMessage(Sensor sensor, User user, User own, boolean flag) {
		UserMessage userMessage = new UserMessage();
		userMessage.setUser(user);
		String content;
		//分享权限
		if(flag){
			content = "尊敬的用户，您好！"+own.getUserName()+"企业已将编号为<a href=\"SENSORURL\">"+sensor.getCode()+"</a>仪表的查看权限分配给贵公司，已获得该仪表查看权限。";
		}else{
			content="尊敬的用户，您好！"+own.getUserName()+"已将编号为"+sensor.getCode()+"仪表的查看权限收回，您已失去查看权限。";
		}
		userMessage.setContent(content);
		userMessage.setStatus(UserMessage.Status.UNREAD);
		userMessage.setType(UserMessage.Type.SHARE_STATION);
		userMessageDao.save(userMessage);
    }

	/**
	 * 实名认证失败
	 * @param certify
	 */
	public void personalCertifyFail(Certify certify) {
		UserMessage userMessage = new UserMessage();
		userMessage.setStatus(UserMessage.Status.UNREAD);
		userMessage.setContent("尊敬的水利云用户您好，您的实名认证由于==>"+certify.getIdentityAdvice()+"<==原因，导致认证失败，请重新进行认证。");
		userMessage.setType(UserMessage.Type.CERTIFY);
		userMessage.setUser(certify.getUser());
	}

	public void companyCertifyFail(Certify certify) {
		UserMessage userMessage = new UserMessage();
		userMessage.setStatus(UserMessage.Status.UNREAD);
		userMessage.setContent("尊敬的水利云用户您好，您的企业认证由于==>"+certify.getCompanyAdvice()+"<==原因，导致认证失败，请重新进行认证。");
		userMessage.setType(UserMessage.Type.CERTIFY);
		userMessage.setUser(certify.getUser());
	}

	public void personalCertifySuccess(Certify certify) {
		UserMessage userMessage = new UserMessage();
		userMessage.setStatus(UserMessage.Status.UNREAD);
		userMessage.setContent("尊敬的水利云用户您好，您的实名认证已经通过认证，水利云将为您提供更多，更优质的服务。");
		userMessage.setType(UserMessage.Type.CERTIFY);
		userMessage.setUser(certify.getUser());
	}

	public void companyCertifySuccess(Certify certify) {
		UserMessage userMessage = new UserMessage();
		userMessage.setStatus(UserMessage.Status.UNREAD);
		userMessage.setContent("尊敬的水利云用户您好，您的企业认证已经通过认证，水利云将为您提供更多企业级功能，更优质的企业级服务。");
		userMessage.setType(UserMessage.Type.CERTIFY);
		userMessage.setUser(certify.getUser());
	}

	public void emailNotice(User user,String message) {
		UserMessage userMessage = new UserMessage();
		userMessage.setStatus(UserMessage.Status.UNREAD);
		userMessage.setContent(message);
		userMessage.setType(UserMessage.Type.CERTIFY);
		userMessage.setUser(user);
	}
}
