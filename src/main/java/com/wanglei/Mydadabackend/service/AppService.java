package com.wanglei.Mydadabackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wanglei.Mydadabackend.model.domain.App;
import com.wanglei.Mydadabackend.model.request.app.AppQueryRequest;
import com.wanglei.Mydadabackend.model.vo.AppVO;
import jakarta.servlet.http.HttpServletRequest;

/**
* @author admin
* @description 针对表【app(应用)】的数据库操作Service
* @createDate 2024-05-28 16:17:40
*/
public interface AppService extends IService<App> {

    QueryWrapper<App> getQueryWrapper(AppQueryRequest appQueryRequest);

    Page<AppVO> getVOPage(Page<App> page, HttpServletRequest request);

    void validApp(App app,boolean add);
}
