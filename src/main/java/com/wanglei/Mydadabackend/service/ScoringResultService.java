package com.wanglei.Mydadabackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wanglei.Mydadabackend.model.domain.ScoringResult;
import com.wanglei.Mydadabackend.model.request.scoringResult.ScoringResultQueryRequest;
import com.wanglei.Mydadabackend.model.vo.ScoringResultVO;
import jakarta.servlet.http.HttpServletRequest;

/**
* @author admin
* @description 针对表【scoring_result(评分结果)】的数据库操作Service
* @createDate 2024-05-28 16:18:48
*/
public interface ScoringResultService extends IService<ScoringResult> {

    void validScoringResult(ScoringResult scoringResult, boolean b);

    QueryWrapper<ScoringResult> getQueryWrapper(ScoringResultQueryRequest scoringResultQueryRequest);

    Page<ScoringResultVO> getVOPage(Page<ScoringResult> page, HttpServletRequest request);
}
