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
import com.wanglei.Mydadabackend.model.domain.Question;
import com.wanglei.Mydadabackend.model.domain.User;
import com.wanglei.Mydadabackend.model.request.question.QuestionAddRequest;
import com.wanglei.Mydadabackend.model.request.question.QuestionContentDTO;
import com.wanglei.Mydadabackend.model.request.question.QuestionQueryRequest;
import com.wanglei.Mydadabackend.model.request.question.QuestionUpdateRequest;
import com.wanglei.Mydadabackend.model.vo.QuestionVO;
import com.wanglei.Mydadabackend.service.QuestionService;
import com.wanglei.Mydadabackend.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/question")
@Slf4j
public class QuestionController {

    @Resource
    private UserService userService;

    @Resource
    private QuestionService questionService;

    /**
     * 添加问题
     *
     * @param questionAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addQuestion(@RequestBody QuestionAddRequest questionAddRequest, HttpServletRequest request) {
        if (questionAddRequest == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        Question question = new Question();
        BeanUtil.copyProperties(questionAddRequest, question);
        List<QuestionContentDTO> questionContent = questionAddRequest.getQuestionContent();
        question.setQuestionContent(JSONUtil.toJsonStr(questionContent));
        questionService.validQuestion(question, true);
        //获取登录用户
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NO_LOGIN);
        }
        question.setUserId(loginUser.getId());

        //插入数据
        boolean result = questionService.save(question);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return ResultUtils.success(question.getId());
    }

    /**
     * 删除问题
     *
     * @param id
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteQuestion(@RequestParam Long id, HttpServletRequest request) {
        if (id == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NO_LOGIN);
        }
        //获取数据
        Question question = questionService.getById(id);
        if (question == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        //仅本人或管理员可删除
        if (!userService.isAdmin(loginUser) && !question.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        boolean result = questionService.removeById(id);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return ResultUtils.success(true);
    }

    /**
     * 更新问题
     *
     * @param questionUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<Boolean> updateQuestion(@RequestBody QuestionUpdateRequest questionUpdateRequest, HttpServletRequest request) {
        if (questionUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NO_LOGIN);
        }
        
        //获取数据
        Question oldQuestion = questionService.getById(questionUpdateRequest.getId());
        if (oldQuestion == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        //仅本人或管理员可更新
        if (!userService.isAdmin(loginUser) && !oldQuestion.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        Question question = new Question();
        BeanUtil.copyProperties(questionUpdateRequest, question);
        List<QuestionContentDTO> questionContent = questionUpdateRequest.getQuestionContent();
        question.setQuestionContent(JSONUtil.toJsonStr(questionContent));
        questionService.validQuestion(question, false);
        boolean result = questionService.updateById(question);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return ResultUtils.success(true);
    }
    

    /**
     * 根据id获取问题
     * @param id
     * @param request
     * @return
     */
    @PostMapping("/get/vo")
    public BaseResponse<QuestionVO> getQuestionVO(@RequestParam Long id, HttpServletRequest request) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (userService.getLoginUser(request) == null) {
            throw new BusinessException(ErrorCode.NO_LOGIN);
        }
        Question app = questionService.getById(id);
        if (app == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        QuestionVO appVO = QuestionVO.objToVo(app);
        return ResultUtils.success(appVO);
    }

    /**
     * 分页获取问题列表（管理员）
     * @param questionQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<Page<Question>> listQuestionByPage(@RequestBody QuestionQueryRequest questionQueryRequest,
                                                 HttpServletRequest request) {
        if (questionQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long current = questionQueryRequest.getCurrent();
        long pageSize = questionQueryRequest.getPageSize();
        if (pageSize > 30) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<Question> queryWrapper = questionService.getQueryWrapper(questionQueryRequest);
        Page<Question> page = questionService.page(new Page<>(current, pageSize), queryWrapper);
        return ResultUtils.success(page);
    }

    /**
     * 分页获取问题列表（用户）
     * @param appQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<QuestionVO>> listQuestionVOByPage(@RequestBody QuestionQueryRequest appQueryRequest,
                                                     HttpServletRequest request) {
        if (appQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long current = appQueryRequest.getCurrent();
        long pageSize = appQueryRequest.getPageSize();
        if (pageSize > 30) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<Question> queryWrapper = questionService.getQueryWrapper(appQueryRequest);
        Page<Question> page = questionService.page(new Page<>(current, pageSize), queryWrapper);
        return ResultUtils.success(questionService.getVOPage(page,request));
    }

    /**
     * 获取当前用户创建问题列表
     * @param appQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<QuestionVO>> listMyQuestionVOByPage(@RequestBody QuestionQueryRequest appQueryRequest,
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
        QueryWrapper<Question> queryWrapper = questionService.getQueryWrapper(appQueryRequest);
        Page<Question> page = questionService.page(new Page<>(current, pageSize), queryWrapper);
        return ResultUtils.success(questionService.getVOPage(page,request));
    }


}
