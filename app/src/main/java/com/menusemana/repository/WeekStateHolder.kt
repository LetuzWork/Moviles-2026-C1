package com.menusemana.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeekStateHolder
    @Inject
    constructor() {
        val currentWeekMonday: LocalDate =
            LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

        private val _weekOffset = MutableStateFlow(0)
        val weekOffset: StateFlow<Int> = _weekOffset.asStateFlow()

        fun weekStartAt(offset: Int): LocalDate = currentWeekMonday.plusWeeks(offset.toLong())

        fun nextWeek() = _weekOffset.update { it + 1 }

        fun previousWeek() = _weekOffset.update { it - 1 }

        fun goToCurrentWeek() {
            _weekOffset.value = 0
        }
    }
