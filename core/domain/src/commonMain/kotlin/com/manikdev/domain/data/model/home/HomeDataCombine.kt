package com.manikdev.domain.data.model.home

import com.manikdev.domain.data.model.home.chart.Chart
import com.manikdev.domain.data.model.mood.Mood
import com.manikdev.domain.utils.Resource

data class HomeDataCombine(
    val home: Resource<Triple<String?, List<HomeItem>, String?>>,
    val mood: Resource<Mood>,
    val chart: Resource<Chart>,
    val newRelease: Resource<List<HomeItem>>,
)