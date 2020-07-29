package com.metro.client.http

import com.metro.exception.ExceptionHandler
import com.metro.service.model.Routes
import com.metro.service.model.Stops
import com.metro.utils.CacheNames
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

/**
 * API Client for MBTA
 */
@Component
class MbtaClient(private val mbtaRestTemplate: RestTemplate) {

    @Cacheable(CacheNames.ROUTES)
    fun getAllRoutes(): Routes {
        try {
            return mbtaRestTemplate.getForObject<Routes>(
                    "$MBTA_BASE_URL/routes",
                    Routes::class.java)!!
        } catch (e: Exception) {
            throw ExceptionHandler.MetroClientException("Mbta /routes endpoint unavailable. Error: " + e.message)
        }
    }

    @Cacheable(CacheNames.STOPS)
    fun getStopsByRoute(route: String): Stops {
        try {
            return mbtaRestTemplate.getForObject<Stops>(
                    "$MBTA_BASE_URL/stops?filter[route]=$route",
                    Stops::class.java)!!
        } catch (e: Exception) {
            throw ExceptionHandler.MetroClientException("Mbta /stops endpoint unavailable. Error: " + e.message)
        }
    }

    companion object {
        const val MBTA_BASE_URL = "https://api-v3.mbta.com/"
    }
}