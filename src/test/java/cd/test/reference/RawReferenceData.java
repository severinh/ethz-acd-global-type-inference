package cd.test.reference;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;

import cd.exceptions.ParseFailure;
import cd.exceptions.SemanticFailure;
import cd.exceptions.SemanticFailure.Cause;
import cd.test.Reference;

import com.google.common.base.Enums;
import com.google.common.base.Optional;

public abstract class RawReferenceData implements ReferenceData {

	public abstract String getParseFailureString() throws IOException;

	@Override
	public Optional<ParseFailure> getParseFailure() throws IOException {
		boolean isParseFailure = getParseFailureString().equals(
				Reference.PARSE_FAILURE);
		if (isParseFailure) {
			return Optional.of(new ParseFailure(-1, "unknown error"));
		} else {
			return Optional.absent();
		}
	}

	public abstract String getSemanticFailureString() throws IOException;

	@Override
	public Optional<SemanticFailure> getSemanticFailure() throws IOException {
		// If there are multiple lines, only use the first one to determine
		// the semantic failure cause
		String[] lines = StringUtils.split(getSemanticFailureString(), "\n");
		String causeString = lines[0];
		Optional<Cause> cause = Enums.getIfPresent(Cause.class, causeString);
		if (cause.isPresent()) {
			return Optional.of(new SemanticFailure(cause.get()));
		} else {
			return Optional.absent();
		}
	}

}
