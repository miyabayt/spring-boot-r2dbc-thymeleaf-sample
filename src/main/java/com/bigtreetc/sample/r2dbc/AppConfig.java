package com.bigtreetc.sample.r2dbc;

import static com.bigtreetc.sample.r2dbc.base.web.BaseWebConst.*;

import com.bigtreetc.sample.r2dbc.base.domain.model.BaseEntity;
import com.bigtreetc.sample.r2dbc.base.util.MessageUtils;
import com.bigtreetc.sample.r2dbc.base.web.controller.converter.IntegerValueEnumConverterFactory;
import com.bigtreetc.sample.r2dbc.base.web.controller.converter.StringValueEnumConverterFactory;
import com.bigtreetc.sample.r2dbc.base.web.filter.ElapsedMillisLoggingFilter;
import com.bigtreetc.sample.r2dbc.base.web.filter.SetRedirectAttributeFilter;
import lombok.val;
import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.modelmapper.spi.PropertyInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.web.ReactivePageableHandlerMethodArgumentResolver;
import org.springframework.format.FormatterRegistry;
import org.springframework.security.web.server.util.matcher.NegatedServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.result.method.annotation.ArgumentResolverConfigurer;
import org.springframework.web.server.adapter.ForwardedHeaderTransformer;

@Configuration
public class AppConfig implements WebFluxConfigurer {

  @Override
  public void addFormatters(FormatterRegistry registry) {
    registry.addConverterFactory(new StringValueEnumConverterFactory());
    registry.addConverterFactory(new IntegerValueEnumConverterFactory());
  }

  @Override
  public void configureArgumentResolvers(ArgumentResolverConfigurer configurer) {
    val resolver = new ReactivePageableHandlerMethodArgumentResolver();
    resolver.setOneIndexedParameters(true);
    configurer.addCustomResolver(resolver);
  }

  @Bean
  public ElapsedMillisLoggingFilter elapsedMillisLoggingFilter() {
    val matcher =
        new NegatedServerWebExchangeMatcher(
            ServerWebExchangeMatchers.pathMatchers(
                WEBJARS_URL, STATIC_RESOURCES_URL, ACTUATOR_URL));
    val filter = new ElapsedMillisLoggingFilter();
    filter.setRequiresAuthenticationMatcher(matcher);
    return filter;
  }

  @Bean
  public ForwardedHeaderTransformer forwardedHeaderTransformer() {
    return new ForwardedHeaderTransformer();
  }

  @Bean
  public SetRedirectAttributeFilter setRedirectAttributeFilter() {
    val matcher =
        new NegatedServerWebExchangeMatcher(
            ServerWebExchangeMatchers.pathMatchers(
                WEBJARS_URL, STATIC_RESOURCES_URL, ACTUATOR_URL));
    val filter = new SetRedirectAttributeFilter();
    filter.setRequiresAuthenticationMatcher(matcher);
    return filter;
  }

  @Bean
  @Primary
  public LocalValidatorFactoryBean beanValidator(MessageSource messageSource) {
    val bean = new LocalValidatorFactoryBean();
    bean.setValidationMessageSource(messageSource);
    return bean;
  }

  @Bean
  public MethodValidationPostProcessor methodValidationPostProcessor(
      LocalValidatorFactoryBean localValidatorFactoryBean) {
    val bean = new MethodValidationPostProcessor();
    bean.setValidator(localValidatorFactoryBean);
    return bean;
  }

  @Bean
  public LayoutDialect layoutDialect() {
    return new LayoutDialect();
  }

  @Bean
  public ModelMapper modelMapper() {
    val modelMapper = new ModelMapper();
    val configuration = modelMapper.getConfiguration();
    configuration.setPropertyCondition(
        context -> {
          // IDは上書きしないようにする
          PropertyInfo propertyInfo = context.getMapping().getLastDestinationProperty();
          return !(context.getParent().getDestination() instanceof BaseEntity
              && propertyInfo.getName().equals("id"));
        });
    // 厳格にマッピングする
    configuration.setMatchingStrategy(MatchingStrategies.STRICT);
    return modelMapper;
  }

  @Autowired
  public void initUtils(MessageSource messageSource) {
    MessageUtils.init(messageSource);
  }
}
