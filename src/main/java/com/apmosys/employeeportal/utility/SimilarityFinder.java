package com.apmosys.employeeportal.utility;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.apmosys.employeeportal.model.QuestionMaster;
import com.apmosys.employeeportal.repository.QuestionMasterRepository;

import ai.djl.Application;
import ai.djl.ModelException;
import ai.djl.huggingface.translator.TextEmbeddingTranslatorFactory;
import ai.djl.inference.Predictor;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.TranslateException;

@Component
public class SimilarityFinder {

    @Autowired
    QuestionMasterRepository questionMasterRepository;

    private static final double SIMILARITY_THRESHOLD = 0.55;
    private static final int BATCH_SIZE = 100;
    private static final int MAX_THREADS = 4;

    private ZooModel<String, float[]> model;
    private Predictor<String, float[]> predictor;
    private ExecutorService executorService;
    
    // Cache for question embeddings
    private Map<Long, float[]> questionEmbeddingsCache = new ConcurrentHashMap<>();
    private volatile List<QuestionMaster> cachedQuestionList = new ArrayList<>();

    @PostConstruct
    public void init() throws IOException, ModelException {
//        Criteria<String, float[]> criteria = Criteria.builder()
//                .setTypes(String.class, float[].class)
//                .optApplication(Application.NLP.TEXT_EMBEDDING)
//                .optEngine("PyTorch")
//                .optModelUrls("djl://ai.djl.huggingface.pytorch/sentence-transformers/all-mpnet-base-v2")
//                .optTranslatorFactory(new TextEmbeddingTranslatorFactory())
//                .build();
//
//        this.model = criteria.loadModel();
//        this.predictor = model.newPredictor();
//        this.executorService = Executors.newFixedThreadPool(MAX_THREADS);
//
//        refreshCache();
    }

    @PreDestroy
    public void close() throws Exception {
        if (executorService != null) executorService.shutdown();
        if (predictor != null) predictor.close();
        if (model != null) model.close();
    }

    public void refreshCache() {
        this.cachedQuestionList = questionMasterRepository.findAll();
        // Pre-compute embeddings for all questions
        precomputeEmbeddings();
    }

    private void precomputeEmbeddings() {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        
        // Split the list into batches
        for (int i = 0; i < cachedQuestionList.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, cachedQuestionList.size());
            List<QuestionMaster> batch = cachedQuestionList.subList(i, end);
            
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                for (QuestionMaster question : batch) {
                    try {
                        String processedText = preprocessText(question.getQuestion());
                        float[] embedding = predictor.predict(processedText);
                        questionEmbeddingsCache.put(question.getQuestionMasterId(), embedding);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, executorService);
            
            futures.add(future);
        }
        
        // Wait for all batches to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    private static String preprocessText(String text) {
        return text.toLowerCase()
                .replaceAll("[^a-zA-Z0-9\\s]", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private static double cosineSimilarity(float[] vectorA, float[] vectorB) {
        double dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < vectorA.length; i++) {
            dot += vectorA[i] * vectorB[i];
            normA += vectorA[i] * vectorA[i];
            normB += vectorB[i] * vectorB[i];
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    private static double sigmoid(double x) {
        double k = 5, midpoint = 0.5;
        return 1 / (1 + Math.exp(-k * (x - midpoint)));
    }

    public List<QuestionMaster> getSimilaryQuestionList(String newQuestion) {
        if (cachedQuestionList.isEmpty()) return Collections.emptyList();

        String processedNewQuestion = preprocessText(newQuestion);
        float[] newQuestionEmbedding;
        try {
            newQuestionEmbedding = predictor.predict(processedNewQuestion);
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }

        // Use parallel stream with cached embeddings
        return cachedQuestionList.parallelStream()
                .filter(q -> {
                    float[] dbEmbedding = questionEmbeddingsCache.get(q.getQuestionMasterId());
                    if (dbEmbedding == null) return false;
                    
                    double similarity = cosineSimilarity(newQuestionEmbedding, dbEmbedding);
                    return sigmoid(similarity) >= SIMILARITY_THRESHOLD;
                })
                .collect(Collectors.toList());
    }
}