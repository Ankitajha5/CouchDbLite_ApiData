package eu.tutorials.apidata_couchdblite.ViewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.tutorials.apidata_couchdblite.Repository.PostRepository
import eu.tutorials.apidata_couchdblite.model.Data
import kotlinx.coroutines.launch

class PostViewModel(private val postRepository: PostRepository) : ViewModel() {
    val _postMutableLiveData: MutableLiveData<List<Data>> = MutableLiveData()
    val postLiveData: LiveData<List<Data>> = _postMutableLiveData

    fun getPost() {
        viewModelScope.launch {
            try {
                val response = postRepository.getPost()
                _postMutableLiveData.postValue(response)
            } catch (e: Exception) {

                Log.d("main", "getPost: ${e.message}")

            }
        }
    }
}