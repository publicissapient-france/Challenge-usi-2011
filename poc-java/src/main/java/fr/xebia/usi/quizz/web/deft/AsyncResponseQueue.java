package fr.xebia.usi.quizz.web.deft;

import java.util.concurrent.LinkedBlockingQueue;

import org.deftserver.io.IOLoop;
import org.deftserver.io.timeout.Timeout;
import org.deftserver.web.AsyncCallback;
import org.deftserver.web.http.HttpResponse;

public class AsyncResponseQueue {

	private final LinkedBlockingQueue<HttpResponse> queue;

	public AsyncResponseQueue() {
		queue = new LinkedBlockingQueue<HttpResponse>();
		addTimeout();
	}

	public void pushResponseToSend(HttpResponse response) {
		try {
			queue.put(response);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void sendQueuedResponses() {
		HttpResponse resp;
		while ((resp = queue.poll()) != null) {
			resp.finish();
		}

		addTimeout();
	}

	private void addTimeout() {
		IOLoop.INSTANCE.addTimeout(new Timeout(10, new AsyncCallback() {

			@Override
			public void onCallback() {
				sendQueuedResponses();
			}
		}));
	}
}
