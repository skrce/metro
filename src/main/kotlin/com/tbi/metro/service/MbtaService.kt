package com.tbi.metro.service

import com.tbi.metro.client.http.MbtaClient
import com.tbi.metro.service.model.Route
import com.tbi.metro.service.model.RouteStopsInfo
import com.tbi.metro.service.model.Routes
import com.tbi.metro.service.model.RoutesLongNames
import com.tbi.metro.service.model.Stop
import com.tbi.metro.service.model.StopInfo
import com.tbi.metro.service.model.StopInfos
import com.tbi.metro.service.model.StopNode
import com.tbi.metro.service.model.Stops
import com.tbi.metro.service.model.TripRoutes
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class MbtaService(private val mbtaClient: MbtaClient) : MetroService {

    companion object {
        const val METRO_NAME = "MBTA"
        private val LOGGER = LoggerFactory.getLogger(MbtaService::class.java)
    }

    /**
     * Returns unsorted list of all Routes' LongNames for the specified attributeType.
     * Uses http-client cached method [getAllRoutes] (the Routes configuration is infrequently modified).
     */
    override fun getRoutesLongNamesByAttributeType(attributeType: String): RoutesLongNames {
        val allRoutes: List<Route> = getAllRoutes().data
        return getRoutesLongNamesByAttributeType(attributeType, allRoutes)
    }

    /**
     * This is the business logic of [getRoutesLongNamesByAttributeType]
     */
    internal fun getRoutesLongNamesByAttributeType(attributeType: String, allRoutes: List<Route>)
            : RoutesLongNames =
            RoutesLongNames(
                    allRoutes
                            .filter { it.attributes.type == attributeType }
                            .map { it.attributes.long_name!! }
            )

    /**
     * Returns the route with most stops, along with the number of stops
     */
    override fun getRouteWithMostStops(): RouteStopsInfo {
        val allRoutesToStops = getAllRoutesWithStops()
        return getRouteWithMostStops(allRoutesToStops)
    }

    /**
     * This is the business logic of [getRouteWithMostStops]
     */
    internal fun getRouteWithMostStops(allRoutesToStops: List<Pair<String, Stops>>): RouteStopsInfo {
        val routeWithMostStops: Pair<String, Stops> = allRoutesToStops.sortedWith(compareBy { it.second.data?.size }).reversed().first()
        return RouteStopsInfo(routeWithMostStops.first, routeWithMostStops.second.data!!.size)
    }

    /**
     * Returns the route with least stops, along with the number of stops.
     * Routes with no stops are not being considered.
     */
    override fun getRouteWithLeastStops(): RouteStopsInfo {
        val allRoutesToStops = getAllRoutesWithStops()
        return getRouteWithLeastStops(allRoutesToStops)
    }

    /**
     * This is the business logic of [getRouteWithLeastStops]
     */
    internal fun getRouteWithLeastStops(allRoutesToStops: List<Pair<String, Stops>>): RouteStopsInfo {
        val routeWithLeastStops: Pair<String, Stops> = allRoutesToStops
                // ignore the routes with no stops
                .filter {
                    val routeHasStops = it.second.data?.isNotEmpty() ?: false
                    LOGGER.warn("Found route with no stops {}", it.first)
                    routeHasStops
                }
                .sortedWith(compareBy { it.second.data?.size }).first()
        return RouteStopsInfo(routeWithLeastStops.first, routeWithLeastStops.second.data!!.size)
    }

    /**
     * Returns the stops that connect 2+ routes
     */
    override fun getStopsWithMultipleRoutes(): StopInfos {
        val allRoutesWithStops = getAllRoutesWithStops()
        return getStopsWithMultipleRoutes(allRoutesWithStops)
    }

    /**
     * This is the business logic of [getStopsWithMultipleRoutes]
     */
    internal fun getStopsWithMultipleRoutes(allRoutesWithStops: List<Pair<String, Stops>>): StopInfos {
        return StopInfos(buildStopsToRoutes(allRoutesWithStops)
                                 .toList()
                                 // keep stops with 2+ routes
                                 .filter { it.second.size >= 2 }
                                 .map {
                                     StopInfo(
                                             stopName = it.first.attributes!!.name,
                                             routeNames = it.second.map { route -> route.second })
                                 }
        )
    }

    /**
     * Returns a list of all routes along with the list of stops the are consisted of.
     */
    private fun getAllRoutesWithStops(): List<Pair<String, Stops>> {
        val allRoutes = getAllRoutes().data.mapNotNull { it.id }.distinct()
        check(allRoutes.isNotEmpty()) { "No MBTA routes found." }

        val routesToStops =
                allRoutes.map {
                    Pair(it, getStopsByRoute(it))
                }
        check(routesToStops.isNotEmpty()) { "No MBTA stops found." }
        return routesToStops
    }

    /**
     * For a give begin- and end-stop, returns a list of routes that connects them with shortest number of stops.
     * If no connection exists - returns empty list.
     * 1. Start from the begin-stop
     * 2. Find all adjacent stops
     *   a) if any adjacent-stop is the end-stop, add the new most recent route
     *   b) otherwise - record the route for each of them, and repeat step a) until
     *      end-stop is found, or all stops are visited.
     *
     * This is a variation of the Dijkstra algorithm. The difference is that the full routes/stops graph is not being traversed all at once,
     *  but each connection is being evaluated per request. The reason is that the "cost" (number-of-stops, number-of-hops, cost-of-ticket, traffic-status etc.)
     *  of each "edge" (Stop-to-Stop connection) is not defined, and it's considered as same for all.
     */
    override fun searchTripRoutes(beginStopName: String,
                                  endStopName: String): TripRoutes {
        val allRoutesWithStops = getAllRoutesWithStops()
        return searchTripRoutes(beginStopName, endStopName, allRoutesWithStops)
    }

    /**
     * This is the business logic of [searchTripRoutes]
     */
    fun searchTripRoutes(beginStopName: String,
                         endStopName: String,
                         allRoutesWithStops: List<Pair<String, Stops>>)
            : TripRoutes {
        val allStopsToRoutes =
                buildStopsToRoutes(allRoutesWithStops)
                        .mapValues { it.value.map { route -> route.second } }
        val stopNameToStopId: Map<String, String> = allStopsToRoutes.keys.associateBy({ it.attributes!!.name!! }, { it.id!! })

        val beginStopId = stopNameToStopId[beginStopName] ?: error("Begin stop not found")
        val endStopId = stopNameToStopId[endStopName] ?: error("End stop not found")

        // initialize the traverse
        var currentStopNodes = listOf(StopNode(beginStopId, beginStopName, emptyList(), emptyList()))
        val visitedStops = currentStopNodes.map { it.toStop() }.toMutableList()

        // traverse until end-stop is found or all stops are visited
        while (visitedStops.size < allStopsToRoutes.size) {
            val nextAdjacentStops = mutableListOf<StopNode>()
            for (currentStopNode in currentStopNodes) {
                val adjacentStops = buildAdjacentStops(currentStopNode, allStopsToRoutes, visitedStops, allRoutesWithStops)
                // if multiple trips reach the destination stop, pick only one
                adjacentStops.firstOrNull { adjacentStop -> adjacentStop.id == endStopId }
                        ?.let {
                            // end-stop found
                            return TripRoutes(it.previousRoutes)
                        }
                // end-stop not reached
                visitedStops.addAll(adjacentStops.map { it.toStop() })
                nextAdjacentStops.addAll(adjacentStops)
            }
            // continue traversing from the adjacent stops
            currentStopNodes = nextAdjacentStops
        }

        // No connection found
        return TripRoutes.empty()
    }

    /**
     * Given list of routes with their stops, builds a map of each stop as key, and list of routes that have that stop as value.
     */
    private fun buildStopsToRoutes(allRoutesWithStops: List<Pair<String, Stops>>):
            Map<Stop, List<Pair<Stop, String>>> {
        return allRoutesWithStops
                .asSequence()
                // expand/flatten the stops per route
                .map { it.second.data!!.map { stop -> Pair(stop, it.first) } }
                .flatten()
                // group the stops with list of routes in a map
                .groupBy { it.first }
    }

    /**
     * Finds all adjacent stops for given stop, excluding the already visited stops
     * For each route that has the [currentStopNode], finds the previous and next stop if available.
     */
    private fun buildAdjacentStops(currentStopNode: StopNode,
                                   allStopsToRoutes: Map<Stop, List<String>>,
                                   visitedStops: List<Stop>,
                                   allRoutesWithStops: List<Pair<String, Stops>>)
            : List<StopNode> {

        val adjacentStops = mutableListOf<StopNode>()
        val routesHavingCurrentStopToRouteStops: Map<String, List<Stop>> = allRoutesWithStops
                .filter { it.first in allStopsToRoutes.getValue(currentStopNode.toStop()) }
                .associateBy { it.first }
                .mapValues { it.value.second.data!! }

        routesHavingCurrentStopToRouteStops.forEach {
            val route = it.key
            val stops = it.value
            val currentStopIndex = stops.indexOf(currentStopNode.toStop())

            if (currentStopIndex > 0) {
                val previousStop = stops[currentStopIndex - 1]
                if (previousStop !in visitedStops) {
                    adjacentStops.add(
                            StopNode(previousStop.id!!,
                                     previousStop.attributes?.name!!,
                                     (currentStopNode.buildRoutesToGetHere(route)),
                                     (currentStopNode.previousStops + previousStop)
                            )
                    )
                }
            }
            if (currentStopIndex < stops.lastIndex) {
                val nextStop = stops[currentStopIndex + 1]
                if (nextStop !in visitedStops) {
                    adjacentStops.add(
                            StopNode(nextStop.id!!,
                                     nextStop.attributes?.name!!,
                                     (currentStopNode.buildRoutesToGetHere(route)),
                                     (currentStopNode.previousStops + nextStop)
                            )
                    )
                }
            }
        }

        return adjacentStops
    }

    private fun getAllRoutes(): Routes = mbtaClient.getAllRoutes()

    private fun getStopsByRoute(route: String) = mbtaClient.getStopsByRoute(route)
}