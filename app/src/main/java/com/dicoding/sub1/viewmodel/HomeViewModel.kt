package com.dicoding.sub1.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.dicoding.sub1.api.APIConfig
import com.dicoding.sub1.api.ResponseStory
import com.dicoding.sub1.api.StoryDetail
import com.dicoding.sub1.data.StoryRepository
import com.dicoding.sub1.data.StoryResponseItem
import retrofit2.Call
import retrofit2.Response

class HomeViewModel(storyRepository: StoryRepository) : ViewModel() {
    private val _stories = MutableLiveData<List<StoryDetail>>()
    val stories:LiveData<PagingData<StoryResponseItem>> =
        storyRepository.getQuote().cachedIn(viewModelScope)



    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    var isError: Boolean = false

    fun getStories(token: String) {
        _isLoading.value = true
        val api = APIConfig.getApiService().getStory("Bearer $token")
        api.enqueue(object : retrofit2.Callback<ResponseStory> {
            override fun onResponse(call: Call<ResponseStory>, response: Response<ResponseStory>) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    isError = false
                    val responseBody = response.body()
                    if (responseBody != null) {
//                        _stories.value = responseBody.listStory
                    }
                    _message.value = responseBody?.message.toString()

                } else {
                    isError = true
                    _message.value = response.message()
                }
            }

            override fun onFailure(call: Call<ResponseStory>, t: Throwable) {
                _isLoading.value = false
                isError = true
                _message.value = t.message.toString()
            }
        })
    }
}
