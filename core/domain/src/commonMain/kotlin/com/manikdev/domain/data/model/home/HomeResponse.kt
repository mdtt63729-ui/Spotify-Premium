package com.manikdev.domain.data.model.home

import com.manikdev.domain.data.model.home.chart.Chart
import com.manikdev.domain.data.model.mood.Mood
import com.manikdev.domain.utils.Resource

data class HomeResponse(
    val homeItem: Resource<ArrayList<HomeItem>>,
    val exploreMood: Resource<Mood>,
    val exploreChart: Resource<Chart>,
)