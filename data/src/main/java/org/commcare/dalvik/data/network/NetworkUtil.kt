package org.commcare.dalvik.data.network

import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.commcare.dalvik.domain.model.AbdmErrorModel
import org.commcare.dalvik.domain.model.HqResponseModel
import org.commcare.dalvik.domain.model.OtpResponseModel
import retrofit2.Response

class NetworkUtil {
    companion object {
        const val BASE_URL = "https://ccind.duckdns.org/abdm/api/"//""https://staging.commcarehq.org/abdm/api/"
        const val TRANSLATION_BASE_URL = "https://raw.githubusercontent.com/"
        fun getTranslationEndpoint(code:String)=
            "https://raw.githubusercontent.com/dimagi/abdm-app/main/resources/languages/${code}/language.json"
    }
}

fun <T> safeApiCall(call: suspend () -> Response<T>) = flow {
    this.emit(HqResponseModel.Loading)
    try {
        val response = call.invoke()
        response.let {
            var responseJsonObject:JsonObject
            when(response.code()){
                200 ->{
                    it.body().toString().let {
                        responseJsonObject = Gson().fromJson(it, JsonObject::class.java)
                        if(responseJsonObject.has("code")){
                            val adbmError:AbdmErrorModel =  Gson().fromJson(it, AbdmErrorModel::class.java)
                            emit(HqResponseModel.AbdmError(500 , adbmError))
                        }else {
                            emit(HqResponseModel.Success(responseJsonObject))
                        }
                    }

                }
                422->{
                    it.errorBody()?.string()?.let{
                        val adbmError:AbdmErrorModel =  Gson().fromJson(it, AbdmErrorModel::class.java)
                        emit(HqResponseModel.AbdmError(500 , adbmError))
                    }

                }
                else ->{

                }
            }

        }

    } catch (t: Throwable) {
        val errJson = JsonObject()
        errJson.addProperty("msg",t.message)
        emit(HqResponseModel.Error(333,errJson))
    }
}


