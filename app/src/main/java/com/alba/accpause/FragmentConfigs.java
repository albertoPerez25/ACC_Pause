package com.alba.accpause;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.alba.accpause.database.Data;
import com.alba.accpause.database.DataDao;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.slider.RangeSlider;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentConfigs#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentConfigs extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private MaterialSwitch mySwitch;
    private RangeSlider rangeSlider;
    private ConstraintLayout switchLabel;
    private float rangeSliderUpperValue;
    private float rangeSliderLowerValue;
    private Context context;
    private CompoundButton.OnCheckedChangeListener dstatus_listener;

    public FragmentConfigs() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FragmentConfigs.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentConfigs newInstance(String param1, String param2) {
        FragmentConfigs fragment = new FragmentConfigs();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_configs, container, false);

        mySwitch = view.findViewById(R.id.mySwitch);
        switchLabel = view.findViewById(R.id.switchLabel);
        rangeSlider = view.findViewById(R.id.rangeSlider);

        switchLabel.setOnClickListener(v ->
                mySwitch.setChecked(!mySwitch.isChecked()));

        /////////////
        dstatus_listener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                boolean acc_enabled;
                try{
                    if (isChecked) {
                        Toast.makeText(getActivity(), "ACC Enabled", Toast.LENGTH_SHORT).show();
                        Runtime.getRuntime().exec("su -c /dev/accd");
                        acc_enabled = true;
                    } else {
                        Toast.makeText(getActivity(), "ACC Disabled", Toast.LENGTH_SHORT).show();
                        Runtime.getRuntime().exec("su -c /dev/accd.");
                        acc_enabled = false;
                    }
                    final boolean finalAcc_enabled = acc_enabled;
                    new Thread(() -> {
                        DataDao dataDao = ((ACCPause) context.getApplicationContext()).getDataDao();
                        Data d_enabled = dataDao.getByKey("daemon_enabled");
                        d_enabled.value = Boolean.toString(finalAcc_enabled);
                        dataDao.update(d_enabled);
                    }).start();
                } catch (IOException e) {
                    Toast.makeText(getActivity(), "Failed to get su permission", Toast.LENGTH_SHORT).show();
                    throw new RuntimeException(e);
                }
            }
        };
        /////////////
        //mySwitch.setOnCheckedChangeListener(dstatus_listener);

        rangeSlider.addOnChangeListener(new RangeSlider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull RangeSlider slider, float value, boolean fromUser) {
                try {
                    //Toast.makeText(getActivity(), Float.toString(rangeSlider.getValues().get(0))+" "+Float.toString(rangeSlider.getValues().get(1)), Toast.LENGTH_SHORT).show();", Toast.LENGTH_SHORT).show();
                    // acc pause_capacity resume_capacity
                    if (!fromUser){
                        return;
                    }

                    Float resume = rangeSlider.getValues().get(0);
                    Float pause = rangeSlider.getValues().get(1);

                    String resume_capacity = Float.toString(resume);
                    String pause_capacity = Float.toString(pause);

                    final String finalResume_capacity = resume_capacity.substring(0, resume_capacity.length()-2);
                    final String finalPause_capacity = pause_capacity.substring(0, pause_capacity.length()-2);
                    Toast.makeText(getActivity(), finalResume_capacity+" "+finalPause_capacity, Toast.LENGTH_SHORT).show();
                    Runtime.getRuntime().exec("su -c /dev/acc " + finalPause_capacity + " " + finalResume_capacity);

                    new Thread(() -> {
                        DataDao dataDao = ((ACCPause) context.getApplicationContext()).getDataDao();

                        Data pauseData = dataDao.getByKey("pause_capacity");
                        pauseData.value = finalPause_capacity;

                        Data resumeData = dataDao.getByKey("resume_capacity");
                        resumeData.value = finalResume_capacity;

                        dataDao.updateAll(pauseData,resumeData);
                    }).start();


                } catch (IOException e) {
                    //Toast.makeText(getActivity(), "Failed to get su permission", Toast.LENGTH_SHORT).show();
                    throw new RuntimeException(e);
                }
            }
        });

        new Thread(() -> {
            ACCPause application = (ACCPause) context.getApplicationContext();
            DataDao dataDao = application.getDataDao();
            String key = "resume_capacity";
            String value = dataDao.getByKey(key).value;
            boolean acc_enabled = true;

            if (value != null){
                rangeSliderLowerValue = Float.parseFloat(value);
            }else {
                rangeSliderLowerValue = 0.0f;
            }
            key = "pause_capacity";
            value = dataDao.getByKey(key).value;
            if (value != null){
                rangeSliderUpperValue = Float.parseFloat(value);
            }else{
                rangeSliderUpperValue = 0.0f;
            }
            List<Float> initialValues = Arrays.asList(rangeSliderLowerValue, rangeSliderUpperValue);
            rangeSlider.setValues(initialValues);

            key = "daemon_enabled";
            value = dataDao.getByKey(key).value;
            if (value != null){
                acc_enabled = Boolean.parseBoolean(value);
            }
            mySwitch.setOnCheckedChangeListener(null);
            mySwitch.setChecked(acc_enabled);
            mySwitch.jumpDrawablesToCurrentState();
            mySwitch.setOnCheckedChangeListener(dstatus_listener);
        }).start();
        return view;
    }
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }
}