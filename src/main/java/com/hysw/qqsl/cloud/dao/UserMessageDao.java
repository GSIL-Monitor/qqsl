package com.hysw.qqsl.cloud.dao;

import com.hysw.qqsl.cloud.entity.data.UserMessage;
import org.springframework.stereotype.Repository;

@Repository("userMessageDao")
public class UserMessageDao extends BaseDao<UserMessage, Long> {

}
