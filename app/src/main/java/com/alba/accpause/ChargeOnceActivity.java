package com.alba.accpause;

import android.os.Bundle;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.slider.Slider;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.alba.accpause.databinding.ActivityChargeOnceBinding;

import java.io.IOException;

public class ChargeOnceActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityChargeOnceBinding binding;
    private Button chargeOnceBackButton;
    private Slider chargeToSlider;
    private Slider chargeToCurrentSlider;
    private String chargeToValue;
    private float chargeToCurrentValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityChargeOnceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        MaterialToolbar toolbar = findViewById(R.id.chargeOnceNav);
        toolbar.setTitle(R.string.charge_once_label);

        chargeOnceBackButton = findViewById(R.id.chargeOnceBackButton);

        chargeToSlider = findViewById(R.id.chargeToSlider);
        chargeToCurrentSlider = findViewById(R.id.chargeToCurrentSlider);

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                chargeToValue = Float.toString(chargeToSlider.getValue());
                chargeToCurrentValue = chargeToCurrentSlider.getValue();

                final String finalChargeToValue = chargeToValue.substring(0, chargeToValue.length()-2);


                try {
                    if (chargeToCurrentValue == 0) // current unlimited
                        Runtime.getRuntime().exec("su -c /dev/acca -f "+finalChargeToValue);
                    else
                        Runtime.getRuntime().exec("su -c /dev/acca -f "+chargeToValue+" "+chargeToCurrentValue);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                Snackbar.make(view, finalChargeToValue, Snackbar.LENGTH_LONG)
                        .setAnchorView(R.id.fab)
                        .setAction("Action", null).show();
                Toast.makeText(view.getContext(), finalChargeToValue, Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        chargeOnceBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.chargeOnceNav);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}