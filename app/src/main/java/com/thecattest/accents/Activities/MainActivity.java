package com.thecattest.accents.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.tabs.TabLayout;
import com.thecattest.accents.BuildConfig;
import com.thecattest.accents.Data.ApiService;
import com.thecattest.accents.Data.Category;
import com.thecattest.accents.Data.Dictionary;
import com.thecattest.accents.Managers.JSONManager;
import com.thecattest.accents.Managers.TasksManager;
import com.thecattest.accents.R;

import java.io.IOException;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    public static final String SHARED_PREF_KEY = "ACCENTS_SHARED_PREF";
    public static final String THEME = "THEME";
    public static final String SCALE = "SCALE";
    public static final int THEME_DARK = 1;
    public static final int THEME_LIGHT = 2;
    public static final String CATEGORY_TITLE = "CATEGORY_TITLE";

    private long lastTimeBackPressed = 0;

    private LinearLayout wordPlaceholder;
    private TextView commentPlaceholder;
    private ConstraintLayout root;
    private TabLayout categoriesNavigation;
    private MaterialToolbar toolbar;
    private MaterialCardView updateAppCard;
    private Button updateButton;
    private ImageButton zoomIn;
    private ImageButton zoomOut;
    private ImageButton restore;
    private int scale;
    private boolean madeMistake;

    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;

    private Dictionary dictionary;
    private Category category;
    private JSONManager jsonManager;
    private Retrofit retrofit;
    private ApiService apiService;

    private final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        jsonManager = new JSONManager(this);
        try {
            dictionary = jsonManager.readObjectFromFile(Dictionary.FILENAME, new Dictionary());
        } catch (IOException e) {
//            Toast.makeText(this, R.string.ioexception, Toast.LENGTH_SHORT).show();
            dictionary = jsonManager.gson.fromJson(
                    jsonManager.filesManager.readFromRawResource(R.raw.dictionary),
                    Dictionary.class);
            jsonManager.writeObjectToFile(dictionary, Dictionary.FILENAME);
        }

        findViews();
        setListeners();
        initRetrofit();
        initCategoriesNavigation();
        sync();

        sharedPref  = getSharedPreferences(SHARED_PREF_KEY, Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        scale = sharedPref.getInt(SCALE, 0);
        int theme = sharedPref.getInt(THEME, THEME_LIGHT);
        if(theme == THEME_DARK) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            toolbar.getMenu().getItem(1).setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_light, null));
        }
        else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            toolbar.getMenu().getItem(1).setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_dark, null));
        }

        next();
    }

    private void initCategoriesNavigation() {
        ArrayList<String> titles = dictionary.getCategoriesTitles();
        categoriesNavigation.removeAllTabs();
        for (String categoryTitle : titles)
            categoriesNavigation.addTab(categoriesNavigation.newTab().setText(categoryTitle));
        category = dictionary.categories.get(0);
    }

    private void initRetrofit() {
        retrofit = new Retrofit.Builder()
                .baseUrl(Dictionary.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);
    }

    public void findViews() {
        wordPlaceholder = findViewById(R.id.task_placeholder);
        commentPlaceholder = findViewById(R.id.extra);
        root = findViewById(R.id.root);
        categoriesNavigation = findViewById(R.id.categories_navigation);
        toolbar = findViewById(R.id.top_app_bar);
        updateAppCard = findViewById(R.id.update_app_card);
        updateButton = findViewById(R.id.update_button);
        zoomIn = findViewById(R.id.zoom_in);
        zoomOut = findViewById(R.id.zoom_out);
        restore = findViewById(R.id.zoom_restore);
    }

    public void setListeners() {
        findViewById(R.id.author).setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.author_url)));
            startActivity(browserIntent);
        });
        findViewById(R.id.dictionary).setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.dictionary_url)));
            startActivity(browserIntent);
        });
        updateButton.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.update_app_url)));
            startActivity(browserIntent);
        });

        zoomIn.setOnClickListener(v -> updateScale(+2));
        zoomOut.setOnClickListener(v -> updateScale(-2));
        restore.setOnClickListener(v -> updateScale(-scale/2));

        toolbar.setOnMenuItemClickListener(this::onMenuItemClick);

        categoriesNavigation.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String tabText = (String) tab.getText();
                category = dictionary.getCategory(tabText);
                next();
            }
            public void onTabUnselected(TabLayout.Tab tab) { }
            public void onTabReselected(TabLayout.Tab tab) { }
        });
    }

    public void updateScale(int delta) {
        scale += delta * 2;
        editor.putInt(SCALE, scale);
        editor.apply();
        zoomIn.setEnabled(scale <= 12);
        zoomOut.setEnabled(scale >= -8);
        next();
    }

    @Override
    public void onBackPressed() {
        long currentTime = System.currentTimeMillis() / 1000L;
        if (currentTime - lastTimeBackPressed <= 3) {
            super.onBackPressed();
        } else {
            lastTimeBackPressed = currentTime;
            Toast.makeText(this, R.string.back_again_to_exit, Toast.LENGTH_SHORT).show();
        }
    }

    private void next() {
        String task;
        try {
            task = category.next(jsonManager);
        } catch (IOException e) {
            Toast.makeText(this, R.string.ioexception, Toast.LENGTH_LONG).show();
            return;
        }
        
        commentPlaceholder.setText(TasksManager.getComment(task, category.getTaskType()));
        commentPlaceholder.setTextSize(TypedValue.COMPLEX_UNIT_SP, 26 + scale);
        wordPlaceholder.removeAllViews();
        madeMistake = false;

        for (TextView tv : TasksManager.getTextViews(this, task, category.getTaskType(), scale))
            wordPlaceholder.addView(tv, layoutParams);
    }

    private void animateBackground(int colorId) {
        TransitionDrawable transition = (TransitionDrawable) ResourcesCompat.getDrawable(getResources(), colorId, null);
        if (transition != null) {
            root.setBackground(transition);
            transition.startTransition(200);
        }
    }

    private void vibrate(int vibrationDuration) {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(vibrationDuration, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            v.vibrate(vibrationDuration);
        }
    }

    private boolean onMenuItemClick(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.refresh) {
            sync();
            return true;
        }
        if (itemId == R.id.theme) {
            if(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                editor.putInt(THEME, THEME_LIGHT);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                editor.putInt(THEME, THEME_DARK);
            }
            editor.apply();
        }
        if (itemId == R.id.words) {
            Intent i = new Intent(MainActivity.this, WordsListActivity.class);
            i.putExtra(CATEGORY_TITLE, category.getTitle());
            startActivity(i);
            return true;
        }
        if (itemId == R.id.shuffle_queue) {
            category.shuffleQueue(jsonManager);
            Toast.makeText(this, R.string.queue_shuffled, Toast.LENGTH_SHORT).show();
            next();
        }
        if (itemId == R.id.forget_mistakes) {
            category.forgetMistakes(jsonManager);
            Toast.makeText(this, R.string.mistakes_forgotten, Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    public void sync() {
        Call<Dictionary> call = apiService.getDictionary();
        call.enqueue(new Callback<Dictionary>() {
            @Override
            public void onResponse(Call<Dictionary> call, Response<Dictionary> response) {
                dictionary = response.body();
                try {
                    dictionary.sync(jsonManager);
                    initCategoriesNavigation();
                    next();
                    if (BuildConfig.VERSION_CODE != dictionary.version)
                        updateAppCard.setVisibility(View.VISIBLE);
                    else
                        updateAppCard.setVisibility(View.INVISIBLE);
                    Toast.makeText(MainActivity.this, R.string.synced, Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    Toast.makeText(MainActivity.this, R.string.ioexception, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Dictionary> call, Throwable t) {
                Toast.makeText(MainActivity.this, R.string.sync_request_error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public View.OnClickListener getTextViewClickListener(String task, boolean correct) {
        return view -> {
            //animateBackground(Character.isUpperCase(c) ? R.drawable.correct : R.drawable.incorrect);
            vibrate(correct ? 15 : 200);
            if (correct) {
                try {
                    category.saveAnswer(task, madeMistake, jsonManager);
                } catch (IOException e) {
                    Toast.makeText(this, R.string.ioexception, Toast.LENGTH_LONG).show();
                }
                next();
            }
            else
                madeMistake = true;
        };
    }

}