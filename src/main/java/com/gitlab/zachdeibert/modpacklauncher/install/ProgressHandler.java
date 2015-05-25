package com.gitlab.zachdeibert.modpacklauncher.install;

public interface ProgressHandler {
    public static final ProgressHandler NULL = new ProgressHandler() {
        private int steps;
        
        @Override
        public void stepForward() {}

        @Override
        public void finishEarly() {}

        @Override
        public void fail(final Throwable ex) {}

        @Override
        public int getSteps() {
            return steps;
        }

        @Override
        public void setSteps(int steps) {
            this.steps = steps;
        }
    };
    
    void stepForward();
    
    default void finishEarly() {
        for ( int i = getSteps(); i > 0; i-- ) {
            stepForward();
        }
    }
    
    default void fail(Throwable ex) {
        if ( ex instanceof RuntimeException ) {
            throw (RuntimeException) ex;
        } else {
            throw new RuntimeException(ex);
        }
    }
    
    int getSteps();
    
    void setSteps(int steps);
    
    default void addSteps(int steps) {
        setSteps(getSteps() + steps);
    }
}
