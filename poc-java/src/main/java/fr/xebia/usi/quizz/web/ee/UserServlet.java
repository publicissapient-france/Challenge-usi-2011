package fr.xebia.usi.quizz.web.ee;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.servlet.AsyncContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.xebia.usi.quizz.model.User;
import fr.xebia.usi.quizz.service.JsonMapper;
import fr.xebia.usi.quizz.service.JsonMapperImpl;
import fr.xebia.usi.quizz.service.UserManager;
import fr.xebia.usi.quizz.service.UserManagerMongoImpl;

@WebServlet(name = "user-servlet", asyncSupported = true, urlPatterns = { "/api/user" })
public class UserServlet extends HttpServlet {

	private ExecutorService executor;

	private static final Logger LOG = LoggerFactory.getLogger("api/user");
	private JsonMapper mapper;
	private UserManager manager;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		mapper = new JsonMapperImpl();
		manager = new UserManagerMongoImpl();
		executor = Executors.newFixedThreadPool(10);
		LOG.info("Instance loading of the UserService");
	}
	
	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		super.destroy();
		executor.shutdownNow();
		
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		this.doPost(req, resp);

	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		if (null != req.getAttribute("error")) {

			if ((Boolean) req.getAttribute("error")) {
				resp.sendError(400, "Bad User Request");
			}
		} else if (!req.isAsyncStarted()) {

			AsyncContext ctx = req.startAsync(req, resp);
			ctx.setTimeout(Long.MAX_VALUE);
			// Parse JSON
			User user = null;
			try {
				user = mapper.mapJsonUser(ctx.getRequest()
						.getInputStream());
			} catch (IOException e1) {
				LOG.error("Failed to write success response", e1);
			}
			executor.execute(new AsyncUserSave(manager, mapper, ctx, user) {

				@Override
				public void run() {


					if (usr != null && usr.isValidForSave()) {
						if (manager.getUser(usr.getMail()) == null) {
							manager.save(usr);
							try {

								ctx.getResponse().getWriter().print(
										"OK User saved :)");
							} catch (IOException e) {
								LOG
										.error(
												"Failed to write success response",
												e);
							}
							ctx.getRequest().setAttribute("error",
									Boolean.FALSE);
							ctx.complete();
						} else {
							ctx.getRequest()
									.setAttribute("error", Boolean.TRUE);
							ctx.dispatch();
						}

					} else {
						ctx.getRequest().setAttribute("error", Boolean.TRUE);
						ctx.dispatch();
					}

				}
			});
		}

	}

	abstract class AsyncUserSave implements Runnable {

		final UserManager manager;
		final JsonMapper jsonMapper;
		final AsyncContext ctx;
		final User usr;

		public AsyncUserSave(UserManager manager, JsonMapper jsonMapper,
				AsyncContext ctx, User usr) {
			this.manager = manager;
			this.jsonMapper = jsonMapper;
			this.ctx = ctx;
			this.usr = usr;
		}

	}

}
