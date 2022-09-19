package org.commcare.dalvik.abha.viewmodel

import android.util.Log
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import org.commcare.dalvik.abha.model.AbhaNumberRequestModel
import org.commcare.dalvik.abha.utility.AppConstants
import org.commcare.dalvik.abha.utility.PropMutableLiveData
import org.commcare.dalvik.data.services.HqServices
import org.commcare.dalvik.data.util.PrefKeys
import org.commcare.dalvik.domain.model.HqResponseModel
import org.commcare.dalvik.domain.model.LanguageManager
import org.commcare.dalvik.domain.usecases.GetTranslationUseCase
import org.commcare.dalvik.domain.usecases.RequestAadhaarOtpUsecase
import org.commcare.dalvik.domain.usecases.RequestMobileOtpUseCase
import org.commcare.dalvik.domain.usecases.SaveDataUsecase
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class GenerateAbhaViewModel @Inject constructor(
    val reqAadhaarOtpUsecase: RequestAadhaarOtpUsecase,
    val reqMobileOtpUseCase: RequestMobileOtpUseCase,
    val saveDataUsecase: SaveDataUsecase,
    val translationUseCase: GetTranslationUseCase
) : BaseViewModel() {

    private val TAG = "GenerateAbhaViewModel"

    var otpFailureCount = MutableLiveData(0)
    var abhaRequestModel: PropMutableLiveData<AbhaNumberRequestModel> = PropMutableLiveData()

    val uiState = MutableStateFlow<GenerateAbhaUiState>(GenerateAbhaUiState.InvalidState)

    fun init(mobileNumber: String) {
        abhaRequestModel.setValue(AbhaNumberRequestModel(mobileNumber))
        //TODO : Remove this for testing only
        abhaRequestModel.value?.aadhaar = "232755042430"
    }

    fun checkIfBlocked() = saveDataUsecase.executeFetch(PrefKeys.OTP_BLOCKED_TS.getKey())

    fun validateData() {
        viewModelScope.launch {
            var isMobileNumberValid = false
            abhaRequestModel.value?.mobileNumber?.apply {
                if (this.isNotEmpty() && this.length == AppConstants.MOBILE_NUMBER_LENGTH) {
                    val firstChar = this.first().toString().toInt()
                    if (firstChar in IntRange(6, 9)) {
                        isMobileNumberValid = true
                    }
                }
            }

            var isAadhaarValid = false
            abhaRequestModel.value?.aadhaar?.apply {
                if (this.isNotEmpty() && this.length == AppConstants.AADHAR_NUMBER_LENGTH) {
                    isAadhaarValid = true
                }
            }

            uiState.emit(if (isMobileNumberValid && isAadhaarValid) GenerateAbhaUiState.ValidState else GenerateAbhaUiState.InvalidState)
        }
    }

    fun requestOtp() {
        viewModelScope.launch {
//            val mobileOtpResponse = async {
//                val mobileOtpFlow =
//                    reqMobileOtpUseCase.execute(abhaRequestModel.value!!.mobileNumber)
//                mobileOtpFlow.collect {
//                    when (it) {
//                        is HqResponseModel.Success<String> -> {
//                            uiState.emit(
//                                GenerateAbhaUiState.Success(
//                                    it.data,
//                                    RequestType.MOBILE_OTP_RESEND
//                                )
//                            )
//                        }
//
//                        is HqResponseModel.Error<String> -> {
//                            uiState.emit(
//                                GenerateAbhaUiState.Error(
//                                    it.error,
//                                    RequestType.MOBILE_OTP_RESEND
//                                )
//                            )
//                        }
//
//
//                        is HqResponseModel.Loading -> {
//                            uiState.emit(GenerateAbhaUiState.Loading(true))
//                        }
//                    }
//                }
//            }

            val aadhaarOtpResponse = async {
                val aadharOtpFlow = reqAadhaarOtpUsecase.execute(abhaRequestModel.value!!.aadhaar)
                aadharOtpFlow.collect {
                    when (it) {
                        is HqResponseModel.Loading -> {
                            uiState.emit(GenerateAbhaUiState.Loading(true))
                        }
                        is HqResponseModel.Success -> {
                            uiState.emit(
                                GenerateAbhaUiState.Success(
                                    it.data,
                                    RequestType.AADHAAR_OTP_RESEND
                                )
                            )
                        }
                        is HqResponseModel.Error -> {
                            uiState.emit(
                                GenerateAbhaUiState.Error(
                                    it.error,
                                    RequestType.AADHAAR_OTP_RESEND
                                )
                            )
                        }
                    }
                }
            }

//            mobileOtpResponse.await()
            aadhaarOtpResponse.await()


        }
    }

    fun getData(key: Preferences.Key<String>) {
        viewModelScope.launch {
            saveDataUsecase.executeFetch(PrefKeys.OTP_BLOCKED_TS.getKey()).collect {
                Log.d(TAG, "OTP TS : ${it}")
            }
        }
    }

    /**
     * Save data in data store
     */
    private fun saveData(key: Preferences.Key<String>, value: String) {
        saveDataUsecase.executeSave(value, key)
    }

    /**
     * Increase count by 1
     */
    private fun incOtpFailureCount() {
        otpFailureCount.value = otpFailureCount.value!!.inc()
    }

    /**
     * Resend Mobile OTP request
     */
    fun resendMobileOtpRequest() {
        viewModelScope.launch(Dispatchers.Main) {
            uiState.emit(GenerateAbhaUiState.MobileOtpRequested)
            uiState.emit(GenerateAbhaUiState.MobileOtpRequested)
            uiState.emit(GenerateAbhaUiState.Loading(true))

            reqMobileOtpUseCase.execute(abhaRequestModel.value?.mobileNumber ?: "12312321")
                .collect {

                    when (it) {
                        is HqResponseModel.Loading -> {
//                        uiState.emit(GenerateAbhaUiState.Loading(true))
                        }
                        is HqResponseModel.Success -> {
                            uiState.emit(
                                GenerateAbhaUiState.Success(
                                    "",
                                    RequestType.MOBILE_OTP_RESEND
                                )
                            )
                        }
                        is HqResponseModel.Error -> {
                            incOtpFailureCount()
                            uiState.emit(
                                GenerateAbhaUiState.Error(
                                    "",
                                    RequestType.MOBILE_OTP_RESEND
                                )
                            )
                        }
                    }
                }

//            saveData(PrefKeys.OTP_BLOCKED_TS.getKey(), System.currentTimeMillis().toString())

        }
    }

    /**
     * Verify Mobile OTP
     */
    fun verifyMobileOtp() {

    }

    /**
     * Resend Aadhaar OTP request
     */
    fun resendAadhaarOtpRequest() {
        viewModelScope.launch(Dispatchers.Main) {
            uiState.emit(GenerateAbhaUiState.AadhaarOtpRequested)

            uiState.emit(GenerateAbhaUiState.Loading(true))
            delay(2000)
            uiState.emit(GenerateAbhaUiState.Error("", RequestType.AADHAAR_OTP_RESEND))


            reqAadhaarOtpUsecase.execute(abhaRequestModel.value!!.aadhaar).collect {

                when (it) {
                    is HqResponseModel.Loading -> {
                        uiState.emit(GenerateAbhaUiState.Loading(true))
                    }
                    is HqResponseModel.Success -> {
                        uiState.emit(
                            GenerateAbhaUiState.Success(
                                "",
                                RequestType.AADHAAR_OTP_RESEND
                            )
                        )
                    }
                    is HqResponseModel.Error -> {
                        incOtpFailureCount()
                        uiState.emit(GenerateAbhaUiState.Error("", RequestType.AADHAAR_OTP_RESEND))
                    }
                }
            }

//            saveData(PrefKeys.OTP_BLOCKED_TS.getKey(), System.currentTimeMillis().toString())

        }
    }

    fun clearBlockState() {
        saveDataUsecase.removeKey(PrefKeys.OTP_BLOCKED_TS.getKey())
    }

    /**
     * Fetch Translations
     */
    fun getTranslation(langCode: String) {
        viewModelScope.launch(Dispatchers.IO) {
            translationUseCase.execute(langCode)?.let{
                LanguageManager.translationModel = it
                uiState.emit(GenerateAbhaUiState.TranslationReceived)
            } ?: Timber.d("XXXXX ")
        }
    }
}

/**
 * UI State
 */
sealed class GenerateAbhaUiState {
    data class Loading(val isLoading: Boolean) : GenerateAbhaUiState()
    object TranslationReceived : GenerateAbhaUiState()
    object ValidState : GenerateAbhaUiState()
    object InvalidState : GenerateAbhaUiState()
    object MobileAadhaarOtpGenerated : GenerateAbhaUiState()
    object MobileOtpRequested : GenerateAbhaUiState()
    object AadhaarOtpRequested : GenerateAbhaUiState()
    object MobileOtpVerified : GenerateAbhaUiState()
    object AadhaarOtpVerified : GenerateAbhaUiState()
    object Blocked : GenerateAbhaUiState()
    data class Success(val errorMsg: String, val requestType: RequestType) : GenerateAbhaUiState()
    data class Error(val errorMsg: String, val requestType: RequestType) : GenerateAbhaUiState()
}

/**
 * Request type sent
 */
enum class RequestType {
    MOBILE_OTP_RESEND,
    MOBILE_OTP_VERIFY,
    AADHAAR_OTP_RESEND,
    AADHAAR_OTP_VERIFY
}