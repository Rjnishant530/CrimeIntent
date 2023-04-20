package com.example.crimeintent;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ShareCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

public class CrimeFragment extends Fragment {

    private Crime crime;
    private File mPhotoFile;
    private EditText mEditText;
    private Button mDateButton, mTimeButton, mReportButton, mSuspectButton, mCall;
    private CheckBox mSolvedCheckBox;
    private ImageView mPhotoView;
    private ImageButton mPhotoButton;

    private static final String ARG_CRIME_ID = "crime_id";
    private static final String DIALOG_DATE = "DialogDate";

    public static final int REQUEST_DATE = 0;
    public static final int REQUEST_TIME = 1;
    private static final int  REQUEST_PHOTO=2;

    private boolean canDial;


    public static final String DELETE_CRIME = "Delete Activity";

//    public static boolean CRIME_UPDATED;
//    public static UUID UPDATED_CRIME_ID;

    private ActivityResultLauncher<String> contactPermission;
    private Callbacks mCallbacks;

    public interface Callbacks {
        void onCrimeUpdated(Crime crime);
    }
    private void updateCrime() {
        CrimeLab.getInstance(getActivity()).updateCrime(crime);
        mCallbacks.onCrimeUpdated(crime);
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

    public static CrimeFragment newInstance(UUID crimeId) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(ARG_CRIME_ID, crimeId);

        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UUID crimeId = (UUID) getArguments().getSerializable(ARG_CRIME_ID);
        crime = CrimeLab.getInstance(getActivity()).getCrime(crimeId);
        mPhotoFile = CrimeLab.getInstance(getActivity()).getPhotoFile(crime);


        contactPermission = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                if (result) {
                    readContact();
                } else {
                    System.out.println("Denied");
                }
            }
        });

    }

    private void readContact() {

        registerForActivityResult(new ActivityResultContracts.PickContact(), result -> {
            String[] query = new String[]{ContactsContract.Contacts.LOOKUP_KEY};
            String[] query1 = new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER};
            String i;
            if (result != null) {
                try (Cursor c = getActivity().getContentResolver().query(result, query, null, null, null);) {
                    if (c.getCount() == 0) {
                        System.out.println("null data c");
                        return;
                    }
                    c.moveToFirst();
                    i = c.getString(0);
                }
                try (Cursor c1 = getActivity().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, query1, ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY + " = ?", new String[]{i}, null);) {
                    if (c1.getCount() == 0) {
                        System.out.println("null data c2");
                        return;
                    }
                    c1.moveToFirst();

                    crime.setSuspect(c1.getString(0));
                    String number =c1.getString(1);
                    number=number.replaceAll("\\s","");
                    crime.setNumber(Long.parseLong(number));
                    mCall.setEnabled(crime.getSuspect() != null&&canDial);
                    mSuspectButton.setText(crime.getSuspect());

                }
            } else {
                System.out.println("Null Data");

            }
        }).launch(null);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crime, container, false);

        mTimeButton = view.findViewById(R.id.crime_time);
        mEditText = view.findViewById(R.id.crime_title);
        mDateButton = view.findViewById(R.id.crime_date);
        mReportButton = view.findViewById(R.id.crime_report);
        mSuspectButton = view.findViewById(R.id.crime_suspect);
        mCall = view.findViewById(R.id.call);
        mSolvedCheckBox = view.findViewById(R.id.crime_solved);
        mPhotoButton = view.findViewById(R.id.crime_camera);
        mPhotoView = view.findViewById(R.id.crime_photo);
        updatePhotoView();
        mEditText.setText(crime.getTitle());
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                crime.setTitle(s.toString());
               updateCrime();

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                DatePickerFragment fragment = DatePickerFragment.newInstance(crime.getMdate());
                fragment.setTargetFragment(CrimeFragment.this, REQUEST_DATE);
                fragment.show(manager, DIALOG_DATE);
            }
        });

        mTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerFragment fragment = TimePickerFragment.newinstance(crime.getMTime());
                fragment.setTargetFragment(CrimeFragment.this, REQUEST_TIME);
                fragment.show(getFragmentManager(), "DIALOG TIME");

            }
        });

        mReportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = ShareCompat.IntentBuilder.from(getActivity())
                        .setChooserTitle(getString(R.string.send_report))
                        .setType("text/plain")
                        .setText(getCrimeReport())
                        .setSubject(getString(R.string.crime_report_subject))
                        .createChooserIntent();

                startActivity(i);
            }
        });

        final Intent pickContact = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);

        PackageManager packageManager = getActivity().getPackageManager();
        if (packageManager.resolveActivity(pickContact,PackageManager.MATCH_DEFAULT_ONLY) == null) {
            mSuspectButton.setEnabled(false);
        }

        final Intent dial = new Intent(Intent.ACTION_DIAL);
        if (packageManager.resolveActivity(dial, PackageManager.MATCH_DEFAULT_ONLY) == null) {
           canDial=false;
        }else
            canDial=true;

        mSuspectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                    readContact();
                } else if (shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS)) {
                    System.out.println("THIS IS Y YOU SHOULD GIVE PREMISSION");
                } else {
                    contactPermission.launch(Manifest.permission.READ_CONTACTS);
                }
            }
        });

        if (crime.getSuspect() != null) {
            mSuspectButton.setText(crime.getSuspect());
        }


        mSolvedCheckBox.setChecked(crime.isSolved());
        mSolvedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateCrime();
                crime.setSolved(isChecked);
            }
        });
        mCall.setEnabled(crime.getSuspect()!=null);
        mCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri number = Uri.parse("tel:"+String.valueOf(crime.getNumber()));
                Intent intent = new Intent(Intent.ACTION_DIAL, number);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_NO_USER_ACTION);
                startActivity(intent);
            }
        });

        final  Intent captureImage= new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        boolean canClick=mPhotoFile!=null && captureImage.resolveActivity(packageManager)!=null;
        mPhotoButton.setEnabled(canClick);
        mPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = FileProvider.getUriForFile(getActivity(),"com.example.crimeintent.fileprovider",mPhotoFile);
                captureImage.putExtra(MediaStore.EXTRA_OUTPUT,uri);

                List<ResolveInfo> cameraActivities = getActivity()
                        .getPackageManager().queryIntentActivities(captureImage,
                                PackageManager.MATCH_DEFAULT_ONLY);

                for (ResolveInfo activity : cameraActivities) {
                    getActivity().grantUriPermission(activity.activityInfo.packageName,
                            uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                }

                startActivityForResult(captureImage, REQUEST_PHOTO);
            }
        });

        setHasOptionsMenu(true);
        updateDate();
        return view;
    }

    private void updatePhotoView() {
        if (mPhotoFile == null || !mPhotoFile.exists()) {
            mPhotoView.setImageDrawable(null);
        } else {
            Bitmap bitmap = PictureUtils.getScaledBitmap(
                    mPhotoFile.getPath(), getActivity());
            mPhotoView.setImageBitmap(bitmap);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        CrimeLab.getInstance(getActivity())
                .updateCrime(crime);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete_item:
                if (getActivity() instanceof CrimePagerActivity) {
                    Intent intent = new Intent();
                    intent.putExtra(DELETE_CRIME, crime.getId());
                    ((CrimePagerActivity) getActivity()).onActivityResult(12, Activity.RESULT_OK, intent);
                }
                return true;
            default:
                return false;

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_DATE) {
            LocalDate date = (LocalDate) data
                    .getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            crime.setMdate(date);

            updateDate();
            updateCrime();

        } else if (requestCode == REQUEST_TIME) {
            LocalTime localTime = (LocalTime) data.getSerializableExtra(TimePickerFragment.EXTRA_TIME);
            crime.setMTime(localTime);
            updateDate();
            updateCrime();
        }else if (requestCode == REQUEST_PHOTO) {
            Uri uri = FileProvider.getUriForFile(getActivity(),
                    "com.bignerdranch.android.criminalintent.fileprovider",
                    mPhotoFile);

            getActivity().revokeUriPermission(uri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            updatePhotoView();
            updateCrime();
        }
    }

    private void updateDate() {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("EEEE, LLLL dd, uuuu");
        mDateButton.setText(dateTimeFormatter.format(crime.getMdate()));
        DateTimeFormatter dateTimeFormatter1 = DateTimeFormatter.ofPattern(" hh:mm a");
        mTimeButton.setText(dateTimeFormatter1.format(crime.getMTime()));
    }

    private String getCrimeReport() {
        String solvedString = null;
        if (crime.isSolved()) {
            solvedString = getString(R.string.crime_report_solved);
        } else {
            solvedString = getString(R.string.crime_report_unsolved);
        }

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("EEEE, LLLL dd, uuuu");
        String dateString = dateTimeFormatter.format(crime.getMdate());

        String suspect = crime.getSuspect();
        if (suspect == null) {
            suspect = getString(R.string.crime_report_no_suspect);
        } else {
            suspect = getString(R.string.crime_report_suspect, suspect);
        }

        String report = getString(R.string.crime_report,
                crime.getTitle(), dateString, solvedString, suspect);

        return report;
    }
}
