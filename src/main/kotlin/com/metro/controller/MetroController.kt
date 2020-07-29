package com.metro.controller

import com.metro.service.MetroOperatorFactory
import com.metro.service.model.RouteStopsInfo
import com.metro.service.model.RoutesLongNames
import com.metro.service.model.StopInfos
import com.metro.service.model.TripRoutes
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import springfox.documentation.swagger2.annotations.EnableSwagger2

@RestController
@EnableSwagger2
class MetroController(private val metroOperatorFactory: MetroOperatorFactory) {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(MetroController::class.java)
    }

    private fun metroService(metroName: String) = metroOperatorFactory.getMetroService(metroName)

    @GetMapping(value = ["/{metroName}/routes/long-names/attribute-type/{attributeType}"],
                produces = [MediaType.APPLICATION_JSON_VALUE])
    @ApiOperation(value = "Returns unsorted list of all Routes' LongNames for the specified attributeType.")
    fun getRoutesLongNamesByAttributeType(@ApiParam(value = "The name of the metro operator",
                                                    allowableValues = "MBTA")
                                          @PathVariable("metroName") metroName: String,
                                          @ApiParam(value = "The type of the Route. 0 = Light Rail; 1 = Heavy Rail, 2 = Commuter Rail, 3 = Bus; 4 = Ferry",
                                                    allowableValues = "0,1,2,3,4")
                                          @PathVariable("attributeType") attributeType: String): RoutesLongNames {
        LOGGER.debug("Called getRoutesLongNamesByAttributeType() for metroName {}, attributeType {}", metroName, attributeType)
        return metroService(metroName).getRoutesLongNamesByAttributeType(attributeType)
    }

    @GetMapping(value = ["/{metroName}/routes/most-stops-route"],
                produces = [MediaType.APPLICATION_JSON_VALUE])
    @ApiOperation(value = "Returns the route name with most stops.",
                  notes = "Other routes with same number of stops may exist.")
    fun getRouteWithMostStops(@ApiParam(value = "The name of the metro operator",
                                        allowableValues = "MBTA")
                              @PathVariable("metroName") metroName: String): RouteStopsInfo {
        LOGGER.debug("Called getRouteWithMostStops() for metroName {}", metroName)
        return metroService(metroName).getRouteWithMostStops()
    }

    @GetMapping(value = ["/{metroName}/routes/least-stops-route"],
                produces = [MediaType.APPLICATION_JSON_VALUE])
    @ApiOperation(value = "Returns the route name with least stops.",
                  notes = "Other routes with same number of stops may exist. " +
                          "Routes with no stops are not being considered.")
    fun getRouteWithLeastStops(@ApiParam(value = "The name of the metro operator",
                                         allowableValues = "MBTA")
                               @PathVariable("metroName") metroName: String): RouteStopsInfo {
        LOGGER.debug("Called getRouteWithLeastStops() for metroName {}", metroName)
        return metroService(metroName).getRouteWithLeastStops()
    }

    @GetMapping(value = ["/{metroName}/stops/multiple-routes-stops"],
                produces = [MediaType.APPLICATION_JSON_VALUE])
    @ApiOperation(value = "Returns the stops that connect two or more routes, along with the route names.")
    fun getStopsWithMultipleRoutes(@ApiParam(value = "The name of the metro operator",
                                             allowableValues = "MBTA")
                                   @PathVariable("metroName") metroName: String): StopInfos {
        LOGGER.debug("Called getStopsWithMultipleRoutes() for metroName {}", metroName)
        return metroService(metroName).getStopsWithMultipleRoutes()
    }

    @GetMapping(value = ["/{metroName}/trip/search-routes"],
                produces = [MediaType.APPLICATION_JSON_VALUE])
    @ApiOperation(value = "Returns a list of route names for a given begin-stop and end-stop, " +
            "representing the routes that connect them with shortest number of stops.")
    fun searchTripRoutes(@ApiParam(value = "The name of the metro operator",
                                   allowableValues = "MBTA")
                         @PathVariable("metroName") metroName: String,
                         @ApiParam(value = "The stop name of the begin station. Example: Oak Grove, Assembly, Downtown Crossing, Davis")
                         @RequestParam("beginStopName") beginStopName: String,
                         @ApiParam(value = "The stop name of the destination station. Example: Oak Grove, Assembly, Downtown Crossing, Davis")
                         @RequestParam("endStopName") endStopName: String): TripRoutes {
        LOGGER.debug("Called searchTripRoutes() for metroName {}, beginStopId {}, endStopId {}", metroName, beginStopName, endStopName)
        return metroService(metroName).searchTripRoutes(beginStopName, endStopName)
    }
}