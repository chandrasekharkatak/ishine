package com.apmosys.employeeportal.utility;

import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.SimpleTokenizer;
import opennlp.tools.util.Span;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.*;

@Component
public class NLPUtils {

    private final SimpleTokenizer tokenizer = SimpleTokenizer.INSTANCE;
    private final POSTaggerME posTagger;
    private final ChunkerME chunker;
    private final List<NameFinderME> nameFinders;

    private static final Set<String> STOPWORDS = new HashSet<>(Arrays.asList(
        "a", "an", "the", "and", "or", "but", "is", "are", "was", "were", "be", "been", "being",
        "in", "on", "at", "to", "for", "with", "by", "about", "against", "between", "into", "through",
        "during", "before", "after", "above", "below", "from", "up", "down", "of", "off", "over", "under",
        "again", "further", "then", "once", "here", "there", "when", "where", "why", "how", "all", "any",
        "both", "each", "few", "more", "most", "other", "some", "such", "no", "nor", "not", "only", "own",
        "same", "so", "than", "too", "very", "s", "t", "can", "will", "just", "don", "should", "now"
    ));

    public NLPUtils() throws Exception {
        // Load POS(Part-of-Speech) Model
        InputStream posStream = getClass().getResourceAsStream("/models/en-pos-maxent.bin");
        POSModel posModel = new POSModel(posStream);
        this.posTagger = new POSTaggerME(posModel);

        /*
         * Load Chunker Model 
         * Useful for more contextual tags, like "full stack development" or "cloud solution"
         * */
        InputStream chunkerStream = getClass().getResourceAsStream("/models/en-chunker.bin");
        ChunkerModel chunkerModel = new ChunkerModel(chunkerStream);
        this.chunker = new ChunkerME(chunkerModel);

        /*
         *    Load NER Models (organization, location, person)
         *    These models help identify proper names like locations, organizations, persons, etc. 
         * */ 
        this.nameFinders = new ArrayList<>();
        this.nameFinders.add(new NameFinderME(new TokenNameFinderModel(
                getClass().getResourceAsStream("/models/en-ner-organization.bin"))));
        this.nameFinders.add(new NameFinderME(new TokenNameFinderModel(
                getClass().getResourceAsStream("/models/en-ner-location.bin"))));
        this.nameFinders.add(new NameFinderME(new TokenNameFinderModel(
                getClass().getResourceAsStream("/models/en-ner-person.bin"))));
    }

    private boolean isAlphanumeric(String token) {
        return token.matches("[a-zA-Z0-9]+");
    }

    public List<String> extractTags(String text) {
        if (text == null || text.isEmpty()) return Collections.emptyList();

        String normalized = text.toLowerCase();
        String[] tokens = tokenizer.tokenize(normalized);
        String[] posTags = posTagger.tag(tokens);
        String[] chunks = chunker.chunk(tokens, posTags);

        Set<String> tagSet = new LinkedHashSet<>();

        // POS Tags: keep nouns and adjectives
        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            String tag = posTags[i];
            if (token.length() > 2 && isAlphanumeric(token) && !STOPWORDS.contains(token)) {
                if (tag.startsWith("NN") || tag.equals("JJ")) {
                    tagSet.add(token);
                    System.out.println(token + " pos");
                }
            }
        }

        // Noun Phrases from Chunking
        for (int i = 0; i < chunks.length; i++) {
            if (chunks[i].equals("NP")) {
                tagSet.add(tokens[i]);
                System.out.println(tokens[i] + " chunk");
            }
        }

        // Named Entities
        for (NameFinderME finder : nameFinders) {
            Span[] spans = finder.find(tokens);
            for (Span span : spans) {
                StringBuilder entity = new StringBuilder();
                for (int i = span.getStart(); i < span.getEnd(); i++) {
                    entity.append(tokens[i]).append(" ");
                }
                String result = entity.toString().trim();
                if (result.length() > 2 && isAlphanumeric(result.replaceAll(" ", ""))) {
                    tagSet.add(result);
                    System.out.println(result + " chunk");
                }
            }
        }

        return new ArrayList<>(tagSet);
    }
}