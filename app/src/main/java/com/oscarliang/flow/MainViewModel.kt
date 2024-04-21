package com.oscarliang.flow

import androidx.lifecycle.ViewModel
import com.oscarliang.flow.repository.UserRepository

class MainViewModel(
    private val repository: UserRepository
) : ViewModel() {

    val darkModeLiveData = repository.darkModeLiveData

}