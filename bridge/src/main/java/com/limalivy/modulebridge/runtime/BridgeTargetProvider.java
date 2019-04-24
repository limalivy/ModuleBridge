package com.limalivy.modulebridge.runtime;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author linmin1 on 2019/4/19.
 */
public class BridgeTargetProvider {

    private static final ConcurrentHashMap<Class<? extends IBridge>, IBridge> sMap = new ConcurrentHashMap<>();
    private static final String PROXY_SUFFIX = "$BridgeProxy";

    private BridgeTargetProvider() {

    }

    public static <T extends IBridge> T getTarget(Class<T> bridge) {
        T target = (T) sMap.get(bridge);
        if (target == null) {
            Class proxyClazz;
            synchronized (bridge) {
                try {
                    proxyClazz = Class.forName(bridge.getCanonicalName() + PROXY_SUFFIX);
                } catch (Exception e) {
                    proxyClazz = null;
                }
            }
            if (proxyClazz != null) {
                try {
                    target = (T) proxyClazz.newInstance();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            sMap.put(bridge, target);
        }
        return target;
    }
}
