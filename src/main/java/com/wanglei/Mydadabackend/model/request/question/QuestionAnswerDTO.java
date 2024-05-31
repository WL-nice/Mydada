package com.wanglei.Mydadabackend.model.request.question;

import lombok.Data;

/**
 * 提交题目答案 (AI评分)
 */
@Data
public class QuestionAnswerDTO {
    /**
     * 题目
     */
    private String title;

    /**
     * 用户答案
     */
    private String userAnswer;
}
