package com.example.crimeintent;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import java.time.format.DateTimeFormatter;
import java.util.List;

public class CrimeListFragment extends Fragment {

    private RecyclerView mCrimeRecyclerView;
    private ImageButton imageButton;
    private TextView nocrimetext;
    private CrimeAdapter crimeAdapter;
    private Crime clickCrime;
    private boolean mSubtitleVisible;
    private static final String SAVED_SUBTITLE_VISIBLE = "subtitle";

    private Callbacks mCallbacks;


    public interface Callbacks {
        void onCrimeSelected(Crime crime);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallbacks = (Callbacks) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            mSubtitleVisible = savedInstanceState.getBoolean(SAVED_SUBTITLE_VISIBLE);
        }

       View  view = inflater.inflate(R.layout.fragment_crime_list, container, false);

        mCrimeRecyclerView = view.findViewById(R.id.crime_recycler_view);

        mCrimeRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        imageButton = view.findViewById(R.id.new_crime_Button);

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Crime c = new Crime();
                clickCrime = c;
                CrimeLab.getInstance(getActivity()).addCrime(c);
                Intent intent = CrimePagerActivity.newIntent(getContext(), c.getId());
                startActivity(intent);
            }
        });
        nocrimetext = view.findViewById(R.id.no_crime_text);
        updateVisiblity(CrimeLab.getInstance(getActivity()).size());
        updateUI();
        return view;
    }

    private void updateVisiblity(int size) {
        if(size==0){
            mCrimeRecyclerView.setVisibility(View.INVISIBLE);
            imageButton.setVisibility(View.VISIBLE);
            nocrimetext.setVisibility(View.VISIBLE);
        }else{
            mCrimeRecyclerView.setVisibility(View.VISIBLE);
            imageButton.setVisibility(View.GONE);
            nocrimetext.setVisibility(View.GONE);
        }
    }


    public void updateUI() {

        if (crimeAdapter == null) {
            crimeAdapter = new CrimeAdapter();
            mCrimeRecyclerView.setAdapter(crimeAdapter);
        }

        if(clickCrime!=null) {
            crimeAdapter.notifyItemChanged(CrimeLab.getInstance(getActivity()).getCrimes().indexOf(clickCrime));
            clickCrime=null;

        }

        updateSubtitle();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateVisiblity(CrimeLab.getInstance(getActivity()).size());
        if (CrimePagerActivity.item_deleted) {
            CrimePagerActivity.item_deleted = false;
            crimeAdapter.notifyItemRemoved(CrimePagerActivity.deleted_index);
        }

        updateUI();

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVED_SUBTITLE_VISIBLE, mSubtitleVisible);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime_list, menu);

        MenuItem subtitleItem = menu.findItem(R.id.show_subtitle);
        if (mSubtitleVisible) {
            subtitleItem.setTitle(R.string.hide_subtitle);
        } else {
            subtitleItem.setTitle(R.string.show_subtitle);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_crime:
                Crime c = new Crime();
                clickCrime = c;
                CrimeLab.getInstance(getActivity()).addCrime(c);
                crimeAdapter.notifyItemInserted(CrimeLab.getInstance(getActivity()).getIndex(c.getId()));
                mCallbacks.onCrimeSelected(c);
                return true;
            case R.id.show_subtitle:
                mSubtitleVisible = !mSubtitleVisible;
                getActivity().invalidateOptionsMenu();
                updateSubtitle();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void updateSubtitle() {
        CrimeLab crimeLab = CrimeLab.getInstance(getActivity());
        int count = crimeLab.getCrimes().size();

        String sub = getResources().getQuantityString(R.plurals.subtitle_plural, count, count);

        if (!mSubtitleVisible) {
            sub = "";
        }
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.getSupportActionBar().setSubtitle(sub);

    }

    private class CrimeHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView mTitleTextView;
        private TextView mDateTextView;
        private Crime mCrime;
        private ImageView mCrimeImage;

        public CrimeHolder(LayoutInflater inflater, ViewGroup parent) {

            super(inflater.inflate(R.layout.list_item_crime, parent, false));

            mTitleTextView = itemView.findViewById(R.id.crime_title);
            mDateTextView = itemView.findViewById(R.id.crime_date);
            mCrimeImage = itemView.findViewById(R.id.crime_image);

            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
//            Intent intent = CrimePagerActivity.newIntent(getActivity(), mCrime.getId());
            clickCrime = mCrime;
//            startActivity(intent);
            mCallbacks.onCrimeSelected(mCrime);
        }

        public void bind(Crime crime) {
            mCrime = crime;
            mTitleTextView.setText(mCrime.getTitle());
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("EEEE, LLLL dd, uuuu");
            mDateTextView.setText(dateTimeFormatter.format(mCrime.getMdate()));
            mCrimeImage.setVisibility(mCrime.isSolved() ? View.VISIBLE : View.INVISIBLE);
        }

    }

    private class CrimeAdapter extends RecyclerView.Adapter<CrimeHolder> {

        @NonNull
        @Override
        public CrimeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new CrimeHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(@NonNull CrimeHolder holder, int position) {
            holder.bind(CrimeLab.getInstance(getActivity()).get(position));
        }

        @Override
        public int getItemCount() {
            return CrimeLab.getInstance(getActivity()).size();
        }

    }
}