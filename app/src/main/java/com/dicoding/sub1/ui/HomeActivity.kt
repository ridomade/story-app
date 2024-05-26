package com.dicoding.sub1.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.sub1.MainActivity
import com.dicoding.sub1.R
import com.dicoding.sub1.api.StoryDetail
import com.dicoding.sub1.dataStore
import com.dicoding.sub1.databinding.ActivityHomeBinding
import com.dicoding.sub1.preference.UserPreferences
import com.dicoding.sub1.ui.adapter.StoryAdapter
import com.dicoding.sub1.viewmodel.HomeViewModel
import com.dicoding.sub1.viewmodel.LoginViewModel
import com.dicoding.sub1.viewmodel.ViewModelFactory

class HomeActivity : AppCompatActivity() {

    private val userPreferences by lazy { UserPreferences.getInstance(dataStore) }
    private lateinit var binding: ActivityHomeBinding
    private lateinit var token: String

    private val homepageViewModel: HomeViewModel by lazy {
        ViewModelProvider(this)[HomeViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Story List"

        setAdapter()
        refreshData()
        setAddStoryButton()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_items, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.maps -> {
//                startActivity(Intent(this, MapsActivity::class.java))
                true
            }
            R.id.logout -> {
                val loginViewModel = ViewModelProvider(this, ViewModelFactory(userPreferences))[LoginViewModel::class.java]
                loginViewModel.logout()

                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setAdapter() {
        val layoutManager = LinearLayoutManager(this)
        binding.rvStories.layoutManager = layoutManager
        val itemDecoration = DividerItemDecoration(this, layoutManager.orientation)
        binding.rvStories.addItemDecoration(itemDecoration)

        val loginViewModel =
            ViewModelProvider(this, ViewModelFactory(userPreferences))[LoginViewModel::class.java]

        loginViewModel.getToken().observe(this) {
            token = it
            homepageViewModel.getStories(token)
        }
//        homepageViewModel.message.observe(this) {
//            setUserData(homepageViewModel.stories.value ?: emptyList())
//        }
        setUserData()
        homepageViewModel.isLoading.observe(this) {
            displayLoading(it)
        }
    }

    private fun setUserData() {
        val adapter = StoryAdapter()
        binding.rvStories.adapter = adapter
        homepageViewModel.stories.observe(this, {
            adapter.submitData(lifecycle, it)
        })
//        adapter.setOnItemClickCallback(object : StoryAdapter.OnItemClickCallback {
//            override fun onItemClicked(user: StoryDetail) {
//                val intent = Intent(this@HomeActivity, DetailStoryActivity::class.java)
//                intent.putExtra(DetailStoryActivity.EXTRA_STORY, user)
//                startActivity(intent)
//            }
//        })
    }

    private fun setAddStoryButton() {
        binding.btnAddStory.setOnClickListener {
            val intent = Intent(this, AddStoryActivity::class.java)
            startActivity(intent)
        }
    }

    private fun refreshData() {
        binding.pullRefresh.setOnRefreshListener {
            homepageViewModel.getStories(token)
            binding.pullRefresh.isRefreshing = false
        }
    }


    private fun displayLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}
