package ai.nvwa.tool.datastore.extract;

import ai.nvwa.components.util.HttpClient;
import ai.nvwa.tool.datastore.extract.mode.Chunk;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.model.embedding.onnx.HuggingFaceTokenizer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
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
import java.util.*;
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

    /**
     * @description 获取摘要
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public String getAbstract(String content, int chunkSize, int overlap, int topK) {
        List<Chunk> chunks = this.splitChunks(content
                .replaceAll("\\s+", " ")
                .replaceAll(" ", " "), chunkSize, overlap);
        Map<String, Double> tfidf = this.calculateTFIDF(chunks);
        StringBuilder abs = new StringBuilder();
        for (Chunk chunk : chunks) {
            for (String sentence : this.scoreSentences(chunk, tfidf, topK)) {
                abs.append(sentence).append("\n");
            }
        }
        return abs.toString();
    }

    /**
     * @description 统计每个分块中的高频词汇，作为摘要的核心内容标识
     * <p>
     *      算法原理：
     *      TF：词频（Term Frequency）= 该词在分块中出现的次数 / 分块总词数
     *      IDF：逆网页频率（Inverse Document Frequency）= log(总分块数 / 含该词的块数)
     * </p>
     *
     * @author 陈晨
     */
    public Map<String, Double> calculateTFIDF(List<Chunk> chunks) {
        Map<String, Integer> wordCount = new HashMap<>();
        Map<String, Integer> docCount = new HashMap<>();
        // 分词
        for (Chunk chunk : chunks) {
            String[] words = chunk.getText().split("\\s+");
            for (String word : words) {
                if (StringUtils.isBlank(word)) {
                    continue;
                }
                wordCount.put(word, wordCount.getOrDefault(word, 0) + 1);
            }
        }
        for (String word : wordCount.keySet()) {
            docCount.put(word, Collections.frequency(wordCount.keySet(), word));
        }
        // 词频统计
        Map<String, Double> tfidf = new HashMap<>();
        for (String word : wordCount.keySet()) {
            double tf = (double) wordCount.get(word) / chunks.size();
            double idf = Math.log((double) chunks.size() / (1 + docCount.get(word)));
            tfidf.put(word, tf * idf);
        }
        return tfidf;
    }

    /**
     * @description 根据关键词权重和句子位置综合评分，选取高评分句子组成摘要
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    private List<String> scoreSentences(Chunk chunk, Map<String, Double> tfidf, int topK) {
        List<String> sentences = Arrays.asList(chunk.getText().split("[。！？]"));
        List<ScoredSentence> scored = new ArrayList<>();
        for (String sentence : sentences) {
            double score = 0;
            String[] words = sentence.split("\\s+");
            for (String word : words) {
                score += tfidf.getOrDefault(word, 0.0);
            }
            // 加权位置得分（前1/3句子权重更高）
            int position = sentences.indexOf(sentence);
            score += 1.5 * (position < sentences.size() / 3 ? 1 : 0.5);
            scored.add(new ScoredSentence(sentence, score));
        }
        // score排序
        scored.sort((a, b) -> Double.compare(b.score, a.score));
        return scored.subList(0, Math.min(topK, scored.size())).stream()
                .map(ScoredSentence::getText)
                .collect(Collectors.toList());
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    private static class ScoredSentence {
        private String text;
        private double score;
    }

    public static void main(String[] args) {
        String url = "https://www.baidu.com/s?wd=Agent%E6%9E%B6%E6%9E%84";
        String html = HttpClient.get(url)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0.0.0")
                .asString();
        System.out.println(html);
    }

}


