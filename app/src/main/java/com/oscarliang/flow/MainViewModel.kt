package com.oscarliang.flow

import androidx.lifecycle.ViewModel
import com.oscarliang.flow.repository.UserRepository

class MainViewModel(
    repository: UserRepository
) : ViewModel() {

    val darkModeLiveData = repository.darkModeLiveData

}