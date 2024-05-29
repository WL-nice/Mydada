package com.wanglei.Mydadabackend.service.impl;

import java.util.Date;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wanglei.Mydadabackend.commmon.ErrorCode;
import com.wanglei.Mydadabackend.constant.CommonConstant;
import com.wanglei.Mydadabackend.exception.BusinessException;
import com.wanglei.Mydadabackend.mapper.AppMapper;
import com.wanglei.Mydadabackend.model.domain.App;
import com.wanglei.Mydadabackend.model.enums.AppTypeEnum;
import com.wanglei.Mydadabackend.model.request.app.AppQueryRequest;
import com.wanglei.Mydadabackend.model.vo.AppVO;
import com.wanglei.Mydadabackend.service.AppService;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author admin
 * @description 针对表【app(应用)】的数据库操作Service实现
 * @createDate 2024-05-28 16:17:40
 */
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App>
        implements AppService {

    @Override
    public QueryWrapper<App> getQueryWrapper(AppQueryRequest appQueryRequest) {
        Long id = appQueryRequest.getId();
        String appName = appQueryRequest.getAppName();
        String appDesc = appQueryRequest.getAppDesc();
        Integer appType = appQueryRequest.getAppType();
        Integer scoringStrategy = appQueryRequest.getScoringStrategy();
        Integer reviewStatus = appQueryRequest.getReviewStatus();
        Long reviewerId = appQueryRequest.getReviewerId();
        Long userId = appQueryRequest.getUserId();
        String sortField = appQueryRequest.getSortField();
        String sortOrder = appQueryRequest.getSortOrder();

        QueryWrapper<App> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(id != null && id > 0, "id", id);
        queryWrapper.eq(reviewerId != null && reviewerId > 0, "reviewerId", reviewerId);
        queryWrapper.eq(userId != null && userId > 0, "userId", userId);
        queryWrapper.like(StringUtils.isNotBlank(appName), "appName", appName);
        queryWrapper.like(StringUtils.isNotBlank(appDesc), "appDesc", appDesc);
        queryWrapper.eq(appType != null && appType > 0, "appType", appType);
        queryWrapper.eq(scoringStrategy != null && scoringStrategy >= 0, "scoringStrategy", scoringStrategy);
        queryWrapper.eq(reviewStatus != null && reviewStatus >= 0, "reviewStatus", reviewStatus);
        queryWrapper.orderBy(StringUtils.isNotBlank(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        return queryWrapper;
    }

    @Override
    public Page<AppVO> getVOPage(Page<App> page, HttpServletRequest request) {
        List<App> appList = page.getRecords();
        Page<AppVO> appVOPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        if (CollUtil.isEmpty(appList)) {
            return appVOPage;
        }
        List<AppVO> appVOList = appList.stream().map(app -> {
            return AppVO.objToVo(app);
        }).toList();
        appVOPage.setRecords(appVOList);
        return appVOPage;
    }

    @Override
    public void validApp(App app, boolean add) {
        String appName = app.getAppName();
        String appDesc = app.getAppDesc();
        Integer appType = app.getAppType();
        Integer scoringStrategy = app.getScoringStrategy();
        Integer reviewStatus = app.getReviewStatus();
        // 创建时，参数必须非空

        if (StringUtils.isBlank(appName)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用名称不能为空");
        }
        if (StringUtils.isBlank(appDesc)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用描述不能为空");
        }

        if (appType == null || AppTypeEnum.getEnumByValue(appType) == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用类型非法");
        }
        if (scoringStrategy == null || AppTypeEnum.getEnumByValue(scoringStrategy) == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用计分策略非法");
        }


        if (StringUtils.isNotBlank(appName) && appName.length() > 50) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用名称不能超过50个字符");
        }
        if (StringUtils.isNotBlank(appDesc) && appDesc.length() > 200) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用描述不能超过200个字符");
        }
        if (reviewStatus == null || AppTypeEnum.getEnumByValue(reviewStatus) == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用审核状态非法");
        }


    }
}




