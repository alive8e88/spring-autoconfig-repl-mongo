package com.alivex.mongo.repl;

public class MongodConfigOption {

    private final boolean enableProcessOutput;

    public MongodConfigOption() {
        this.enableProcessOutput = false;
    }

    public MongodConfigOption(boolean enableProcessOutput) {
        this.enableProcessOutput = enableProcessOutput;
    }

    public boolean isEnableProcessOutput() {
        return enableProcessOutput;
    }
}
