<?xml version="1.0" encoding="utf-8"?>
<%@page import="java.util.ResourceBundle"%>
<%@page contentType="text/html; charset=utf-8"%>
<%
String hostName = java.net.InetAddress.getLocalHost().getHostName();
String serverName = request.getServerName();
int serverPort = request.getServerPort();

ResourceBundle resources = ResourceBundle.getBundle("webtex");

%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" 
   "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title>About WebTex</title>
</head>
<body>
	<h1>About WebTex</h1>

	<hr />

	<dl>
		<dt>Version:</dt>
		<dd><%= resources.getString("webtex.version") %></dd>

		<dt>Build date:</dt>
		<dd><%= resources.getString("webtex.builddate") %></dd>

		<dt>ServerName:</dt>
		<dd><%=serverName%></dd>

		<dt>ServerPort:</dt>
		<dd><%=serverPort%></dd>

		<dt>ServingHost:</dt>
		<dd><%=hostName%></dd>
	</dl>

	<hr />

	<dl>
        <dt>For testing and demo:</dt>
        <dd>
            <a href="./">Demo page</a>
        </dd>
		<dt>System status:</dt>
		<dd>
			<a href="_monitor">System status</a>
		</dd>
	</dl>
</body>
</html>
