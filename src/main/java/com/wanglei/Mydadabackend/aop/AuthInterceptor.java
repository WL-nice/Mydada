package com.wanglei.Mydadabackend.aop;



import com.wanglei.Mydadabackend.annotation.AuthCheck;
import com.wanglei.Mydadabackend.commmon.ErrorCode;
import com.wanglei.Mydadabackend.constant.UserConstant;
import com.wanglei.Mydadabackend.exception.BusinessException;
import com.wanglei.Mydadabackend.model.domain.User;
import com.wanglei.Mydadabackend.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class AuthInterceptor {

    @Resource
    private UserService userService;


    /**
     * 执行拦截
     *
     * @param joinPoint
     * @param authCheck
     * @return
     */
    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        String mustRole = authCheck.mustRole();
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        // 当前登录用户
        User user = userService.getLoginUser(request);

        // 必须有所有权限才通过
        if (StringUtils.isNotBlank(mustRole)) {
            Integer userRoleNum = user.getUserRole();
            String userRole = "";
            if(userRoleNum == UserConstant.ADMIN_ROLE){
                userRole = "admin";
            }
            if (!mustRole.equals(userRole)) {
                throw new BusinessException(ErrorCode.NO_AUTH);
            }
        }
        // 通过权限校验，放行
        return joinPoint.proceed();
    }
}