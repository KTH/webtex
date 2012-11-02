package se.kth.webtex;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import se.kth.sys.util.StringUtil;

/**
 * Servlet implementation. Does the parameter handling and response/request logic.
 */
public class WebTex extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final String PARAMETER_TEX = "tex";
    private static final String PARAMETER_RESOLUTION = "D";
    private static int EXPIRES_AFTER = 7*24*60*60;
    private static final Pattern INVALID_PATTERNS = 
            Pattern.compile(".*(\\\\def|\\\\input|\\\\output|\\\\read|\\\\write|\\\\openin|\\\\openout|\\\\catcode|\\\\let).*");

    private Cache cache;
    private TexRunner texRunner;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public WebTex() {
        super();
    }


    @Override
    public void init(ServletConfig config) {
        ServletContext context = config.getServletContext();
        String root = context.getRealPath("");
        cache = Cache.initCache(context, root);
        texRunner = new TexRunner(root);
    }

    
    @Override
    public void destroy() {
    	cache.destroy();
	}

    
    /**
     * @see HttpServlet#getLastModified(HttpServletRequest)
     */
    protected long getLastModified(HttpServletRequest request) {
        try {
            String expression = request.getParameter(PARAMETER_TEX);
            if (isValid(expression)) {
                int resolution = getResolution(request);
                createImage(expression, resolution);
                File file = cache.file(expression, resolution);
                return file.lastModified();
            } else {
                return -1;
            }
        } catch (Exception e) {
            return -1;
        }
    }


    /**
     * @see HttpServlet#doHead(HttpServletRequest, HttpServletResponse)
     */
    protected void doHead(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String expression = request.getParameter(PARAMETER_TEX);
        if (isValid(expression)) {
            int resolution = getResolution(request);
            createImage(expression, resolution);
            writeHeaders(response, expression, resolution);		
        } else {
            // Handle the case where there is no parameter.
            // Probably use some sort of default error image.
            System.out.println(request.getQueryString());
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }


    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String expression = request.getParameter(PARAMETER_TEX);
        if (isValid(expression)) {
            int resolution = getResolution(request);
            createImage(expression, resolution);
            writeHeaders(response, expression, resolution);
            writeImage(response, expression, resolution);
        } else {
            // Handle the case where there is no parameter.
            // Probably use some sort of default error image.
            System.out.println(request.getQueryString());
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private boolean isValid(String expression) {
        if (StringUtil.isValue(expression) && 
                !INVALID_PATTERNS.matcher(expression).matches()) {
            return true;
        }

        return false;
    }


    private int getResolution(HttpServletRequest request) {
        if (request.getParameter(PARAMETER_RESOLUTION) != null) {
            int resolution = Integer.parseInt(request.getParameter(PARAMETER_RESOLUTION));
            resolution = Math.min(resolution, TexRunner.MAX_RESOLUTION);
            return Math.max(resolution, TexRunner.MIN_RESOLUTION);
        } else {
            return TexRunner.DEFAULT_RESOLUTION;
        }
    }

    private void writeHeaders(HttpServletResponse response, String expression, int resolution) {
        File file = cache.file(expression, resolution);
        String logMessage = cache.logMessage(expression, resolution);

        if (logMessage == null) {
            response.addHeader("X-MathImage-log", "OK");
        } else {
            response.addHeader("X-MathImage-log", logMessage);
        }

        if (file == null) {
            return;
        }

        response.addHeader("X-MathImage-tex", expression);
        response.addIntHeader("X-MathImage-depth", cache.depth(expression, resolution));
        response.addHeader("Cache-Control", "public, max-age=" + EXPIRES_AFTER);
        response.setContentType("image/png");
        response.setContentLength((int)file.length());
    }

    private void writeImage(HttpServletResponse response, String expression, int resolution) throws ServletException, IOException {
        File file = cache.file(expression, resolution);

        InputStream fileContents;
        fileContents = new FileInputStream(file);
        ServletOutputStream outputStream = response.getOutputStream();

        int c;
        while ((c = fileContents.read()) >= 0) {
            outputStream.write(c);
        }
        fileContents.close();
    }

    private void createImage(String expression, Integer depth) throws IOException, ServletException {
        if (! cache.contains(expression, depth)) {
            texRunner.create(expression, depth, cache);
        }
    }
}
