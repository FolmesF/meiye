package com.zhangmei.store.config;

import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    /** 雪花 ID 超出 JS 安全整数，序列化成字符串避免前端精度丢失。 */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer longAsString() {
        return builder -> builder.serializerByType(Long.class, ToStringSerializer.instance);
    }
}
