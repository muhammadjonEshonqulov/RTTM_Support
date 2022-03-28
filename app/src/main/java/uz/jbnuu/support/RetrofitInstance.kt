package uz.jbnuu.support

import android.content.Context
import com.readystatesoftware.chuck.ChuckInterceptor
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import uz.jbnuu.support.utils.Constants.Companion.BASE_URL_FIREBASE

class RetrofitInstance {
    companion object {
        private fun retrofit(context: Context):Retrofit {
            return  Retrofit.Builder()
                .baseUrl(BASE_URL_FIREBASE)
                .client(OkHttpClient().newBuilder()
                    .addNetworkInterceptor(HttpLoggingInterceptor())
                    .addInterceptor(ChuckInterceptor(context))
                    .build())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }

        internal fun api(context: Context) : NotificationApi {
            return retrofit(context).create(NotificationApi::class.java)
        }
    }
}