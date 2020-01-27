package se.kth.webtex;

/*
  Copyright (C) 2017 KTH, Kungliga tekniska hogskolan, http://www.kth.se

  This file is part of WebTex.

  WebTex is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  WebTex is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with WebTex.  If not, see <http://www.gnu.org/licenses/>
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import se.kth.sys.util.StringUtil;
import se.kth.webtex.Cache.CacheData;

/**
 * Servlet implementation. Does the parameter handling and response/request
 * logic.
 */
public class WebTex extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final String PARAMETER_TEX = "tex";
    private static final String PARAMETER_RESOLUTION = "D";
    private static int EXPIRES_AFTER = 7 * 24 * 60 * 60;
    private static final Pattern INVALID_PATTERNS = Pattern.compile(
            ".*(\\\\def|\\\\input|\\\\output|\\\\read|\\\\write|\\\\openin|\\\\openout|\\\\catcode|\\\\let).*");

    private Cache cache;
    private TexRunner texRunner;
    private static final ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");

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

    /**
     * @see HttpServlet#getLastModified(HttpServletRequest)
     */
    protected long getLastModified(HttpServletRequest request) {
        try {
            String expression = getTeX(request);
            if (isValid(expression)) {
                int resolution = getResolution(request);
                createImage(expression, resolution);
                File file = cache.get(expression, resolution).getFile();
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
    protected void doHead(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String expression = getTeX(request);
            if (isValid(expression)) {
                int resolution = getResolution(request);
                createImage(expression, resolution);
                writeHeaders(response, expression, resolution);
            } else {
                // Handle the case where there is no parameter.
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            }
        } catch (DviException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     *      response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String expression = getTeX(request);
            if (isValid(expression)) {
                int resolution = getResolution(request);
                createImage(expression, resolution);
                writeHeaders(response, expression, resolution);
                writeImage(response, expression, resolution);
            } else {
                // Handle the case where there is no parameter.
                // Could possibly use some sort of default error image.
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            }
        } catch (DviException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private boolean isValid(String expression) {
        if (StringUtil.isValue(expression) && !INVALID_PATTERNS.matcher(expression).matches()) {
            return true;
        }

        return false;
    }

    private String getTeX(HttpServletRequest request) {
        return request.getParameter(PARAMETER_TEX);
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

    private String encodeURIComponent(String unencoded) {
        try {
            String escaped = unencoded.replace("\\", "\\\\").replaceAll("(\\r|\\n)+", " ").replace("'", "\\'");
            return (String) engine.eval("encodeURIComponent('" + escaped + "')");
        } catch (ScriptException e) {
            System.out.println("Error encoding string: '" + unencoded + "': " + e.getMessage());
            return "";
        }
    }

    private void writeHeaders(HttpServletResponse response, String expression, int resolution) {
        CacheData data = cache.get(expression, resolution);
        File file = data.getFile();
        String logMessage = data.getLogMessage();

        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Allow-Methods", "*");
        response.addHeader("Access-Control-Allow-Headers", "*");
        response.addHeader("Access-Control-Expose-Headers", "*");

        response.addHeader("X-MathImage-tex", encodeURIComponent(expression));

        if (logMessage == null) {
            response.addHeader("X-MathImage-log", "OK");
        } else {
            response.addHeader("X-MathImage-log", encodeURIComponent(logMessage));
        }

        response.addHeader("Cache-Control", "public, max-age=" + EXPIRES_AFTER);
        response.setContentType("image/png");

        if (file == null) {
            response.addIntHeader("X-MathImage-depth", 0);
            response.addIntHeader("X-MathImage-width", 0);
            response.addIntHeader("X-MathImage-height", 0);
            response.setContentLength(0);
        } else {
            response.addIntHeader("X-MathImage-depth", data.getDepth());
            response.addIntHeader("X-MathImage-width", data.getWidth());
            response.addIntHeader("X-MathImage-height", data.getHeight());
            response.setContentLength((int) file.length());
        }
    }

    private void writeImage(HttpServletResponse response, String expression, int resolution)
            throws ServletException, IOException {
        File file = cache.get(expression, resolution).getFile();
        if (file == null) {
            return;
        }

        InputStream fileContents;
        fileContents = new FileInputStream(file);
        ServletOutputStream outputStream = response.getOutputStream();

        int c;
        while ((c = fileContents.read()) >= 0) {
            outputStream.write(c);
        }
        fileContents.close();
    }

    private void createImage(String expression, Integer resolution) throws IOException, ServletException {
        if (!cache.contains(expression, resolution)) {
            texRunner.create(expression, resolution, cache);
        }
    }
}
