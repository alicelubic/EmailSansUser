package com.alicelubic.fishercenteremailform;

/**
 * Created by owlslubic on 12/1/16.
 */

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;

/**
 * here i implement a datasource for data to send...
 */
public class ByteArrayDataSource implements DataSource {
    private byte[] mData;
    private String mType;

    public ByteArrayDataSource(byte[] data, String type) {
        super();
        mData = data;
        mType = type;
    }

    public ByteArrayDataSource(byte[] data) {
        super();
        mData = data;
    }

    public void setType(String type) {
        mType = type;
    }

    public String getContentType() {
        if (mType == null)
            return "application/octet-stream";
        else
            return mType;
    }

    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(mData);
    }

    public String getName() {
        return "ByteArrayDataSource";
    }

    public OutputStream getOutputStream() throws IOException {
        throw new IOException("Not Supported");
    }
}