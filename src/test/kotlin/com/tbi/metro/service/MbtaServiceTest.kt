package com.tbi.metro.service

import com.nhaarman.mockito_kotlin.mock
import com.tbi.metro.service.model.Route
import com.tbi.metro.service.model.RouteAttributes
import com.tbi.metro.service.model.Stop
import com.tbi.metro.service.model.StopAttributes
import com.tbi.metro.service.model.Stops
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Testing the business logic of [MbtaService]
 * Contains only basic test cases.
 */
internal class MbtaServiceTest {

    private val mbtaService = MbtaService(mock())

    @Test
    fun testGetRoutesLongNamesByAttributeType() {

        // GIVEN
        val allRoutes = listOf(
                Route(attributes = RouteAttributes(long_name = "Orange", type = "0"),
                      id = "Or"),
                Route(attributes = RouteAttributes(long_name = "Red", type = "0"),
                      id = "Rd"),
                Route(attributes = RouteAttributes(long_name = "Purple", type = "1"),
                      id = "Pr")
        )

        // WHEN
        val routesLongNamesType0 = mbtaService.getRoutesLongNamesByAttributeType("0", allRoutes)

        // THEN
        assertEquals(2, routesLongNamesType0.longNames.size, "Should have found 2 routes")
        assertTrue(routesLongNamesType0.longNames.map { it }.contains("Orange"), "Orange route not found")
        assertTrue(routesLongNamesType0.longNames.map { it }.contains("Red"), "Red route not found")

        // WHEN
        val routesLongNamesType1 = mbtaService.getRoutesLongNamesByAttributeType("1", allRoutes)

        // THEN
        assertEquals(1, routesLongNamesType1.longNames.size, "Should have found 1 route")
        assertTrue(routesLongNamesType1.longNames.map { it }.contains("Purple"), "Purple route not found")

        // WHEN
        val routesLongNamesType2 = mbtaService.getRoutesLongNamesByAttributeType("2", allRoutes)

        // THEN
        assertEquals(0, routesLongNamesType2.longNames.size, "Shouldn't have found any route")
    }

    @Test
    fun testGetStopsWithMultipleRoutes() {

        // GIVEN
        val allRoutesWithStops: List<Pair<String, Stops>> =
                listOf(
                        Pair("Orange", Stops(listOf(
                                Stop("route-oakgr", StopAttributes("Oak G")),
                                Stop("route-state", StopAttributes("State")),
                                Stop("route-downtown", StopAttributes("Downtown"))))
                        ),
                        Pair("Red", Stops(listOf(
                                Stop("route-downtown", StopAttributes("Downtown")),
                                Stop("route-state", StopAttributes("State")),
                                Stop("route-davis", StopAttributes("Davis"))))
                        ),
                        Pair("Short", Stops(
                                listOf(Stop("route-short", StopAttributes("short"))))
                        )
                )

        // WHEN
        val stopsWithMultipleRoutes = mbtaService.getStopsWithMultipleRoutes(allRoutesWithStops)

        // THEN
        assertEquals(2, stopsWithMultipleRoutes.stopInfos?.size, "Should have found 2 stops")
        assertTrue(stopsWithMultipleRoutes.stopInfos?.map { it.stopName }?.contains("route-state")
                           ?: false, "Stop State not found")
        assertTrue(stopsWithMultipleRoutes.stopInfos?.map { it.stopName }?.contains("route-downtown")
                           ?: false, "Stop Downtown not found")
    }

    @Test
    fun testGetRouteWithMostStops() {

        // GIVEN
        val allRoutesWithStops: List<Pair<String, Stops>> =
                listOf(
                        Pair("Orange", Stops(listOf(
                                Stop("route-oakgr", StopAttributes("Oak G")),
                                Stop("route-state", StopAttributes("State")),
                                Stop("route-downtown", StopAttributes("Downtown"))))
                        ),
                        Pair("Red", Stops(listOf(
                                Stop("route-downtown", StopAttributes("Downtown")),
                                Stop("route-davis", StopAttributes("Davis"))))
                        ),
                        Pair("Short", Stops(
                                listOf(Stop("route-short", StopAttributes("short"))))
                        )
                )

        // WHEN
        val routeWithMostStopsInfo = mbtaService.getRouteWithMostStops(allRoutesWithStops)

        // THEN
        assertEquals("Orange", routeWithMostStopsInfo.routeName, "Incorrect route")
        assertEquals(3, routeWithMostStopsInfo.numberOfStops, "Incorrect number of stops")
    }

    @Test
    fun testGetRouteWithLeastStops() {

        // GIVEN
        val allRoutesWithStops: List<Pair<String, Stops>> =
                listOf(
                        Pair("Orange", Stops(listOf(
                                Stop("route-oakgr", StopAttributes("Oak G")),
                                Stop("route-state", StopAttributes("State")),
                                Stop("route-downtown", StopAttributes("Downtown"))))
                        ),
                        Pair("Red", Stops(listOf(
                                Stop("route-downtown", StopAttributes("Downtown")),
                                Stop("route-davis", StopAttributes("Davis"))))
                        ),
                        Pair("Short", Stops(
                                listOf(Stop("route-short", StopAttributes("short"))))
                        )
                )

        // WHEN
        val routeWithMostStopsInfo = mbtaService.getRouteWithLeastStops(allRoutesWithStops)

        // THEN
        assertEquals("Short", routeWithMostStopsInfo.routeName, "Incorrect route")
        assertEquals(1, routeWithMostStopsInfo.numberOfStops, "Incorrect number of stops")
    }

    @Test
    fun testSearchTripRoutes() {

        // GIVEN
        val allRoutesWithStops: List<Pair<String, Stops>> =
                listOf(
                        Pair("Orange", Stops(listOf(
                                Stop("route-oakgr", StopAttributes("Oak Grove")),
                                Stop("route-malden", StopAttributes("Malden")),
                                Stop("route-assembly", StopAttributes("Assembly")),
                                Stop("route-state", StopAttributes("State"))))
                        ),
                        Pair("Red", Stops(listOf(
                                Stop("route-downtown", StopAttributes("Downtown")),
                                Stop("route-state", StopAttributes("State")),
                                Stop("route-davis", StopAttributes("Davis"))))
                        ),
                        Pair("Green", Stops(listOf(
                                Stop("route-davis", StopAttributes("Davis")),
                                Stop("route-copl ", StopAttributes("Copley"))))
                        )
                )

        // WHEN
        val searchTripRoutes1 = mbtaService.searchTripRoutes("Oak Grove", "Assembly", allRoutesWithStops)

        // THEN
        assertEquals(listOf("Orange"), searchTripRoutes1.routeNames, "Incorrect route")

        // WHEN
        val searchTripRoutes2 = mbtaService.searchTripRoutes("Oak Grove", "Davis", allRoutesWithStops)

        // THEN
        assertEquals(listOf("Orange", "Red"), searchTripRoutes2.routeNames, "Incorrect route")

        // WHEN
        val searchTripRoutes3 = mbtaService.searchTripRoutes("Oak Grove", "Copley", allRoutesWithStops)

        // THEN
        assertEquals(listOf("Orange", "Red", "Green"), searchTripRoutes3.routeNames, "Incorrect route")
    }
}