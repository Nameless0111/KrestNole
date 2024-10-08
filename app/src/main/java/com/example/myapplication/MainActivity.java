package com.example.myapplication;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import java.util.Random;

public class MainActivity extends AppCompatActivity {
    SharedPreferences themeSettings;
    SharedPreferences.Editor settingsEditor;
    ImageButton imageTheme;

    private Button[][] buttons = new Button[3][3];
    private boolean playerXTurn = true;
    private int xWins = 0, oWins = 0, draws = 0;
    private boolean playingWithBot = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        themeSettings = getSharedPreferences("SETTINGS", MODE_PRIVATE);

        if (!themeSettings.contains("MODE_NIGHT_ON")) {
            settingsEditor = themeSettings.edit();
            settingsEditor.putBoolean("MODE_NIGHT_ON", false);
            settingsEditor.apply();
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            Toast.makeText(this, "Темная тема отключена", Toast.LENGTH_SHORT).show();
        } else {
            setCurrentTheme();
        }

        setContentView(R.layout.activity_main);
        imageTheme = findViewById(R.id.Ing);
        updateImageButton();

        setupGameBoard();
        setupButtons();

        imageTheme.setOnClickListener(view -> toggleTheme());
    }

    private void setupGameBoard() {
        GridLayout gridLayout = findViewById(R.id.gridLayout);

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                String buttonID = "button" + i + j;
                int resID = getResources().getIdentifier(buttonID, "id", getPackageName());
                buttons[i][j] = findViewById(resID);
                buttons[i][j].setOnClickListener(new ButtonClickListener(i, j));
            }
        }
    }

    private void setupButtons() {
        Button resetButton = findViewById(R.id.resetButton);
        resetButton.setOnClickListener(v -> resetGame());

        Button statisticsButton = findViewById(R.id.statisticsButton);
        statisticsButton.setOnClickListener(v -> showStatistics());

        Button playWithBotButton = findViewById(R.id.playWithBotButton);
        playWithBotButton.setOnClickListener(v -> startGameWithBot());
    }

    private void toggleTheme() {
        boolean isNightMode = themeSettings.getBoolean("MODE_NIGHT_ON", false);
        settingsEditor = themeSettings.edit();

        if (isNightMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            settingsEditor.putBoolean("MODE_NIGHT_ON", false);
            Toast.makeText(MainActivity.this, "Темная тема отключена", Toast.LENGTH_SHORT).show();
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            settingsEditor.putBoolean("MODE_NIGHT_ON", true);
            Toast.makeText(MainActivity.this, "Темная тема включена", Toast.LENGTH_SHORT).show();
        }

        settingsEditor.apply();
        updateImageButton();
    }

    private void updateImageButton() {
        if (themeSettings.getBoolean("MODE_NIGHT_ON", false)) {
            imageTheme.setImageResource(R.drawable.icon);
        } else {
            imageTheme.setImageResource(R.drawable.imgg);
        }
    }

    private void setCurrentTheme() {
        if (themeSettings.getBoolean("MODE_NIGHT_ON", false)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    private class ButtonClickListener implements View.OnClickListener {
        int row, col;

        public ButtonClickListener(int row, int col) {
            this.row = row;
            this.col = col;
        }

        @Override
        public void onClick(View v) {
            if (buttons[row][col].getText().toString().equals("")) {
                buttons[row][col].setText(playerXTurn ? "X" : "O");
                buttons[row][col].setEnabled(false);

                if (checkForWinner()) {
                    String winner = playerXTurn ? "X" : "O";
                    Toast.makeText(MainActivity.this, winner + " победил!", Toast.LENGTH_SHORT).show();
                    updateStatistics(winner);
                    resetGame();
                    playerXTurn = false;
                    if(winner == "X" && playingWithBot){playerXTurn = false;}

                } else if (isBoardFull()) {
                    Toast.makeText(MainActivity.this, "Ничья!", Toast.LENGTH_SHORT).show();
                    updateStatistics("");
                    resetGame();
                    playerXTurn = false;
                    if(playingWithBot){playerXTurn = false;}
                }

                playerXTurn = !playerXTurn;

                if (playingWithBot && !playerXTurn) {
                    botMove();
                }
            }
        }
    }

    private boolean checkForWinner() {
        for (int i = 0; i < 3; i++) {
            if ((buttons[i][0].getText().equals(buttons[i][1].getText()) &&
                    buttons[i][1].getText().equals(buttons[i][2].getText()) &&
                    !buttons[i][0].getText().toString().equals("")) ||
                    (buttons[0][i].getText().equals(buttons[1][i].getText()) &&
                            buttons[1][i].getText().equals(buttons[2][i].getText()) &&
                            !buttons[0][i].getText().toString().equals(""))) {
                return true;
            }
        }
        return (buttons[0][0].getText().equals(buttons[1][1].getText()) &&
                buttons[1][1].getText().equals(buttons[2][2].getText()) &&
                !buttons[0][0].getText().toString().equals("")) ||
                (buttons[0][2].getText().equals(buttons[1][1].getText()) &&
                        buttons[1][1].getText().equals(buttons[2][0].getText()) &&
                        !buttons[0][2].getText().toString().equals(""));
    }

    private boolean isBoardFull() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (buttons[i][j].getText().toString().equals("")) return false;
            }
        }
        return true;
    }

    private void updateStatistics(String winner) {
        if (winner.equals("X")) xWins++;
        else if (winner.equals("O"))oWins++;
        else draws++;

        SharedPreferences.Editor editor = themeSettings.edit();
        editor.putInt("XWins", xWins);
        editor.putInt("OWins", oWins);
        editor.putInt("Draws", draws);
        editor.apply();
    }

    private void resetGame() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                buttons[i][j].setText("");
                buttons[i][j].setEnabled(true);
            }
        }
        playerXTurn = true;
    }

    private void showStatistics() {
        xWins = themeSettings.getInt("XWins", 0);
        oWins = themeSettings.getInt("OWins", 0);
        draws = themeSettings.getInt("Draws", 0);

        String statsMessage = "Победы X: " + xWins + "\nПобеды O: " + oWins + "\nНичьи: " + draws;
        new AlertDialog.Builder(this)
                .setTitle("Статистика")
                .setMessage(statsMessage)
                .setPositiveButton("OK", null)
                .show();
    }

    private void startGameWithBot() {
        playingWithBot = true;
        playerXTurn = true;
        resetGame();
    }

    private void botMove() {
        Random random = new Random();
        int row, col;
        do {
            row = random.nextInt(3);
            col = random.nextInt(3);
        } while (!buttons[row][col].getText().toString().equals(""));

        buttons[row][col].setText("O");
        buttons[row][col].setEnabled(false);

        if (checkForWinner()) {
            Toast.makeText(this, "O победил!", Toast.LENGTH_SHORT).show();
            updateStatistics("O");
            resetGame();
        } else if (isBoardFull()) {
            Toast.makeText(this, "Ничья!", Toast.LENGTH_SHORT).show();
            draws++;
            resetGame();
        }

        playerXTurn = true;
    }
}