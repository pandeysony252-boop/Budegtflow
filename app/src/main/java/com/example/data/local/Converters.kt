package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.TransactionType

class Converters {
    @TypeConverter
    fun fromTransactionType(type: TransactionType): String = type.name

    @TypeConverter
    fun toTransactionType(value: String): TransactionType =
        TransactionType.entries.firstOrNull { it.name == value } ?: TransactionType.EXPENSE
}
