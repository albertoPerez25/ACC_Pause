package com.alba.accpause;

import android.content.Context;
import android.os.Bundle;
import android.os.CombinedVibration;
import android.os.Handler;
import android.os.Looper;
import android.os.VibratorManager;
import android.view.MenuItem;
import android.os.VibrationEffect;

import com.alba.accpause.databinding.LayoutBottomNavigationBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.alba.accpause.databinding.ActivityMainBinding;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding activityBinding;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final VibratorManager vibrator = (VibratorManager) getApplicationContext().getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
        final VibrationEffect click = VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK);

        activityBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(activityBinding.getRoot());
        DynamicColors.applyToActivityIfAvailable(this);
        final int animDuration = 200;

        // Get a reference to the BottomNavigationView
        android.view.View includedNavRootView = activityBinding.includedBottomNavView.getRoot();
        LayoutBottomNavigationBinding bottomNavBinding = LayoutBottomNavigationBinding.bind(includedNavRootView);
        bottomNavigationView = bottomNavBinding.bottomNavigationView;

        // Set the item selected listener
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment;
                CombinedVibration combinedClick = CombinedVibration.createParallel(click);
                int enterAnimation;
                int exitAnimation;

                int itemId = item.getItemId();
                if (itemId == R.id.item_1) {
                    vibrator.vibrate(combinedClick);
                    selectedFragment = new HomeFragment();
                    enterAnimation = R.anim.fade_in;
                    exitAnimation = R.anim.fade_out;
                } else if (itemId == R.id.item_2) {
                    vibrator.vibrate(combinedClick);
                    selectedFragment = new ConfigsFragment();
                    enterAnimation = R.anim.fade_in;
                    exitAnimation = R.anim.fade_out;

                } else {
                    return false;
                }

                final Fragment finalSelectedFragment = selectedFragment;
                final int finalEnterAnimation = enterAnimation;

                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(0, exitAnimation)
                        .replace(R.id.fragment_container, new BlankFragment())
                        .commit();
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    // Start a new transaction to replace the blank fragment with the selected fragment
                    FragmentTransaction newTransaction = getSupportFragmentManager().beginTransaction();
                    newTransaction.setCustomAnimations(finalEnterAnimation, 0); // Only fade_zoom_in for the new fragment
                    newTransaction.replace(R.id.fragment_container, finalSelectedFragment);
                    newTransaction.commit();
                }, animDuration);
                return true;
            }
        });
        // Show the HomeFragment by default
        replaceFragment(new HomeFragment());
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }
}