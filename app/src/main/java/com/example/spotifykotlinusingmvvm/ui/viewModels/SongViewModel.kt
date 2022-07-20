package com.example.spotifykotlinusingmvvm.ui.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spotifykotlinusingmvvm.constants.Constants.UPDATE_PLAYER_INTERVAL
import com.example.spotifykotlinusingmvvm.exoplayer.MusicService
import com.example.spotifykotlinusingmvvm.exoplayer.MusicServiceConnection
import com.example.spotifykotlinusingmvvm.exoplayer.currentPlaybackPosition
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SongViewModel @Inject constructor( // ViewModelInject
    musicServiceConnection: MusicServiceConnection
):ViewModel() {

    init {
       //  updateCurrentPlayerPosition()  // error in this update task
    }

    private val playbackstate = musicServiceConnection.playbackState

    private val _curSongDuration = MutableLiveData<Long>()
    val curSongDuration : LiveData<Long> = _curSongDuration


    private val _curPlayerPosition = MutableLiveData<Long>()
    val curPlayerPosition:LiveData<Long> = _curPlayerPosition


    private fun updateCurrentPlayerPosition(){
        viewModelScope.launch {
            while (true){
                val pos = playbackstate.value?.currentPlaybackPosition
                if (curPlayerPosition.value != pos){
                    _curPlayerPosition.postValue(pos)
                    _curSongDuration.postValue(MusicService.curSongDuration)
                }
                delay(UPDATE_PLAYER_INTERVAL)
            }
        }
    }

}