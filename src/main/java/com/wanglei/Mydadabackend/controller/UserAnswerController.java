package com.wanglei.Mydadabackend.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wanglei.Mydadabackend.annotation.AuthCheck;
import com.wanglei.Mydadabackend.commmon.BaseResponse;
import com.wanglei.Mydadabackend.commmon.ErrorCode;
import com.wanglei.Mydadabackend.commmon.ResultUtils;
import com.wanglei.Mydadabackend.exception.BusinessException;
import com.wanglei.Mydadabackend.model.domain.App;
import com.wanglei.Mydadabackend.model.domain.UserAnswer;
import com.wanglei.Mydadabackend.model.domain.User;
import com.wanglei.Mydadabackend.model.enums.ReviewStatusEnum;
import com.wanglei.Mydadabackend.model.request.userAnswer.UserAnswerAddRequest;
import com.wanglei.Mydadabackend.model.request.userAnswer.UserAnswerQueryRequest;
import com.wanglei.Mydadabackend.model.request.userAnswer.UserAnswerUpdateRequest;
import com.wanglei.Mydadabackend.model.vo.UserAnswerVO;
import com.wanglei.Mydadabackend.scoring.ScoringStrategyExecutor;
import com.wanglei.Mydadabackend.service.AppService;
import com.wanglei.Mydadabackend.service.UserAnswerService;
import com.wanglei.Mydadabackend.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/userAnswer")
@Slf4j
public class UserAnswerController {

    @Resource
    private UserService userService;

    @Resource
    private UserAnswerService userAnswerService;

    @Resource
    private AppService appService;

    @Resource
    private ScoringStrategyExecutor scoringStrategyExecutor;

    /**
     * 添加答题结果
     *
     * @param userAnswerAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addUserAnswer(@RequestBody UserAnswerAddRequest userAnswerAddRequest, HttpServletRequest request) throws Exception {
        if (userAnswerAddRequest == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        UserAnswer userAnswer = new UserAnswer();
        BeanUtil.copyProperties(userAnswerAddRequest, userAnswer);
        List<String> choices = userAnswerAddRequest.getChoices();
        userAnswer.setChoices(JSONUtil.toJsonStr(choices));
        userAnswerService.validUserAnswer(userAnswer, true);

        Long appId = userAnswerAddRequest.getAppId();
        if (appId == null || appId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用id非法");
        }
        App app = appService.getById(appId);
        if (app == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用不存在");
        }
        if(!Objects.equals(ReviewStatusEnum.getEnumByValue(app.getReviewStatus()), ReviewStatusEnum.PASS)){
            throw new BusinessException(ErrorCode.NO_AUTH, "应用未审核通过");
        }

        //获取登录用户
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NO_LOGIN);
        }
        userAnswer.setUserId(loginUser.getId());


        //插入数据
        boolean result = userAnswerService.save(userAnswer);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }

        //评分
        Long userAnswerId = userAnswer.getId();
        try{
            UserAnswer newUserAnswer = scoringStrategyExecutor.doScore(choices, app);
            newUserAnswer.setId(userAnswerId);
            userAnswerService.updateById(newUserAnswer);
        }catch (Exception e){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"评分错误");
        }

        return ResultUtils.success(userAnswerId);
    }

    /**
     * 删除答题结果
     *
     * @param id
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUserAnswer(@RequestParam Long id, HttpServletRequest request) {
        if (id == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NO_LOGIN);
        }
        //获取数据
        UserAnswer userAnswer = userAnswerService.getById(id);
        if (userAnswer == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        //仅本人或管理员可删除
        if (!userService.isAdmin(loginUser) && !userAnswer.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        boolean result = userAnswerService.removeById(id);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return ResultUtils.success(true);
    }

    /**
     * 更新答题结果
     *
     * @param userAnswerUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<Boolean> updateUserAnswer(@RequestBody UserAnswerUpdateRequest userAnswerUpdateRequest, HttpServletRequest request) {
        if (userAnswerUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NO_LOGIN);
        }

        //获取数据
        UserAnswer oldUserAnswer = userAnswerService.getById(userAnswerUpdateRequest.getId());
        if (oldUserAnswer == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        //仅本人或管理员可更新
        if (!userService.isAdmin(loginUser) && !oldUserAnswer.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        UserAnswer userAnswer = new UserAnswer();
        BeanUtil.copyProperties(userAnswerUpdateRequest, userAnswer);
        List<String> choices = userAnswerUpdateRequest.getChoices();
        userAnswer.setChoices(JSONUtil.toJsonStr(choices));
        userAnswerService.validUserAnswer(userAnswer, false);
        boolean result = userAnswerService.updateById(userAnswer);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return ResultUtils.success(true);
    }


    /**
     * 根据id获取答题结果
     *
     * @param id
     * @param request
     * @return
     */
    @PostMapping("/get/vo")
    public BaseResponse<UserAnswerVO> getUserAnswerVO(@RequestParam Long id, HttpServletRequest request) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (userService.getLoginUser(request) == null) {
            throw new BusinessException(ErrorCode.NO_LOGIN);
        }
        UserAnswer app = userAnswerService.getById(id);
        if (app == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        UserAnswerVO appVO = UserAnswerVO.objToVo(app);
        return ResultUtils.success(appVO);
    }

    /**
     * 分页获取答题结果列表（管理员）
     *
     * @param userAnswerQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<Page<UserAnswer>> listUserAnswerByPage(@RequestBody UserAnswerQueryRequest userAnswerQueryRequest,
                                                               HttpServletRequest request) {
        if (userAnswerQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long current = userAnswerQueryRequest.getCurrent();
        long pageSize = userAnswerQueryRequest.getPageSize();
        if (pageSize > 30) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<UserAnswer> queryWrapper = userAnswerService.getQueryWrapper(userAnswerQueryRequest);
        Page<UserAnswer> page = userAnswerService.page(new Page<>(current, pageSize), queryWrapper);
        return ResultUtils.success(page);
    }

    /**
     * 分页获取答题结果列表（用户）
     *
     * @param appQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<UserAnswerVO>> listUserAnswerVOByPage(@RequestBody UserAnswerQueryRequest appQueryRequest,
                                                                   HttpServletRequest request) {
        if (appQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long current = appQueryRequest.getCurrent();
        long pageSize = appQueryRequest.getPageSize();
        if (pageSize > 30) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<UserAnswer> queryWrapper = userAnswerService.getQueryWrapper(appQueryRequest);
        Page<UserAnswer> page = userAnswerService.page(new Page<>(current, pageSize), queryWrapper);
        return ResultUtils.success(userAnswerService.getVOPage(page, request));
    }

    /**
     * 获取当前用户创建答题结果列表
     *
     * @param appQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<UserAnswerVO>> listMyUserAnswerVOByPage(@RequestBody UserAnswerQueryRequest appQueryRequest,
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
        QueryWrapper<UserAnswer> queryWrapper = userAnswerService.getQueryWrapper(appQueryRequest);
        Page<UserAnswer> page = userAnswerService.page(new Page<>(current, pageSize), queryWrapper);
        return ResultUtils.success(userAnswerService.getVOPage(page, request));
    }


}
