package com.wanglei.Mydadabackend.model.dto;

import lombok.Data;

/**
 * 用户回答数量
 */
@Data
public class UserAnswerCountDTO {

    /**
     * 应用id
     */
    private Long appId;

    /**
     * 回答数量
     */
    private Long answerCount;
}
