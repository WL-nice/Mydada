package com.wanglei.Mydadabackend.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wanglei.Mydadabackend.annotation.AuthCheck;
import com.wanglei.Mydadabackend.commmon.BaseResponse;
import com.wanglei.Mydadabackend.commmon.ErrorCode;
import com.wanglei.Mydadabackend.commmon.ResultUtils;
import com.wanglei.Mydadabackend.commmon.ReviewRequest;
import com.wanglei.Mydadabackend.exception.BusinessException;
import com.wanglei.Mydadabackend.model.domain.App;
import com.wanglei.Mydadabackend.model.domain.User;
import com.wanglei.Mydadabackend.model.enums.ReviewStatusEnum;
import com.wanglei.Mydadabackend.model.request.app.AppAddRequest;
import com.wanglei.Mydadabackend.model.request.app.AppEditRequest;
import com.wanglei.Mydadabackend.model.request.app.AppQueryRequest;
import com.wanglei.Mydadabackend.model.request.app.AppUpdateRequest;
import com.wanglei.Mydadabackend.model.vo.AppVO;
import com.wanglei.Mydadabackend.service.AppService;
import com.wanglei.Mydadabackend.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("/app")
@CrossOrigin(origins = "http://localhost:8080",allowCredentials = "true")
@Slf4j
public class AppController {

    @Resource
    private UserService userService;

    @Resource
    private AppService appService;

    /**
     * 添加应用
     *
     * @param appAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addApp(@RequestBody AppAddRequest appAddRequest, HttpServletRequest request) {
        if (appAddRequest == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        App app = new App();
        BeanUtil.copyProperties(appAddRequest, app);
        //默认为审核中
        app.setReviewStatus(ReviewStatusEnum.REVIEWING.getValue());
        appService.validApp(app, true);
        //获取登录用户
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NO_LOGIN);
        }
        app.setUserId(loginUser.getId());

        //插入数据
        boolean result = appService.save(app);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return ResultUtils.success(app.getId());
    }

    /**
     * 删除应用
     *
     * @param id
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteApp(@RequestParam Long id, HttpServletRequest request) {
        if (id == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NO_LOGIN);
        }
        //获取数据
        App app = appService.getById(id);
        if (app == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        //仅本人或管理员可删除
        if (!userService.isAdmin(loginUser) && !app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        boolean result = appService.removeById(id);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return ResultUtils.success(true);
    }

    /**
     * 更新应用（管理员）
     *
     * @param appUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<Boolean> updateApp(@RequestBody AppUpdateRequest appUpdateRequest, HttpServletRequest request) {
        if (appUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NO_LOGIN);
        }
        //获取数据
        App oldApp = appService.getById(appUpdateRequest.getId());
        if (oldApp == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        App app = new App();
        BeanUtil.copyProperties(appUpdateRequest, app);
        appService.validApp(app, false);
        boolean result = appService.updateById(app);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return ResultUtils.success(true);
    }

    /**
     * 编辑应用（用户）
     * @param appEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editApp(@RequestBody AppEditRequest appEditRequest, HttpServletRequest request) {
        if (appEditRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NO_LOGIN);
        }
        //获取数据
        App oldApp = appService.getById(appEditRequest.getId());
        if (oldApp == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        //仅本人可修改
        if (!oldApp.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        App app = new App();
        BeanUtil.copyProperties(appEditRequest, app);
        //修改后需要再次审核
        app.setReviewStatus(ReviewStatusEnum.REVIEWING.getValue());
        appService.validApp(app, false);
        boolean result = appService.updateById(app);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return ResultUtils.success(true);
    }

    /**
     * 根据id获取应用
     * @param id
     * @param request
     * @return
     */
    @PostMapping("/get/vo")
    public BaseResponse<AppVO> getAppVO(@RequestParam Long id, HttpServletRequest request) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (userService.getLoginUser(request) == null) {
            throw new BusinessException(ErrorCode.NO_LOGIN);
        }
        App app = appService.getById(id);
        if (app == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        AppVO appVO = AppVO.objToVo(app);
        appVO.setUser(userService.getUserVOById(app.getUserId()));
        return ResultUtils.success(appVO);
    }

    /**
     * 分页获取应用列表（管理员）
     * @param appQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<Page<App>> listAppByPage(@RequestBody AppQueryRequest appQueryRequest,
                                                 HttpServletRequest request) {
        if (appQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long current = appQueryRequest.getCurrent();
        long pageSize = appQueryRequest.getPageSize();
        if (pageSize > 30) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<App> queryWrapper = appService.getQueryWrapper(appQueryRequest);
        Page<App> page = appService.page(new Page<>(current, pageSize), queryWrapper);
        return ResultUtils.success(page);
    }

    /**
     * 分页获取应用列表（用户）
     * @param appQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<AppVO>> listAppVOByPage(@RequestBody AppQueryRequest appQueryRequest,
                                                     HttpServletRequest request) {
        if (appQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //仅展示审核通过的应用
        appQueryRequest.setReviewStatus(ReviewStatusEnum.PASS.getValue());
        long current = appQueryRequest.getCurrent();
        long pageSize = appQueryRequest.getPageSize();
        if (pageSize > 30) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<App> queryWrapper = appService.getQueryWrapper(appQueryRequest);
        Page<App> page = appService.page(new Page<>(current, pageSize), queryWrapper);
        return ResultUtils.success(appService.getVOPage(page,request));
    }

    /**
     * 获取当前用户创建应用列表
     * @param appQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<AppVO>> listMyAppVOByPage(@RequestBody AppQueryRequest appQueryRequest,
                                                       HttpServletRequest request) {
        if (appQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        appQueryRequest.setUserId(userService.getLoginUser(request).getId());
        long current = appQueryRequest.getCurrent();
        long pageSize = appQueryRequest.getPageSize();
        if (pageSize > 30) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<App> queryWrapper = appService.getQueryWrapper(appQueryRequest);
        Page<App> page = appService.page(new Page<>(current, pageSize), queryWrapper);
        return ResultUtils.success(appService.getVOPage(page,request));
    }

    /**
     * 应用审核
     *
     * @param reviewRequest
     * @param request
     * @return
     */
    @PostMapping("/review")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<Boolean> doAppReview(@RequestBody ReviewRequest reviewRequest, HttpServletRequest request) {
        if(reviewRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Long id = reviewRequest.getId();
        Integer reviewStatus = reviewRequest.getReviewStatus();
        // 校验
        ReviewStatusEnum reviewStatusEnum = ReviewStatusEnum.getEnumByValue(reviewStatus);
        if (id == null || reviewStatusEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 判断是否存在
        App oldApp = appService.getById(id);
        if(oldApp == null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        // 已是该状态
        if (oldApp.getReviewStatus().equals(reviewStatus)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请勿重复审核");
        }
        // 更新审核状态
        User loginUser = userService.getLoginUser(request);
        App app = new App();
        app.setId(id);
        app.setReviewStatus(reviewStatus);
        app.setReviewMessage(reviewRequest.getReviewMessage());
        app.setReviewerId(loginUser.getId());
        app.setReviewTime(new Date());
        boolean result = appService.updateById(app);
        if(!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新审核状态失败");
        }
        return ResultUtils.success(true);

 }

}
