package com.example.projetws.utils;

import android.content.Context;
import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class MultipartRequest extends Request<NetworkResponse> {
    private final Response.Listener<NetworkResponse> mListener;
    private final Response.ErrorListener mErrorListener;
    private final Map<String, String> mHeaders;
    private final Map<String, String> mStringParams;
    private final Map<String, File> mFileParams;
    private final String BOUNDARY = "Boundary-" + System.currentTimeMillis();
    private final String LINE_FEED = "\r\n";

    public MultipartRequest(int method, String url,
                            Map<String, String> headers,
                            Map<String, String> stringParams,
                            Map<String, File> fileParams,
                            Response.Listener<NetworkResponse> listener,
                            Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.mListener = listener;
        this.mErrorListener = errorListener;
        this.mHeaders = headers;
        this.mStringParams = stringParams;
        this.mFileParams = fileParams;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return (mHeaders != null) ? mHeaders : super.getHeaders();
    }

    @Override
    public String getBodyContentType() {
        return "multipart/form-data;boundary=" + BOUNDARY;
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);

        try {
            // Add string params
            if (mStringParams != null && !mStringParams.isEmpty()) {
                for (Map.Entry<String, String> entry : mStringParams.entrySet()) {
                    buildTextPart(dos, entry.getKey(), entry.getValue());
                }
            }

            // Add file params
            if (mFileParams != null && !mFileParams.isEmpty()) {
                for (Map.Entry<String, File> entry : mFileParams.entrySet()) {
                    buildFilePart(dos, entry.getKey(), entry.getValue());
                }
            }

            // End of multipart/form-data
            dos.writeBytes("--" + BOUNDARY + "--" + LINE_FEED);
            return bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                dos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private void buildTextPart(DataOutputStream dataOutputStream, String parameterName, String parameterValue) throws IOException {
        dataOutputStream.writeBytes("--" + BOUNDARY + LINE_FEED);
        dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"" + parameterName + "\"" + LINE_FEED);
        dataOutputStream.writeBytes("Content-Type: text/plain; charset=UTF-8" + LINE_FEED);
        dataOutputStream.writeBytes(LINE_FEED);
        dataOutputStream.writeBytes(parameterValue + LINE_FEED);
    }

    private void buildFilePart(DataOutputStream dataOutputStream, String fieldName, File uploadFile) throws IOException {
        String fileName = uploadFile.getName();
        String contentType = "application/octet-stream"; // Default to generic binary content type

        // Determine content type based on the file extension
        if (fileName.toLowerCase().endsWith(".jpg") || fileName.toLowerCase().endsWith(".jpeg")) {
            contentType = "image/jpeg";
        } else if (fileName.toLowerCase().endsWith(".png")) {
            contentType = "image/png";
        } else if (fileName.toLowerCase().endsWith(".gif")) {
            contentType = "image/gif";
        }
        // You can extend this list for other file types like PDFs, videos, etc.

        dataOutputStream.writeBytes("--" + BOUNDARY + LINE_FEED);
        dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + fileName + "\"" + LINE_FEED);
        dataOutputStream.writeBytes("Content-Type: " + contentType + LINE_FEED);
        dataOutputStream.writeBytes("Content-Transfer-Encoding: binary" + LINE_FEED);
        dataOutputStream.writeBytes(LINE_FEED);

        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(uploadFile);
            int bytesAvailable = fileInputStream.available();
            int maxBufferSize = 1024 * 1024;  // 1MB
            int bufferSize = Math.min(bytesAvailable, maxBufferSize);
            byte[] buffer = new byte[bufferSize];

            int bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            while (bytesRead > 0) {
                dataOutputStream.write(buffer, 0, bytesRead);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            }
            dataOutputStream.writeBytes(LINE_FEED);
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected Response<NetworkResponse> parseNetworkResponse(NetworkResponse response) {
        return Response.success(response, HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    protected void deliverResponse(NetworkResponse response) {
        mListener.onResponse(response);
    }
}
