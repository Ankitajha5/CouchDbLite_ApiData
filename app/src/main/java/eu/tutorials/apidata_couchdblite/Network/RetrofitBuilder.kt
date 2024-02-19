package eu.tutorials.apidata_couchdblite.Network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitBuilder {
    private val retrofit by lazy{
        Retrofit.Builder()
            .baseUrl(Url.url)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    }
    val api: Api by lazy{
        retrofit.create(Api::class.java)
    }


}