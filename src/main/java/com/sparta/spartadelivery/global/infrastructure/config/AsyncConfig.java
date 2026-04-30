package com.sparta.spartadelivery.global.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 핵심 쓰레드 수: 언제나 유지되는 쓰레드 개수
        executor.setCorePoolSize(5);

        // 최대 쓰레드 수: 큐가 찼을 때 확장되는 최대 개수
        executor.setMaxPoolSize(10);

        // 큐 용량: 작업 쓰레드가 corePoolSize만큼 찼을 때 대기하는 공간
        executor.setQueueCapacity(500);

        // 쓰레드 이름 접두사: 로그 확인 시 비동기 쓰레드임을 구분하기 위함
        executor.setThreadNamePrefix("SpartaAsync-");

        // 초기화 후 반환
        executor.initialize();
        return executor;
    }
}
