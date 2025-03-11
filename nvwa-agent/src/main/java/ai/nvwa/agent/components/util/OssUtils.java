//package ai.nvwa.agent.components.util;
//
//import com.alibaba.fastjson.JSON;
//import org.apache.http.HttpEntity;
//import org.apache.http.client.methods.CloseableHttpResponse;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.entity.ContentType;
//import org.apache.http.entity.mime.HttpMultipartMode;
//import org.apache.http.entity.mime.MultipartEntityBuilder;
//import org.apache.http.entity.mime.content.ByteArrayBody;
//import org.apache.http.entity.mime.content.InputStreamBody;
//import org.apache.http.impl.client.CloseableHttpClient;
//import org.apache.http.impl.client.HttpClients;
//import org.apache.http.util.EntityUtils;
//
//import java.io.*;
//import java.net.URL;
//import java.util.UUID;
//
//public class OssUtils {
//
//    public static InputStream readFileAsStream(String sourceUrl) throws IOException {
//        URL url = new URL(sourceUrl);
//        return url.openStream();
//    }
//
//    public static byte[] readFileAsBytes(String sourceUrl) throws IOException {
//        URL url = new URL(sourceUrl);
//        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//        try (InputStream is = url.openStream()) {
//            int length = -1;
//            byte[] buff = new byte[1024];
//            while (-1 != (length = is.read(buff))) {
//                byteArrayOutputStream.write(buff, 0, length);
//                byteArrayOutputStream.flush();
//            }
//        }
//        return byteArrayOutputStream.toByteArray();
//    }
//
//    /**
//     * 以流的方式上传
//     * @param inputStream 文件流
//     * @param uploadUrl http://192.168.1.10:7001/oss/uploadFile
//     * @param packageName wms
//     * @param fileName 要有后缀名
//     * @return
//     * @throws Exception
//     */
//    public static String uploadFile(InputStream inputStream, String uploadUrl, String packageName, String fileName) throws Exception {
//        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
//            HttpPost httpPost = new HttpPost(uploadUrl);
//            MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create().setMode(HttpMultipartMode.RFC6532);
//            entityBuilder.addTextBody(" packName", packageName);
//            entityBuilder.addTextBody(" fileName", fileName, ContentType.create("text/plain", "UTF-8"));
//            entityBuilder.addPart("file", new InputStreamBody(inputStream, ContentType.create("text/plain", "UTF-8"), fileName));
//            httpPost.setEntity(entityBuilder.build());
//            System.out.println("executing request " + httpPost.getRequestLine());
//            try (CloseableHttpResponse response = httpclient.execute(httpPost);) {
//                System.out.println(response.getStatusLine());
//                HttpEntity resEntity = response.getEntity();
//                String result = EntityUtils.toString(resEntity);
//                System.out.println(result);
//                return JSON.parseObject(result).getString("data");
//            }
//        }
//    }
//
//    /**
//     * 以字节数组上传
//     * @param uploadUrl http://192.168.1.10:7001/oss/uploadFile
//     * @param packageName wms
//     * @param fileBytes 文件字节数组
//     * @return
//     * @throws Exception
//     */
//    public static String uploadFile(String uploadUrl, String packageName, byte[] fileBytes) throws Exception {
//        try (CloseableHttpClient httpclient = HttpClients.createDefault();) {
//            HttpPost httpPost = new HttpPost(uploadUrl);
//            MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
//            entityBuilder.addTextBody(" packName", packageName);
//            entityBuilder.addTextBody(" fileName", UUID.randomUUID().toString().replaceAll("-", "") + ".png");
//            entityBuilder.addPart("file", new ByteArrayBody(fileBytes, "file"));
//            httpPost.setEntity(entityBuilder.build());
//            System.out.println("executing request " + httpPost.getRequestLine());
//            try (CloseableHttpResponse response = httpclient.execute(httpPost);) {
//                System.out.println(response.getStatusLine());
//                HttpEntity resEntity = response.getEntity();
//                String result = EntityUtils.toString(resEntity);
//                System.out.println(result);
//                return JSON.parseObject(result).getString("data");
//            }
//        }
//    }
//
//    public static byte[] getByteToFile(String physicalFileName) {
//        File file = new File(physicalFileName);
//        byte[] by = new byte[(int) file.length()];
//        InputStream is = null;
//        try {
//            is = new FileInputStream(file);
//            ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
//            byte[] bb = new byte[2048];
//            int ch = is.read(bb);
//            while (ch != -1) {
//                bytestream.write(bb, 0, ch);
//                ch = is.read(bb);
//            }
//            by = bytestream.toByteArray();
//        } catch (Exception e) {
//            e.getStackTrace();
//        } finally {
//            freeIO(is, null);
//        }
//        return by;
//    }
//
//    public static void freeIO(InputStream is, OutputStream os) {
//        try {
//            if (is != null) {
//                is.close();
//            }
//            if (os != null) {
//                os.close();
//            }
//        } catch (IOException e) {
//            e.getStackTrace();
//        }
//    }
//
//}
//
//
