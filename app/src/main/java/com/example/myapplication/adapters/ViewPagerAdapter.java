package com.example.myapplication.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

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
            case 0:
                return new InvoicesFragment();
            case 1:
                return new ExpensesFragment();
            default:
                return new InvoicesFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2; // Number of tabs
    }
}

