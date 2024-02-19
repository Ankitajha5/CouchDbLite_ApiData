package eu.tutorials.apidata_couchdblite.Network

import eu.tutorials.apidata_couchdblite.model.Data
import retrofit2.http.GET

interface Api {
    @GET("posts")
    suspend fun getPost():List<Data>
}