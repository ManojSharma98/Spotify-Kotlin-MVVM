package com.example.spotifykotlinusingmvvm.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.spotifykotlinusingmvvm.R
import com.example.spotifykotlinusingmvvm.adapters.SongAdapter
import com.example.spotifykotlinusingmvvm.databinding.FragmentHomeBinding
import com.example.spotifykotlinusingmvvm.resources.Status
import com.example.spotifykotlinusingmvvm.ui.viewModels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private lateinit var  mainViewModel: MainViewModel
    private lateinit var binding: FragmentHomeBinding

    @Inject
    lateinit var songAdapter: SongAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        setupRecyclerView()
        subscribeToObserves()

        songAdapter.setItemClickListener {
            mainViewModel.playOrToggleSong(it)
        }
    }

    private fun setupRecyclerView() = binding.rvAllSongs.apply {
        adapter = songAdapter
        layoutManager  = LinearLayoutManager(requireContext())
    }

    private fun subscribeToObserves(){
        mainViewModel.mediaItem.observe(viewLifecycleOwner){ result->
            when(result.status){
                Status.SUCCESS -> {
                    binding.allSongsProgressBar.isVisible = false
                    result.data?.let {
                        songs->
                        songAdapter.songs = songs
                    }
                }
                Status.ERROR-> Unit
                Status.LOADING -> binding.allSongsProgressBar.isVisible = true

            }
        }
    }

}