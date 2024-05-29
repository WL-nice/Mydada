package com.wanglei.Mydadabackend.service.impl;

import java.util.Date;
import java.util.List;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wanglei.Mydadabackend.commmon.ErrorCode;
import com.wanglei.Mydadabackend.constant.CommonConstant;
import com.wanglei.Mydadabackend.exception.BusinessException;
import com.wanglei.Mydadabackend.mapper.UserAnswerMapper;
import com.wanglei.Mydadabackend.model.domain.App;
import com.wanglei.Mydadabackend.model.domain.ScoringResult;
import com.wanglei.Mydadabackend.model.domain.UserAnswer;
import com.wanglei.Mydadabackend.model.request.userAnswer.UserAnswerQueryRequest;
import com.wanglei.Mydadabackend.model.vo.ScoringResultVO;
import com.wanglei.Mydadabackend.model.vo.UserAnswerVO;
import com.wanglei.Mydadabackend.service.AppService;
import com.wanglei.Mydadabackend.service.UserAnswerService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * @author admin
 * @description 针对表【user_answer(用户答题记录)】的数据库操作Service实现
 * @createDate 2024-05-28 16:18:55
 */
@Service
public class UserAnswerServiceImpl extends ServiceImpl<UserAnswerMapper, UserAnswer>
        implements UserAnswerService {

    @Resource
    private AppService appService;

    @Override
    public void validUserAnswer(UserAnswer userAnswer, boolean add) {

        String choices = userAnswer.getChoices();

        if (choices == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "选项不能为空");
        }


    }

    @Override
    public QueryWrapper<UserAnswer> getQueryWrapper(UserAnswerQueryRequest userAnswerQueryRequest) {
        Long id = userAnswerQueryRequest.getId();
        Long appId = userAnswerQueryRequest.getAppId();
        Integer appType = userAnswerQueryRequest.getAppType();
        Integer scoringStrategy = userAnswerQueryRequest.getScoringStrategy();
        String choices = userAnswerQueryRequest.getChoices();
        Long resultId = userAnswerQueryRequest.getResultId();
        String resultName = userAnswerQueryRequest.getResultName();
        String resultDesc = userAnswerQueryRequest.getResultDesc();
        Long userId = userAnswerQueryRequest.getUserId();
        String sortOrder = userAnswerQueryRequest.getSortOrder();
        String sortField = userAnswerQueryRequest.getSortField();

        QueryWrapper<UserAnswer> queryWrapper = new QueryWrapper<>();
        queryWrapper.like(StringUtils.isNotBlank(resultName), "resultName", resultName);
        queryWrapper.like(StringUtils.isNotBlank(resultDesc), "resultDesc", resultDesc);
        queryWrapper.like(StringUtils.isNotBlank(choices), "choices", choices);


        queryWrapper.eq(id != null && id > 0, "id", id);
        queryWrapper.eq(appId != null && appId > 0, "appId", appId);
        queryWrapper.eq(userId != null && userId > 0, "userId", userId);
        queryWrapper.eq(appType != null && appType > 0, "appType", appType);
        queryWrapper.eq(scoringStrategy != null && scoringStrategy > 0, "scoringStrategy", scoringStrategy);
        queryWrapper.eq(resultId != null, "resultId", resultId);
        queryWrapper.orderBy(StringUtils.isNotBlank(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        return queryWrapper;

    }

    @Override
    public Page<UserAnswerVO> getVOPage(Page<UserAnswer> page, HttpServletRequest request) {
        List<UserAnswer> userAnswerList = page.getRecords();
        Page<UserAnswerVO> userAnswerVOPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        if (CollUtil.isEmpty(userAnswerList)) {
            return userAnswerVOPage;
        }
        List<UserAnswerVO> questionVOList = userAnswerList.stream().map(userAnswer -> {
            return UserAnswerVO.objToVo(userAnswer);
        }).toList();
        userAnswerVOPage.setRecords(questionVOList);
        return userAnswerVOPage;
    }
}




