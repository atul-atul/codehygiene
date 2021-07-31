package codehygiene;

import codehygiene.exception.CodeHygieneException;
import codehygiene.extn.CheckExposedSecrets;
import codehygiene.util.FileLinesTuple;
import org.apache.tools.ant.DirectoryScanner;
import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.tooling.GradleConnectionException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static codehygiene.util.ConstantsAndFunctions.*;
import static java.lang.System.out;
import static java.nio.file.Paths.get;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

public class CodeHygienePlugin implements Plugin<Project> {


	public void apply(Project project) {
		exposedSecretsTask(project);

	}

	private void exposedSecretsTask(Project project) {
		CheckExposedSecrets exposedSecretsExtn = project.getExtensions().create("exposedSecretsCheck", CheckExposedSecrets.class);

		project.getTasks().register("exposed_secrets", task -> {
			final Set<String> userAllowedPatterns = stream(exposedSecretsExtn.getUserAllowedPatterns().split(COMMA))
																								.map(String::trim)
																								.collect(Collectors.toSet());
			checkForbiddenPatterns(userAllowedPatterns);
			final File rootDir = project.getRootDir();
			List<String> filesToCheckForSecrets = filesToCheckForSecrets(rootDir);
			checkLeakedSecrets(rootDir, userAllowedPatterns, filesToCheckForSecrets);
		});
	}

	private void checkForbiddenPatterns(Set<String> userAllowedPatterns) {
		boolean tryingToAllowForbiddenPattern = userAllowedPatterns.stream()
																															 .filter(Objects::nonNull)
																															 .filter(s -> s.length() > 0)
																															 .anyMatch(s -> s.toLowerCase().matches(REGEX));
		if (tryingToAllowForbiddenPattern) {
			throw new GradleConnectionException("Words like password, secret, token are not allowed.");
		}
	}

	private void checkLeakedSecrets(File rootDir, Set<String> userAllowedPatterns, List<String> filesToCheckForSecrets) {
		final List<FileLinesTuple> fileLinesTuples = filesToCheckForSecrets.parallelStream()
																																			 .map(f -> rootDir + System.getProperty("file.separator") + f)
																																			 .map(f -> scanFile(f, userAllowedPatterns))
																																			 .filter(Objects::nonNull)
																																			 .collect(toList());
		if (fileLinesTuples.isEmpty()) {
			out.println("no suspect files");
			return;
		}
		out.println("\n Following files contain lines with suspect plain text secrets");
		fileLinesTuples.stream().forEach((out::println));
		throw new GradleException("Some files contain plain text secrets. Please check build log");
	}

	private List<String> filesToCheckForSecrets(File rootDir) {
		DirectoryScanner directoryScanner = new DirectoryScanner();
		directoryScanner.setBasedir(rootDir);
		directoryScanner.setCaseSensitive(false);

		String[] gitIgnoreExclusions = gitIgnoreExclusions(rootDir);

		directoryScanner.addExcludes(EXCLUDES);
		directoryScanner.addExcludes(gitIgnoreExclusions);
		directoryScanner.setIncludes(INCLUDES);

		directoryScanner.scan();

		return asList(directoryScanner.getIncludedFiles());
	}

	private String[] gitIgnoreExclusions(File rootDir) {
		String gitIgnorePath = rootDir + System.getProperty("file.separator") + ".gitignore";
		if (new File(gitIgnorePath).exists()) {
			try (final Stream<String> gitIgnorePatterns = Files.lines(get(gitIgnorePath))) {
				return gitIgnorePatterns.toArray(String[]::new);
			} catch (IOException e) {
				throw new CodeHygieneException(e);
			}
		}
		return new String[0];
	}

	private FileLinesTuple scanFile(String file, Set<String> userAllowedPatterns) {
		List<String> lines = new ArrayList<>(0);
		try (BufferedReader br = Files.newBufferedReader(get(file))) {
			final List<String> suspectLines = br.lines()
																					.filter(line -> line.toLowerCase().matches(REGEX))
																					.filter(line -> CONTAINS_ALLOWED_PATTERN.negate().test(line.toLowerCase(), STANDARD_ALLOWED_PATTERNS))
																					.filter(line -> CONTAINS_ALLOWED_PATTERN.negate().test(line.toLowerCase(), userAllowedPatterns))
																					.collect(toList());

			if(!suspectLines.isEmpty()){
				lines.addAll(suspectLines);
			}
		} catch (Throwable e) {
			throw new CodeHygieneException(e);
		}
		if(lines.isEmpty()){
			return null;
		}
		return new FileLinesTuple(file, lines);
	}
}
