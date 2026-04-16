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
import java.util.stream.Collectors;

@Component
public class NLPUtils {

	private final SimpleTokenizer tokenizer = SimpleTokenizer.INSTANCE;
	private final POSTaggerME posTagger;
	private final ChunkerME chunker;
	private final List<NameFinderME> nameFinders;

	private static final Set<String> STOPWORDS = new HashSet<>(
			Arrays.asList("a", "an", "the", "and", "or", "but", "is", "are", "was", "were", "be", "been", "being", "in",
					"on", "at", "to", "for", "with", "by", "about", "against", "between", "into", "through", "during",
					"before", "after", "above", "below", "from", "up", "down", "of", "off", "over", "under", "again",
					"further", "then", "once", "here", "there", "when", "where", "why", "how", "all", "any", "both",
					"each", "few", "more", "most", "other", "some", "such", "no", "nor", "not", "only", "own", "same",
					"so", "than", "too", "very", "s", "t", "can", "will", "just", "don", "should", "now"));

	Set<String> allowedTags = Set.of("NN", "NNS", "NNP", "NNPS", // nouns & proper nouns
			"JJ", // adjectives
			"VB", "VBD", "VBN", "VBG" // verb forms
			, "RB" // for adverbs
	);

	public NLPUtils() throws Exception {
		// Load POS(Part-of-Speech) Model
		InputStream posStream = getClass().getResourceAsStream("/models/en-pos-maxent.bin");
		POSModel posModel = new POSModel(posStream);
		this.posTagger = new POSTaggerME(posModel);

		/*
		 * Load Chunker Model Useful for more contextual tags, like
		 * "full stack development" or "cloud solution"
		 */
		InputStream chunkerStream = getClass().getResourceAsStream("/models/en-chunker.bin");
		ChunkerModel chunkerModel = new ChunkerModel(chunkerStream);
		this.chunker = new ChunkerME(chunkerModel);

		/*
		 * Load NER Models (organization, location, person) These models help identify
		 * proper names like locations, organizations, persons, etc.
		 */
		this.nameFinders = new ArrayList<>();
		this.nameFinders.add(new NameFinderME(
				new TokenNameFinderModel(getClass().getResourceAsStream("/models/en-ner-organization.bin"))));
		this.nameFinders.add(new NameFinderME(
				new TokenNameFinderModel(getClass().getResourceAsStream("/models/en-ner-location.bin"))));
		this.nameFinders.add(new NameFinderME(
				new TokenNameFinderModel(getClass().getResourceAsStream("/models/en-ner-person.bin"))));
	}

	private boolean isAlphanumeric(String token) {
		return token.matches("[a-zA-Z0-9]+");
	}

	public List<String> extractTags(String text) {
		if (text == null || text.isEmpty())
			return Collections.emptyList();

		String normalized = text.toLowerCase();
		String[] tokens = tokenizer.tokenize(normalized);
		String[] posTags = posTagger.tag(tokens);
		String[] chunks = chunker.chunk(tokens, posTags);

		Set<String> tagSet = new LinkedHashSet<>();
		List<String> phrases = new ArrayList<>();

		// First pass: collect valid single tokens
		for (int i = 0; i < tokens.length; i++) {
			String token = tokens[i];
			String tag = posTags[i];
			if (token.length() > 2 && isAlphanumeric(token) && !STOPWORDS.contains(token)) {
				if (allowedTags.contains(tag)) {
					tagSet.add(token);
					System.out.println(token + "pos");
				}
			}
		}

		// Second pass: collect phrases
		StringBuilder currentPhrase = new StringBuilder();
		String currentType = null;
		boolean isValidPhrase = true;

		for (int i = 0; i < chunks.length; i++) {
			if (!chunks[i].contains("-")) {
				if (currentPhrase.length() > 0 && isValidPhrase) {
					String phrase = currentPhrase.toString().trim().toLowerCase();
					if (isValidTag(phrase, currentType)) {
						phrases.add(phrase);
					}
				}
				currentPhrase = new StringBuilder();
				currentType = null;
				isValidPhrase = true;
				continue;
			}

			String[] parts = chunks[i].split("-");
			String prefix = parts[0];
			String type = parts[1];

			if (prefix.equals("B")) {
				// Handle previous phrase if exists
				if (currentPhrase.length() > 0 && isValidPhrase) {
					String phrase = currentPhrase.toString().trim().toLowerCase();
					if (isValidTag(phrase, currentType)) {
						phrases.add(phrase);
					}
				}

				// Start new phrase
				if (!STOPWORDS.contains(tokens[i]) && isAlphanumeric(tokens[i])) {
					currentPhrase = new StringBuilder(tokens[i]);
					currentType = type;
					isValidPhrase = true;
				} else {
					currentPhrase = new StringBuilder();
					currentType = null;
					isValidPhrase = false;
				}
			} else if (prefix.equals("I") && currentType != null && type.equals(currentType) && isValidPhrase) {
				if (!STOPWORDS.contains(tokens[i]) && isAlphanumeric(tokens[i])) {
					currentPhrase.append(" ").append(tokens[i]);
				} else {
					isValidPhrase = false;
				}
			}
		}

		// Handle last phrase
		if (currentPhrase.length() > 0 && isValidPhrase) {
			String phrase = currentPhrase.toString().trim().toLowerCase();
			if (isValidTag(phrase, currentType)) {
				phrases.add(phrase);
			}
		}

		// Add multi-word phrases to tagSet
		tagSet.addAll(phrases.stream().filter(p -> {
			String[] words = p.split("\\s+");
			return words.length >= 2 && words.length <= 4;
		}).collect(Collectors.toSet()));

		for (String obj : phrases) {
			System.out.println(obj + " chunks");
		}

		// Add Named Entities
		for (NameFinderME finder : nameFinders) {
			Span[] spans = finder.find(tokens);
			
			for (Span span : spans) {
				StringBuilder entity = new StringBuilder();
				for (int i = span.getStart(); i < span.getEnd(); i++) {
					entity.append(tokens[i]).append(" ");
				}
				String result = entity.toString().trim().toLowerCase();
				if (result.length() > 2 && isAlphanumeric(result.replaceAll(" ", ""))) {
					tagSet.add(result);
					System.out.println("Added entity: " + result);
				}
			}
		}

		return new ArrayList<>(tagSet);
	}

	private boolean isValidTag(String phrase, String type) {
		if (!"NP".equals(type))
			return false;
		String[] words = phrase.split("\\s+");
		if (words.length > 4)
			return false;
		for (int i = 1; i < words.length - 1; i++) {
			if (STOPWORDS.contains(words[i]))
				return false;
			if (STOPWORDS.contains(words[0]) || STOPWORDS.contains(words[words.length - 1]))
				return false;
			if (!phrase.matches("^[a-zA-Z\\s]+$"))
				return false;
			if (words.length > 1 && Arrays.stream(words).distinct().count() == 1)
				return false;

		}
		return phrase.length() > 3;
	}

}