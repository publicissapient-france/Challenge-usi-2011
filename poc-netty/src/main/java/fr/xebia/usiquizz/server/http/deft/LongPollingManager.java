package fr.xebia.usiquizz.server.http.deft;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.deftserver.web.AsyncCallback;

public class LongPollingManager {

    private Map<AsyncCallback, AsyncCallback> longPoolingResponse = new ConcurrentHashMap<AsyncCallback, AsyncCallback>();

    public void add(AsyncCallback asyncCallback) {
        longPoolingResponse.put(asyncCallback, asyncCallback);
    }

    public void sendAllResponse() {
        for (AsyncCallback ac : longPoolingResponse.keySet()) {
            ac.onCallback();
        }
    }

    public void clear() {
        longPoolingResponse.clear();
    }
}
