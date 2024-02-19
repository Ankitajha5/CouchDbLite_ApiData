package eu.tutorials.apidata_couchdblite.Repository

import eu.tutorials.apidata_couchdblite.Network.RetrofitBuilder
import eu.tutorials.apidata_couchdblite.model.Data


class PostRepository {
    val retrofit = RetrofitBuilder()
    suspend fun getPost():List<Data> = retrofit.api.getPost()
}