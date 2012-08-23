package se.kth.webtex;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import se.kth.sys.util.ApplicationMonitor;

public class Monitor extends HttpServlet {
    private static final int TIMEOUT = 5;
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        ApplicationMonitor monitor = new ApplicationMonitor(TIMEOUT);

        monitor.addCheck("CACHE", new CacheMonitor());
        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");
        response.getOutputStream().print(monitor.createMonitorReport());
    }
}
