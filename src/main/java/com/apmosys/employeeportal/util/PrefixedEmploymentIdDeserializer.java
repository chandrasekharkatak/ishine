package com.apmosys.employeeportal.util;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

/**
 * Accepts numeric employment ids or prefixed display values (A-/CS-/AP-/APCS-) for Long fields.
 */
public class PrefixedEmploymentIdDeserializer extends JsonDeserializer<Long> {

	@Override
	public Long deserialize(JsonParser parser, DeserializationContext context) throws IOException {
		JsonToken token = parser.currentToken();
		if (token == JsonToken.VALUE_NULL) {
			return null;
		}
		if (token == JsonToken.VALUE_NUMBER_INT || token == JsonToken.VALUE_NUMBER_FLOAT) {
			return parser.getLongValue();
		}
		String text = parser.getValueAsString();
		if (text == null) {
			return null;
		}
		text = text.trim();
		if (text.isEmpty()) {
			return null;
		}
		String numeric = text.replaceFirst("(?i)^(APCS-|AP-|CS-|A-)", "");
		try {
			return Long.parseLong(numeric);
		} catch (NumberFormatException ex) {
			return (Long) context.handleWeirdStringValue(Long.class, text,
					"Invalid employment id; expected numeric or prefixed A-/CS-/AP-/APCS- value");
		}
	}
}
