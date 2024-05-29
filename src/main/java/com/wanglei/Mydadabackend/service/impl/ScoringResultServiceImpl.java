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
import com.wanglei.Mydadabackend.mapper.ScoringResultMapper;
import com.wanglei.Mydadabackend.model.domain.App;
import com.wanglei.Mydadabackend.model.domain.Question;
import com.wanglei.Mydadabackend.model.domain.ScoringResult;
import com.wanglei.Mydadabackend.model.request.scoringResult.ScoringResultQueryRequest;
import com.wanglei.Mydadabackend.model.vo.QuestionVO;
import com.wanglei.Mydadabackend.model.vo.ScoringResultVO;
import com.wanglei.Mydadabackend.service.AppService;
import com.wanglei.Mydadabackend.service.ScoringResultService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * @author admin
 * @description 针对表【scoring_result(评分结果)】的数据库操作Service实现
 * @createDate 2024-05-28 16:18:48
 */
@Service
public class ScoringResultServiceImpl extends ServiceImpl<ScoringResultMapper, ScoringResult>
        implements ScoringResultService {

    @Resource
    private AppService appService;

    @Override
    public void validScoringResult(ScoringResult scoringResult, boolean add) {
        String resultName = scoringResult.getResultName();
        String resultDesc = scoringResult.getResultDesc();
        Long appId = scoringResult.getAppId();
        if (resultName == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "名称不能为空");
        } else if (resultName.length() > 128) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "名称不能超过128个字符");
        }
        if (resultDesc != null && resultDesc.length() > 50) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "描述不能超过50个字符");
        }
        if (add) {
            if (appId == null || appId < 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用id非法");
            }
            App app = appService.getById(appId);
            if (app == null) {
                throw new BusinessException(ErrorCode.NULL_ERROR, "应用不存在");
            }

        }


    }

    @Override
    public QueryWrapper<ScoringResult> getQueryWrapper(ScoringResultQueryRequest scoringResultQueryRequest) {
        Long id = scoringResultQueryRequest.getId();
        String resultName = scoringResultQueryRequest.getResultName();
        String resultDesc = scoringResultQueryRequest.getResultDesc();
        String resultProp = scoringResultQueryRequest.getResultProp();
        Integer resultScoreRange = scoringResultQueryRequest.getResultScoreRange();
        Long appId = scoringResultQueryRequest.getAppId();
        Long userId = scoringResultQueryRequest.getUserId();
        String sortOrder = scoringResultQueryRequest.getSortOrder();
        String sortField = scoringResultQueryRequest.getSortField();

        QueryWrapper<ScoringResult> queryWrapper = new QueryWrapper<>();
        queryWrapper.like(StringUtils.isNotBlank(resultName), "resultName", resultName);
        queryWrapper.like(StringUtils.isNotBlank(resultDesc), "resultDesc", resultDesc);
        queryWrapper.like(StringUtils.isNotBlank(resultProp), "resultProp", resultProp);

        queryWrapper.eq(id != null && id > 0, "id", id);
        queryWrapper.eq(appId != null && appId > 0, "appId", appId);
        queryWrapper.eq(userId != null && userId > 0, "userId", userId);
        queryWrapper.eq(resultScoreRange != null, "resultScoreRange", resultScoreRange);
        queryWrapper.orderBy(StringUtils.isNotBlank(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        return queryWrapper;
    }

    @Override
    public Page<ScoringResultVO> getVOPage(Page<ScoringResult> page, HttpServletRequest request) {
        List<ScoringResult> scoringResultList = page.getRecords();
        Page<ScoringResultVO> scoringResultVOPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        if (CollUtil.isEmpty(scoringResultList)) {
            return scoringResultVOPage;
        }
        List<ScoringResultVO> questionVOList = scoringResultList.stream().map(scoringResult -> {
            return ScoringResultVO.objToVo(scoringResult);
        }).toList();
        scoringResultVOPage.setRecords(questionVOList);
        return scoringResultVOPage;
    }
}




