/*
 * Team 4
 * Task 13
 * Date: May 214, 2015
 * Only for educational use
 */
package controller;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import databeans.UserBean;
import model.Model;

public class Controller extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public void init() throws ServletException {
		Model model = new Model(getServletConfig());
		Action.add(new RegisterAction(model));
		Action.add(new TripPlanAction(model));
		Action.add(new PrivacyAction(model));
		Action.add(new GetTimeAction(model));
		Action.add(new LoginAction(model));
		Action.add(new LogoutAction(model));
		Action.add(new ChangePswAction(model));
		Action.add(new SetAddr(model));
		Action.add(new CheckFareAction(model));
		Action.add(new ManageAction(model));
		Action.add(new ExploreAction(model));
		Action.add(new TakeHomeAction(model));
		Action.add(new TakeWorkAction(model));
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String nextPage = performTheAction(request);
		sendToNextPage(nextPage, request, response);
	}

	/*
	 * Extracts the requested action and (depending on whether the user is
	 * logged in) perform it (or make the user login).
	 * 
	 * @param request
	 * 
	 * @return the next page (the view)
	 */
	private String performTheAction(HttpServletRequest request) {
		HttpSession session = request.getSession(true);
		String servletPath = request.getServletPath();
		UserBean user = (UserBean) session.getAttribute("user");
		String action = getActionName(servletPath);
		if (user != null) System.out.print(user.getFirstName());
		System.out.println("Action:" + action);
		
		if (session.getAttribute("policy") == null) {
			session.setAttribute("policy", "done");
			return "PrivacyAction.do";
		}
		
		if (action.equals("welcome")) {
			// User is logged in, but at the root of our web app
			return Action.perform("manage.do", request);
		}

		// Let the logged in user run his chosen action
		return Action.perform(action, request);
	}

	/*
	 * If nextPage is null, send back 404 If nextPage ends with ".do", redirect
	 * to this page. If nextPage ends with ".jsp", dispatch (forward) to the
	 * page (the view) This is the common case
	 */
	private void sendToNextPage(String nextPage, HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException {
		if (nextPage == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND,
					request.getServletPath());
			return;
		}

		if (nextPage.endsWith(".do")) {
			response.sendRedirect(nextPage);
			return;
		}

		if (nextPage.endsWith(".jsp")) {
			RequestDispatcher d = request.getRequestDispatcher("WEB-INF/"
					+ nextPage);
			d.forward(request, response);
			return;
		}

		if (!nextPage.isEmpty()) {
			if (nextPage.startsWith("http://")) {
				response.sendRedirect(nextPage);
			} else {
				response.sendRedirect("http://" + nextPage);
			}
			return;
		}

		throw new ServletException(Controller.class.getName()
				+ ".sendToNextPage(\"" + nextPage + "\"): invalid extension.");
	}

	/*
	 * Returns the path component after the last slash removing any "extension"
	 * if present.
	 */
	private String getActionName(String path) {
		// We're guaranteed that the path will start with a slash
		int slash = path.lastIndexOf('/');
		return path.substring(slash + 1);
	}
}
