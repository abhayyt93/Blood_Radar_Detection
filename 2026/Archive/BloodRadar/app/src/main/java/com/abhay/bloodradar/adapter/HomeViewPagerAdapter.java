package com.abhay.bloodradar.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.abhay.bloodradar.fragment.DonationsFragment;
import com.abhay.bloodradar.fragment.DonorsFragment;
import com.abhay.bloodradar.fragment.MessagesFragment;
import com.abhay.bloodradar.fragment.RequestsFragment;

public class HomeViewPagerAdapter extends FragmentStateAdapter {

    public HomeViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new RequestsFragment();
            case 1:
                return new DonationsFragment();
            case 2:
                return new DonorsFragment();
            case 3:
                return new MessagesFragment();
            default:
                return new RequestsFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 4; // Requests, Donations, Donors, and Messages tabs
    }
}

