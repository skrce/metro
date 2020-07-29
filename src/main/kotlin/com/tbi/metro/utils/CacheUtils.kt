package com.tbi.metro.utils

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.cache.caffeine.CaffeineCache
import java.util.concurrent.TimeUnit

object CacheUtils {
    fun buildCache(name: String,
                   expireMinutesAfterWrite: Int? = null,
                   maximumSize: Int? = null): CaffeineCache {
        return CaffeineCache(name, Caffeine.newBuilder()
                .also { c ->
                    expireMinutesAfterWrite?.let { c.expireAfterWrite(expireMinutesAfterWrite.toLong(), TimeUnit.MINUTES) }
                    maximumSize?.let { c.maximumSize(maximumSize.toLong()) }
                }.build())
    }
}