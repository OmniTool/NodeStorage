package hex.multinode.storage.aspect;

import hex.multinode.storage.model.data.MultiNode;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Component
@Aspect
@Log4j2
public class LoggingAspect {

    @Around("@annotation(hex.multinode.storage.aspect.NodeToLog)")
    public MultiNode findNodeLoggingAdvice(ProceedingJoinPoint pjp) throws Throwable {
        String methodName = pjp.getSignature().getName();
        MultiNode result = (MultiNode) pjp.proceed();
        log.info(methodName + " node: [id = " + result.getId() + ", title = " + result.getTitle() + "]");
        return result;
    }

    @Before("@annotation(hex.multinode.storage.aspect.GrpcRqToLog)")
    public void grpcRqLoggingAdvice(JoinPoint jp) {
        String methodName = jp.getSignature().getName();
        log.info(methodName + " gRPC request: [" + jp.getArgs()[0] + "]");
    }

}
