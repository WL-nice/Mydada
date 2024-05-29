package com.wanglei.Mydadabackend.model.request.user;


import com.wanglei.Mydadabackend.commmon.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class UserQueryRequest extends PageRequest {
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


}