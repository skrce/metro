package com.tbi.metro.service

import org.springframework.stereotype.Component

@Component
class MetroOperatorFactory(private val mbtaService: MbtaService) {

    fun getMetroService(metroName: String): MetroService =
            when (metroName) {
                MbtaService.METRO_NAME -> mbtaService
                else -> {
                    error("Metro operator not supported")
                }
            }
}