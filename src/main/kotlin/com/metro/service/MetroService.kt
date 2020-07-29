package com.metro.service

import com.metro.service.model.RouteStopsInfo
import com.metro.service.model.RoutesLongNames
import com.metro.service.model.StopInfos
import com.metro.service.model.TripRoutes

interface MetroService {
    fun getRoutesLongNamesByAttributeType(attributeType: String): RoutesLongNames
    fun getStopsWithMultipleRoutes(): StopInfos
    fun getRouteWithLeastStops(): RouteStopsInfo
    fun getRouteWithMostStops(): RouteStopsInfo
    fun searchTripRoutes(beginStopName: String, endStopName: String): TripRoutes
}