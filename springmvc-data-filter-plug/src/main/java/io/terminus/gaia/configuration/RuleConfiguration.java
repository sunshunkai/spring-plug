package io.terminus.gaia.configuration;


import io.terminus.gaia.core.json.CommonDesensitizate;
import io.terminus.gaia.core.json.jackson.JacksonDesensitizate;
import io.terminus.gaia.interceptor.DataFilterInterceptor;
import io.terminus.gaia.load.EnvLoadRule;
import io.terminus.gaia.load.LoadRule;
import io.terminus.gaia.load.ResourceLoadRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author 孙顺凯（惊云）
 * @date 2021/11/14
 */
@Configuration
@ComponentScan("io.terminus.gaia")
public class RuleConfiguration implements WebMvcConfigurer {

    private static final String FLOW = "/api/trantor/flow";
    private static final String FUNC = "/api/trantor/func";

    @Autowired
    private RuleConfig ruleConfig;

    @Bean
    @ConditionalOnMissingBean
    public LoadRule loadRule(){
        return new EnvLoadRule();
    }

    @Bean
    @ConditionalOnMissingBean
    public CommonDesensitizate commonDesensitizate(){
        return new JacksonDesensitizate(loadRule().loadRule());
    }


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new DataFilterInterceptor())
                            .addPathPatterns(ruleConfig.getFilterUrl());
    }
}
