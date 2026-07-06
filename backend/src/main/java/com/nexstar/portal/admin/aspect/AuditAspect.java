package com.nexstar.portal.admin.aspect;

import com.nexstar.portal.admin.service.AuditService;
import com.nexstar.portal.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditAspect {

    private final AuditService auditService;

    @Pointcut("execution(public * com.nexstar.portal..*Controller.*(..))")
    public void controllerMethods() {
    }

    @AfterReturning("controllerMethods()")
    public void afterControllerMethod(JoinPoint joinPoint) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()
                    || !(authentication.getPrincipal() instanceof UserPrincipal)) {
                return;
            }

            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
            String methodName = joinPoint.getSignature().getName();
            String controllerName = joinPoint.getTarget().getClass().getSimpleName();
            String action = controllerName.replace("Controller", "").toUpperCase()
                    + "_" + camelToScreamingSnake(methodName);
            String description = controllerName + "#" + methodName + " executed successfully";

            auditService.log(principal.getId(), principal.getEmail(), action, description);
        } catch (Exception ex) {
            log.debug("AuditAspect: failed to log audit entry: {}", ex.getMessage());
        }
    }

    private String camelToScreamingSnake(String camel) {
        return camel.replaceAll("([a-z])([A-Z])", "$1_$2").toUpperCase();
    }
}
