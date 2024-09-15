package com.example.audiolibrary.ViewPager1;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.audiolibrary.RecyclerView.audiolistRecyclerView.Audio;
import com.example.audiolibrary.ViewPager1.screens.SecondFragment;
import com.example.audiolibrary.ViewPager1.screens2.screens.FirstFragmentPredict;

import java.util.ArrayList;

public class ViewPagerAdapterPredict extends FragmentStateAdapter {

    Fragment fragment;
    Bundle args = new Bundle();


    private int currentPosition = 0;
    private String user_mood = "";
    private ArrayList<Audio> currentAudioList = new ArrayList();


    public ViewPagerAdapterPredict(FragmentManager fragmentManager, Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    public void setData(int currentPosition, ArrayList<Audio> currentAudioList, String user_mood) {
        this.currentPosition = currentPosition;
        this.currentAudioList = currentAudioList;
        this.user_mood = user_mood;
    }

    @Override
    public Fragment createFragment(int position) {
        // Возвращайте нужный фрагмент в зависимости от позиции
        switch (position) {
            case 0:
                fragment = new FirstFragmentPredict();
                // Здесь вы можете передать данные, как в вашем примере
                args.putInt("position", currentPosition);
                args.putSerializable("audio_list", currentAudioList);
                args.putString("user_mood", user_mood);
                fragment.setArguments(args);
                return fragment; // Возвращаем фрагмент с установленными аргументами
            case 1:
                return new SecondFragment();
            // Добавьте другие фрагменты по аналогии
            default:
                return null;
        }
    }

    @Override
    public int getItemCount() {
        // Возвращайте общее количество фрагментов
        return 2; // Например, у вас есть два фрагмента
    }
}
