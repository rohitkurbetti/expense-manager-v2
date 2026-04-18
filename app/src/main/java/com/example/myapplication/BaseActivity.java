package com.example.myapplication;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        applyUserTheme();
        applyUserFont();
        super.onCreate(savedInstanceState);
    }

    private void applyUserTheme() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String theme = prefs.getString("app_theme", "Theme.ExpenseUtility");

        switch (theme) {
            case "Default":
                setTheme(R.style.Base_Theme_MyApplication);
                break;
            case "Red":
                setTheme(R.style.AppTheme_Red);
                break;
            case "Blue":
                setTheme(R.style.AppTheme_Blue);
                break;
            case "Green":
                setTheme(R.style.AppTheme_Green);
                break;
            case "Purple":
                setTheme(R.style.AppTheme_Purple);
                break;
            case "Orange":
                setTheme(R.style.AppTheme_Orange);
                break;
            case "Teal":
                setTheme(R.style.AppTheme_Teal);
                break;
            case "Pink":
                setTheme(R.style.AppTheme_Pink);
                break;
            case "Cyan":
                setTheme(R.style.AppTheme_Cyan);
                break;
            case "Lime":
                setTheme(R.style.AppTheme_Lime);
                break;
            case "Brown":
                setTheme(R.style.AppTheme_Brown);
                break;
            case "Mint":
                setTheme(R.style.AppTheme_Mint);
                break;
            case "Coral":
                setTheme(R.style.AppTheme_Coral);
                break;
            case "Steel":
                setTheme(R.style.AppTheme_Steel);
                break;
            case "Lavender":
                setTheme(R.style.AppTheme_Lavender);
                break;
            case "Mustard":
                setTheme(R.style.AppTheme_Mustard);
                break;
            default:
                setTheme(R.style.Base_Theme_MyApplication);
                break;
        }
    }

    private void applyUserFont() {
        SharedPreferences prefs = getSharedPreferences(MainActivity.SHARED_PREFS_FILE, MODE_PRIVATE);
        String font = prefs.getString("app_font", "Poppins");

        switch (font) {
            case "Nunito":
                getTheme().applyStyle(R.style.Overlay_Font_Nunito, true);
                break;
            case "Poppins":
                getTheme().applyStyle(R.style.Overlay_Font_Poppins, true);
                break;
            case "Roboto":
                getTheme().applyStyle(R.style.Overlay_Font_Roboto, true);
                break;
            case "FontFamily":
                getTheme().applyStyle(R.style.Overlay_Font_FontFamily, true);
                break;
            case "Noticia":
                getTheme().applyStyle(R.style.Overlay_Font_Noticia, true);
                break;
            default:
                getTheme().applyStyle(R.style.Overlay_Font_Poppins, true);
                break;
        }
    }
}
