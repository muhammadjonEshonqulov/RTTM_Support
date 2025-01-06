package uz.rttm.support.di

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.google.auth.oauth2.GoogleCredentials
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import uz.rttm.support.BuildConfig
import uz.rttm.support.R
import uz.rttm.support.data.network.ApiService
import uz.rttm.support.data.network.NotificationApi
import uz.rttm.support.utils.Constants.Companion.BASE_URL
import uz.rttm.support.utils.Constants.Companion.BASE_URL_FIREBASE
import uz.rttm.support.utils.Prefs
import java.io.InputStream
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Singleton
    @Provides
    fun provideSharedPref(@ApplicationContext context: Context) = Prefs(context)

    @Singleton
    @Provides
    fun provideContext(@ApplicationContext context: Context) = context

    @Singleton
    @Provides
    @Named("standardHttpClient")
    fun provideHttpClient(
        prefs: Prefs,
        @ApplicationContext context: Context
    ): OkHttpClient {
        val builder = OkHttpClient().newBuilder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer " + prefs.get(prefs.token, ""))
                    .build()
                chain.proceed(request)
            }
            .connectTimeout(10000L, TimeUnit.MILLISECONDS)
            .readTimeout(10000L, TimeUnit.MILLISECONDS)
            .writeTimeout(10000L, TimeUnit.MILLISECONDS)

        if (BuildConfig.isDebug) {
            builder.addInterceptor(
                ChuckerInterceptor.Builder(context)
                    .collector(ChuckerCollector(context))
                    .build()
            )
        }

        return builder.build()
    }

    @Singleton
    @Provides
    @Named("notificationHttpClient")
    fun provideHttpClientNotification(
        prefs: Prefs,
        @ApplicationContext context: Context
    ): OkHttpClient {
        val builder = OkHttpClient().newBuilder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer " + getAccessToken(context))
                    .build()
                chain.proceed(request)
            }
            .connectTimeout(10000L, TimeUnit.MILLISECONDS)
            .readTimeout(10000L, TimeUnit.MILLISECONDS)
            .writeTimeout(10000L, TimeUnit.MILLISECONDS)

        if (BuildConfig.isDebug) {
            builder.addInterceptor(
                ChuckerInterceptor.Builder(context)
                    .collector(ChuckerCollector(context))
                    .build()
            )
        }

        return builder.build()
    }

    private fun getAccessToken(context: Context): String? {
        val serviceAccountStream: InputStream = context.resources.openRawResource(R.raw.service_account)
        val credentials = GoogleCredentials.fromStream(serviceAccountStream)
            .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))
        val accessToken = credentials.refreshAccessToken()
        return accessToken?.tokenValue
    }

    @Singleton
    @Provides
    @Named("standardRetrofit")
    fun provideRetrofit(
        @Named("standardHttpClient") okHttpClient: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .baseUrl(BASE_URL)
            .build()
    }

    @Singleton
    @Provides
    @Named("notificationRetrofit")
    fun provideRetrofitNotification(
        @Named("notificationHttpClient") notificationHttpClient: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .client(notificationHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .baseUrl(BASE_URL_FIREBASE)
            .build()
    }

    @Singleton
    @Provides
    fun provideApiService(
        @Named("standardRetrofit") retrofit: Retrofit
    ): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    @Singleton
    @Provides
    fun provideApiServiceNotification(
        @Named("notificationRetrofit") retrofit: Retrofit
    ): NotificationApi {
        return retrofit.create(NotificationApi::class.java)
    }
}
