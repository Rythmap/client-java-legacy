package com.mvnh.rythmap;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mvnh.rythmap.R;
import com.mvnh.rythmap.databinding.FragmentTestAccountBinding;

public class TestAccountFragment extends Fragment {

    private FragmentTestAccountBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentTestAccountBinding.inflate(inflater, container, false);



        return binding.getRoot();
    }
}