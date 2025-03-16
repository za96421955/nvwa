package ai.nvwa.agent.tool.datastore.extract;

import ai.nvwa.agent.tool.datastore.extract.mode.Chunk;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.model.embedding.onnx.HuggingFaceTokenizer;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 提取器
 * <p> <功能详细描述> </p>
 *
 * @author 陈晨
 */
@Component
public class Extractor {
    private static final int DEFAULT_CHUNK_SIZE = 512;
    private static final int DEFAULT_OVER_LAP = 128;
    private static final String BAIKE_URL = "https://baike.baidu.com/item/%s?fromModule=lemma_search-box";

    /**
     * @description 文件类型检测
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public String fileTypeDetector(String filePath) {
        try {
            return new Tika().detect(new File(filePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @description 文件内容提取
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public String fileExtractor(String filePath) {
        try {
            return new Tika().parseToString(new File(filePath));
        } catch (IOException | TikaException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @description 文件内容提取
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public String fileExtractor(InputStream input) {
        try {
            return new Tika().parseToString(input);
        } catch (IOException | TikaException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @description 网站内容提取
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public String urlExtractor(String url) {
        try {
            return new Tika().parseToString(new URL(url));
        } catch (IOException | TikaException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @description 元数据提取
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public Map<String, String> metadataExtractor(FileInputStream input) {
        try {
            BodyContentHandler handler = new BodyContentHandler();
            Metadata metadata = new Metadata();
            AutoDetectParser parser = new AutoDetectParser();
            parser.parse(input, handler, metadata);
            Map<String, String> metas = new HashMap<>();
            for (String name : metadata.names()) {
                metas.put(name, metadata.get(name));
            }
            return metas;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyMap();
    }

    /**
     * @description 元数据提取
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public Map<String, String> metadataExtractor(String filePath) {
        try (FileInputStream input = new FileInputStream(filePath)) {
            return this.metadataExtractor(input);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyMap();
    }

    /**
     * @description 内容分块
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public List<Chunk> splitChunks(String content, int chunkSize, int overlap) {
        DocumentSplitter splitter = DocumentSplitters.recursive(chunkSize, overlap, new HuggingFaceTokenizer());
        return splitter.split(Document.document(content)).stream()
                .map(segment -> Chunk.builder()
                    .text(segment.text())
                    .metas(segment.metadata().toMap())
                    .build())
                .collect(Collectors.toList());
    }

    /**
     * @description 内容分块
     * <p>
     *     默认512token, 25%重叠
     * </p>
     *
     * @author 陈晨
     */
    public List<Chunk> splitChunks(String content) {
        return this.splitChunks(content, DEFAULT_CHUNK_SIZE, DEFAULT_OVER_LAP);
    }

    public static void main(String[] args) {
        String filePath = "/Users/chenchen/Downloads/HIoT演进升级概要设计说明书_初稿.docx";
        String url = String.format(BAIKE_URL, "%E6%B0%91%E6%B3%95%E5%85%B8%E5%A9%9A%E5%A7%BB%E5%AE%B6%E5%BA%AD%E7%BC%96%EF%BC%88%E8%8D%89%E6%A1%88%EF%BC%89");

        Extractor tika = new Extractor();
//        Map<String, String> metas = tika.metadataExtractor(filePath);
//        System.out.println("\n\n元数据:");
//        System.out.println(metas);

        String content = tika.urlExtractor(url);
        List<Chunk> chunks = tika.splitChunks(content);
        System.out.println("\n\n分块:");
        int i = 0;
        for (Chunk chunk : chunks) {
            System.out.println("\n\n<<<" + (++i) + ">>>");
            System.out.println("Metas: ");
            System.out.println(chunk.getMetas());
            System.out.println("Text: ");
            System.out.println(chunk.getText());
        }
    }

}


