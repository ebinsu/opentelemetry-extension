# Extensions

## Introduction
- undertow

方法dispatchRequest 进入前，记录当前登录账号的id（principal.id ）。

- spring mvc - dispatch-servlet

方法processDispatchResult 执行后，如果有发生异常，记录异常的异常码、异常消息（error.code,error.message）。

- health check endpoint sampler

通过配置项：OTEL.TRACES.SAMPLER.HEALTH.ENDPOINT ，来过滤健康检测端口。

## Build extensions

为了简化部署，将extensions嵌入OpenTelemetry Java Agent，成为一个jar 包。

命令：

```bash
   ./gradlew extendedAgent
```

最终产物： opentelemetry-javaagent.jar

 
