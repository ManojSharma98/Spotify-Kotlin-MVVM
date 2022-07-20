package com.example.spotifykotlinusingmvvm.ui

import android.media.session.PlaybackState
import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.RequestManager
import com.example.spotifykotlinusingmvvm.R
import com.example.spotifykotlinusingmvvm.adapters.SwipeSongAdapter
import com.example.spotifykotlinusingmvvm.data.entities.Song
import com.example.spotifykotlinusingmvvm.databinding.ActivityMainBinding
import com.example.spotifykotlinusingmvvm.exoplayer.isPlaying
import com.example.spotifykotlinusingmvvm.exoplayer.toSong
import com.example.spotifykotlinusingmvvm.resources.Status
import com.example.spotifykotlinusingmvvm.ui.viewModels.MainViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    @Inject
    lateinit var swipeSongAdapter: SwipeSongAdapter

    private lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var glide: RequestManager

    private var curPlayingSong: Song? = null
    private var playbackstate : PlaybackStateCompat? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        subscribeToObserver()
        binding.vpSong.adapter = swipeSongAdapter

        binding.vpSong.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (playbackstate?.isPlaying == true){
                    mainViewModel.playOrToggleSong(swipeSongAdapter.songs[position])
                }
                else{
                    curPlayingSong = swipeSongAdapter.songs[position]
                }
            }
        })

        binding.ivPlayPause.setOnClickListener {
            curPlayingSong?.let {
                mainViewModel.playOrToggleSong(it,true)
            }
        }

        swipeSongAdapter.setItemClickListener {
            navHostFragment.findNavController().navigate(R.id.globalActionToSongFragment)
        }

        navHostFragment.findNavController().addOnDestinationChangedListener{
            _,destination, _ ->
            when(destination.id){
                R.id.songFragment -> hideBottomBar()
                R.id.homeFragment -> showBottomBar()
                else -> showBottomBar()
            }
        }
    }

    private fun hideBottomBar(){
        binding.ivCurSongImage.isVisible = false
        binding.vpSong.isVisible = false
        binding.ivPlayPause.isVisible = false
    }


    private fun showBottomBar(){
        binding.ivCurSongImage.isVisible = true
        binding.vpSong.isVisible = true
        binding.ivPlayPause.isVisible = true
    }

    private fun switchViewPagerToCurrentSong(song: Song) {
        val newItemIndex = swipeSongAdapter.songs.indexOf(song)
        if (newItemIndex != -1) {
            binding.vpSong.currentItem = newItemIndex
            curPlayingSong = song
        }
    }

    private fun subscribeToObserver() {
        mainViewModel.mediaItem.observe(this) {
            it?.let { result ->
                when (result.status) {
                    Status.SUCCESS -> {

                        result.data?.let { songs ->
                            swipeSongAdapter.songs = songs
                            if (songs.isNotEmpty()) {
                                glide.load((curPlayingSong ?: songs[0]).imageUrl)
                                    .into(binding.ivCurSongImage)
                            }
                            switchViewPagerToCurrentSong(curPlayingSong ?: return@observe)
                        }
                    }
                    Status.ERROR -> Unit
                    Status.LOADING -> Unit
                }
            }
        }

        mainViewModel.curPlayingSong.observe(this) {
            if (it == null) return@observe

            curPlayingSong = it.toSong()
            glide.load(curPlayingSong?.imageUrl).into(binding.ivCurSongImage)
            switchViewPagerToCurrentSong(curPlayingSong ?: return@observe)
        }

        mainViewModel.playbackState.observe(this){
            playbackstate = it
            if (playbackstate?.isPlaying == true){
                binding.ivPlayPause.setImageResource(R.drawable.ic_pause)
            }
            else{
                binding.ivPlayPause.setImageResource(R.drawable.ic_play)
            }
           /* binding.ivPlayPause.setImageResource(
                if (playbackstate?.isPlaying == true) R.drawable.ic_pause else R.drawable.ic_play

            )*/
        }
        mainViewModel.isConnected.observe(this){
            it?.getContentIfNotHandled()?.let {
                result->
                    when(result.status){
                        Status.ERROR-> Toast.makeText(this, "Error occurred",Toast.LENGTH_LONG).show()
                        else-> Unit
                    }

            }
        }
        mainViewModel.networkError.observe(this){
            it?.getContentIfNotHandled()?.let {
                    result->
                when(result.status){
                    Status.ERROR-> Toast.makeText(this, "Error occurred",Toast.LENGTH_LONG).show()
                    else-> Unit
                }

            }
        }
    }


}