package com.suje.http;

import android.util.Log;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpUriRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


public abstract class RangeFileAsyncHttpResponseHandler extends FileAsyncHttpResponseHandler {
	private static final String LOG_TAG = "RangeFileAsyncHttpResponseHandler";

	private long current = 0;
	private boolean append = false;

	/**
	 * Obtains new RangeFileAsyncHttpResponseHandler and stores response in passed file
	 * 
	 * @param file File to store response within, must not be null
	 */
	public RangeFileAsyncHttpResponseHandler(File file) {
		super(file);
	}

	@Override
	public void sendResponseMessage(HttpResponse response) throws IOException {
		if (!isCancle()) {
			StatusLine status = response.getStatusLine();
			if (status.getStatusCode() == HttpStatus.SC_REQUESTED_RANGE_NOT_SATISFIABLE){
				//already finished
				if (!isCancle())
					sendSuccessMessage(status.getStatusCode(), response.getAllHeaders(), null);
			}
			else if (status.getStatusCode() >= 300) {
				if (!isCancle())
					sendFailureMessage(status.getStatusCode(), response.getAllHeaders(), null, new HttpResponseException(status.getStatusCode(), status.getReasonPhrase()));
			}
			else {
				if (!isCancle()) {
//					Header header = response.getFirstHeader("Content-Range");
//					if (header == null) {
//						append = false;
//						current = 0;
//					}
//					else
					byte[] datas = getResponseData(response.getEntity());
					if(!isCancle()){
						sendSuccessMessage(status.getStatusCode(), response.getAllHeaders(), datas);
					}
				}
			}
		}
	}

	@Override
	protected byte[] getResponseData(HttpEntity entity) throws IOException {
		if (entity != null) {
			InputStream instream = entity.getContent();
			long contentLength = entity.getContentLength() + current;
			FileOutputStream buffer = new FileOutputStream(getTargetFile(), append);
			if (instream != null) {
				try {
					byte[] tmp = new byte[BUFFER_SIZE];
					int l;
					while (current < contentLength && (l = instream.read(tmp)) != -1 && !Thread.currentThread().isInterrupted() && !isCancle())
					{
						current += l;
						buffer.write(tmp, 0, l);
						sendProgressMessage((int)current, (int)contentLength);
					}
				} finally {
					instream.close();
					buffer.flush();
					buffer.close();
				}
			}
		}
		return null;
	}

	public void updateRequestHeaders(HttpUriRequest uriRequest) {
		if (mFile.exists() && mFile.canWrite())
			current = mFile.length();
		if (current > 0) {
			append = true;
			uriRequest.setHeader("Range", "bytes=" + current + "-");
		}
	}
}
