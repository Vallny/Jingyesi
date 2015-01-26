package com.vallny.jing.util.http;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONException;

import android.text.TextUtils;

import com.vallny.jing.R;
import com.vallny.jing.global.GlobalContext;
import com.vallny.jing.util.file.FileManager;
import com.vallny.jing.util.global.Utility;

public class HttpUtility {

	private static final int CONNECT_TIMEOUT = 10 * 1000;
	private static final int READ_TIMEOUT = 10 * 1000;

	private static final int DOWNLOAD_CONNECT_TIMEOUT = 15 * 1000;
	private static final int DOWNLOAD_READ_TIMEOUT = 60 * 1000;

	private static final int UPLOAD_CONNECT_TIMEOUT = 15 * 1000;
	private static final int UPLOAD_READ_TIMEOUT = 5 * 60 * 1000;
	private static final String PREFIX = "--";
	private static final String LINE_END = "\r\n";

	private static HttpUtility httpUtility = new HttpUtility();

	private HttpUtility() {
	}

	public static HttpUtility getInstance() {
		return httpUtility;
	}

	public boolean executeNormalTask(HttpMethod httpMethod, String url, Map<String, String> param) {

		switch (httpMethod) {
		case Post:
			return doPost(url, param);
			// case Get:
			// return doGet(url, param);
		}
		return false;

	}

	// public boolean executeDownloadTask(String url, String path,
	// FileDownloaderHttpHelper.DownloadListener downloadListener) {
	// return !Thread.currentThread().isInterrupted() && new
	// JavaHttpUtility().doGetSaveFile(url, path, downloadListener);
	// }

	public boolean executeUploadTask(String url, Map<String, String> param, String path, String imageParamName, FileUploaderHttpHelper.ProgressListener listener) {
		return doUploadFile(url, param, path, imageParamName, listener);
	}

	private static Proxy getProxy() {
		String proxyHost = System.getProperty("http.proxyHost");
		String proxyPort = System.getProperty("http.proxyPort");
		if (!TextUtils.isEmpty(proxyHost) && !TextUtils.isEmpty(proxyPort))
			return new Proxy(java.net.Proxy.Type.HTTP, new InetSocketAddress(proxyHost, Integer.valueOf(proxyPort)));
		else
			return null;
	}

	public boolean doPost(String urlAddress, Map<String, String> param) {
		GlobalContext globalContext = GlobalContext.getInstance();
		String errorStr = globalContext.getString(R.string.online_error);
		globalContext = null;
		try {
			URL url = new URL(urlAddress);
			Proxy proxy = getProxy();
			HttpsURLConnection uRLConnection;
			if (proxy != null)
				uRLConnection = (HttpsURLConnection) url.openConnection(proxy);
			else
				uRLConnection = (HttpsURLConnection) url.openConnection();

			uRLConnection.setDoInput(true);
			uRLConnection.setDoOutput(true);
			uRLConnection.setRequestMethod("POST");
			uRLConnection.setUseCaches(false);
			uRLConnection.setConnectTimeout(CONNECT_TIMEOUT);
			uRLConnection.setReadTimeout(READ_TIMEOUT);
			uRLConnection.setInstanceFollowRedirects(false);
			uRLConnection.setRequestProperty("Connection", "Keep-Alive");
			uRLConnection.setRequestProperty("Charset", "UTF-8");
			uRLConnection.setRequestProperty("Accept-Encoding", "gzip, deflate");
			uRLConnection.connect();

			DataOutputStream out = new DataOutputStream(uRLConnection.getOutputStream());
			out.write(Utility.encodeUrl(param).getBytes());
			out.flush();
			out.close();
			return handleResponse(uRLConnection);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;

	}

	public boolean doGetSaveFile(String urlStr, String path, FileDownloaderHttpHelper.DownloadListener downloadListener) {

		File file = FileManager.createNewFileInSDCard(path);
		if (file == null) {
			return false;
		}

		BufferedOutputStream out = null;
		InputStream in = null;
		HttpURLConnection urlConnection = null;
		try {

			URL url = new URL(urlStr);
			Proxy proxy = getProxy();
			if (proxy != null)
				urlConnection = (HttpURLConnection) url.openConnection(proxy);
			else
				urlConnection = (HttpURLConnection) url.openConnection();

			urlConnection.setRequestMethod("GET");
			urlConnection.setDoOutput(false);
			urlConnection.setConnectTimeout(DOWNLOAD_CONNECT_TIMEOUT);
			urlConnection.setReadTimeout(DOWNLOAD_READ_TIMEOUT);
			urlConnection.setRequestProperty("Connection", "Keep-Alive");
			urlConnection.setRequestProperty("Charset", "UTF-8");
			urlConnection.setRequestProperty("Accept-Encoding", "gzip, deflate");

			urlConnection.connect();

			int status = urlConnection.getResponseCode();

			if (status != HttpURLConnection.HTTP_OK) {
				return false;
			}

			int bytetotal = (int) urlConnection.getContentLength();
			int bytesum = 0;
			int byteread = 0;
			out = new BufferedOutputStream(new FileOutputStream(file));
			in = new BufferedInputStream(urlConnection.getInputStream());

			final Thread thread = Thread.currentThread();
			byte[] buffer = new byte[1444];
			while ((byteread = in.read(buffer)) != -1) {
				if (thread.isInterrupted()) {
					file.delete();
					throw new InterruptedIOException();
				}

				bytesum += byteread;
				out.write(buffer, 0, byteread);
				if (downloadListener != null && bytetotal > 0) {
					downloadListener.pushProgress(bytesum, bytetotal);
				}
			}
			if (downloadListener != null) {
				downloadListener.completed();
			}
			return true;

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			Utility.closeSilently(in);
			Utility.closeSilently(out);
			if (urlConnection != null)
				urlConnection.disconnect();
		}

		return false;
	}

	private boolean handleResponse(HttpURLConnection httpURLConnection) {
		GlobalContext globalContext = GlobalContext.getInstance();
		String errorStr = globalContext.getString(R.string.online_error);
		globalContext = null;
		int status = 0;
		try {
			status = httpURLConnection.getResponseCode();
		} catch (IOException e) {
			e.printStackTrace();
			httpURLConnection.disconnect();
			return false;
		}

		if (status != HttpURLConnection.HTTP_OK) {
			return false;
		}

		// return readResult(httpURLConnection);
		return true;
	}

	private String readResult(HttpURLConnection urlConnection) {
		InputStream is = null;
		BufferedReader buffer = null;
		GlobalContext globalContext = GlobalContext.getInstance();
		String errorStr = globalContext.getString(R.string.online_error);
		globalContext = null;
		try {
			is = urlConnection.getInputStream();

			String content_encode = urlConnection.getContentEncoding();

			if (null != content_encode && !"".equals(content_encode) && content_encode.equals("gzip")) {
				is = new GZIPInputStream(is);
			}

			buffer = new BufferedReader(new InputStreamReader(is));
			StringBuilder strBuilder = new StringBuilder();
			String line;
			while ((line = buffer.readLine()) != null) {
				strBuilder.append(line);
			}

			return strBuilder.toString();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			Utility.closeSilently(is);
			Utility.closeSilently(buffer);
			urlConnection.disconnect();
		}
		return errorStr;

	}

	private static String getBoundry() {
		StringBuffer _sb = new StringBuffer();
		for (int t = 1; t < 12; t++) {
			long time = System.currentTimeMillis() + t;
			if (time % 3 == 0) {
				_sb.append((char) time % 9);
			} else if (time % 3 == 1) {
				_sb.append((char) (65 + time % 26));
			} else {
				_sb.append((char) (97 + time % 26));
			}
		}
		return _sb.toString();
	}

	private String getBoundaryMessage(String boundary, Map params, String fileField, String fileName, String fileType) {
		StringBuffer res = new StringBuffer(PREFIX).append(boundary).append(LINE_END);

		Iterator keys = params.keySet().iterator();

		while (keys.hasNext()) {
			String key = (String) keys.next();
			String value = (String) params.get(key);
			res.append("Content-Disposition: form-data; name=\"").append(key).append("\"").append(LINE_END).append(LINE_END).append(value).append(LINE_END).append(PREFIX).append(boundary)
					.append(LINE_END);
		}
		res.append("Content-Disposition: form-data; name=\"").append(fileField).append("\"; filename=\"").append(fileName).append("\"").append(LINE_END).append("Content-Type: ").append(fileType)
				.append("\r\n\r\n");

		return res.toString();
	}

	public boolean doUploadFile(String urlStr, Map<String, String> param, String path, String imageParamName, final FileUploaderHttpHelper.ProgressListener listener) {
		String BOUNDARYSTR = getBoundry();

		File targetFile = new File(path);

		byte[] barry = null;
		int contentLength = 0;
		String sendStr = "";
		int line_end_length = 0;
		try {
			barry = (PREFIX + BOUNDARYSTR + PREFIX + LINE_END).getBytes("UTF-8");
			line_end_length = LINE_END.getBytes("UTF-8").length;

			sendStr = getBoundaryMessage(BOUNDARYSTR, param, imageParamName, new File(path).getName(), "image/png");
			contentLength = sendStr.getBytes("UTF-8").length + (int) targetFile.length() + barry.length + line_end_length;
		} catch (UnsupportedEncodingException e) {
return false;
		}
		int totalSent = 0;
		String lenstr = Integer.toString(contentLength);

		HttpURLConnection urlConnection = null;
		BufferedOutputStream out = null;
		FileInputStream fis = null;
		GlobalContext globalContext = GlobalContext.getInstance();
		String errorStr = globalContext.getString(R.string.online_error);
		globalContext = null;
		try {
			URL url = null;

			url = new URL(urlStr);

			Proxy proxy = getProxy();
			if (proxy != null)
				urlConnection = (HttpURLConnection) url.openConnection(proxy);
			else
				urlConnection = (HttpURLConnection) url.openConnection();

			urlConnection.setConnectTimeout(UPLOAD_CONNECT_TIMEOUT);
			urlConnection.setReadTimeout(UPLOAD_READ_TIMEOUT);
			urlConnection.setDoInput(true);
			urlConnection.setDoOutput(true);
			urlConnection.setRequestMethod("POST");
			urlConnection.setUseCaches(false);
			urlConnection.setRequestProperty("Connection", "Keep-Alive");
			urlConnection.setRequestProperty("Charset", "UTF-8");
			urlConnection.setRequestProperty("Content-type", "multipart/form-data;boundary=" + BOUNDARYSTR);
			urlConnection.setRequestProperty("Content-Length", lenstr);
			((HttpURLConnection) urlConnection).setFixedLengthStreamingMode(contentLength);
			urlConnection.connect();

			out = new BufferedOutputStream(urlConnection.getOutputStream());
			out.write(sendStr.getBytes("UTF-8"));
			totalSent += sendStr.getBytes("UTF-8").length;

			fis = new FileInputStream(targetFile);

			int bytesRead;
			int bytesAvailable;
			int bufferSize;
			byte[] buffer;
			int maxBufferSize = 1 * 1024;

			bytesAvailable = fis.available();
			bufferSize = Math.min(bytesAvailable, maxBufferSize);
			buffer = new byte[bufferSize];
			bytesRead = fis.read(buffer, 0, bufferSize);
			long transferred = 0;
			final Thread thread = Thread.currentThread();
			while (bytesRead > 0) {

				if (thread.isInterrupted()) {
					targetFile.delete();
					throw new InterruptedIOException();
				}
				out.write(buffer, 0, bufferSize);
				bytesAvailable = fis.available();
				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				bytesRead = fis.read(buffer, 0, bufferSize);
				transferred += bytesRead;
				if (transferred % 50 == 0){
					out.flush();
				}
				if (listener != null){
					listener.transferred(transferred);
				}
			}

			out.write(LINE_END.getBytes("UTF-8"));
			totalSent += line_end_length;
			out.write(barry);
			totalSent += barry.length;
			out.flush();
			out.close();
			if (listener != null) {
				listener.waitServerResponse();
			}
			int status = urlConnection.getResponseCode();

			if (status != HttpURLConnection.HTTP_OK) {
				return false;
			}

		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} finally {
			Utility.closeSilently(fis);
			Utility.closeSilently(out);
			if (urlConnection != null)
				urlConnection.disconnect();
		}

		return true;
	}

	public enum HttpMethod {
		Post, Get, Get_AVATAR_File, Get_PICTURE_FILE
	}

}
