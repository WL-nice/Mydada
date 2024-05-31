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
import com.wanglei.Mydadabackend.manager.AiManager;
import com.wanglei.Mydadabackend.model.domain.App;
import com.wanglei.Mydadabackend.model.domain.Question;
import com.wanglei.Mydadabackend.model.domain.User;
import com.wanglei.Mydadabackend.model.enums.AppTypeEnum;
import com.wanglei.Mydadabackend.model.request.question.*;
import com.wanglei.Mydadabackend.model.vo.QuestionVO;
import com.wanglei.Mydadabackend.service.AppService;
import com.wanglei.Mydadabackend.service.QuestionService;
import com.wanglei.Mydadabackend.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/question")
@CrossOrigin(origins = "http://localhost:8080",allowCredentials = "true")
@Slf4j
public class QuestionController {

    @Resource
    private UserService userService;

    @Resource
    private QuestionService questionService;

    @Resource
    private AppService appService;

    @Resource
    private AiManager aiManager;

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

    // region AI 生成题目功能
    private static final String GENERATE_QUESTION_SYSTEM_MESSAGE = "你是一位严谨的出题专家，我会给你如下信息：\n" +
            "```\n" +
            "应用名称，\n" +
            "【【【应用描述】】】，\n" +
            "应用类别，\n" +
            "要生成的题目数，\n" +
            "每个题目的选项数\n" +
            "```\n" +
            "\n" +
            "请你根据上述信息，按照以下步骤来出题：\n" +
            "1. 要求：题目和选项尽可能地短，题目不要包含序号，每题的选项数以我提供的为主，题目不能重复\n" +
            "2. 严格按照下面的 json 格式输出题目和选项\n" +
            "```\n" +
            "[{\"options\":[{\"value\":\"选项内容\",\"key\":\"A\"},{\"value\":\"\",\"key\":\"B\"}],\"title\":\"题目标题\"}]\n" +
            "```\n" +
            "title 是题目，options 是选项，每个选项的 key 按照英文字母序（比如 A、B、C、D）以此类推，value 是选项内容\n" +
            "3. 检查题目是否包含序号，若包含序号则去除序号\n" +
            "4. 返回的题目列表格式必须为 JSON 数组";

    /**
     * 生成题目的用户消息
     *
     * @param app
     * @param questionNumber
     * @param optionNumber
     * @return
     */
    private String getGenerateQuestionUserMessage(App app, int questionNumber, int optionNumber) {
        StringBuilder userMessage = new StringBuilder();
        userMessage.append(app.getAppName()).append("\n");
        userMessage.append(app.getAppDesc()).append("\n");
        userMessage.append(AppTypeEnum.getEnumByValue(app.getAppType()).getText()).append("\n");
        userMessage.append(questionNumber).append("\n");
        userMessage.append(optionNumber);
        return userMessage.toString();
    }

    @PostMapping("/ai_generate")
    public BaseResponse<List<QuestionContentDTO>> aiGenerateQuestion(
            @RequestBody AiGenerateQuestionRequest aiGenerateQuestionRequest) {
        if(aiGenerateQuestionRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 获取参数
        Long appId = aiGenerateQuestionRequest.getAppId();
        int questionNumber = aiGenerateQuestionRequest.getQuestionNumber();
        int optionNumber = aiGenerateQuestionRequest.getOptionNumber();
        // 获取应用信息
        App app = appService.getById(appId);
        if(app == null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        // 封装 Prompt
        String userMessage = getGenerateQuestionUserMessage(app, questionNumber, optionNumber);
        // AI 生成
        String result = aiManager.doSyncRequest(GENERATE_QUESTION_SYSTEM_MESSAGE, userMessage);
        // 截取需要的 JSON 信息
        int start = result.indexOf("[");
        int end = result.lastIndexOf("]");
        String json = result.substring(start, end + 1);
        List<QuestionContentDTO> questionContentDTOList = JSONUtil.toList(json, QuestionContentDTO.class);
        return ResultUtils.success(questionContentDTOList);
    }

    // endregion


}
