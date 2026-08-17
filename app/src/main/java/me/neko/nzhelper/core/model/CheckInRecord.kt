package me.neko.nzhelper.core.model

import androidx.compose.runtime.Immutable

@Immutable
data class CheckInRecord(
    val sourceKey: String,
    val date: String,
    val time: String,
    val type: String,
    val sideDishes: String,
    val feeling: String
)
