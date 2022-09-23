package org.commcare.dalvik.abha.ui.main.fragment

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.gson.Gson
import kotlinx.coroutines.launch
import org.commcare.dalvik.abha.R
import org.commcare.dalvik.abha.databinding.SelectAuthMethodBinding
import org.commcare.dalvik.abha.viewmodel.GenerateAbhaUiState
import org.commcare.dalvik.abha.viewmodel.GenerateAbhaViewModel
import org.commcare.dalvik.abha.viewmodel.RequestType
import org.commcare.dalvik.domain.model.OtpResponseModel

class SelectAuthenticationFragment :
    BaseFragment<SelectAuthMethodBinding>(SelectAuthMethodBinding::inflate),
    AdapterView.OnItemClickListener {

    private val viewModel: GenerateAbhaViewModel by activityViewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.clickHandler = this
        observeUiState()
        fetchAuthMehtods()
    }

    private fun fetchAuthMehtods() {
        arguments?.getString("abha_id")?.let {
            viewModel.getAuthenticationMethods(it)
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    when (it) {
                        is GenerateAbhaUiState.Success -> {
                            when (it.requestType) {
                                RequestType.GENERATE_AUTH_OTP -> {
                                    val otResponseModel =
                                        Gson().fromJson(it.data, OtpResponseModel::class.java)
                                    viewModel.abhaRequestModel.value?.txnId = otResponseModel.txnId
                                    navigateToNextScreen()
                                }
                                RequestType.AUTH_METHODS -> {
                                    val filter = listOf("AADHAAR_OTP", "MOBILE_OTP")
                                    val authList = mutableListOf<String>()
                                    it.data.getAsJsonArray("auth_methods")
                                        .forEach {
                                            if (filter.contains(it.asString)) {
                                                authList.add(it.asString)
                                            }
                                        }
                                    val adapter =
                                        ArrayAdapter(
                                            requireContext(),
                                            R.layout.dropdown_item,
                                            authList
                                        )
                                    (binding.authSelection as? MaterialAutoCompleteTextView)?.apply {
                                        setAdapter(adapter)
                                        setOnItemClickListener(this@SelectAuthenticationFragment)
                                    }
                                }
                            }

                            viewModel.uiState.emit(GenerateAbhaUiState.Loading(false))
                        }

                        is GenerateAbhaUiState.Error -> {
                            viewModel.uiState.emit(GenerateAbhaUiState.Loading(false))
                        }

                        is GenerateAbhaUiState.AbdmError -> {
                            viewModel.uiState.emit(GenerateAbhaUiState.Loading(false))
                        }

                    }
                }
            }

        }
    }


    override fun onClick(view: View?) {
        super.onClick(view)
        arguments?.getString("abha_id")?.let { healthId ->
            viewModel.selectedAuthMethod?.let {
                viewModel.getAuthOtp(healthId, it)
            }
        }
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        parent?.getItemAtPosition(position).toString().let {
            viewModel.selectedAuthMethod = it
            binding.startVerfication.isEnabled = true
        }
    }

    private fun navigateToNextScreen() {
        findNavController().navigate(
            R.id.action_selectAuthenticationFragment_to_verifyMobileOtpFragment
        )
    }
}