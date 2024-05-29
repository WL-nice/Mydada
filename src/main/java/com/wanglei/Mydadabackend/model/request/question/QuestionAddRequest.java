package com.wanglei.Mydadabackend.model.request.question;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class QuestionAddRequest implements Serializable {

    /**
     * 题目内容（json格式）
     */
    private List<QuestionContentDTO> questionContent;

    /**
     * 应用 id
     */
    private Long appId;

    private static final long serialVersionUID = 1L;
}