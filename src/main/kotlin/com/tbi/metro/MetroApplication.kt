package com.tbi.metro

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.google.common.base.Predicates
import com.tbi.metro.utils.CacheNames
import com.tbi.metro.utils.CacheUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.support.SimpleCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import kotlin.reflect.full.memberProperties


@SpringBootApplication
@EnableCaching
class MetroApplication

fun main(args: Array<String>) {
    runApplication<MetroApplication>(*args)
}

@Configuration
class MvcConfig : WebMvcConfigurer {

    @Bean
    fun objectMapperBuilder(): Jackson2ObjectMapperBuilder =
            Jackson2ObjectMapperBuilder()
                    .modulesToInstall(KotlinModule())

    @Bean
    fun getRestTemplate(@Value("\${http.client.mbta.api-key}") mbtaApiKey: String): RestTemplate {
        val restTemplate = RestTemplate()
        val jsonConverter = MappingJackson2HttpMessageConverter()
        jsonConverter.supportedMediaTypes = listOf(MediaType.APPLICATION_JSON, MediaType("application", "vnd.api+json"))
        jsonConverter.objectMapper = ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        restTemplate.messageConverters = listOf(jsonConverter)
        restTemplate.interceptors.add(
                ClientHttpRequestInterceptor { request, body, execution ->
                    request.headers.set("x-api-key", mbtaApiKey)
                    execution.execute(request, body)
                }
        )
        return restTemplate
    }
}

@Configuration
internal class SwaggerConfig {
    @Bean
    fun api(): Docket {
        return Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any())
                .paths(Predicates.not(PathSelectors.regex("/error.*")))
                .build()
    }
}

@Configuration
internal class SecurityConfig : WebSecurityConfigurerAdapter() {
    override fun configure(http: HttpSecurity) {
        http.csrf().disable()
                .authorizeRequests()
                .anyRequest().authenticated()
                .and()
                .httpBasic()
    }
}

@Component
class CacheConfig {
    @Bean
    fun cacheManager(@Value("\${cache.expiration:60}") cacheExpiration: Int,
                     @Value("\${cache.max-size:10000}") cacheMaxSize: Int): CacheManager =
            SimpleCacheManager().also { scm ->
                scm.setCaches(
                        CacheNames::class.memberProperties
                                .map { it.name }
                                .map { cacheName ->
                                    CacheUtils.buildCache(
                                            name = cacheName,
                                            expireMinutesAfterWrite = cacheExpiration,
                                            maximumSize = cacheMaxSize
                                    )
                                }
                )
            }
}