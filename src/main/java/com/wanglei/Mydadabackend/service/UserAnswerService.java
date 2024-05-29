package com.wanglei.Mydadabackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wanglei.Mydadabackend.model.domain.UserAnswer;
import com.wanglei.Mydadabackend.model.request.userAnswer.UserAnswerQueryRequest;
import com.wanglei.Mydadabackend.model.vo.UserAnswerVO;
import jakarta.servlet.http.HttpServletRequest;

/**
* @author admin
* @description 针对表【user_answer(用户答题记录)】的数据库操作Service
* @createDate 2024-05-28 16:18:55
*/
public interface UserAnswerService extends IService<UserAnswer> {

    void validUserAnswer(UserAnswer userAnswer, boolean b);

    QueryWrapper<UserAnswer> getQueryWrapper(UserAnswerQueryRequest userAnswerQueryRequest);

    Page<UserAnswerVO> getVOPage(Page<UserAnswer> page, HttpServletRequest request);
}
