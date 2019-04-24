package com.limalivy.test;

import android.util.Log;

import com.limalivy.modulebridge.runtime.BridgeTarget;
import com.limalivy.modulebridge.runtime.BridgeTargetProvider;

import java.io.IOException;
import java.sql.SQLClientInfoException;

/**
 * @author linmin1 on 2019/4/19.
 */
@BridgeTarget({ITest3.class, ITest2.class})
public class Test2Impl implements ITest3 {

    private ITest1 mITest3 = BridgeTargetProvider.getTarget(ITest1.class);

    @Override
    public void test333(String var1, int var2, String... var3) throws IOException, SQLClientInfoException {
        Log.e("tagxx", "test333");
       // mITest3.test1();
    }

    @Override
    public void onCreate() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ITest1 mITest3 = BridgeTargetProvider.getTarget(ITest1.class);
        Log.e("tagxx", "onCreate3");
    }

    @Override
    public void test12() {

    }

    @Override
    public int test22() {
        Log.e("tagxx", "test22");
        return 0;
    }
}
