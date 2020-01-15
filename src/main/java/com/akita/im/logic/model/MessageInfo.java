package com.akita.im.logic.model;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.util.Date;


@Data
@Builder
@TableName("message_info")
public class MessageInfo {
    private String uid;

    @TableField("msg_from")
    private String msgFrom;

    @TableField("msg_to")
    private String msgTo;

    @TableField("group_id")
    private String groupId;

    private String content;

    private Integer type;

    private String url;

    private Long size;

    private String trumbnail;

    private Long duration;

    @TableField("file_type")
    private String fileType;

    @TableField("created_user")
    private String createdUser;

    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private Date createdTime;

    @TableField("updated_user")
    private String updatedUser;

    @TableField(value = "updated_time", fill = FieldFill.INSERT_UPDATE)
    private Date updatedTime;
}
