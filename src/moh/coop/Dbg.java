package moh.coop;

/**
 * Created by mohamadjb@gmail.com on 16/4/20.
 */

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.util.*;

public class Dbg {

static final String Name="coop.Dbg";

public static void p(Object...p){for(Object s:p)System.out.print(s);System.out.println();}

public static void main(String[]args)throws Exception{
	CoopSrvlt s= CoopSrvlt.sttc;//Srvlt.sttc;
	s.pc=new PC();
	s.pc.a= SrvltContxt.sttc();
	s.pc.q.ssn=new Ssn();
	String[][]prms= {
		/*1stString is method
		, 2ndString is url(class/app/typ/key)
			where typ is optional depending on method
		, 3rdString is body ,and most methods
			take the body as JsonStorage.val*/
		{"signup","CoopSrvlt/", "{cid:'m',pw:'bQ==',val:{name:'moh',cid:'m',tel:'99876454',email:'mohamadjb@gmail.com',coopId:'1',govId:'',areaId:'',blockId:'',branchId:'1'}}"} // signup
		,{"logout","CoopSrvlt/", ""} // logout
		,{"login","CoopSrvlt/m/1230/12340", "{pw:'bQ=='}"} // login
		,{"chngPw","CoopSrvlt/", "{pw:'bQ=='}"} // chngPw
		,{"put","CoopSrvlt/coop/1/test#/0","{x:''}"} // update
		,{"patch","CoopSrvlt/coop/1/test#/0","{x:''}"} // updateMeta
		,{"post","CoopSrvlt/x/1","{x:''}"} // insert
		,{"get","CoopSrvlt/123456/1234567000", ""} // byDates
		,{"logout","CoopSrvlt/", ""} // logout
	};

	for(String[]p:prms){
		s.pc.q.init(p);
		s.service( s.pc.q,s.pc.p );
		s.pc.q.ssn.newlySsn=false;
	}
}// Dbg$main

//////////////////////////////////////////////////////////////////////

public static class Req implements HttpServletRequest {
	public Ssn ssn;//=new Ssn();
	String contentType="text/json"
		,protocolVersion=""
		,uri="",bodyData//,data
		,queryString=""
			 ;//,method="POST";//
	PC pc;Req(PC p){pc=p;}Req(PC p,String data){pc=p;init(data);}
	InputStream inps;// from Http.Response.data
	BufferedReader bufr;
	long contentLength;// from Http.Response
	boolean chunkedTransfer;// from Http.Response
	boolean keepAlive;  // from Http.Response
	List<String> cookieHeaders;// from Http.Response
	String methd;Method method= Method.GET;// from Http.Response.requestMethod
	/**
	 * HTTP Request methods, with the ability to decode a <code>String</code> back
	 * to its enum value.
	 */
	public enum Method {
		GET,
		PUT(true),
		POST(true),
		DELETE,
		HEAD,
		OPTIONS,
		TRACE,
		CONNECT,
		PATCH,
		PROPFIND,
		PROPPATCH,
		MKCOL,
		MOVE,
		COPY,
		LOCK,
		UNLOCK;Method(){this(false);}
		boolean bodyData;Method(boolean bData){bodyData=bData;}
		public static Method lookup(String method,Method defVal) {
			if (method != null)try {
				defVal= valueOf(method);
			} catch (IllegalArgumentException e) {}
			return defVal;
		}
	}
	GzipUsage gzipUsage = GzipUsage.DEFAULT;// from Http.Response
	static enum GzipUsage {DEFAULT,ALWAYS,NEVER;}// from Http.Response


	//public void addCookieHeader(String cookie) {cookieHeaders.add(cookie);}

	public Req init( String[] data) {
		try {method= Method.valueOf( methd=data[0] );}catch ( Exception ex){method=null;}
		uri=data[1];
		return init(bodyData=data[2]);}

	Req init( String data) {contentLength=(bodyData=data).length();
		inps = new ByteArrayInputStream(bodyData.getBytes());bufr=null;
		return this;}

	Req init( String mimeType, InputStream data, long totalBytes) {
		contentType= mimeType;
		if (data == null) {
			inps = new ByteArrayInputStream(new byte[0]);bufr=null;
			contentLength = 0L;
		} else {
			inps = data;bufr=null;
			contentLength = totalBytes;
		}
		chunkedTransfer = contentLength < 0;
		keepAlive = true;
		cookieHeaders = new ArrayList(10);
		return this;}

	public void setUseGzip(boolean useGzip) {gzipUsage = useGzip ? GzipUsage.ALWAYS : GzipUsage.NEVER;}
	// If a Gzip usage has been enforced, use it.
	// Else decide whether or not to use Gzip.
	public boolean useGzipWhenAccepted() {
		if (gzipUsage == GzipUsage.DEFAULT)
			return contentType != null && (contentType.toLowerCase().contains("text/") || contentType.toLowerCase().contains("/json"));
		else
			return gzipUsage == GzipUsage.ALWAYS;
	}

	public void close() throws IOException {
		if (inps!= null)
			inps.close();
	}

	public void closeConnection(boolean close) {
		if (close)
			headers.put("connection", "close");
		else
			headers.remove("connection");
	}

	public boolean isCloseConnection() {
		return "close".equals(getHeader("connection"));
	}
	public void setKeepAlive(boolean useKeepAlive) {this.keepAlive = useKeepAlive;}

	String findSessionCookie(){for(String s:cookieHeaders)if(s!=null&&s.indexOf( "session" )!=-1)return s;return null;}

	/**
	 * Decodes the sent headers and loads the data into Key/value pairs
	 */
	void initFromInputStream()throws Exception{
		readHeadersFromInputStream();
		bodyData=readBodyData();//if(method.bodyData)
		decodeParms( bodyData );}

	void readHeadersFromInputStream()throws Exception {//BufferedReader in	, Map<String, String> pre//, Map<String, List<String>> parms, Map<String, String> headers
		try {BufferedReader in=getReader();
			// Read the request line
			String line = in.readLine();
			if (line == null)
				return;

			StringTokenizer st = new StringTokenizer(line);
			if (!st.hasMoreTokens())
				throw new Exception( "BAD REQUEST: Syntax error. ");//Rsp.Status.BAD_REQUEST,

			method= Method.lookup( st.nextToken(), Method.GET );//method=;//pre.put("method",);

			if (!st.hasMoreTokens())
				throw new Exception("BAD REQUEST: Missing URI. ");//Status.BAD_REQUEST,


			uri = st.nextToken();//String

			// Decode parameters from the URI
			int qmi = uri.indexOf('?');
			if (qmi >= 0) {
				decodeParms(queryString=uri.substring(qmi + 1));
				uri = urlDecode(uri.substring(0, qmi));
			} else
				uri = urlDecode(uri);

			// If there's another token, its protocol version,
			// followed by HTTP headers.
			// NOTE: this now forces header names lower case since they are
			// case insensitive and vary by client.
			if (st.hasMoreTokens()) {
				protocolVersion = st.nextToken();
			} else {
				protocolVersion = "HTTP/1.1";
				M.TL.tl().log("no protocol version specified, strange. Assuming HTTP/1.1.");//NanoHTTPD.LOG.log(Level.FINE,
			}
			line = in.readLine();
			while (line != null && !line.trim().isEmpty()) {
				int p = line.indexOf(':');
				if (p >= 0) {
					String hname=line.substring(0, p).trim().toLowerCase(Locale.US)
						,hval=line.substring(p + 1).trim();
					if("cookie".equals(hval))
						cookieHeaders.add( hval );else
						headers.put(hname, hval);
				}
				line = in.readLine();
			}

			//uri;//pre.put("uri",);
		} catch (IOException ioe) {
			throw new Exception( "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage(), ioe);//Status.INTERNAL_ERROR,
		}
	}

	String readBodyData(){
		StringBuilder b=new StringBuilder(  );
		try {
			BufferedReader in = getReader();
			String line = in.readLine();
			if(line!=null){b.append( line );
				in.readLine();}
			while (line!=null ) {
				b.append( '\n' ).append( line );
				in.readLine();}
		}catch ( Exception ex ){}
		return b.toString();}

	void decodeParms(String parms){
		StringTokenizer st = new StringTokenizer(parms, "&");
		while (st.hasMoreTokens()) {
			String e = st.nextToken();
			int sep = e.indexOf('=');
			String key = null;
			String value = null;

			if (sep >= 0) {
				key = urlDecode(e.substring(0, sep)).trim();
				value = urlDecode(e.substring(sep + 1));
			} else {
				key = urlDecode(e).trim();
				value = "";
			}

			String[] values=null,a = prms.get(key);
			int n=a==null?0:a.length;
			values=new String[n+1];
			if(n>0)
				System.arraycopy( a,0,values,0,n );
			values[n]=value;
			prms.put(key, values);
		}
	}


	/**
	 * Find the byte positions where multipart boundaries start. This reads a
	 * large block at a time and uses a temporary buffer to optimize (memory
	 * mapped) file access.
	 */
	private int[] getBoundaryPositions(ByteBuffer b, byte[] boundary) {
		int[] res = new int[0];
		if (b.remaining() < boundary.length) {
			return res;
		}

		int search_window_pos = 0;
		byte[] search_window = new byte[4 * 1024 + boundary.length];

		int first_fill = (b.remaining() < search_window.length) ? b.remaining() : search_window.length;
		b.get(search_window, 0, first_fill);
		int new_bytes = first_fill - boundary.length;

		do {
			// Search the search_window
			for (int j = 0; j < new_bytes; j++) {
				for (int i = 0; i < boundary.length; i++) {
					if (search_window[j + i] != boundary[i])
						break;
					if (i == boundary.length - 1) {
						// Match found, add it to results
						int[] new_res = new int[res.length + 1];
						System.arraycopy(res, 0, new_res, 0, res.length);
						new_res[res.length] = search_window_pos + j;
						res = new_res;
					}
				}
			}
			search_window_pos += new_bytes;

			// Copy the end of the buffer to the start
			System.arraycopy(search_window, search_window.length - boundary.length, search_window, 0, boundary.length);

			// Refill search_window
			new_bytes = search_window.length - boundary.length;
			new_bytes = (b.remaining() < new_bytes) ? b.remaining() : new_bytes;
			b.get(search_window, boundary.length, new_bytes);
		} while (new_bytes > 0);
		return res;
	}


	/**
	 * Decode percent encoded <code>String</code> values.
	 *
	 * @param str the percent encoded <code>String</code>
	 * @return expanded form of the input, for example "foo%20bar" becomes
	 * "foo bar"
	 */
	public static String urlDecode( String str ) {
		String decoded = null;
		try {
			decoded = URLDecoder.decode( str, "UTF8" );
		} catch ( UnsupportedEncodingException ex ) {
			//NanoHTTPD.LOG.log( Level.WARNING, "Encoding not supported, ignored", ignored );
			M.TL.tl().error(ex,"Encoding not supported, ignored");
		}
		return decoded;
/*
	/**
	 * Decodes the Multipart Body data and put it into Key/Value pairs.
	 * /
		private void decodeMultipartFormData(ContentType contentType, ByteBuffer fbuf, Map<String, List<String>> parms, Map<String, String> files) throws NanoHTTPD.ResponseException {
			int pcount = 0;
			try {
				int[] boundaryIdxs = getBoundaryPositions(fbuf, contentType.getBoundary().getBytes());
				if (boundaryIdxs.length < 2) {
					throw new NanoHTTPD.ResponseException(Status.BAD_REQUEST, "BAD REQUEST: Content type is multipart/form-data but contains less than two boundary strings.");
				}
				byte[] partHeaderBuff = new byte[MAX_HEADER_SIZE];
				for (int boundaryIdx = 0; boundaryIdx < boundaryIdxs.length - 1; boundaryIdx++) {
					fbuf.position(boundaryIdxs[boundaryIdx]);
					int len = (fbuf.remaining() < MAX_HEADER_SIZE) ? fbuf.remaining() : MAX_HEADER_SIZE;
					fbuf.get(partHeaderBuff, 0, len);
					BufferedReader in =
							new BufferedReader(new InputStreamReader(new ByteArrayInputStream(partHeaderBuff, 0, len), Charset.forName(contentType.getEncoding())), len);
					int headerLines = 0;
					// First line is boundary string
					String mpline = in.readLine();
					headerLines++;
					if (mpline == null || !mpline.contains(contentType.getBoundary())) {
						throw new NanoHTTPD.ResponseException(Status.BAD_REQUEST, "BAD REQUEST: Content type is multipart/form-data but chunk does not start with boundary.");
					}
					String partName = null, fileName = null, partContentType = null;
					// Parse the reset of the header lines
					mpline = in.readLine();
					headerLines++;
					while (mpline != null && mpline.trim().length() > 0) {
						Matcher matcher = NanoHTTPD.CONTENT_DISPOSITION_PATTERN.matcher(mpline);
						if (matcher.matches()) {
							String attributeString = matcher.group(2);
							matcher = NanoHTTPD.CONTENT_DISPOSITION_ATTRIBUTE_PATTERN.matcher(attributeString);
							while (matcher.find()) {
								String key = matcher.group(1);
								if ("name".equalsIgnoreCase(key)) {
									partName = matcher.group(2);
								} else if ("filename".equalsIgnoreCase(key)) {
									fileName = matcher.group(2);
									// add these two line to support multiple
									// files uploaded using the same field Id
									if (!fileName.isEmpty()) {
										if (pcount > 0)
											partName = partName + String.valueOf(pcount++);
										else
											pcount++;
									}
								}
							}
						}
						matcher = NanoHTTPD.CONTENT_TYPE_PATTERN.matcher(mpline);
						if (matcher.matches()) {
							partContentType = matcher.group(2).trim();
						}
						mpline = in.readLine();
						headerLines++;
					}
					int partHeaderLength = 0;
					while (headerLines-- > 0) {
						partHeaderLength = scipOverNewLine(partHeaderBuff, partHeaderLength);
					}
					// Read the part data
					if (partHeaderLength >= len - 4) {
						throw new NanoHTTPD.ResponseException(Status.INTERNAL_ERROR, "Multipart header size exceeds MAX_HEADER_SIZE.");
					}
					int partDataStart = boundaryIdxs[boundaryIdx] + partHeaderLength;
					int partDataEnd = boundaryIdxs[boundaryIdx + 1] - 4;
					fbuf.position(partDataStart);
					List<String> values = parms.get(partName);
					if (values == null) {
						values = new ArrayList<String>();
						parms.put(partName, values);
					}
					if (partContentType == null) {
						// Read the part into a string
						byte[] data_bytes = new byte[partDataEnd - partDataStart];
						fbuf.get(data_bytes);
						values.add(new String(data_bytes, contentType.getEncoding()));
					} else {
						// Read it into a file
						String path = saveTmpFile(fbuf, partDataStart, partDataEnd - partDataStart, fileName);
						if (!files.containsKey(partName)) {
							files.put(partName, path);
						} else {
							int count = 2;
							while (files.containsKey(partName + count)) {
								count++;
							}
							files.put(partName + count, path);
						}
						values.add(fileName);
					}
				}
			} catch (NanoHTTPD.ResponseException re) {
				throw re;
			} catch (Exception e) {
				throw new NanoHTTPD.ResponseException(Status.INTERNAL_ERROR, e.toString());
			}
		}
	public void parseBody(Map<String, String> files) throws IOException, NanoHTTPD.ResponseException {
		RandomAccessFile randomAccessFile = null;
		try {
			long size = getBodySize();
			ByteArrayOutputStream baos = null;
			DataOutput requestDataOutput = null;
			// Store the request in memory or a file, depending on size
			if (size < MEMORY_STORE_LIMIT) {
				baos = new ByteArrayOutputStream();
				requestDataOutput = new DataOutputStream(baos);
			} else {
				randomAccessFile = getTmpBucket();
				requestDataOutput = randomAccessFile;
			}
			// Read all the body and write it to request_data_output
			byte[] buf = new byte[REQUEST_BUFFER_LEN];
			while (this.rlen >= 0 && size > 0) {
				this.rlen = this.inputStream.read(buf, 0, (int) Math.min(size, REQUEST_BUFFER_LEN));
				size -= this.rlen;
				if (this.rlen > 0) {
					requestDataOutput.write(buf, 0, this.rlen);
				}
			}
			ByteBuffer fbuf = null;
			if (baos != null) {
				fbuf = ByteBuffer.wrap(baos.toByteArray(), 0, baos.size());
			} else {
				fbuf = randomAccessFile.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, randomAccessFile.length());
				randomAccessFile.seek(0);
			}
			// If the method is POST, there may be parameters
			// in data section, too, read it:
			if (Method.POST.equals(this.method)) {
				ContentType contentType = new ContentType(this.headers.get("content-type"));
				if (contentType.isMultipart()) {
					String boundary = contentType.getBoundary();
					if (boundary == null) {
						throw new NanoHTTPD.ResponseException(Status.BAD_REQUEST, "BAD REQUEST: Content type is multipart/form-data but boundary missing. Usage: GET /example/file.html");
					}
					decodeMultipartFormData(contentType, fbuf, this.parms, files);
				} else {
					byte[] postBytes = new byte[fbuf.remaining()];
					fbuf.get(postBytes);
					String postLine = new String(postBytes, contentType.getEncoding()).trim();
					// Handle application/x-www-form-urlencoded
					if ("application/x-www-form-urlencoded".equalsIgnoreCase(contentType.getContentType())) {
						decodeParms(postLine, this.parms);
					} else if (postLine.length() != 0) {
						// Special case for raw POST data => create a
						// special files entry "postData" with raw content
						// data
						files.put(POST_DATA, postLine);
					}
				}
			} else if (Method.PUT.equals(this.method)) {
				files.put("content", saveTmpFile(fbuf, 0, fbuf.limit(), null));
			}
		} finally {
			NanoHTTPD.safeClose(randomAccessFile);
		}
	}*/}



	//"{op:'query',sql:'select d,count(*),min(no),max(no) from d group by y,m,w'}"
	HashMap<String,Object> attribs=new HashMap<String,Object>();
	HashMap<String,String> headers=new HashMap<String,String>();
	HashMap<String,String[]>prms=new HashMap<String,String[]>();
	@Override public AsyncContext getAsyncContext() {return null;}
	@Override public Object getAttribute(String p) {return attribs.get(p);}
	@Override public Enumeration<String> getAttributeNames() {return new Enumeration<String>() {
		Iterator<String>i=attribs.keySet().iterator();
		@Override	public boolean hasMoreElements() {	return i.hasNext();}
		@Override	public String nextElement() {return i.next();}};}
	@Override public String getCharacterEncoding() {return "utf8";}
	@Override public int getContentLength() {return bodyData==null?(int)contentLength:bodyData.length();}
	@Override public long getContentLengthLong() {return bodyData==null?0:bodyData.length();}
	@Override public String getContentType() {p("Req.getContentType:",contentType);return contentType;}
	@Override public String getContextPath() {return uri;}
	@Override public DispatcherType getDispatcherType() {return null;}
	@Override public ServletInputStream getInputStream() throws IOException{
		return new ServletInputStream() {int i=0;
			@Override public int read() throws IOException{return bodyData.charAt(i++);}
			@Override public void setReadListener(ReadListener p){}
			@Override public boolean isReady() {return true;}
			@Override public boolean isFinished() {return i>=bodyData.length();}};}//new java.io.ByteArrayInputStream(data.getBytes());}//(new java.io.StringReader(data));//StringBufferInputStream(data);
	@Override public String getLocalAddr() {return null;}
	@Override public String getLocalName() {return null;}
	@Override public int getLocalPort() {return 0;}
	@Override public Locale getLocale() {return null;}
	@Override public Enumeration<Locale> getLocales() {return null;}
	@Override public String getParameter(String p){String[]a= prms.get(p);return a!=null&&a.length>0?a[0]:null;}
	@Override public Map<String, String[]> getParameterMap() {return prms;}
	@Override public Enumeration<String> getParameterNames() {return new Enumeration<String>() {
		Iterator<String>i=prms.keySet().iterator();
		@Override	public boolean hasMoreElements() {	return i.hasNext();}
		@Override	public String nextElement() {return i.next();}};}
	@Override public String[] getParameterValues(String p) {return prms.get(p);}
	@Override public String getProtocol() {return protocolVersion;}
	@Override public BufferedReader getReader() throws IOException {
		return bufr!=null?bufr:(bufr=new BufferedReader( new InputStreamReader(inps )));}////new BufferedReader(new java.io.CharArrayReader(data.toCharArray()));
	@Override public String getRealPath(String p) {return null;}
	@Override public String getRemoteAddr() {return "127.0.0.1";}
	@Override public String getRemoteHost() {return null;}
	@Override public int getRemotePort() {return 0;}
	@Override public RequestDispatcher getRequestDispatcher(String p) {return null;}
	@Override public String getScheme() {return null;}
	@Override public String getServerName() {return null;}
	@Override public int getServerPort() {return 0;}
	@Override public ServletContext getServletContext() {return null;}
	@Override public boolean isAsyncStarted() {return false;}
	@Override public boolean isAsyncSupported() {return false;}
	@Override public boolean isSecure() {return false;}
	@Override public void removeAttribute(String p) {attribs.remove(p);}
	@Override public void setAttribute(String p, Object p2) {attribs.put(p, p2);}
	@Override public void setCharacterEncoding(String p) throws UnsupportedEncodingException {}
	@Override public AsyncContext startAsync() throws IllegalStateException {return null;}
	@Override public AsyncContext startAsync(ServletRequest p, ServletResponse p2) throws IllegalStateException {return null;}
	@Override public boolean authenticate(HttpServletResponse p) throws IOException, ServletException {return false;}
	@Override public String changeSessionId() {return null;}
	@Override public String getAuthType() {return null;}
	@Override public Cookie[] getCookies() {return null;}
	@Override public long getDateHeader(String p) {return 0;}
	@Override public String getHeader(String p) {return headers.get(p);}
	@Override public Enumeration<String> getHeaderNames() {return new Enumeration<String>() {
		Iterator<String>i=headers.keySet().iterator();
		@Override	public boolean hasMoreElements() {	return i.hasNext();}
		@Override	public String nextElement() {return i.next();}};}
	@Override public Enumeration<String> getHeaders(String p) {return null;}
	@Override public int getIntHeader(String p) {return 0;}
	@Override public String getMethod() {return method==null?methd:method.name();}
	@Override public Part getPart(String p) throws IOException, ServletException {return null;}
	@Override public Collection<Part> getParts() throws IOException, ServletException {return null;}
	@Override public String getPathInfo() {return null;}
	@Override public String getPathTranslated() {return null;}
	@Override public String getQueryString() {return "/adoqs/xhr.jsp";}
	@Override public String getRemoteUser() {return null;}
	@Override public String getRequestURI() {return uri;}
	@Override public StringBuffer getRequestURL() {return new StringBuffer( uri);}
	@Override public String getRequestedSessionId() {return null;}
	@Override public String getServletPath() {return null;}
	@Override public HttpSession getSession() {return ssn;}
	@Override public HttpSession getSession(boolean p) {return ssn;}
	@Override public java.security.Principal getUserPrincipal() {return null;}
	@Override public boolean isRequestedSessionIdFromCookie() {return false;}
	@Override public boolean isRequestedSessionIdFromURL() {return false;}
	@Override public boolean isRequestedSessionIdFromUrl() {return false;}
	@Override public boolean isRequestedSessionIdValid() {return false;}
	@Override public boolean isUserInRole(String p) {return false;}
	@Override public void login(String p, String p2) throws ServletException {}
	@Override public void logout() throws ServletException {}
	@Override public <T extends HttpUpgradeHandler> T upgrade(Class<T>
		                                                          p) throws IOException, ServletException {return null;}

}//class Req

//////////////////////////////////////////////////////////////////////

public static class Rsp implements HttpServletResponse{
	String contentType="";
	Sos sos;
	static final String Name= Dbg.Name+".Rsp";
	PC pc;
	Rsp(PC p){pc=p;}
	HashMap<String,String> headers=new HashMap<String,String>();

	boolean chunkedTransfer;//from Http.Response
	Status status;//from Http.Response

	/**
	 * Some HTTP response status codes
	 */
	public enum Status{// implements IStatus
		SWITCH_PROTOCOL(101, "Switching Protocols"),

		OK(200, "OK"),
		CREATED(201, "Created"),
		ACCEPTED(202, "Accepted"),
		NO_CONTENT(204, "No Content"),
		PARTIAL_CONTENT(206, "Partial Content"),
		MULTI_STATUS(207, "Multi-Status"),

		REDIRECT(301, "Moved Permanently"),
		/**
		 * Many user agents mishandle 302 in ways that violate the RFC1945 spec
		 * (i.e., redirect a POST to a GET). 303 and 307 were added in RFC2616 to
		 * address this. You should prefer 303 and 307 unless the calling user agent
		 * does not support 303 and 307 functionality
		 */
		@Deprecated
		FOUND(302, "Found"),
		REDIRECT_SEE_OTHER(303, "See Other"),
		NOT_MODIFIED(304, "Not Modified"),
		TEMPORARY_REDIRECT(307, "Temporary Redirect"),

		BAD_REQUEST(400, "Bad Request"),
		UNAUTHORIZED(401, "Unauthorized"),
		FORBIDDEN(403, "Forbidden"),
		NOT_FOUND(404, "Not Found"),
		METHOD_NOT_ALLOWED(405, "Method Not Allowed"),
		NOT_ACCEPTABLE(406, "Not Acceptable"),
		REQUEST_TIMEOUT(408, "Request Timeout"),
		CONFLICT(409, "Conflict"),
		GONE(410, "Gone"),
		LENGTH_REQUIRED(411, "Length Required"),
		PRECONDITION_FAILED(412, "Precondition Failed"),
		PAYLOAD_TOO_LARGE(413, "Payload Too Large"),
		UNSUPPORTED_MEDIA_TYPE(415, "Unsupported Media Type"),
		RANGE_NOT_SATISFIABLE(416, "Requested Range Not Satisfiable"),
		EXPECTATION_FAILED(417, "Expectation Failed"),
		TOO_MANY_REQUESTS(429, "Too Many Requests"),

		INTERNAL_ERROR(500, "Internal Server Error"),
		NOT_IMPLEMENTED(501, "Not Implemented"),
		SERVICE_UNAVAILABLE(503, "Service Unavailable"),
		UNSUPPORTED_HTTP_VERSION(505, "HTTP Version Not Supported");

		private final int requestStatus;

		private final String description;

		Status(int requestStatus, String description) {
			this.requestStatus = requestStatus;
			this.description = description;
		}

		public static Status lookup(int requestStatus) {
			for (Status status : Status.values()) {
				if (status.requestStatus == requestStatus) {
					return status;
				}
			}
			return null;
		}

		public String getDescription() {return "" + this.requestStatus + " " + this.description;}

	}
	JspWrtr jspWrtr=new JspWrtr();
	PrintWriter out=new PrintWriter(jspWrtr);//System.out//strW);StringWriter strW=new StringWriter(); //TODO: with the server, must not initialize out, instead should remove the member-variable
	@Override public void flushBuffer() throws IOException {if(out!=null)out.flush();if(sos!=null)sos.flush();}
	@Override public int getBufferSize() {p(Name,".getBufferSize:0");return 0;}
	@Override public String getCharacterEncoding() {p(Name,".getCharacterEcoding");return null;}
	@Override public String getContentType() {p(Name,".getContentType:",contentType);return contentType;}
	@Override public Locale getLocale() {p(Name,".getLocale");return null;}
	@Override public ServletOutputStream getOutputStream() throws IOException {p(Name,".getOutputStream");return sos;}
	@Override public PrintWriter getWriter() throws IOException {p(Name,".getWriter");return out;}
	@Override public boolean isCommitted() {p(Name,".isCommited");return false;}
	@Override public void reset() {p(Name,".reset");}
	@Override public void resetBuffer() {p(Name,".resetBuffer");}
	@Override public void setBufferSize(int p) {p(Name,".setBufferSize:",p);}
	@Override public void setCharacterEncoding(String p) {p(Name,".setCharacterEncoding:",p);}
	@Override public void setContentLength(int p) {p(Name,".setContentLength:",p);}
	@Override public void setContentLengthLong(long p) {p(Name,".setContentLengthLong:",p);}
	@Override public void setContentType(String p) {p(Name,".setContentType:",p);contentType=p;}
	@Override public void setLocale(Locale p) {p(Name,".setLocale:",p);}
	@Override public void addCookie(Cookie p) {p(Name,".addCookie:",p);}
	@Override public void addDateHeader(String p, long p2) {p(Name,".addDateHeader:",p,",",p2);}
	@Override public void addHeader(String p, String p2) {p(Name,".addHeader:",p,",",p2);}
	@Override public void addIntHeader(String p, int p2) {p(Name,".addIntHeader:",p,",",p2);}
	@Override public boolean containsHeader(String p) {p(Name,".containsHeader:",p);return false;}
	@Override public String encodeRedirectURL(String p) {p(Name,".ecodeRedirectURL:",p);return null;}
	@Override public String encodeRedirectUrl(String p) {p(Name,".encodeRedirectUrl:",p);return null;}
	@Override public String encodeURL(String p) {p(Name,".encodeURL:",p);return null;}
	@Override public String encodeUrl(String p) {p(Name,".encodeUrl:",p);return null;}
	@Override public String getHeader(String p) {p(Name,".getHeader:",p);return null;}
	@Override public Collection<String> getHeaderNames() {p(Name,".getHeaderNames");return null;}
	@Override public Collection<String> getHeaders(String p) {p(Name,".getHeaders:",p);return null;}
	@Override public int getStatus() {p(Name,".getStatus");return 0;}
	@Override public void sendError(int p) throws IOException {p(Name,".sendError:",p);}
	@Override public void sendError(int p, String p2) throws IOException {p(Name,".sendError:",p,",",p2);}
	@Override public void sendRedirect(String p) throws IOException {p(Name,".sendRedirect:",p);}
	@Override public void setDateHeader(String p, long p2) {p(Name,".setDateHeader:",p,",",p2);}
	@Override public void setHeader(String p, String p2) {p(Name,".setHeader:",p,",",p2);}
	@Override public void setIntHeader(String p, int p2) {p(Name,".setIntHeader:",p,",",p2);}
	@Override public void setStatus(int p) {p(Name,".setStatus:",p);}
	@Override public void setStatus(int p, String p2) {p(Name,".setStatus:",p,",",p2);}

	public static class Sos extends ServletOutputStream{
		OutputStream o;StringBuilder sb=new StringBuilder();
		Sos(OutputStream p){p("\n------------------------------\nSos.<init>:",o=p);}
		@Override public boolean isReady() {return true;}
		@Override public void setWriteListener(WriteListener p) {}//super.setWriteListener(p);}
		@Override public void write(int p) throws IOException {sb.append((char)p);if(o!=null)o.write( p );}//p("Sos.write(int:",p,"):",(char)p);}//o.write(p);
		@Override public void flush() throws IOException {p("Dbg.Sos.flush:",sb.toString());sb.setLength(0);if(o!=null)o.flush();}//super.flush();}//o.flush();
		@Override public void close() throws IOException {p("Dbg.Sos.close:",sb.toString());sb.setLength(0);if(o!=null)o.close();}//super.close();}//o.close();
		@Override public void write(byte[] p) throws IOException {sb.append(new String(p));if(o!=null)o.write(p);}//p("Sos.write(byte[]):",new String(p));super.write(p);}//o.write(p);}
		@Override public void write(byte[] a, int b, int c) throws IOException {sb.append(new String(a,b,c));if(o!=null)o.write(a,b,c);}//p("Sos.write(byte[]:",a,",int:",b,",int:",c,"):",new String(a, b, c));super.write(a, b, c);}//o.write(a, b, c);}
		public void p(Object...p){for(Object o:p)try{print(String.valueOf( o ));}catch ( Exception ex ){}}
		@Override public void print(String p) throws IOException {sb.append(p);if(o!=null)o.write(p.getBytes());}
		@Override public void print(int p) throws IOException {p(p);}
		@Override public void print(boolean p) throws IOException {p(p);}
		@Override public void print(char p) throws IOException {p(p);}
		@Override public void print(double p) throws IOException {p(p);}
		@Override public void print(float p) throws IOException {p(p);}
		@Override public void print(long p) throws IOException {p(p);}
		@Override public void println(int p) throws IOException {p(p,'\n');}
		@Override public void println(boolean p) throws IOException {p(p,'\n');}
		@Override public void println() throws IOException {p('\n');}
		@Override public void println(char p) throws IOException {p(p,'\n');}
		@Override public void println(double p) throws IOException {p(p,'\n');}
		@Override public void println(float p) throws IOException {p(p,'\n');}
		@Override public void println(long p) throws IOException {p(p,'\n');}
		@Override public void println(String p) throws IOException {p(p,'\n');}
	}//class Sos

	public static class JspWrtr extends javax.servlet.jsp.JspWriter{

		@Override public void clear() throws IOException { clearBuffer();}
		@Override public void clearBuffer() throws IOException {sb.replace( 0,sb.length(),"" ); }
		//@Override public void close() throws IOException { }
		@Override public void newLine() throws IOException { println();}
		//@Override public void flush() throws IOException { }
		@Override public int getRemaining() {return sb.length(); }

		//@Override public void write( int i ) throws IOException { }
		//@Override public void write(char[]a, int i,int n ) throws IOException { }
		//@Override public void write( String i ) throws IOException { }

		@Override public void print( int i ) throws IOException { sb.append( i );}
		@Override public void print( boolean i ) throws IOException { sb.append( i );}
		@Override public void print( char i ) throws IOException { sb.append( i );}
		@Override public void print( char[] i ) throws IOException { sb.append( i );}
		@Override public void print( long i ) throws IOException { sb.append( i );}
		@Override public void print( float i ) throws IOException { sb.append( i );}
		@Override public void print( double i ) throws IOException { sb.append( i );}
		@Override public void print( String i ) throws IOException { sb.append( i );}
		@Override public void print( Object i ) throws IOException { sb.append( i );}

		@Override public void println( int i ) throws IOException { sb.append( i ).append( '\n' );}
		@Override public void println( boolean i ) throws IOException { sb.append( i ).append( '\n' );}
		@Override public void println( char i ) throws IOException { sb.append( i ).append( '\n' );}
		@Override public void println( char[] i ) throws IOException { sb.append( i ).append( '\n' );}
		@Override public void println( long i ) throws IOException { sb.append( i ).append( '\n' );}
		@Override public void println( float i ) throws IOException { sb.append( i ).append( '\n' );}
		@Override public void println( double i ) throws IOException { sb.append( i ).append( '\n' );}
		@Override public void println( String i ) throws IOException { sb.append( i ).append( '\n' );}
		@Override public void println( Object i ) throws IOException { sb.append( i ).append( '\n' );}
		@Override public void println( ) throws IOException { sb.append( '\n' );}


		//public static class SrvltWrtr extends java.io.Writer{
		//SrvltWrtr
		JspWrtr(){super(10,false);
			p("SrvltWrtr.<init>");}
		StringBuilder sb=new StringBuilder();
		@Override public void flush() throws IOException {p("SrvltWrtr.flush:",sb.toString());sb.setLength(0);}
		@Override public void close() throws IOException {p("SrvltWrtr.close:",sb.toString());sb.setLength(0);}
		//@Override public String toString() {String s=sb.toString();p("SrvltWrtr.toString:",s);return s;}
		@Override public void write(char[] cbuf, int off, int len) throws IOException {sb.append(cbuf, off, len);}
		@Override public void write(char[] cbuf) throws IOException {sb.append(cbuf);}
		@Override public void write(String p) throws IOException {sb.append(p);}
		@Override public void write(String p, int off, int len) throws IOException {sb.append(p, off, len);}
		//p("SrvltWrtr.write(char[]",cbuf,",off=",off,",len=",len,"):",String.valueOf(cbuf,off,len));
		@Override public void write(int p){sb.append((char)p);}
		@Override public Writer append(CharSequence p) throws IOException {sb.append(p);return this;}
		@Override public Writer append(CharSequence p, int off, int len) throws IOException {sb.append(p, off, len);return this;}
		@Override public Writer append(char p) throws IOException {sb.append(p);return this;}
		//@Override public Writer append(String p)  {sb.append(p);super.;return this;}
		//@Override public Writer append(String p, int off, int len) throws IOException {sb.append(p, off, len);return this;}
		//}//public static class SrvltWrtr extends java.io.Writer
	}//class JspWrtr , writer

}//class Rsp

//////////////////////////////////////////////////////////////////////

public static class PC extends javax.servlet.jsp.PageContext{
	public Req q=new Req(this);public Rsp p=new Rsp(this);
	public SrvltContxt a=null;Socket socket;


	@Override public void forward(String arg0) throws ServletException, IOException {}
	@Override public Exception getException() {return null;}
	@Override public Object getPage() {return null;}
	@Override public ServletRequest getRequest(){return q;}
	@Override public ServletResponse getResponse(){return p;}
	@Override public ServletConfig getServletConfig(){return Srvlt.sttc.getServletConfig();}
	@Override public ServletContext getServletContext(){return Srvlt.sttc.getServletContext();}
	@Override public HttpSession getSession(){return q.ssn;}
	@Override public void handlePageException(Exception arg0) throws ServletException, IOException{}
	@Override public void handlePageException(Throwable arg0) throws ServletException, IOException{}
	@Override public void include(String arg0) throws ServletException, IOException{}
	@Override public void include(String arg0, boolean arg1) throws ServletException, IOException{}
	@Override public void initialize(Servlet arg0, ServletRequest arg1, ServletResponse arg2, String arg3,
	                                 boolean arg4, int arg5,boolean arg6) throws IOException, IllegalStateException, IllegalArgumentException{}
	@Override public void release(){}
	@Override public Object findAttribute(String n){Object o=q.getAttribute(n);
		if(o==null)o=q.ssn.getAttribute(n);if(o==null)o=a.getAttribute(n);return o;}
	@Override public Object getAttribute(String n){return findAttribute(n);}
	@Override public Object getAttribute(String n, int arg1){return null;}
	@Override public Enumeration<String> getAttributeNamesInScope(int arg0){return null;}
	@Override public int getAttributesScope(String arg0) {M.TL.tl().log(
		"Dbg.PC.getAttributesScope:not implemented:return 0");return 0;}

	@Override public javax.servlet.jsp.el.ExpressionEvaluator getExpressionEvaluator(){return null;}
	@Override public javax.el.ELContext getELContext(){M.TL.tl().log("Dbg.PC.getELContext:not implemented:return null");return null;}

	@Override public javax.servlet.jsp.JspWriter getOut(){M.TL.tl().log(
		"Dbg.PC.getOut");return Srvlt.sttc.pc.p.jspWrtr;}
	@Override public javax.servlet.jsp.el.VariableResolver getVariableResolver(){return null;}
	@Override public void removeAttribute(String arg0){M.TL.tl().log(
		"Dbg.PC.removeAttribute a:not implemented");}
	@Override public void removeAttribute(String arg0, int arg1){M.TL.tl().log("Dbg.PC.removeAttribute a,b:not implemented:return null");}
	@Override public void setAttribute(String arg0, Object arg1) {M.TL.tl().log("Dbg.PC.setAttribute a,b:not implemented:return null");}
	@Override public void setAttribute(String arg0, Object arg1, int arg2) {M.TL.tl().log("Dbg.PC.setAttribute a,b,c:not implemented:return null");}
//public static class ELContext{}
}//class PC

//////////////////////////////////////////////////////////////////////

public static class Srvlt extends GenericServlet{
	static final String Name= Dbg.Name+".Srvlt";
	@Override public void service(ServletRequest q, ServletResponse p)throws ServletException, IOException {
		p(Name,".service:",q,",",p);
		//M.this.service(q,p);
	}

	public PC pc=null;//new PC();
	public static Srvlt sttc=new Srvlt();

	@Override public void log(String message, Throwable t) {log(message);t.printStackTrace();}//super.log(message, t);}
	@Override public void log(String msg) {p("log:",msg);}//super.log(msg);
	@Override public ServletContext getServletContext() {return SrvltContxt.sttc();}//pc.a;super.getServletContext()
	@Override public String getServletName() {return Name;}//super.getServletName();
}//class Srvlt


//////////////////////////////////////////////////////////////////////

public static class Ssn implements HttpSession {
	HashMap<String,Object> attribs=new HashMap<String,Object>();long expir;boolean newlySsn=true;
	public static Map<String,Ssn>sessions=new HashMap<String,Ssn>();

	@Override public Object getAttribute(String p){return attribs.get(p);}
	@Override public Enumeration<String> getAttributeNames() {return new Enumeration<String>() {
		Iterator<String>i=attribs.keySet().iterator();
		@Override	public boolean hasMoreElements() {	return i.hasNext();}
		@Override	public String nextElement() {return i.next();}};}
	@Override public long getCreationTime() {return 0;}
	@Override public String getId() {return null;}
	@Override public long getLastAccessedTime() {return 0;}
	@Override public int getMaxInactiveInterval() {return 0;}
	@Override public ServletContext getServletContext() {return SrvltContxt.sttc();}//
	@Override public HttpSessionContext getSessionContext() {return null;}
	@Override public Object getValue(String p){return null;}
	@Override public String[] getValueNames() {return null;}
	@Override public void invalidate(){}
	@Override public boolean isNew() {return newlySsn;}
	@Override public void putValue(String p, Object p2){}
	@Override public void removeAttribute(String p){}
	@Override public void removeValue(String p){}
	@Override public void setAttribute(String k, Object v){attribs.put(k, v);}
	@Override public void setMaxInactiveInterval(int p){}}

//////////////////////////////////////////////////////////////////////

public static class SrvltContxt implements ServletContext{//static SrvltContxt sttc;
	HashMap<String,Object> attribs=new HashMap<String,Object>();
	@Override public FilterRegistration.Dynamic addFilter( String arg0, String p2){return null;}
	@Override public FilterRegistration.Dynamic addFilter(String arg0, Filter p2){return null;}
	@Override public FilterRegistration.Dynamic addFilter(String arg0, Class<? extends Filter> p2){return null;}
	@Override public void addListener(String p){}
	@Override public <T extends EventListener > void addListener( T p){}
	@Override public void addListener(Class<? extends EventListener> p){}
	@Override public ServletRegistration.Dynamic addServlet(String arg0, String p2){return null;}
	@Override public ServletRegistration.Dynamic addServlet(String arg0, Servlet p2){return null;}
	@Override public ServletRegistration.Dynamic addServlet(String arg0, Class<? extends Servlet> p2){return null;}
	@Override public <T extends Filter> T createFilter(Class<T> p2)throws ServletException{return null;}
	@Override public <T extends EventListener> T createListener(Class<T> p2)throws ServletException{return null;}
	@Override public <T extends Servlet> T createServlet(Class<T> p2)throws ServletException{return null;}
	@Override public void declareRoles(String... p){}
	@Override public Object getAttribute(String p){return attribs.get(p);}
	@Override public Enumeration<String> getAttributeNames(){return new Enumeration<String>() {
		Iterator<String>i=attribs.keySet().iterator();
		@Override	public boolean hasMoreElements() {	return i.hasNext();}
		@Override	public String nextElement() {return i.next();}};}
	@Override public ClassLoader getClassLoader(){return null;}
	@Override public ServletContext getContext(String p){return null;}
	@Override public String getContextPath(){return null;}
	@Override public Set<SessionTrackingMode> getDefaultSessionTrackingModes(){return null;}
	@Override public int getEffectiveMajorVersion(){return 0;}
	@Override public int getEffectiveMinorVersion(){return 0;}
	@Override public Set<SessionTrackingMode> getEffectiveSessionTrackingModes(){return null;}
	@Override public FilterRegistration getFilterRegistration(String p){return null;}
	@Override public Map<String, ? extends FilterRegistration> getFilterRegistrations(){return null;}
	@Override public String getInitParameter(String p){return null;}
	@Override public Enumeration<String> getInitParameterNames(){return null;}
	@Override public javax.servlet.descriptor.JspConfigDescriptor getJspConfigDescriptor(){return null;}
	@Override public int getMajorVersion(){return 0;}
	@Override public int getMinorVersion(){return 0;}
	@Override public String getMimeType(String p){
		final String def="application/octet-stream";
		Map m=(Map)attribs.get( "mapFileExt2MimeType" );
		if(m==null) {m=M.Util.mapCreate(
			"woff","application/font-woff"
			,"woff2","application/font-woff2"
			,"jar"  ,"application/java-archive"
			,"js"   ,"application/javascript"
			,"json" ,"application/json"
			,"exe"  ,def
			,"pdf"  ,"application/pdf"
			,"7z"   ,"application/x-7z-compressed"
			,"tgz"  ,"application/x-compressed"
			,"gz"   ,"application/x-gzip"
			,"tar"  ,"application/x-tar"
			,"xhtml","application/xhtml+xml"
			,"zip"  ,"application/zip"
			,"mp3"  ,"audio/mpeg"
			,"gif"  ,"image/gif"
			,"jpg"  ,"image/jpeg"
			,"jpeg" ,"image/jpeg"
			,"png"  ,"image/png"
			,"svg"  ,"image/svg+xml"
			,"ico"  ,"image/x-icon"
			,"css"  ,"text/css"
			,"csv"  ,"text/csv"
			,"htm"  ,"text/html; charset=utf-8"
			,"html" ,"text/html; charset=utf-8"
			,"txt"  ,"text/plain"
			,"text" ,"text/plain"
			,"log"  ,"text/plain"
			,"xml"  ,"text/xml" );
		}p=p==null?null:(String)m.get(p);
		return p==null?def:p;}
	@Override public RequestDispatcher getNamedDispatcher(String p){return null;}
	@Override public String getRealPath(String p){return M.context.getRealPath( M.TL.tl(),p );}
	@Override public RequestDispatcher getRequestDispatcher(String p){return null;}
	@Override public java.net.URL getResource(String arg0) throws java.net.MalformedURLException {return null;}
	@Override public InputStream getResourceAsStream( String p){return null;}
	@Override public Set<String> getResourcePaths(String p){return null;}
	@Override public String getServerInfo(){return null;}
	@Override public Servlet getServlet(String uri)throws ServletException{return null;}
	@Override public String getServletContextName(){return null;}
	@Override public Enumeration<String> getServletNames(){return null;}
	@Override public ServletRegistration getServletRegistration(String p){return null;}
	@Override public Map<String, ? extends ServletRegistration> getServletRegistrations(){return null;}
	@Override public Enumeration<Servlet> getServlets(){return null;}
	@Override public SessionCookieConfig getSessionCookieConfig(){return null;}
	@Override public String getVirtualServerName(){return null;}
	@Override public void log(String p){p("log:",p);}
	@Override public void log(Exception x, String p){x.printStackTrace();p(p);}
	@Override public void log(String p, Throwable x){log(p);x.printStackTrace();}
	@Override public void removeAttribute(String p){attribs.remove( p );}
	@Override public void setAttribute(String p, Object v){p(Name,".SrvltContxt.setAttribute:",p,",",v);attribs.put(p, v);}
	@Override public boolean setInitParameter(String arg0, String p2){return false;}
	@Override public void setSessionTrackingModes(Set<SessionTrackingMode> p){}

	Thread servrThread=null;

	static SrvltContxt sttc;public static SrvltContxt sttc(){return sttc==null?new SrvltContxt():sttc;}
	private SrvltContxt(){if(sttc==null)sttc=this;}

	void startServer(final int port){
		servrThread=new Thread( new Runnable() {public void run(){
			try{ServerSocket servrSocket = new ServerSocket( port );
				while(servrThread!=null){
					Socket socket=servrSocket.accept();
					PC pc=new PC();
					pc.q.inps=socket.getInputStream();
					pc.p.sos=new Rsp.Sos( socket.getOutputStream());
					pc.q.initFromInputStream(  );
					if(pc.q.cookieHeaders==null || pc.q.cookieHeaders.size()==0)
					{	if(pc.q.cookieHeaders==null)
						pc.q.cookieHeaders=new LinkedList<>(  );

					}
					Servlet s=getServlet(pc.q.uri); // if(s==null) s=new FSrvlt();
					s.service( pc.q,pc.p );
				}//while
			}catch(Exception ex )
			{p( "startServer:ex", ex ); }
		}//run
		}//Runnable
		);//Thread
		servrThread.start();
	}//startServer

}//SrvltContxt

}