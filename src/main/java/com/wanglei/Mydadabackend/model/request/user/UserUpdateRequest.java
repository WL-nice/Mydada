package com.wanglei.Mydadabackend.model.request.user;

import lombok.Data;

import java.util.List;

@Data
public class UserUpdateRequest {
    /**
     * 用户id
     */

    private Long id;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 头像
     */
    private String userAvatar;

    /**
     * 简介
     */
    private String userProfile;
}
