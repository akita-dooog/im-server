package com.akita.im.router.rest;

import com.akita.im.logic.model.MessageInfo;
import com.akita.im.logic.service.MessageInfoService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/message")
public class MessageController {
    @Autowired
    MessageInfoService service;

    @RequestMapping("/getOfflineMsg")
    public List<MessageInfo> getOfflineMsg(@RequestParam("userid") String userid) {
        QueryWrapper<MessageInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("msg_to", userid);
        return service.list(queryWrapper);
    }

    @RequestMapping("/test")
    public String test(){
        return "running!!!";
    }
}
