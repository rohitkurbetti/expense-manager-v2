package com.example.myapplication.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.myapplication.PandLFragment;
import com.example.myapplication.fragments.ExpensesFragment;
import com.example.myapplication.fragments.InvoicesFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 2:
                return new PandLFragment();
            case 1:
                return new ExpensesFragment();
            case 0:
            default:
                return new InvoicesFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3; // Number of tabs
    }
}

