package com.wanglei.Mydadabackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wanglei.Mydadabackend.model.domain.Question;
import com.wanglei.Mydadabackend.model.request.question.QuestionQueryRequest;
import com.wanglei.Mydadabackend.model.vo.QuestionVO;
import jakarta.servlet.http.HttpServletRequest;

/**
* @author admin
* @description 针对表【question(题目)】的数据库操作Service
* @createDate 2024-05-28 16:18:40
*/
public interface QuestionService extends IService<Question> {

    /**
     * 校验题目
     * @param question
     * @param add
     */
    void validQuestion(Question question, boolean add);

    /**
     * 获取题目查询条件
     * @param questionQueryRequest
     * @return
     */
    QueryWrapper<Question> getQueryWrapper(QuestionQueryRequest questionQueryRequest);

    /**
     * 获取题目分页
     * @param page
     * @param request
     * @return
     */
    Page<QuestionVO> getVOPage(Page<Question> page, HttpServletRequest request);
}
