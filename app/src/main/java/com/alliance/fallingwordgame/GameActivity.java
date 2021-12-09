package com.alliance.fallingwordgame;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.alliance.fallingwordgame.game.GameAnimatorListener;
import com.alliance.fallingwordgame.game.GameEngine;
import com.alliance.fallingwordgame.model.Dictionary;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class GameActivity extends AppCompatActivity {
    private static String TAG = "GAME_ACTIVITY";

    private Dictionary dictionary;
    private TextView textWord;
    private TextView textTranslation;
    private ImageView topBorder;
    private ImageView bottomBorder;
    private View container;
    private Button buttonRight;
    private Button buttonWrong;
    private TextView textCorrect;
    private TextView textWrong;
    private TextView textNoAnswer;
    private GameEngine gameEngine;
    private GameAnimatorListener gameAnimatorListener;
    private int duration;
    private int bottomCoordinate;
    private ObjectAnimator objectAnimator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        initObjects();
        loadWords();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setOnClickListeners();
        container.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                container.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int[] locations1 = new int[2];
                topBorder.getLocationOnScreen(locations1);
                int y1 = locations1[1];
                int[] locations2 = new int[2];
                bottomBorder.getLocationOnScreen(locations2);
                int y2 = locations2[1];
                bottomCoordinate = y2 - bottomBorder.getHeight() - textTranslation.getHeight() - y1;
                startAnimation(duration);
            }
        });
    }

    private String readJsonFromFile() throws IOException {
        StringBuilder fileData = new StringBuilder();
        Resources res = getResources();
        InputStream inputStream = res.openRawResource(R.raw.words);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line = reader.readLine();
        while (line != null) {
            fileData.append(line);
            line = reader.readLine();
        }
        return fileData.toString();
    }

    public void initObjects() {
        textTranslation = (TextView) findViewById(R.id.text_translation);
        textWord = (TextView) findViewById(R.id.text_word);
        buttonRight = (Button) findViewById(R.id.btn_correct);
        buttonWrong = (Button) findViewById(R.id.btn_wrong);
        textCorrect = (TextView) findViewById(R.id.text_correct);
        textWrong = (TextView) findViewById(R.id.text_wrong);
        textNoAnswer = (TextView) findViewById(R.id.text_noanswer);
        topBorder = (ImageView) findViewById(R.id.top_border);
        bottomBorder = (ImageView) findViewById(R.id.bottom_border);
        container = findViewById(R.id.container);
        duration = 10000;
    }

    private void setOnClickListeners() {
        buttonRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gameEngine.isTranslationCorrect()) {
                    gameEngine.increaseCorrectCounter();
                    duration -= 50;
                    if (duration < 500) {
                        duration = 500;
                    }
                } else {
                    gameEngine.increaseWrongCounter();
                }
                RestartAnimation(duration);
            }
        });

        buttonWrong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!gameEngine.isTranslationCorrect()) {
                    gameEngine.increaseCorrectCounter();
                    duration -= 50;
                    if (duration < 500) {
                        duration = 500;
                    }
                } else {
                    gameEngine.increaseWrongCounter();
                }
                RestartAnimation(duration);
            }
        });
    }

    private void startAnimation(int duration) {
        gameEngine.setWord();
        textTranslation.clearAnimation();
        objectAnimator = ObjectAnimator.ofFloat(textTranslation, "translationY", 0f, bottomCoordinate);
        objectAnimator.setDuration(duration);
        objectAnimator.setRepeatCount(ValueAnimator.INFINITE);
        objectAnimator.setInterpolator(new AccelerateInterpolator());
        objectAnimator.addListener(gameAnimatorListener);
        objectAnimator.start();
    }

    private void RestartAnimation(int duration) {
        gameEngine.setWord();
        objectAnimator.setDuration(duration);
        objectAnimator.cancel();
        objectAnimator.start();
    }

    private void loadWords() {
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<Dictionary> jsonAdapter = moshi.adapter(Dictionary.class);

        try {
            String json = readJsonFromFile();
            dictionary = jsonAdapter.fromJson(json);
            gameEngine = new GameEngine(dictionary, textWord, textTranslation, textCorrect, textWrong, textNoAnswer);
            gameAnimatorListener = new GameAnimatorListener(gameEngine);
        } catch (IOException e) {
            Log.d(TAG, "Error reading json file data");
        }
    }
}