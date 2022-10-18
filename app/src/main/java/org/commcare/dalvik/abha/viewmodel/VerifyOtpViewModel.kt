package org.commcare.dalvik.abha.viewmodel

import dagger.hilt.android.lifecycle.HiltViewModel
import org.commcare.dalvik.domain.usecases.VerifyAadhaarOtpUseCase
import javax.inject.Inject

@HiltViewModel
class VerifyOtpViewModel @Inject constructor(verifyAadhaarOtpUseCase: VerifyAadhaarOtpUseCase) :
    BaseViewModel()