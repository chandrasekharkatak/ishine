package com.apmosys.employeeportal.utility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.Question;
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
    
    private final double SIMILARITY_THRESHOLD = 0.85;
    
    
    private static String preprocessText(String text) {
        return text.toLowerCase()
                .replaceAll("[^a-zA-Z0-9\\s]", "") // Remove special characters
                .replaceAll("\\s+", " ")           // Normalize whitespace
                .trim();
    }
    
    public static void main(String[] args) throws IOException, ModelException, TranslateException {
        String sentence1 = "what does Springboot annotation does?";
        String sentence2 = "expalin springboot annotation";
        
        // Preprocess both sentences
        String processedText1 = preprocessText(sentence1);
        String processedText2 = preprocessText(sentence2);
        
        // Use a more powerful model for better semantic understanding
        Criteria<String, float[]> criteria = Criteria.builder()
                .setTypes(String.class, float[].class)
                .optApplication(Application.NLP.TEXT_EMBEDDING)
                .optEngine("PyTorch")
                .optModelUrls("djl://ai.djl.huggingface.pytorch/sentence-transformers/all-mpnet-base-v2")
                .optTranslatorFactory(new TextEmbeddingTranslatorFactory())
                .build();

        try (ZooModel<String, float[]> model = criteria.loadModel();
             Predictor<String, float[]> predictor = model.newPredictor()) {

            // Get embeddings for both sentences
            float[] embedding1 = predictor.predict(processedText1);
            float[] embedding2 = predictor.predict(processedText2);

            // Calculate cosine similarity
            double similarity = cosineSimilarity(embedding1, embedding2);
            
            // Apply a sigmoid function to enhance the similarity score
            double enhancedSimilarity = sigmoid(similarity);
            
            System.out.printf("Original Similarity: %.4f%n", similarity);
            System.out.printf("Enhanced Similarity: %.4f%n", enhancedSimilarity);
        }
    }

    private static double cosineSimilarity(float[] vectorA, float[] vectorB) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += Math.pow(vectorA[i], 2);
            normB += Math.pow(vectorB[i], 2);
        }
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
    
    private static double sigmoid(double x) {
        // Sigmoid function to enhance similarity scores
        // This will push scores closer to 1 for high similarities
        // and closer to 0 for low similarities
    	double k = 5;        // Steepness of the curve
        double midpoint = 0.5; // Midpoint of the curve
        return 1 / (1 + Math.exp(-k * (x - midpoint)));
    }
    
    public double findSimilarity(String text1, String text2) throws ModelException, TranslateException, IOException {
        String processedText1 = preprocessText(text1);
        String processedText2 = preprocessText(text2);
        
        Criteria<String, float[]> criteria = Criteria.builder()
                .setTypes(String.class, float[].class)
                .optApplication(Application.NLP.TEXT_EMBEDDING)
                .optEngine("PyTorch")
                .optModelUrls("djl://ai.djl.huggingface.pytorch/sentence-transformers/all-mpnet-base-v2")
                .optTranslatorFactory(new TextEmbeddingTranslatorFactory())
                .build();

        try (ZooModel<String, float[]> model = criteria.loadModel();
             Predictor<String, float[]> predictor = model.newPredictor()) {

            float[] embedding1 = predictor.predict(processedText1);
            float[] embedding2 = predictor.predict(processedText2);

            double similarity = cosineSimilarity(embedding1, embedding2);
            return sigmoid(similarity);
        }
    }

}
