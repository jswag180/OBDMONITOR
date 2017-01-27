package swagyyydevelopment.jswag180.com.obdmonitor;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

public class gage extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gage);
        ImageView i = (ImageView) findViewById(R.id.IMVTEST);
        final ImageView j = (ImageView) findViewById(R.id.IMVNED);
        SeekBar SKB = (SeekBar) findViewById(R.id.SKB);
        //Toast.makeText(getApplicationContext(),Long.toString(Start),Toast.LENGTH_SHORT).show();

        SKB.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                j.animate().rotation(progress);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

                /*
                //long Start = System.currentTimeMillis();
                int rotation = 1;
                try {
                    while(true) {
                        //if (System.currentTimeMillis() >= Start + 2000) {

                            if (rotation == 360) {
                                rotation = 1;
                                break;
                            } else {
                                rotation = rotation + 1;
                                //Toast.makeText(getApplicationContext(),Integer.toString(rotation),Toast.LENGTH_SHORT).show();
                                j.animate().rotation(rotation);
                            }
                            //Start = System.currentTimeMillis();
                        //}
                    }
                }catch (Exception e){
                    Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_LONG).show();
                }
                */






    }
}
