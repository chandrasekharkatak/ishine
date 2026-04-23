package com.apmosys.employeeportal.service;

import org.springframework.stereotype.Service;

@Service
public class FacetExtractorService {

    // private final StanfordCoreNLP pipeline;

    // // Example stopwords & blockwords (extend as needed)
    // private static final Set<String> STOPWORDS = new HashSet<>(
    // Arrays.asList("a", "an", "the", "and", "or", "but", "is", "are", "was",
    // "were", "be", "been", "being", "in",
    // "on", "at", "to", "for", "with", "by", "about", "against", "between", "into",
    // "through", "during",
    // "before", "after", "above", "below", "from", "up", "down", "of", "off",
    // "over", "under", "again",
    // "further", "then", "once", "here", "there", "when", "where", "why", "how",
    // "all", "any", "both",
    // "each", "few", "more", "most", "other", "some", "such", "no", "nor", "not",
    // "only", "own", "same",
    // "so", "than", "too", "very", "s", "t", "can", "will", "just", "don",
    // "should", "now"));

    // private static final Set<String> BLOCKWORDS = new HashSet<>(Arrays.asList(
    // "this", "that", "these", "those", "which", "who", "whom", "whose", "what",
    // "where", "when", "why", "how"));

    // public FacetExtractorService(StanfordCoreNLP pipeline) {
    // this.pipeline = pipeline;
    // }

    // public List<String> extractRelevantFacets(String text, String question) {
    // try {

    // if (text == null || text.isEmpty())
    // return Collections.emptyList();

    // CoreDocument doc = new CoreDocument(text);
    // pipeline.annotate(doc);

    // Set<String> facets = new LinkedHashSet<>();

    // // 1. Named Entities
    // doc.tokens().forEach(token -> {
    // String ne = token.get(NamedEntityTagAnnotation.class);
    // if (!"O".equals(ne)) {
    // String cleaned = cleanPhrase(token.word(), "NER");
    // if (isValidChunk(cleaned)) {
    // facets.add(cleaned);
    // }
    // }
    // });

    // // 2. Noun Phrases
    // doc.sentences().forEach(sentence -> {
    // Tree tree = sentence.coreMap().get(TreeCoreAnnotations.TreeAnnotation.class);
    // collectNounPhrases(tree, facets);
    // });

    // // 3. Rule-based relevance filtering
    // List<String> normalised = facets.stream()
    // .map(f -> normalizeFacet(f))
    // .collect(Collectors.toList());

    // return normalised;
    // } catch (Exception e) {
    // e.printStackTrace();
    // return Collections.emptyList();
    // }
    // }

    // private String normalizeFacet(String phrase) {
    // if (phrase == null)
    // return null;

    // // unify casing and collapse spaces
    // String cleaned = phrase.trim().replaceAll("\\s+", " ");
    // // title case for readability
    // return Arrays.stream(cleaned.split(" "))
    // .map(w -> w.substring(0, 1).toUpperCase() + w.substring(1))
    // .collect(Collectors.joining(" "));
    // }

    // private void collectNounPhrases(Tree tree, Set<String> facets) {
    // if (tree == null) {
    // return;
    // }

    // if ("NP".equals(tree.label().value())) {
    // String np = String.join(" ", tree.yieldWords().stream()
    // .map(w -> w.word())
    // .collect(Collectors.toList()));

    // String cleaned = cleanPhrase(np, "NP");
    // if (isValidChunk(cleaned)) {
    // facets.add(cleaned);
    // }
    // }

    // for (Tree child : tree.children()) {
    // collectNounPhrases(child, facets);
    // }
    // }

    // // --- Refinement rules ---
    // private String cleanPhrase(String phrase, String type) {
    // if (!isValidTag(phrase, type))
    // return null;

    // String[] words = phrase.split("\\s+");
    // int start = 0, end = words.length - 1;

    // // Drop leading/trailing stopwords
    // while (start <= end && STOPWORDS.contains(words[start].toLowerCase())) {
    // start++;
    // }
    // while (end >= start && STOPWORDS.contains(words[end].toLowerCase())) {
    // end--;
    // }

    // if (start > end) {
    // return null; // all stopwords
    // }

    // String cleaned = String.join(" ", Arrays.copyOfRange(words, start, end + 1));

    // // 🔹 Normalize unwanted chars inside phrase
    // cleaned = cleaned
    // .replaceAll("[/\\\\]", " ") // remove slashes
    // .replaceAll("\\s+", " ") // collapse spaces
    // .trim();

    // // 🔹 Remove trailing/leading brackets, colons, semicolons, commas, periods
    // cleaned = cleaned.replaceAll("^[\\p{Punct}]+", ""); // leading punctuation
    // cleaned = cleaned.replaceAll("[\\p{Punct}]+$", ""); // trailing punctuation

    // // Ensure at least one strong word remains
    // boolean hasStrongWord = Arrays.stream(cleaned.split("\\s+"))
    // .anyMatch(w -> !STOPWORDS.contains(w.toLowerCase()) && w.length() >= 2);

    // return hasStrongWord ? cleaned : null;
    // }

    // private boolean isValidTag(String phrase, String type) {
    // if (phrase == null) {
    // return false;
    // }
    // if ("NP".equals(type)) {
    // String[] words = phrase.split("\\s+");
    // if (words.length > 4) {
    // return false; // restrict to short NP chunks
    // }
    // if (words.length > 1 && Arrays.stream(words).distinct().count() == 1) {
    // return false; // avoid repeated words
    // }
    // if (!phrase.matches(".*[a-zA-Z].*")) {
    // return false; // must have letters
    // }
    // }
    // return phrase.length() > 3;
    // }

    // private boolean isValidChunk(String phrase) {
    // if (phrase == null || phrase.isEmpty()) {
    // return false;
    // }
    // String lower = phrase.toLowerCase().trim();
    // if (STOPWORDS.contains(lower) || BLOCKWORDS.contains(lower)) {
    // return false;
    // }
    // if (lower.length() == 1) {
    // return false;
    // }
    // if (phrase.matches("[-–—()\\[\\]{}.,;:!?]+")) {
    // return false;
    // }
    // return true;
    // }

}
