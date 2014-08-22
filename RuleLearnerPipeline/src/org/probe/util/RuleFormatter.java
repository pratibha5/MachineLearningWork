package org.probe.util;

public class RuleFormatter {
	public static String removeAllWhiteSpace(String str) {
		StringBuilder sb = new StringBuilder();

		for (char character : str.toCharArray()) {
			if (character != ' ') {
				sb.append(character);
			}
		}

		return sb.toString();
	}

	public static String removeParentheses(String str) {
		if (str.startsWith("(") && str.endsWith(")")) {
			return str.substring(1, str.length() - 1);
		}
		return str.trim();
	}

	public static String formatRuleString(String str) {
		String formattedStr = removeParentheses(removeAllWhiteSpace(str));

		return formattedStr;
	}
}
