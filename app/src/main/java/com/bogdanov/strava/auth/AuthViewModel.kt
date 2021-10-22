package com.bogdanov.strava.auth

import android.app.Application
import android.content.Intent
import androidx.lifecycle.*
import com.bogdanov.strava.R
import com.bogdanov.strava.utils.SingleLiveEvent
import kotlinx.coroutines.launch
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationService
import net.openid.appauth.TokenRequest

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val authorizationService: AuthorizationService,
) : ViewModel(){
    private val openAuthPageLiveEvent = SingleLiveEvent<Intent>()
    private val authSuccessLiveEvent = SingleLiveEvent<Unit>()
    private val loadingMutableLiveData = MutableLiveData(false)
    private val toastLiveEvent = SingleLiveEvent<Int>()

    val openAuthPageLiveData: LiveData<Intent>
        get() = openAuthPageLiveEvent

    val loadingLiveData: LiveData<Boolean>
        get() = loadingMutableLiveData

    val authSuccessLiveData: LiveData<Unit>
        get() = authSuccessLiveEvent

    val toastLiveData: LiveData<Int>
        get() = toastLiveEvent

    fun onAuthCodeFailed(exception: AuthorizationException) {
        toastLiveEvent.postValue(R.string.auth_canceled)
    }

    fun onAuthCodeReceived(tokenRequest: TokenRequest) {

        authRepository.performTokenRequest(
            authService = authorizationService,
            tokenRequest = tokenRequest,
            onComplete = {
                loadingMutableLiveData.postValue(false)
                authSuccessLiveEvent.postValue(Unit) },
             onError = {
                loadingMutableLiveData.postValue(false)
                toastLiveEvent.postValue(R.string.auth_canceled)
            }
        )

    }

    fun openLoginPage() {
        val openAuthPageIntent = authorizationService.getAuthorizationRequestIntent(
            authRepository.getAuthRequest()
        )

        openAuthPageLiveEvent.postValue(openAuthPageIntent)
    }

    override fun onCleared() {
        super.onCleared()
        authorizationService.dispose()
    }
}