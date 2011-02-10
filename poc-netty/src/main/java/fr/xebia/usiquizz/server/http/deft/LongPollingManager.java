package fr.xebia.usiquizz.server.http.deft;


import org.deftserver.web.AsyncCallback;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
}
