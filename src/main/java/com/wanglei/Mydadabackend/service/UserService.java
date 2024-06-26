package com.wanglei.Mydadabackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wanglei.Mydadabackend.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wanglei.Mydadabackend.model.request.user.UserQueryRequest;
import com.wanglei.Mydadabackend.model.request.user.UserUpdateRequest;
import com.wanglei.Mydadabackend.model.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * 用户服务
 *
 * @author muqiu
 */
public interface UserService extends IService<User> {


    /**
     * 用户注册
     *
     * @param userAccount   账号
     * @param userPassword  密码
     * @param checkPassword 验证密码
     * @return 用户id
     */
    long UserRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 用户登录
     *
     * @param userAccount  账号
     * @param userPassword 密码
     * @return 脱敏后的用户信息
     */
    User doLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用户脱敏
     *
     * @param user
     * @return
     */

    User getSafetUser(User user);

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    int userLogout(HttpServletRequest request);

    /**
     * 根据标签搜索用户
     *
     * @param list
     * @return
     */
    List<User> searchUserByTags(List<String> list);

    /**
     * 用户信息修改
     *
     * @param userUpdateRequest
     * @return
     */
    Integer updateUser(UserUpdateRequest userUpdateRequest, User loginUser);


    /**
     * 获取登录用户信息
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 判断是否为管理员
     */
    boolean isAdmin(HttpServletRequest request);

    boolean isAdmin(User loginUser);

    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    UserVO getUserVO(User user);

    UserVO getUserVOById(Long id);
}




