package com.wanglei.Mydadabackend.model.request.userAnswer;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class UserAnswerAddRequest implements Serializable {
    /**
     * 答案 id (用于保证幂等性)
     */
    private Long id;

    /**
     * 应用 id
     */
    private Long appId;

    /**
     * 用户答案（JSON 数组）
     */
    private List<String> choices;

    private static final long serialVersionUID = 1L;
}