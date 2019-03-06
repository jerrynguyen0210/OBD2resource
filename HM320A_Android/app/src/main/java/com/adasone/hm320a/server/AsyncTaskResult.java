package com.adasone.hm320a.server;


public class AsyncTaskResult<T> {
    private T result = null;
    private Exception error = null;

    public T getResult() {
        return result;
    }

    public Exception getError() {
        return error;
    }

    public AsyncTaskResult(T result) {
        this.result = result;
    }

    public AsyncTaskResult(Exception error) {
        this.error = error;
    }
}