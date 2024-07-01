package com.wanglei.Mydadabackend.model.dto;

import lombok.Data;

/**
 * 用户回答结果数量
 */
@Data
public class AppAnswerResultCountDTO {

    /**
     * 结果名称
     */
    private String  resultName;

    /**
     * 结果数量
     */
    private Long resultCount;
}
