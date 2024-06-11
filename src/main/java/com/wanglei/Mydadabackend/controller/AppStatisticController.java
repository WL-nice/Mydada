package com.wanglei.Mydadabackend.controller;

import com.wanglei.Mydadabackend.commmon.BaseResponse;
import com.wanglei.Mydadabackend.commmon.ResultUtils;
import com.wanglei.Mydadabackend.mapper.UserAnswerMapper;
import com.wanglei.Mydadabackend.model.dto.UserAnswerCountDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/app/statistic")
@CrossOrigin(origins = "http://localhost:8080",allowCredentials = "true")
@Slf4j
public class AppStatisticController {

    @Resource
    private UserAnswerMapper userAnswerMapper;

    @GetMapping("/get/answer_count")
    public BaseResponse<List<UserAnswerCountDTO>> getUserAnswerCount() {
        return ResultUtils.success(userAnswerMapper.getUserAnswerCount());
    }
}
