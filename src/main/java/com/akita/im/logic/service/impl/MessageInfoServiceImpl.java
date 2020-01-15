package com.akita.im.logic.service.impl;

import com.akita.im.logic.dao.MessageInfoDao;
import com.akita.im.logic.model.MessageInfo;
import com.akita.im.logic.service.MessageInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class MessageInfoServiceImpl extends ServiceImpl<MessageInfoDao, MessageInfo> implements MessageInfoService {
}
