package com.wanglei.Mydadabackend.scoring;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wanglei.Mydadabackend.model.domain.App;
import com.wanglei.Mydadabackend.model.domain.Question;
import com.wanglei.Mydadabackend.model.domain.ScoringResult;
import com.wanglei.Mydadabackend.model.domain.UserAnswer;
import com.wanglei.Mydadabackend.model.request.question.QuestionContentDTO;
import com.wanglei.Mydadabackend.model.vo.QuestionVO;
import com.wanglei.Mydadabackend.service.QuestionService;
import com.wanglei.Mydadabackend.service.ScoringResultService;
import jakarta.annotation.Resource;

import java.util.List;
import java.util.Optional;

/**
 * 自定义测评类策略
 */
@ScoringStrategyConfig(appType = 0, scoringStrategy = 0)
public class CustomScoreScoringStrategy implements ScoringStrategy {

    @Resource
    private QuestionService questionService;

    @Resource
    private ScoringResultService scoringResultService;

    @Override
    public UserAnswer doScore(List<String> choices, App app) {
        //1.获取题目和题目结果
        Long appId = app.getId();
        QueryWrapper<Question> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("appId", appId);
        Question question = questionService.getOne(queryWrapper);
        QueryWrapper<ScoringResult> scoringResultQueryWrapper = new QueryWrapper<>();
        scoringResultQueryWrapper.eq("appId", appId);
        scoringResultQueryWrapper.orderByDesc("resultScoreRange");
        List<ScoringResult> scoringResultList = scoringResultService.list(scoringResultQueryWrapper);
        // 2. 统计用户的总得分
        int totalScore = 0;
        QuestionVO questionVO = QuestionVO.objToVo(question);
        List<QuestionContentDTO> questionContent = questionVO.getQuestionContent();

        // 遍历题目列表
        for (QuestionContentDTO questionContentDTO : questionContent) {
            // 遍历答案列表
            for (String answer : choices) {
                // 遍历题目中的选项
                for (QuestionContentDTO.Option option : questionContentDTO.getOptions()) {
                    // 如果答案和选项的key匹配
                    if (option.getKey().equals(answer)) {
                        int score = Optional.of(option.getScore()).orElse(0);
                        totalScore += score;
                    }
                }
            }
        }

        // 3. 遍历得分结果，找到第一个用户分数大于得分范围的结果，作为最终结果
        ScoringResult maxScoringResult = scoringResultList.get(0);
        for (ScoringResult scoringResult : scoringResultList) {
            if (totalScore >= scoringResult.getResultScoreRange()) {
                maxScoringResult = scoringResult;
                break;
            }
        }

        // 4. 构造返回值，填充答案对象的属性
        UserAnswer userAnswer = new UserAnswer();
        userAnswer.setAppId(appId);
        userAnswer.setAppType(app.getAppType());
        userAnswer.setScoringStrategy(app.getScoringStrategy());
        userAnswer.setChoices(JSONUtil.toJsonStr(choices));
        userAnswer.setResultId(maxScoringResult.getId());
        userAnswer.setResultName(maxScoringResult.getResultName());
        userAnswer.setResultDesc(maxScoringResult.getResultDesc());
        userAnswer.setResultPicture(maxScoringResult.getResultPicture());
        userAnswer.setResultScore(totalScore);
        return userAnswer;
    }
}

