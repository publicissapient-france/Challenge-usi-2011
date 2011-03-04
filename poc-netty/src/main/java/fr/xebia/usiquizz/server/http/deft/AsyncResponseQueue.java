package fr.xebia.usiquizz.server.http.deft;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.deftserver.io.IOLoop;
import org.deftserver.io.timeout.Timeout;
import org.deftserver.web.AsyncCallback;
import org.deftserver.web.http.HttpResponse;

public class AsyncResponseQueue {

	private final LinkedBlockingQueue<HttpResponse> queue;
	private final AsyncCallback cb = new AsyncCallback() {

		@Override
		public void onCallback() {
			sendQueuedResponses();
		}
	};
	private Boolean planned;
	
	private final AtomicInteger ai = new AtomicInteger();

	public AsyncResponseQueue() {
		queue = new LinkedBlockingQueue<HttpResponse>();
		planned = false;
	}
	
	public void planify(){
		ai.incrementAndGet();
		if (!planned){
			addTimeout();
			planned = true;
		}
	}

	public void pushResponseToSend(HttpResponse response) {
		try {
			queue.put(response);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}

	private void sendQueuedResponses() {
		HttpResponse resp;
		while ((resp = queue.poll()) != null) {
			resp.finish();
			ai.decrementAndGet();
		}

		if (ai.intValue()== 0){
			planned = false;
		} else {
			addTimeout();
		}
		
	}

	private void addTimeout() {
		IOLoop.INSTANCE.addTimeout( new Timeout(10,cb));
	}
}
