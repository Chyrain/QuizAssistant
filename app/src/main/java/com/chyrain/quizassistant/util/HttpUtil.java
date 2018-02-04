package com.chyrain.quizassistant.util;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HttpUtil {
    private static final String TAG = "HttpUtil";
    private static final boolean USE_HTTPS = false;
    private static final int SOCKET_TIMEOUT = 20000;
    private static final int UPLOAD_TIMEOUT = 30000;

    private static final int MAX_PIC_SIZE = 1500; // 最大上传图片大小1500KB
    private static final int MIN_PIC_SIZE_UNCOMPRESS = 1500; // 最低不压缩图片大小1500KB

    public enum HttpMethod {
        POST,
        GET
    }

    public static String urlStringCheck(String path) {
        if (path.startsWith("http://") || path.startsWith("https://")) {
            return path;
        } else if (path.startsWith("//")) {
            return "http:" + path;
        } else {
            return "http://" + path;
        }
    }

    public static void post(final String url, final String entity, final HttpResponseHandler handler) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json");
                headers.put("Origin", "http://chat.v5kf.com");
                // 获得上传信息的字节大小以及长度
                StringBuffer buffer = new StringBuffer();
                if (entity != null) {
                    buffer.append(entity);
                }
                byte[] myData = buffer.toString().getBytes();
                httpSync(url, HttpMethod.POST, myData, headers, handler);
            }
        }).start();
    }

    public static void get(final String url, Map<String, String> headers, final HttpResponseHandler handler) {
        if (headers == null) {
            headers = new HashMap<String, String>();
            headers.put("Content-Type", "application/json");
            headers.put("Origin", "http://chat.v5kf.com");
        }
        final Map<String, String> _headers = headers;
        new Thread(new Runnable() {

            @Override
            public void run() {
                httpSync(url, HttpMethod.GET, null, _headers, handler);
            }
        }).start();
    }

    public static void get(final String url, final String auth, final HttpResponseHandler handler) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json");
                headers.put("Origin", "http://chat.v5kf.com");
                headers.put("Authorization", auth);
                httpSync(url, HttpMethod.GET, null, headers, handler);
            }
        }).start();
    }

    public static void httpSync(String path, HttpMethod method, byte[] myData, Map<String, String> headers, HttpResponseHandler handler) {
        URL url;
        try {
            String urlStr = path; // urlStringCheck(path);
            Logger.d(TAG, "[httpSync] path:" + path);
            url = new URL(urlStr);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(30000);
            urlConnection.setReadTimeout(30000);
            urlConnection.setDoInput(true);// 表示从服务器获取数据
            if (method == HttpMethod.POST) {
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);// 表示向服务器写数据
            } else if (method == HttpMethod.GET) {
                urlConnection.setRequestMethod("GET");
            }
            // 设置请求的头
//			urlConnection.setRequestProperty("Connection", "keep-alive");
//            urlConnection.setRequestProperty("origin", V5ClientConfig.ORIGIN);
//            urlConnection.setRequestProperty("Charset","UTF-8");
//            urlConnection.setRequestProperty("Content-Type",
//                    "application/json");
            if (headers != null && !headers.isEmpty()) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    urlConnection.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }

            if (HttpMethod.POST == method && myData != null) { // POST输出数据
                urlConnection.setRequestProperty("Content-Length",
                        String.valueOf(myData.length));
                Logger.d(TAG, "Content-Length:" + String.valueOf(myData.length));
                // 获得输出流,向服务器输出数据
                OutputStream outputStream = urlConnection.getOutputStream();
                outputStream.write(myData);
                outputStream.flush();
                outputStream.close();
            }
            int responseCode = urlConnection.getResponseCode();
            if (responseCode == 200) {
                InputStream is = urlConnection.getInputStream();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] data = new byte[1024];
                int len = 0;
                String result = "";
                if (is != null) {
                    try {
                        while ((len = is.read(data)) != -1) {
                            baos.write(data, 0, len);
                        }
                        // 释放资源
                        is.close();
                        baos.close();

                        result = new String(baos.toByteArray(), "UTF-8");
                        if (handler != null) {
                            handler.onSuccess(responseCode, result);
                            return;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        if (handler != null) {
                            handler.onFailure(responseCode, e.getMessage());
                            return;
                        }
                    }
                }
            }
            if (handler != null) {
                handler.onFailure(responseCode, "no InputStream be read");
                return;
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            if (handler != null) {
                handler.onFailure(-11, e.getMessage());
                return;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            if (handler != null) {
                handler.onFailure(-12, e.getMessage());
                return;
            }
        } catch (ProtocolException e) {
            e.printStackTrace();
            if (handler != null) {
                handler.onFailure(-13, e.getMessage());
                return;
            }
        } catch (SocketTimeoutException e) {
            if (handler != null) {
                handler.onFailure(1, "<SocketTimeoutException> " + e.getMessage());
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (handler != null) {
                handler.onFailure(-14, e.getMessage());
                return;
            }
        }
    }

    public static byte[] byteAppend(byte[] byte_1, byte[] byte_2){
        byte[] byte_3 = new byte[byte_1.length+byte_2.length];
        System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);
        System.arraycopy(byte_2, 0, byte_3, byte_1.length, byte_2.length);
        return byte_3;
    }

    public static void postFile(final String contentType, final File file, final String urlStr,
                                final String authorization,
                                final HttpResponseHandler httpResponseHandler) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                Logger.v(TAG, "[postFile] url:" + urlStr + " file:" + file.getName());

                String BOUNDARY = "----" + UUID.randomUUID().toString(); // 边界标识 随机生成
                final String PREFIX = "--", LINE_END = "\r\n";
                final String CONTENT_TYPE = "multipart/form-data";

                String firstBoundary = PREFIX + BOUNDARY + LINE_END;
//				String commonBoundary = LINE_END + firstBoundary;
                String lastBoundary = LINE_END + PREFIX + BOUNDARY + PREFIX + LINE_END;

                StringBuffer fileContent = new StringBuffer();
                fileContent.append("Content-Disposition: form-data; name=\"FileContent\"; filename=\""
                        + file.getName() + "\"" + LINE_END);
                fileContent.append("Content-Type: " + contentType + LINE_END);
                fileContent.append(LINE_END);

                URL url;
                try {
                    String Url = urlStr;
                    if (USE_HTTPS) {
                        if (urlStr.startsWith("http://")) {
                            // http头替换成https头
                            StringBuffer str=new StringBuffer(urlStr);
                            str.replace(0, 7, "https://");
                            Url = str.toString();
                            Logger.w("HttpUtil", urlStr);
                        } else if (urlStr.startsWith("https://")) {
                            // 无需改动
                        } else {
                            // 添加https头
                            Url = "https://" + urlStr;
                        }
                    }

                    url = new URL(Url);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setConnectTimeout(SOCKET_TIMEOUT);
                    urlConnection.setReadTimeout(UPLOAD_TIMEOUT);
                    urlConnection.setDoInput(true);// 表示从服务器获取数据
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setDoOutput(true);// 表示向服务器写数据

                    urlConnection.setRequestProperty("Authorization", authorization);
                    urlConnection.setRequestProperty("Connection", "keep-alive");
                    urlConnection.setRequestProperty("Origin", "http://chat.v5kf.com");
                    urlConnection.setRequestProperty("Content-Type", CONTENT_TYPE + "; boundary="
                            + BOUNDARY);
                    long contentLength = firstBoundary.length() + fileContent.length() + lastBoundary.length();
                    long fileSize = Util.getFileSize(file);
                    byte[] imageBuffer = null;
                    if (contentType.startsWith("image") &&
                            fileSize / 1000 > MIN_PIC_SIZE_UNCOMPRESS) {
                        imageBuffer = Util.compressImageToByteArray(Util.getCompressBitmap(file.getAbsolutePath()), MAX_PIC_SIZE);
                        contentLength += imageBuffer.length;
                    } else {
                        contentLength += fileSize;
                    }
                    Logger.d("HttpUtil", "Content-Length:" + contentLength);
//					urlConnection.setRequestProperty("Content-Length",
//							String.valueOf(contentLength));
//					Logger.d("HttpUtil", "set Content-Length:" + contentLength);

                    DataOutputStream ds = new DataOutputStream(urlConnection.getOutputStream());
                    ds.writeBytes(firstBoundary);
                    ds.writeBytes(fileContent.toString());

                    // 读取文件转为字节流
					/* 写入文件数据 */
                    if (contentType.startsWith("image")) {
                        if (fileSize / 1000 <= MIN_PIC_SIZE_UNCOMPRESS) {
                            try {
                                FileInputStream fstream = new FileInputStream(file);
                                //ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
								/* 设定每次写入1024bytes */
                                int bufferSize = 1024;
                                byte[] buffer = new byte[bufferSize];
                                int n;
                                while ((n = fstream.read(buffer)) != -1) {
									/* 将数据写入DataOutputStream中 */
                                    ds.write(buffer, 0, n);
                                }
                                fstream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else { // 压缩图片
                            if (imageBuffer != null) {
                                Logger.d(TAG, "CompressSize>>>:" + imageBuffer.length);
                            } else {
                                Logger.e(TAG, "CompressSize>>>: null");
                                ds.close();
                                return;
                            }
                            ds.write(imageBuffer, 0, imageBuffer.length);
                        }
                    } else {
                        try {
                            FileInputStream stream = new FileInputStream(file);
                            //ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
                            int bufferSize = 1024;
                            byte[] buffer = new byte[bufferSize];
                            int n;
                            while ((n = stream.read(buffer)) != -1) {
								/* 将数据写入DataOutputStream中 */
                                ds.write(buffer, 0, n);
                            }
                            stream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    ds.writeBytes(lastBoundary);
                    ds.flush();
                    ds.close();

					/* 获得Response内容 */
                    int responseCode = urlConnection.getResponseCode();
                    if (responseCode == 200) {
                        InputStream is = urlConnection.getInputStream();
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        byte[] data = new byte[1024];
                        if (is != null) {
                            try {
                                int len;
                                while ((len = is.read(data)) != -1) {
                                    baos.write(data, 0, len);
                                }
                                // 释放资源
                                is.close();
                                baos.close();

                                String result = new String(baos.toByteArray(), "UTF-8");
                                if (httpResponseHandler != null) {
                                    httpResponseHandler.onSuccess(responseCode, result);
                                    return;
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                                if (httpResponseHandler != null) {
                                    httpResponseHandler.onFailure(responseCode, e.getMessage());
                                    return;
                                }
                            }
                        }
                    }
                    if (httpResponseHandler != null) {
                        httpResponseHandler.onFailure(responseCode, "no InputStream be read");
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    if (httpResponseHandler != null) {
                        httpResponseHandler.onFailure(-11, e.getMessage());
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    if (httpResponseHandler != null) {
                        httpResponseHandler.onFailure(-12, e.getMessage());
                    }
                } catch (ProtocolException e) {
                    e.printStackTrace();
                    if (httpResponseHandler != null) {
                        httpResponseHandler.onFailure(-13, e.getMessage());
                    }
                } catch (SocketTimeoutException e) {
                    if (httpResponseHandler != null) {
                        httpResponseHandler.onFailure(-10, "<SocketTimeoutException> " + e.getMessage());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    if (httpResponseHandler != null) {
                        httpResponseHandler.onFailure(-14, e.getMessage());
                    }
                }
            }
        }).start();
    }
}
