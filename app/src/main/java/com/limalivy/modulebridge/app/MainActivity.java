package com.limalivy.modulebridge.app;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.limalivy.modulebridge.runtime.BridgeTargetProvider;
import com.limalivy.test.ITest1;
import com.limalivy.test.ITest3;

import java.io.IOException;
import java.sql.SQLClientInfoException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new Thread(new Runnable() {
            @Override
            public void run() {
                BridgeTargetProvider.getTarget(ITest3.class).test22();
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                BridgeTargetProvider.getTarget(ITest1.class).test1();
            }
        }).start();

        // try {
        //     BridgeTargetProvider.getTarget(ITest3.class).test333("",1);
        // } catch (IOException e) {
        //     e.printStackTrace();
        // } catch (SQLClientInfoException e) {
        //     e.printStackTrace();
        // }
    }
}
