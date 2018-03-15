package com.hysw.qqsl.cloud.core.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;

/**
 * 后台发给前台的处理消息
 *
 * @since 2015年10月9日
 * @author 雪庭(flysic) qq: 119238122 github: https://github.com/flysic
 *
 */
public class Message implements Serializable{
	private static final long serialVersionUID = 148187703824551920L;

	private Type type;
	private String total;
	private Object data;
	private int status;

	public Message(Type type){
		this.type = type;
		this.status = type.getStatus();
	}

	public Message(Type type, Object data) {
		this.type = type;
		this.data = data;
		this.status = type.getStatus();
	}

	public Message(Type type, Object data, String total) {
		this.type = type;
		this.data = data;
		this.total = total;
		this.status = type.getStatus();
	}

	//	private String description;
	public enum Type {
		/** 请求已成功。实际的响应将取决于所使用的请求方法。在GET请求中，响应
		将包含所希望数据；POST请求中，响应将包含描述操作结果的数据。*/
		OK(2000),
//				4xx：客户端错误,这类的状态码代表了客户端看起来可能发生了错误，妨碍了
//		服务器的处理。服务器就应该返回一个解释当前错误码。除了401在GET和POST
//		请求可能返回，其他的客户端错误码都是针对POST请求的。

		/** 参数错误，表示服务器收到的参数不合法，比如预期是整型，客户端传递的
		 是字符串；预期是手机号，客户端传递的不合法；请求被拒绝。*/
		PARAMETER_ERROR(4000),
		/** 操作失败，标识服务器执行过程中出现异常，无法继续执行，服务器要保证
		 失败后恢复到执行前的转台，比如失败后保证不修改数据库和缓存。*/
		FAIL(4001),
		/** 密码错误，比如修改密码时，原密码输入错误。*/
		PASSWORD_ERROR(4002),
		/** 上传文件过大，服务器拒绝。*/
		FILE_TOO_MAX(4003),
		/** 文件类型出错,比如前台发送office文件给后台，后台记录这个文件信息，用于检测子系统进行文件转换，如果文件类型不是office文件，报出这个错误。*/
		FILE_TYPE_ERROR(4004),
		/** 即“未认证”，即用户没有必要的凭据。该状态码表示当前请求需要用户验证。*/
		UNAUTHORIZED(4010),
		/** 无SESSION，即服务器端对应SESSION已经失效或删除，客户端无法连接，必须重新登录。*/
		NO_SESSION(4011),
		/** 数据已存在，即要添加数据已经存在，不能重复添加，比如此手机号已注册，再次注册产生这个错误。*/
		DATA_EXIST(4020),
		/** 数据不存在，无法继续执行操作，比如坐标上传时，根据客户端发送的项目
		 id，数据库查询不到项目；再比如web端登陆时，根据code查询不到用户，
		 返回这个错误。*/
		DATA_NOEXIST(4021),
		/** 请求的数据不属于自己，拒绝操作，比如删除不是自己的项目,请求不时自己的订单,不是自己的测站等操作。*/
		DATA_REFUSE(4022),
		/** 数据已被锁定，比如用户登录时，如果用户被锁定，返回这个错误。*/
		DATA_LOCK(4023),
		/** 千寻连接池已满，无法提供服务，请稍候尝试连接。*/
		QXWZ_FULL(4030),
		/** 需要验证码，比如web端第一次登录，或登录过，但是15天内没有登陆过，再次登录时产生这个错误。*/
		CODE_NEED(4040),
		/** 验证码过期，需要重新获取，验证码一般只有5-10分钟的有效期，超过这个时间，验证码需要重新获取。*/
		CODE_INVALID(4041),
		/** 验证码输入错误，请重新提交。*/
		CODE_ERROR(4042),
		/** 未获取验证码，请点击获取。*/
		CODE_NOEXIST(4043),
		/** 套餐已过期，无法提供服务，比如无法上传文件、无法修改要素等操作，套餐必须要续费。*/
		PACKAGE_EXPIRED(4050),
		/** 餐限制，不允许执行，比如空间已满或流量用尽，不允许上传文件；新建
		 项目数达套到上限，不允许建立项目。*/
		PACKAGE_LIMIT(4051),
		/** 套餐不满足购买条件，比如空间大小,子账户数已超过套餐限制,无法购买。*/
		PACKAGE_NOALLOW_BUY(4052),
		/** 套餐不满足续费条件,比如测试套餐不允许续费。*/
		PACKAGE_NOALLOW_RENEW(4053),
		/** 套餐不满足升级条件，比如升级后套餐小于等于目前套餐。*/
		PACKAGE_NOALLOW_UPDATE(4054),
		/** 未进行实名认证，无法购买套餐等操作,或未进行实名认证,直接进行企业认
		 证也返回这个错误。*/
		CERTIFY_NO_PERSONAL(4060),
		/** 未进行企业认证，无法购买高级套餐，购买数据服务。*/
		CERTIFY_NO_COMPANY(4061),
		/** 重复认证错误，账号已认证不能再次认证了，如果再次发起认证，将返回这个错误。*/
		CERTIFY_REPEAT(4062),
		/** 项目中心未设置，上传坐标时，返回这个错误。*/
		COOR_PROJECT_NO_CENTER(4070),
		/** 上传的坐标excel中建筑物中心点或定位点格式错误，无法添加建筑物。*/
		COOR_BUILD_CENTER_ERROR(4071),
		/** 上传的坐标excel文件格式错误，文件损坏，excel文件格式不是预期的，excel数据不是预期的。*/
		COOR_FORMAT_ERROR(4072),
		/** 订单过期，比如对过期订单进行支付，返回这个错误。*/
		TRADE_EXPIRED(4080),
		/** 订单已支付，比如关闭一个已完成支付的订单，返回这个错误。*/
		TRADE_PAYED(4081),
		/** 有未支付的订单，比如已有未支付的订单，再次生成订单，返回这个错误。*/
		TRADE_HAS_NOPAY(4082),
		/** 订单未支付，比如删除一个未支付的订单，返回这个错误。*/
		TRADE_NOPAY(4083),
		/** 子账号信息不全，比如子账号刚注册，没有完善昵称、真实姓名等信息，这时候对子账号在协同工作分配项目，返回这个错误。*/
		ACCOUNT_NOINFO(4090);
		//必须增加一个构造函数,变量,得到该变量的值
		private int  status=0;
		private Type(int status) {
			this.status=status;
		}
		public static int getStatus(int index) {
			for (Type c : Type.values()) {
				if (c.ordinal() == index) {
					return c.status;
				}
			}
			return 0;
		}

		public int getStatus() {
			return status;
		}

		public void setStatus(int status) {
			this.status = status;
		}
	}

	@JsonIgnore
	public Type getType() {
		return type;
	}

	public Object getData() {
		return this.data;
	}

	public String getTotal() {
		return total;
	}

	public int getStatus() {
		return status;
	}

}
