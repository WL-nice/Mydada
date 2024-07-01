package com.wanglei.Mydadabackend.controller;

import com.wanglei.Mydadabackend.commmon.BaseResponse;
import com.wanglei.Mydadabackend.commmon.ResultUtils;
import com.wanglei.Mydadabackend.mapper.UserAnswerMapper;
import com.wanglei.Mydadabackend.model.dto.AppAnswerResultCountDTO;
import com.wanglei.Mydadabackend.model.dto.UserAnswerCountDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/app/statistic")
@CrossOrigin(origins = "http://localhost:8080", allowCredentials = "true")
@Slf4j
public class AppStatisticController {

    @Resource
    private UserAnswerMapper userAnswerMapper;

    /**
     * 热门应用及回答数统计(top 10)
     *
     * @return
     */
    @GetMapping("/answer_count")
    public BaseResponse<List<UserAnswerCountDTO>> getUserAnswerCount() {
        return ResultUtils.success(userAnswerMapper.getUserAnswerCount());
    }

    /**
     * 某应用回答结果分布统计（top 10）
     *
     * @param appId
     * @return
     */
    @GetMapping("/result_count")
    public BaseResponse<List<AppAnswerResultCountDTO>> getAppAnswerResultCount(@RequestParam Long appId) {
        return ResultUtils.success(userAnswerMapper.getAppAnswerResultCount(appId));
    }
}
