package com.notcbw.usbgadgetconfig;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button btn;
    USBGadgetController ctr;
    String funcHandle = "";
    boolean enabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ctr = new USBGadgetController();

        btn = findViewById(R.id.button);
        btn.setText(R.string.button_enable);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!enabled) {
                    List<String> func = ctr.getAvailableFunctions();
                    String funcMS = null;
                    for (String s: func) {
                        if (s.contains("mass_storage"))
                            funcMS = s;
                    }
                    if (funcMS == null) return;

                    ctr.disableGadget();
                    boolean rtn = ctr.setMassStorageFile(funcMS, 0, "/dev/block/mmcblk1");
                    if (rtn) {
                        funcHandle = ctr.enableFunction(funcMS);
                        enabled = true;
                        btn.setText(R.string.button_disable);
                    }
                    ctr.enableGadget();
                } else {
                    ctr.disableGadget();
                    ctr.disableFunction(funcHandle);
                    ctr.enableGadget();
                    enabled = false;
                    btn.setText(R.string.button_enable);
                }


            }
        });
    }
}