package com.example.audiolibrary.ViewPager1;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.audiolibrary.R;
import com.example.audiolibrary.RecyclerView.audiolistRecyclerView.Audio;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;

public class BottomSheetPlayerPredict extends BottomSheetDialogFragment {

    private int position;
    private String user_mood;
    private ArrayList<Audio> audioList;
    private ViewPager2 viewPager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bs_player, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            position = getArguments().getInt("position");
            audioList = (ArrayList<Audio>) getArguments().getSerializable("audio_list");
            user_mood = getArguments().getString("user_mood");

            viewPager = view.findViewById(R.id.viewPager);
            ViewPagerAdapterPredict pagerAdapterPredict = new ViewPagerAdapterPredict(getChildFragmentManager(), getLifecycle());
            pagerAdapterPredict.setData(position, audioList, user_mood);
            viewPager.setAdapter(pagerAdapterPredict);
        }
    }

    public static BottomSheetPlayerPredict newInstance(int position, ArrayList<Audio> audioList) {
        BottomSheetPlayerPredict fragment = new BottomSheetPlayerPredict();
        Bundle args = new Bundle();
        args.putInt("position", position);
        args.putSerializable("audio_list", audioList);
        fragment.setArguments(args);
        return fragment;
    }

    public void showBottomSheet(AppCompatActivity activity) {
        this.show(activity.getSupportFragmentManager(), "BottomSheetAdd");
    }
}
