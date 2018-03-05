package com.hysw.qqsl.cloud.core.entity;

/**
 * 后台发给前台的处理消息
 *
 * @since 2015年10月9日
 * @author 雪庭(flysic) qq: 119238122 github: https://github.com/flysic
 *
 */
public class Message {
	private Type type;
	private String total;
	private Object data;
	private String status;

	//	private String description;
	public enum Type {
		/** 请求已成功。实际的响应将取决于所使用的请求方法。在GET请求中，响应
		将包含所希望数据；POST请求中，响应将包含描述操作结果的数据。*/
		bOK("2000"),
//				4xx：客户端错误,这类的状态码代表了客户端看起来可能发生了错误，妨碍了
//		服务器的处理。服务器就应该返回一个解释当前错误码。除了401在GET和POST
//		请求可能返回，其他的客户端错误码都是针对POST请求的。

		/** 参数错误，表示服务器收到的参数不合法，比如预期是整型，客户端传递的
		 是字符串；预期是手机号，客户端传递的不合法；请求被拒绝。*/
		bPARAMETER_ERROR("4000"),
		/** 操作失败，标识服务器执行过程中出现异常，无法继续执行，服务器要保证
		 失败后恢复到执行前的转台，比如失败后保证不修改数据库和缓存。*/
		bFAIL("4001"),
		/** 即“未认证”，即用户没有必要的凭据。该状态码表示当前请求需要用户验证。*/
		bUNAUTHORIZED("4010"),
		/** 无SESSION，即服务器端对应SESSION已经失效或删除，客户端无法连接，必须重新登录。*/
		bNO_SESSION("4011"),
		/** 数据已存在，即要添加数据已经存在，不能重复添加，比如此手机号已注册，再次注册产生这个错误。*/
		bDATA_EXIST("4020"),
		/** 数据不存在，无法继续执行操作，比如坐标上传时，根据客户端发送的项目
		 id，数据库查询不到项目；再比如web端登陆时，根据code查询不到用户，
		 返回这个错误。*/
		bDATA_NOEXIST("4021"),
		/** 请求的数据不属于自己，拒绝操作，比如删除不是自己的项目,请求不时自己的订单,不是自己的测站等操作。*/
		bDATA_REFUSE("4022"),
		/** 验证码过期，需要重新获取，验证码一般只有5-10分钟的有效期，超过这个时间，验证码需要重新获取。*/
		bCODE_INVALID("4041"),
		/** 验证码输入错误，请重新提交。*/
		bCODE_ERROR("4042"),
		/** 套餐已过期，无法提供服务，比如无法上传文件、无法修改要素等操作，套餐必须要续费。*/
		bPACKAGE_EXPIRED("4050"),
		/** 套餐限制，不允许执行，比如空间已满或流量用尽，不允许上传文件；新建
		 项目数达到上限，不允许建立项目。*/
		bPACKAGE_LIMIT("4051"),
		/** 套餐不满足购买条件，比如空间大小,子账户数已超过套餐限制,无法购买。*/
		bPACKAGE_NOALLOW_BUY("4052"),
		/** 未进行实名认证，无法购买套餐等操作,或未进行实名认证,直接进行企业认
		 证也返回这个错误。*/
		bCERTIFY_NO_PERSONAL("4060"),
		/** 未进行企业认证，无法购买高级套餐，购买数据服务。*/
		bCERTIFY_NO_COMPANY("4061"),
		/** 千寻过期，千寻服务时间过期，请求时返回。*/
		bQXWE_EXPIRED("4030"),
		/** 上传文件过大，服务器拒绝。*/
		bFILE_TOO_MAX("4003"),
		/** 项目中心未设置，上传坐标时，返回这个错误。*/
		bCOOR_PROJECT_NO_CENTER("4070"),
		/** 上传的坐标excel中建筑物中心点或定位点格式错误，无法添加建筑物。*/
		bCOOR_BUILD_CENTER_ERROR("4071"),
		/** 上传的坐标excel文件格式错误，文件损坏，excel文件格式不是预期的，excel数据不是预期的。*/
		bCOOR_FORMAT_ERROR("4072"),
		/** 密码错误，比如修改密码时，原密码输入错误。*/
		bPASSWORD_ERROR("4002"),
		/** 需要验证码，比如web端第一次登录，或登录过，但是15天内没有登陆过，再次登录时产生这个错误。*/
		bCODE_NEED("4040"),
		/** 文件类型出错,比如前台发送office文件给后台，后台记录这个文件信息，用于检测子系统进行文件转换，如果文件类型不是office文件，报出这个错误。*/
		bFILE_TYPE_ERROR("4004"),
		/** 未获取验证码，请点击获取。*/
		bCODE_NOEXIST("4043");
		//必须增加一个构造函数,变量,得到该变量的值
		private String  mState="0";
		private Type(String value)
		{
			mState=value;
		}
		public static String getStatus(int index) {
			for (Type c : Type.values()) {
				if (c.ordinal() == index) {
					return c.mState;
				}
			}
			return null;
		}
	}

//	@JsonIgnore
	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public Object getData() {
		return this.data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public String getTotal() {
		return total;
	}

	public void setTotal(String total) {
		this.total = total;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

}
