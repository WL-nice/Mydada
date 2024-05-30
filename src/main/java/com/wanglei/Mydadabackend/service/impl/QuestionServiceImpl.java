package com.wanglei.Mydadabackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wanglei.Mydadabackend.commmon.ErrorCode;
import com.wanglei.Mydadabackend.constant.CommonConstant;
import com.wanglei.Mydadabackend.exception.BusinessException;
import com.wanglei.Mydadabackend.mapper.QuestionMapper;
import com.wanglei.Mydadabackend.model.domain.App;
import com.wanglei.Mydadabackend.model.domain.Question;
import com.wanglei.Mydadabackend.model.domain.User;
import com.wanglei.Mydadabackend.model.request.question.QuestionQueryRequest;
import com.wanglei.Mydadabackend.model.vo.QuestionVO;
import com.wanglei.Mydadabackend.service.AppService;
import com.wanglei.Mydadabackend.service.QuestionService;
import com.wanglei.Mydadabackend.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author admin
 * @description 针对表【question(题目)】的数据库操作Service实现
 * @createDate 2024-05-28 16:18:40
 */
@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question>
        implements QuestionService {

    @Resource
    private AppService appService;

    @Resource
    private UserService userService;

    @Override
    public void validQuestion(Question question, boolean add) {
        String questionContent = question.getQuestionContent();
        Long appId = question.getAppId();

        if (add) {
            if (appId == null || appId <= 0) {
                throw new BusinessException(ErrorCode.NULL_ERROR, "应用id非法");
            } else {
                App app = appService.getById(appId);
                if (app == null) {
                    throw new BusinessException(ErrorCode.NULL_ERROR, "应用不存在");
                }

            }

        }

        if (questionContent == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "题目内容不能为空");
        }


    }

    @Override
    public QueryWrapper<Question> getQueryWrapper(QuestionQueryRequest questionQueryRequest) {
        Long id = questionQueryRequest.getId();
        String questionContent = questionQueryRequest.getQuestionContent();
        Long appId = questionQueryRequest.getAppId();
        Long userId = questionQueryRequest.getUserId();

        String sortOrder = questionQueryRequest.getSortOrder();
        String sortField = questionQueryRequest.getSortField();

        QueryWrapper<Question> queryWrapper = new QueryWrapper<>();
        queryWrapper.like(StringUtils.isNotBlank(questionContent), "questionContent", questionContent);

        queryWrapper.eq(id != null && id > 0, "id", id);
        queryWrapper.eq(appId != null && appId > 0, "appId", appId);
        queryWrapper.eq(userId != null && userId > 0, "userId", userId);

        queryWrapper.orderBy(StringUtils.isNotBlank(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        return queryWrapper;

    }

    @Override
    public Page<QuestionVO> getVOPage(Page<Question> page, HttpServletRequest request) {
        List<Question> questionList = page.getRecords();
        Page<QuestionVO> questionVOPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        if (CollUtil.isEmpty(questionList)) {
            return questionVOPage;
        }
        List<QuestionVO> questionVOList = questionList.stream().map(question -> {
            return QuestionVO.objToVo(question);
        }).toList();
        // 1. 关联查询用户信息
        Set<Long> userIdSet = questionList.stream().map(Question::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 填充信息
        questionVOList.forEach(questionVO -> {
            Long userId = questionVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            questionVO.setUser(userService.getUserVO(user));
        });
        questionVOPage.setRecords(questionVOList);
        return questionVOPage;
    }
}




