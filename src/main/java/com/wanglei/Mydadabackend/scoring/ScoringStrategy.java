package com.wanglei.Mydadabackend.scoring;

import com.wanglei.Mydadabackend.model.domain.App;
import com.wanglei.Mydadabackend.model.domain.UserAnswer;

import java.util.List;

/**
 * 评分策略
 */
public interface ScoringStrategy {
    /**
     * 评分
     * @param choices
     * @param app
     * @return
     */
    UserAnswer doScore(List<String> choices, App app);
}
