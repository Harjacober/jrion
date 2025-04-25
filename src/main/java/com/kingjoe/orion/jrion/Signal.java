package com.kingjoe.orion.jrion;

public abstract class Signal extends RuntimeException {

    protected Signal() {
        // exception is used for control flow and not error handling, hence no need for overhead like stack traces
        super(null, null, false, false);
    }

    static class Break extends Signal {
    }

    static class Continue extends Signal{}

    static class Return extends Signal {
        final Object value;
        public Return(Object value) {
            super();
            this.value = value;
        }
    }

}
