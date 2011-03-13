package fr.xebia.usiquizz.injector;

import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.Response;

public abstract class AsyncAuditedCompletionHandler<T> extends AsyncCompletionHandler<T> {

    private String uniqueRequestIdentifier;
    private long requestExecutedAt;
    private long receiveResponseAt;

    public AsyncAuditedCompletionHandler(String uniqueRequestIdentifier, long requestNanoTime) {
        this.uniqueRequestIdentifier = uniqueRequestIdentifier;
        this.requestExecutedAt = requestNanoTime;
    }

    @Override
    public T onCompleted(Response response) throws Exception {
        this.receiveResponseAt = System.nanoTime();
        return onCompleted(response, uniqueRequestIdentifier);
    }


    public abstract T onCompleted(Response response, String uniqueRequestIdentifier) throws Exception;

    public long getRequestTimeInMillis() {
        return (receiveResponseAt - requestExecutedAt) / 1000000l;
    }
}
