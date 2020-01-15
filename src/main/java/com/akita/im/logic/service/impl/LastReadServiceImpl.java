package com.akita.im.logic.service.impl;

import com.akita.im.logic.dao.LastReadMessageInfoDao;
import com.akita.im.logic.model.LastReadMessageInfo;
import com.akita.im.logic.service.LastReadService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class LastReadServiceImpl extends ServiceImpl<LastReadMessageInfoDao, LastReadMessageInfo> implements LastReadService {
}
