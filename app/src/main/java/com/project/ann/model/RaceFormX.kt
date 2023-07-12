package com.project.ann.model

data class RaceFormX(
    val additional_data: String,
    val distance: Int,
    val distance_type: DistanceType,
    val distance_type_id: String,
    val generated: Int,
    val race_comment: String,
    val race_comment_alternative: String,
    val silk_base_url: String,
    val track_condition: TrackCondition,
    val track_condition_id: String,
    val weather: Weather,
    val weather_id: String
)