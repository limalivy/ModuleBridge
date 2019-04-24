package com.limalivy.test;

import android.util.Log;

import com.limalivy.modulebridge.runtime.BridgeTarget;
import com.limalivy.modulebridge.runtime.BridgeTargetProvider;

/**
 * @author linmin1 on 2019/4/19.
 */
@BridgeTarget(value = ITest1.class)
public class Test1Impl implements ITest1{



    @Override
    public void onCreate() {
        try {
            Thread.sleep(1200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ITest3 mITest3 = BridgeTargetProvider.getTarget(ITest3.class);
        Log.e("tagxx", "onCreate1");
    }

    @Override
    public void test1() {
        Log.e("tagxx", "test1");
    }

    @Override
    public int test2() {
        return 0;
    }
}
