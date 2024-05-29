package com.wanglei.Mydadabackend.model.request.userAnswer;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class UserAnswerAddRequest implements Serializable {

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