package com.varunkumar.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.varunkumar.myapplication.data.DatabaseHelper

class ViewModelFactory(private val dbHelper: DatabaseHelper) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(KeystrokeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return KeystrokeViewModel(dbHelper) as T
        }
        if (modelClass.isAssignableFrom(TouchViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TouchViewModel(dbHelper) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
