package com.limalivy.test;

import java.io.IOException;
import java.sql.SQLClientInfoException;

/**
 * @author linmin1 on 2019/4/22.
 */
public interface ITest3 extends ITest2 {
    int a = 0;

    void test333(String var1, int var2, String... var3) throws IOException, SQLClientInfoException;
}
