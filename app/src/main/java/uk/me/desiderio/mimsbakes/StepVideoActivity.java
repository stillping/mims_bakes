package uk.me.desiderio.mimsbakes;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import uk.me.desiderio.mimsbakes.data.model.Step;


public class StepVideoActivity extends AppCompatActivity {

    public static final String EXTRA_STEP = "extra_step";

    private StepVideoFragment videoFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_video);

        Intent intent = getIntent();

        boolean isDualPane = getResources().getBoolean(R.bool.has_two_panes);
        if(isDualPane) {
            setResult(Activity.RESULT_OK, intent);
            finish();
        }

        Step step = intent.getParcelableExtra(EXTRA_STEP);

        videoFragment = (StepVideoFragment) getSupportFragmentManager()
                .findFragmentById(R.id.step_video_fragment);

        videoFragment.swapData(step);
    }
}
