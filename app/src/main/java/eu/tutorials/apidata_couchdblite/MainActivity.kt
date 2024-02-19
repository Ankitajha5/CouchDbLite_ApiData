package eu.tutorials.apidata_couchdblite


import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.couchbase.lite.CouchbaseLite
import com.couchbase.lite.Database
import com.couchbase.lite.DatabaseConfigurationFactory
import android.util.Log
import android.widget.TextView
import com.couchbase.lite.Collection
import com.couchbase.lite.DataSource
import com.couchbase.lite.Expression
import com.couchbase.lite.Meta.id
import com.couchbase.lite.MutableDocument
import com.couchbase.lite.Query
import com.couchbase.lite.QueryBuilder
import com.couchbase.lite.SelectResult
import com.couchbase.lite.ValueIndex
import com.couchbase.lite.newConfig
import com.google.gson.Gson
import eu.tutorials.apidata_couchdblite.Adapter.PostAdapter
import eu.tutorials.apidata_couchdblite.Repository.PostRepository
import eu.tutorials.apidata_couchdblite.ViewModel.PostViewModel
import eu.tutorials.apidata_couchdblite.ViewModel.PostViewModelFactory
import eu.tutorials.apidata_couchdblite.databinding.ActivityMainBinding
import eu.tutorials.apidata_couchdblite.model.Data
import kotlin.collections.Collection as Collection1


@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private lateinit var postViewModel: PostViewModel
    private var progressBar: ProgressBar? = null
    private lateinit var database: Database
    private lateinit var collection: Collection
    private val gson = Gson()
    private var isFirstTimeDataLoaded = true



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        CouchbaseLite.init(this)
        database = Database("MyDatabase", DatabaseConfigurationFactory.newConfig())
        collection = database.createCollection("CollectionNameApi")
        collection = database.getCollection("CollectionNameApi")!!
        collection = database.defaultScope.getCollection("CollectionNameApi")!!


        progressBar = binding.progressBar
        recyclerView = binding.recyclerView
        initRecyclerView()

        val postRepository = PostRepository()
        val viewModelFactory = PostViewModelFactory(postRepository)
        postViewModel = ViewModelProvider(this, viewModelFactory)[PostViewModel::class.java]
        if(isInternetAvailable(applicationContext)) {
            postViewModel.getPost()
            observeViewModel()
        }else {
            loadDataFromDatabase()
        }

    }

    private fun initRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView)
        postAdapter = PostAdapter(this)
        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = postAdapter
        }
    }
    private fun observeViewModel() {
        postViewModel.postLiveData.observe(this, Observer { posts ->
            posts?.let {
                progressBar?.visibility  = View.GONE
                recyclerView.visibility = View.VISIBLE
                postAdapter.setData(it)
                if(isFirstTimeDataLoaded) {
                    saveDataToDatabase(it)
                    isFirstTimeDataLoaded = false
                }
            }
        })
    }

    private fun saveDataToDatabase(posts: List<Data>) {

         posts.forEach { post ->
             val jsonString = gson.toJson(post)
             val mutableDocument = MutableDocument().setString("data", jsonString)
             Log.d("check", "checkdata" + mutableDocument.toString())
             // Save the document to the collection
             collection.save(mutableDocument)

     }

    }

    private fun loadDataFromDatabase() {
        // Query the collection for all documents
        val query = QueryBuilder
            .select(SelectResult.all())
            .from(DataSource.collection(collection))

        Log.d("query55", "parsing data" +query.execute().allResults().size)


        query.execute().use { rs ->
            val dataList : ArrayList<Data> = ArrayList()
            rs.allResults().forEach { result ->
                val jsonData = (result.toMap().get("CollectionNameApi") as HashMap<*, *>).get("data").toString()
                Log.d("query2", "parsing data" +jsonData)

                val parsedItem = gson.fromJson(jsonData, Data::class.java)
                dataList.add(Data( parsedItem.id,parsedItem.title ?: ""))
            }

            binding.progressBar.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            postAdapter.setData(dataList)
        }


    }

    private fun processData(jsonData: String) {
//        val gson = Gson()

    }



    fun isInternetAvailable(context: Context): Boolean {
        var result = false
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?

        // For Android 10 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            connectivityManager?.let {
                it.getNetworkCapabilities(connectivityManager.activeNetwork)?.apply {
                    result = when {
                        hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                        hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                        hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                        else -> false
                    }
                }
            }
        } else { // For below Android 10
            connectivityManager?.activeNetworkInfo?.run {
                result = when (type) {
                    ConnectivityManager.TYPE_WIFI -> true
                    ConnectivityManager.TYPE_MOBILE -> true
                    ConnectivityManager.TYPE_ETHERNET -> true
                    else -> false
                }
            }
        }
        return result
    }

    override fun onDestroy() {
        super.onDestroy()
        database.close()
    }


}














