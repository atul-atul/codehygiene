package codehygiene.util;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;

import static java.util.Arrays.asList;

public class ConstantsAndFunctions {
	public static final String COMMA = ",";
	public static final String REGEX = ".*(?:password|secret|token).*";
	public static final String[] INCLUDES = new String[]{"**/*.yaml", "**/*.yml", "**/*.properties"};
	public static final String[] EXCLUDES = new String[]{"target/**", ".gradle/**", "build/**"};
	public static final List<String> STANDARD_ALLOWED_PATTERNS = asList("${", "{cipher}", "http", "fake", "dummy");

	public static final BiPredicate<String, Collection<String>> CONTAINS_ALLOWED_PATTERN = (line, patterns) -> patterns.stream()
																																																										 .filter(Objects::nonNull)
																																																										 .filter(s -> s.length() > 0)
																																																										 .map(String::toLowerCase)
																																																										 .anyMatch(line::contains);
}
