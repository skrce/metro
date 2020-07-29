package com.tbi.metro.client.http

import com.tbi.metro.service.model.Routes
import com.tbi.metro.service.model.Stops
import com.tbi.metro.utils.CacheNames
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

/**
 * API Client for MBTA
 */
@Component
class MbtaClient(private val mbtaRestTemplate: RestTemplate) {

    @Cacheable(CacheNames.ROUTES)
    fun getAllRoutes(): Routes = mbtaRestTemplate.getForObject<Routes>(
            "${MBTA_BASE_URL}/routes",
            Routes::class.java)!!


    @Cacheable(CacheNames.STOPS)
    fun getStopsByRoute(route: String): Stops = mbtaRestTemplate.getForObject<Stops>(
            "${MBTA_BASE_URL}/stops?filter[route]=" + route,
            Stops::class.java)!!

    companion object {
        const val MBTA_BASE_URL = "https://api-v3.mbta.com/"
    }
}