package org.commcare.dalvik.domain.model

import com.google.gson.Gson
import com.google.gson.JsonObject

object LanguageManager {

    const val DEFAULT_TRANSLATIONS = "{\n" +
            "  \"meta\": {\n" +
            "    \"code\": \"EN\",\n" +
            "    \"language\": \"English\"\n" +
            "  },\n" +
            "  \"app_data\": {\n" +
            "    \"PROCEED_CLOSE\": \"Do you want to exit?\",\n" +
            "    \"VERIFY\": \"Verify\",\n" +
            "    \"START_VERIFICATION\": \"Start Verification\",\n" +
            "    \"VERIFY_OTP\": \"Verify OTP\",\n" +
            "    \"RESEND_OTP\": \"Resend OTP\",\n" +
            "    \"GEN_OTP\": \"Generate OTP\",\n" +
            "    \"ENTER_ADHR_OTP\": \"Enter Aadhaar OTP\",\n" +
            "    \"ENTER_MOB_OTP\": \"Enter Mobile OTP\",\n" +
            "    \"BENF_MOB_NUM\": \"Beneficiary Mobile Number\",\n" +
            "    \"BENF_ADHR_NUM\": \"Beneficiary Aadhaar Number\",\n" +
            "    \"USE_ADHR_DATA_IN_COMMCARE\": \"Use Aadhaar data in Commcare\",\n" +
            "    \"ADHR_DATA\": \"Aadhaar Data\",\n" +
            "    \"ABHA_NUM\": \"ABHA Number\",\n" +
            "    \"RETURN\": \"Return\",\n" +
            "    \"STATUS\": \"Status\",\n" +
            "    \"VERIFICATION_STATUS\": \"Verification Status\",\n" +
            "    \"SEL_AUTH_METHOD\": \"Select auth method\",\n" +
            "    \"ABHA_VERIFICATION\": \"ABHA Verification\",\n" +
            "    \"ABHA_CREATION\": \"ABHA Creation\",\n" +
            "    \"BENF_ABHA_NUM\": \"Beneficiary ABHA Number\",\n" +
            "    \"NO_INTERNET\": \"No internet available\"\n" +
            "  },\n" +
            "  \"abdm_health_data\": {\n" +
            "    \"healthIdNumber\": \"Health ID Number\",\n" +
            "    \"name\": \"Name\",\n" +
            "    \"gender\": \"Gender\",\n" +
            "    \"yearOfBirth\": \"Year of Birth\",\n" +
            "    \"monthOfBirth\": \"Month of Birth\",\n" +
            "    \"dayOfBirth\": \"Day of Birth\",\n" +
            "    \"firstName\": \"First Name\",\n" +
            "    \"healthId\": \"Health ID\",\n" +
            "    \"lastName\": \"Last Name\",\n" +
            "    \"middleName\": \"Middle Name\",\n" +
            "    \"stateCode\": \"State Code\",\n" +
            "    \"districtCode\": \"District Code\",\n" +
            "    \"stateName\": \"State Name\",\n" +
            "    \"email\": \"Email Address\",\n" +
            "    \"kycPhoto\": \"KYC Photo\",\n" +
            "    \"profilePhoto\": \"Profile Photo\",\n" +
            "    \"mobile\": \"Mobile Number\",\n" +
            "    \"authMethods\": \"Authentication Methods\",\n" +
            "    \"pincode\": \"Pin Code\",\n" +
            "    \"AADHAAR_OTP\": \"Aadhaar Otp\",\n" +
            "    \"MOBILE_OTP\": \"Mobile Otp\"\n" +
            "  }\n" +
            "}"

    lateinit var translationModel: TranslationModel


    fun getDefaultTranslation(key: String): String {
        return Gson().fromJson(DEFAULT_TRANSLATIONS, JsonObject::class.java).run {
            this.getAsJsonObject("app_data")?.get(key)?.let {
                return it.asString
            } ?: this.getAsJsonObject("abdm_health_data")?.get(key)?.let {
                return it.asString
            } ?: key
        }
    }

    fun getTranslatedValue(key: TranslationKey) = translationModel.getTranslatedString(key.name)

    fun getTranslatedValue(key: String) = translationModel.getTranslatedString(key)


    fun init() {
        translationModel =
            Gson().fromJson(DEFAULT_TRANSLATIONS, TranslationModel::class.java)
    }
}

enum class TranslationKey {
    GEN_OTP,
    RESEND_OTP,
    VERIFY,
    BENF_MOB_NUM,
    BENF_ADHR_NUM,
    BENF_ABHA_NUM,
    ENTER_ADHR_OTP,
    ENTER_MOB_OTP,
    START_VERIFICATION,
    SEL_AUTH_METHOD,
    VERIFICATION_STATUS,
    STATUS,
    RETURN,
    ABHA_NUM,
    ADHR_DATA,
    USE_ADHR_DATA_IN_COMMCARE,
    ABHA_VERIFICATION,
    ABHA_CREATION,
    NO_INTERNET,
    TOKEN_MISSING,
    REQ_DATA_MISSING,
    AUTH_METHODS_NOT_RECEIVED,
    PROCEED_CLOSE,
    YES,
    USER_ABORTED
}


