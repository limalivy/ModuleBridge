package com.limalivy.modulebridge.compiler;

/**
 * @author linmin1 on 2019/4/22.
 */
public class BridgeCompilperException extends RuntimeException {
    public BridgeCompilperException() {
        super("Bridge Compiler fail,BridgeTarget values element only interface");
    }
}
