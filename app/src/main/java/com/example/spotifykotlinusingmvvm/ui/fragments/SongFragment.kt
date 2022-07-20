package com.example.spotifykotlinusingmvvm.ui.fragments

import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.RequestManager
import com.example.spotifykotlinusingmvvm.R
import com.example.spotifykotlinusingmvvm.data.entities.Song
import com.example.spotifykotlinusingmvvm.exoplayer.isPlaying
import com.example.spotifykotlinusingmvvm.exoplayer.toSong
import com.example.spotifykotlinusingmvvm.resources.Status
import com.example.spotifykotlinusingmvvm.ui.viewModels.MainViewModel
import com.example.spotifykotlinusingmvvm.ui.viewModels.SongViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_song.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class SongFragment : Fragment() {

    @Inject
    lateinit var glide :RequestManager

    private lateinit var mainViewModel: MainViewModel
    private val songViewModel :SongViewModel by viewModels()

    private var curPlayingSong : Song? = null
    private var playbackState : PlaybackStateCompat? = null
    private var shouldUpdateSeekbar  = true


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_song, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        subscribeToObservers()

        ivPlayPauseDetail.setOnClickListener {
            curPlayingSong?.let {
                mainViewModel.playOrToggleSong(it,true)

            }
        }

        ivSkipPrevious.setOnClickListener {
            mainViewModel.skipToPreviousSong()
        }

        ivSkip.setOnClickListener {
            mainViewModel.skipToNextSong()
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                if (p2){ // here p2 is from user input
                    setCurPlayerTimeToTextView(p1.toLong()) // p1 is progress
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
                shouldUpdateSeekbar = false
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                p0?.let {
                    mainViewModel.seekTo(it.progress.toLong())
                    shouldUpdateSeekbar = true
                }
            }

        })
    }

    private fun updateTitleAndSongImage(song: Song){
        val title = "${song.title} - ${song.subtitle}"
        tvSongName.text = title
        glide.load(song.imageUrl).into(ivSongImage)

    }

    private fun subscribeToObservers(){
        mainViewModel.mediaItem.observe(viewLifecycleOwner){
            it?.let {
                result->
                when(result.status){
                    Status.SUCCESS-> {
                        result.data?.let {
                            songs-> if (curPlayingSong == null && songs.isNotEmpty()){
                                curPlayingSong = songs[0]
                            updateTitleAndSongImage(songs[0])
                        }
                        }
                    }
                    else-> Unit
                }
            }
        }

        mainViewModel.curPlayingSong.observe(viewLifecycleOwner){
            if (it== null) return@observe
            curPlayingSong = it.toSong()
            updateTitleAndSongImage(curPlayingSong!!)
        }

        mainViewModel.playbackState.observe(viewLifecycleOwner){
            playbackState = it
            ivPlayPauseDetail.setImageResource(
                if (playbackState?.isPlaying == true) R.drawable.ic_pause else R.drawable.ic_play

            )
            seekBar.progress = it?.position?.toInt() ?:0
        }

        songViewModel.curPlayerPosition.observe(viewLifecycleOwner){
            if (shouldUpdateSeekbar){
                seekBar.progress = it.toInt()
                setCurPlayerTimeToTextView(it)
            }
        }

        songViewModel.curSongDuration.observe(viewLifecycleOwner){
            seekBar.max = it.toInt()

            val dateFormat = SimpleDateFormat("mm::ss", Locale.getDefault())
            tvSongDuration.text = dateFormat.format(it)
        }
    }

    private fun setCurPlayerTimeToTextView(ms: Long) {

        val dateFormat = SimpleDateFormat("mm::ss", Locale.getDefault())
        tvCurTime.text = dateFormat.format(ms)
    }
}