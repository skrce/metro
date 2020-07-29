package com.metro.service.model

import io.swagger.annotations.ApiModel

@ApiModel(description = "Represents a list of route long-names.")
data class RoutesLongNames(val longNames: List<String> = emptyList())

data class Routes(val data: List<Route> = emptyList())

data class Route(val attributes: RouteAttributes = RouteAttributes(),
                 val id: String? = null,
                 val type: String? = null)

data class RouteAttributes(val long_name: String? = null,
                           val type: String? = null)

data class Stop(val id: String? = null,
                val attributes: StopAttributes? = null)

data class StopAttributes(val name: String? = null)

data class StopInfos(val stopInfos: List<StopInfo>? = emptyList())

@ApiModel(description = "Represents the stop-name along with the route-names that go through that stop.")
data class StopInfo(val stopName: String? = null,
                    val routeNames: List<String>? = emptyList())

data class Stops(val data: List<Stop>? = emptyList())

data class RouteStopsInfo(val routeName: String? = null,
                          val numberOfStops: Int? = null)

@ApiModel(description = "Represents a list of route names that connect two stops.")
data class TripRoutes(val routeNames: List<String>? = emptyList()) {
    companion object {
        fun empty() = TripRoutes()
    }
}

/**
 * Represents a node in the routes' graph
 */
data class StopNode(val id: String,
                    val name: String,
                    val previousRoutes: List<String>,
                    val previousStops: List<Stop>) {
    fun toStop() = Stop(id, StopAttributes(name))
    fun buildRoutesToGetHere(mostRecentRoute: String): List<String> =
            if (previousRoutes.isNotEmpty() && mostRecentRoute == previousRoutes.last())
                previousRoutes
            else
                previousRoutes + mostRecentRoute
}