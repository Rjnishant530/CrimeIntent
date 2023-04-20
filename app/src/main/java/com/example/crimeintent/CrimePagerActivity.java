package com.example.crimeintent;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;


import java.util.List;
import java.util.UUID;

public class CrimePagerActivity extends AppCompatActivity implements CrimeFragment.Callbacks {

    public static boolean item_deleted;
    public static int deleted_index;

    private ViewPager2 viewPager;

    private Button previous, next;


    private static final String EXTRA_CRIME_ID =
            "com.bignerdranch.android.criminalintent.crime_id";

    public static Intent newIntent(Context packageContext, UUID crimeId) {
        Intent intent = new Intent(packageContext, CrimePagerActivity.class);
        intent.putExtra(EXTRA_CRIME_ID, crimeId);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        item_deleted = false;
        setContentView(R.layout.activity_crime_pager);

        viewPager = findViewById(R.id.crime_view_pager);
        previous = findViewById(R.id.first_crime);
        next = findViewById(R.id.last_crime);

        viewPager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                return CrimeFragment.newInstance(CrimeLab.getInstance(getBaseContext()).get(position).getId());
            }


            @Override
            public int getItemCount() {
                return CrimeLab.getInstance(getBaseContext()).size();
            }
        });

        UUID crimeId = (UUID) getIntent().getSerializableExtra(EXTRA_CRIME_ID);
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (position == 0) {
                    previous.setEnabled(false);
                } else
                    previous.setEnabled(true);

                if (position == CrimeLab.getInstance(CrimePagerActivity.this).getCrimes().size() - 1) {
                    next.setEnabled(false);
                } else
                    next.setEnabled(true);
            }
        });

        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(0,true);
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(viewPager.getAdapter().getItemCount() - 1,true);
            }
        });

        viewPager.setCurrentItem(CrimeLab.getInstance(getBaseContext()).getIndex(crimeId),true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == 12) {
            UUID uuid = (UUID) data.getSerializableExtra(CrimeFragment.DELETE_CRIME);
            deleted_index = CrimeLab.getInstance(getBaseContext()).getIndex(uuid);
            viewPager.getAdapter().notifyItemRemoved(deleted_index);
            CrimeLab.getInstance(getBaseContext()).deleteItem(uuid);
            item_deleted=true;
            Intent intent = new Intent(getBaseContext(), CrimeListActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);

        }
    }

    @Override
    public void onCrimeUpdated(Crime crime) {

    }
}
