package com.tbi.metro.service

import com.tbi.metro.service.model.RouteStopsInfo
import com.tbi.metro.service.model.RoutesLongNames
import com.tbi.metro.service.model.StopInfos
import com.tbi.metro.service.model.TripRoutes

interface MetroService {
    fun getRoutesLongNamesByAttributeType(attributeType: String): RoutesLongNames
    fun getStopsWithMultipleRoutes(): StopInfos
    fun getRouteWithLeastStops(): RouteStopsInfo
    fun getRouteWithMostStops(): RouteStopsInfo
    fun searchTripRoutes(beginStopName: String, endStopName: String): TripRoutes
}