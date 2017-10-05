package swagyyydevelopment.jswag180.com.obdmonitor;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

public class diagnosticSettings extends Activity {

    ToggleButton tbOCA, tbFuleLVL;
    EditText txtOCA;
    SeekBar sbFuel;
    TextView tvFuel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diagnostic_settings);

        tbOCA = (ToggleButton) findViewById(R.id.tbOCA);
        tbFuleLVL = (ToggleButton) findViewById(R.id.tbFuleLVL);
        txtOCA = (EditText) findViewById(R.id.txtOCA);
        sbFuel = (SeekBar) findViewById(R.id.sbFuel);
        tvFuel = (TextView) findViewById(R.id.tvFuel);

        final SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPref.edit();

        tbOCA.setChecked(sharedPref.getBoolean("OilChangeAlert", false));
        if (sharedPref.getBoolean("OilChangeAlert", false)) {
            txtOCA.setVisibility(View.VISIBLE);
        } else {
            txtOCA.setVisibility(View.GONE);
        }
        tbFuleLVL.setChecked(sharedPref.getBoolean("FuleLVL", false));
        if (sharedPref.getBoolean("FuleLVL", false)) {
            sbFuel.setVisibility(View.VISIBLE);
            tvFuel.setVisibility(View.VISIBLE);
        } else {
            sbFuel.setVisibility(View.GONE);
            tvFuel.setVisibility(View.GONE);
        }
        sbFuel.setProgress(sharedPref.getInt("FuelAlertLVL", 0));

        tbOCA.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    txtOCA.setVisibility(View.VISIBLE);
                    editor.putBoolean("OilChangeAlert", isChecked);
                    editor.apply();
                } else {
                    editor.putBoolean("OilChangeAlert", isChecked);
                    editor.apply();
                    txtOCA.setVisibility(View.GONE);
                }
            }
        });

        tbFuleLVL.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    sbFuel.setProgress(sharedPref.getInt("FuelAlertLVL", 0));
                    sbFuel.setVisibility(View.VISIBLE);
                    tvFuel.setVisibility(View.VISIBLE);
                    editor.putBoolean("FuleLVL", isChecked);
                    editor.apply();
                } else {
                    editor.putBoolean("FuleLVL", isChecked);
                    editor.apply();
                    tvFuel.setVisibility(View.GONE);
                    sbFuel.setVisibility(View.GONE);
                }

            }
        });

        sbFuel.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                editor.putInt("FuelAlertLVL", sbFuel.getProgress());
                editor.apply();
            }
        });

    }
}
