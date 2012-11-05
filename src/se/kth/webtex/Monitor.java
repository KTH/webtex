package se.kth.webtex;

/*
  Copyright (C) 2012 KTH, Kungliga tekniska hogskolan, http://www.kth.se

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

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import se.kth.sys.util.ApplicationMonitor;

public class Monitor extends HttpServlet {
    private static final int TIMEOUT = 5;
    private static final long serialVersionUID = 1L;
    private Cache cache;

    @Override
    public void init(ServletConfig config) {
        ServletContext context = config.getServletContext();
        String root = context.getRealPath("");
        this.cache = Cache.initCache(context, root);
    }    

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ApplicationMonitor monitor = new ApplicationMonitor(TIMEOUT);

        CacheMonitor cacheMonitor = new CacheMonitor();
        cacheMonitor.setCache(this.cache);

        monitor.addCheck("CACHE", cacheMonitor);
        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");
        response.getOutputStream().print(monitor.createMonitorReport());
    }
}
