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
import com.wanglei.Mydadabackend.model.domain.ScoringResult;
import com.wanglei.Mydadabackend.model.domain.User;
import com.wanglei.Mydadabackend.model.request.scoringResult.ScoringResultAddRequest;

import com.wanglei.Mydadabackend.model.request.scoringResult.ScoringResultQueryRequest;
import com.wanglei.Mydadabackend.model.request.scoringResult.ScoringResultUpdateRequest;
import com.wanglei.Mydadabackend.model.vo.ScoringResultVO;
import com.wanglei.Mydadabackend.service.ScoringResultService;
import com.wanglei.Mydadabackend.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/scoringResult")
@CrossOrigin(origins = "http://localhost:8080",allowCredentials = "true")
@Slf4j
public class ScoringResultController {

    @Resource
    private UserService userService;

    @Resource
    private ScoringResultService scoringResultService;

    /**
     * 添加评分结果
     *
     * @param scoringResultAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addScoringResult(@RequestBody ScoringResultAddRequest scoringResultAddRequest, HttpServletRequest request) {
        if (scoringResultAddRequest == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        ScoringResult scoringResult = new ScoringResult();
        BeanUtil.copyProperties(scoringResultAddRequest, scoringResult);
        List<String> resultProp = scoringResultAddRequest.getResultProp();
        scoringResult.setResultProp(JSONUtil.toJsonStr(resultProp));
        scoringResultService.validScoringResult(scoringResult, true);
        //获取登录用户
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NO_LOGIN);
        }
        scoringResult.setUserId(loginUser.getId());

        //插入数据
        boolean result = scoringResultService.save(scoringResult);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return ResultUtils.success(scoringResult.getId());
    }

    /**
     * 删除评分结果
     *
     * @param id
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteScoringResult(@RequestParam Long id, HttpServletRequest request) {
        if (id == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NO_LOGIN);
        }
        //获取数据
        ScoringResult scoringResult = scoringResultService.getById(id);
        if (scoringResult == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        //仅本人或管理员可删除
        if (!userService.isAdmin(loginUser) && !scoringResult.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        boolean result = scoringResultService.removeById(id);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return ResultUtils.success(true);
    }

    /**
     * 更新评分结果
     *
     * @param scoringResultUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateScoringResult(@RequestBody ScoringResultUpdateRequest scoringResultUpdateRequest, HttpServletRequest request) {
        if (scoringResultUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NO_LOGIN);
        }
        
        //获取数据
        ScoringResult oldScoringResult = scoringResultService.getById(scoringResultUpdateRequest.getId());
        if (oldScoringResult == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        //仅本人或管理员可更新
        if (!userService.isAdmin(loginUser) && !oldScoringResult.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        ScoringResult scoringResult = new ScoringResult();
        BeanUtil.copyProperties(scoringResultUpdateRequest, scoringResult);
        List<String> resultProp = scoringResultUpdateRequest.getResultProp();
        scoringResult.setResultProp(JSONUtil.toJsonStr(resultProp));
        scoringResultService.validScoringResult(scoringResult, false);
        boolean result = scoringResultService.updateById(scoringResult);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return ResultUtils.success(true);
    }
    

    /**
     * 根据id获取评分结果
     * @param id
     * @param request
     * @return
     */
    @PostMapping("/get/vo")
    public BaseResponse<ScoringResultVO> getScoringResultVO(@RequestParam Long id, HttpServletRequest request) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (userService.getLoginUser(request) == null) {
            throw new BusinessException(ErrorCode.NO_LOGIN);
        }
        ScoringResult app = scoringResultService.getById(id);
        if (app == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        ScoringResultVO appVO = ScoringResultVO.objToVo(app);
        return ResultUtils.success(appVO);
    }

    /**
     * 分页获取评分结果列表（管理员）
     * @param scoringResultQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<Page<ScoringResult>> listScoringResultByPage(@RequestBody ScoringResultQueryRequest scoringResultQueryRequest,
                                                 HttpServletRequest request) {
        if (scoringResultQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long current = scoringResultQueryRequest.getCurrent();
        long pageSize = scoringResultQueryRequest.getPageSize();
        if (pageSize > 30) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<ScoringResult> queryWrapper = scoringResultService.getQueryWrapper(scoringResultQueryRequest);
        Page<ScoringResult> page = scoringResultService.page(new Page<>(current, pageSize), queryWrapper);
        return ResultUtils.success(page);
    }

    /**
     * 分页获取评分结果列表（用户）
     * @param appQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<ScoringResultVO>> listScoringResultVOByPage(@RequestBody ScoringResultQueryRequest appQueryRequest,
                                                     HttpServletRequest request) {
        if (appQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long current = appQueryRequest.getCurrent();
        long pageSize = appQueryRequest.getPageSize();
        if (pageSize > 30) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<ScoringResult> queryWrapper = scoringResultService.getQueryWrapper(appQueryRequest);
        Page<ScoringResult> page = scoringResultService.page(new Page<>(current, pageSize), queryWrapper);
        return ResultUtils.success(scoringResultService.getVOPage(page,request));
    }

    /**
     * 获取当前用户创建评分结果列表
     * @param appQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<ScoringResultVO>> listMyScoringResultVOByPage(@RequestBody ScoringResultQueryRequest appQueryRequest,
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
        QueryWrapper<ScoringResult> queryWrapper = scoringResultService.getQueryWrapper(appQueryRequest);
        Page<ScoringResult> page = scoringResultService.page(new Page<>(current, pageSize), queryWrapper);
        return ResultUtils.success(scoringResultService.getVOPage(page,request));
    }


}
