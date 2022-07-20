package com.example.spotifykotlinusingmvvm.data.remote

import com.example.spotifykotlinusingmvvm.constants.Constants.SONG_COLLECTION
import com.example.spotifykotlinusingmvvm.data.entities.Song
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class MusicDatabase {

    private val firestore = FirebaseFirestore.getInstance()
    private val songCollection = firestore.collection(SONG_COLLECTION)


    suspend fun getAllSongs() :List<Song>{
        return  try {
            songCollection.get().await().toObjects(Song::class.java)
        }
        catch (e:Exception){
            emptyList()
        }
    }
}