package com.example.mobilehw1_nivshalom;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;

import android.os.Handler;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final Handler game_clock = new Handler();
    private static final Random rand = new Random();

    private static final int grid_width = 3;
    private static final int grid_height = 5;
    private static int lives = 3;

    private final ImageView[][] grid = new ImageView[grid_width][grid_height];

    private int[] hunter_pos;
    private int[] wolf_pos;

    private int hunter_dir;
    private int score;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LinearLayout grid_main_frame = findViewById(R.id.grid_main_frame);
        grid_main_frame.post(() -> {
            CreateGrid(grid_main_frame, MainActivity.this);
            InitializeGame();
            SetupControls();
            StartGameClock(1000);
        });
    }

    // Game setup

    private void CreateGrid(LinearLayout grid_main_frame, Context context)
    {
        // Parameters for grid cell size
        LinearLayout.LayoutParams cell_params = new LinearLayout.LayoutParams(
            grid_main_frame.getWidth() / grid_width,
            grid_main_frame.getHeight() / grid_height
        );

        // Draw the grid
        for (int j = 0; j < grid_height; j++)
        {
            LinearLayout row = new LinearLayout(context);
            row.setOrientation(LinearLayout.HORIZONTAL);

            for (int i = 0; i < grid_width; i++)
            {
                this.grid[i][j] = new ImageView(context);

                this.grid[i][j].setBackgroundResource(R.drawable.imageview_border);
                this.grid[i][j].setLayoutParams(cell_params);

                row.addView(this.grid[i][j]);
            }

            grid_main_frame.addView(row);
        }
    }

    private void InitializeGame()
    {
        // Internal variables setup
        hunter_dir = rand.nextInt(4);
        score = 0;

        // Choose random positions for hunter and wolf
        int mid_width = grid_width / 2;

        hunter_pos = new int[]
        {
                rand.nextInt(mid_width),
                rand.nextInt(grid_height)
        };

        wolf_pos = new int[]
        {
            mid_width + rand.nextInt(mid_width),
            rand.nextInt(grid_height)
        };

        this.grid[hunter_pos[0]][hunter_pos[1]].setImageResource(R.drawable.caveman);
        this.grid[wolf_pos[0]][wolf_pos[1]].setImageResource(R.drawable.wolf);
    }

    private void SetupControls()
    {
        Button up = findViewById(R.id.up_btn);
        Button down = findViewById(R.id.down_btn);
        Button right = findViewById(R.id.right_btn);
        Button left = findViewById(R.id.left_btn);

        up.setOnClickListener(view -> hunter_dir = 2);
        down.setOnClickListener(view -> hunter_dir = 0);
        right.setOnClickListener(view -> hunter_dir = 1);
        left.setOnClickListener(view -> hunter_dir = 3);
    }

    private void StartGameClock(long tickDelay)
    {
        TextView score_text = findViewById(R.id.score_text);
        TextView lives_text = findViewById(R.id.lives_text);

        Resources res = getResources();
        String score_label = res.getString(R.string.score_text_label);
        String lives_label = res.getString(R.string.lives_text_label);

        score_text.setText(String.format(score_label, score));
        lives_text.setText(String.format(lives_label, lives));

        game_clock.postDelayed(new Runnable() {
            public void run() {
                // Check if wolf got hunter
                CheckForGameOver();

                // Move hunter using player input
                int x_step = hunter_dir % 2;
                int y_step = 1 - (hunter_dir % 2);

                if (hunter_dir > 1)
                    MoveCharacter(hunter_pos, -x_step, -y_step, R.drawable.caveman);
                else MoveCharacter(hunter_pos, x_step, y_step, R.drawable.caveman);

                // Compute distance between hunter and wolf
                x_step = hunter_pos[0] - wolf_pos[0];
                y_step = hunter_pos[1] - wolf_pos[1];

                // Chunk if hunter ran into wolf
                CheckForGameOver();

                // Randomly choose axis of movement
                boolean axis = rand.nextBoolean();
                if (x_step == 0)
                    axis = false;
                else if (y_step == 0)
                    axis = true;

                // Move the wolf
                if (axis)
                    MoveCharacter(wolf_pos, x_step > 0 ? 1 : -1, 0, R.drawable.wolf);
                else
                    MoveCharacter(wolf_pos, 0, y_step > 0 ? 1 : -1, R.drawable.wolf);

                score_text.setText(String.format(score_label, score++));
                lives_text.setText(String.format(lives_label, lives));
                game_clock.postDelayed(this, tickDelay);
            }
        }, tickDelay);
    }

    // Utility

    private void MoveCharacter(int[] pos, int x_move, int y_move, int image_drawable)
    {
        int new_x = pos[0] + x_move;
        int new_y = pos[1] + y_move;

        if (new_x < 0 || new_x >= grid_width || new_y < 0 || new_y >= grid_height)
            return;

        this.grid[pos[0]][pos[1]].setImageResource(0);

        pos[0] = new_x;
        pos[1] = new_y;

        this.grid[pos[0]][pos[1]].setImageResource(image_drawable);
    }

    private void CheckForGameOver()
    {
        if (hunter_pos[0] == wolf_pos[0] && hunter_pos[1] == wolf_pos[1])
        {
            if (lives-- == 1)
            {
                finish();
                System.exit(0);
            }

            grid[wolf_pos[0]][wolf_pos[1]].setImageResource(0);
            InitializeGame();
        }
    }

}