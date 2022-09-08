package uz.rttm.support.di

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
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
import uz.rttm.support.data.network.ApiService
import uz.rttm.support.data.network.NotificationApi
import uz.rttm.support.utils.Constants.Companion.BASE_URL
import uz.rttm.support.utils.Constants.Companion.BASE_URL_FIREBASE
import uz.rttm.support.utils.Prefs
import java.util.concurrent.TimeUnit
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

//    @Singleton
//    @Provides
//    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
//        return HttpLoggingInterceptor()
//    }

    @Singleton
    @Provides
    fun provideChuckInterceptor(
        @ApplicationContext context: Context
    ): ChuckerInterceptor {
        return ChuckerInterceptor.Builder(context).collector(
            ChuckerCollector(context)
        ).build()
    }

    @Singleton
    @Provides
    fun provideHttpClient(
        prefs: Prefs,
        @ApplicationContext context: Context
    ): OkHttpClient {
        val builder = OkHttpClient().newBuilder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder().addHeader("Authorization", "Bearer " + prefs.get(prefs.token, "")).build()
                chain.proceed(request)
            }
            .connectTimeout(10000L, TimeUnit.MILLISECONDS)
            .readTimeout(10000L, TimeUnit.MILLISECONDS)
            .writeTimeout(10000L, TimeUnit.MILLISECONDS)

        if (BuildConfig.isDebug) {
            builder.addInterceptor(ChuckerInterceptor.Builder(context).collector(ChuckerCollector(context)).build())
        }

        return builder.build()
    }

    @Singleton
    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .baseUrl(BASE_URL)
            .build()
    }

    //    @Singleton
//    @Provides
    fun provideRetrofitNotification(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .baseUrl(BASE_URL_FIREBASE)
            .build()
    }

    @Singleton
    @Provides
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    @Singleton
    @Provides
    fun provideApiServiceNotification(okHttpClient: OkHttpClient): NotificationApi = provideRetrofitNotification(okHttpClient).create(
        NotificationApi::class.java
    )
}