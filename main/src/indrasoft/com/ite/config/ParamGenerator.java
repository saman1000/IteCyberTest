package indrasoft.com.ite.config;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParamGenerator {
	
	private Configuration config;
	
	private ParamGenerator() throws Exception {
		config = new Configuration();
	}

	private void parseParams(String[] params) {
		if (params == null) {
			return;
		}
		Pattern decParams = Pattern.compile("(dec=)(.*)");
		Pattern encParams = Pattern.compile("(enc=)(.*)");
		String result = null;
		StringBuilder builder = new StringBuilder();
		for (String text : params) {
			builder.append(text);
			Matcher decMatch = decParams.matcher(text);
			if (decMatch.matches()) {
				result = config.decryptText(decMatch.group(2));
			} else {
				Matcher encMatch = encParams.matcher(text);
				if (encMatch.matches()) {
					result = config.encryptText(encMatch.group(2));
				}
			}
			builder.append("-->");
			builder.append(result);
			builder.append("\n");
		}
		System.out.println(builder.toString());
	}
	
	public static void main(String[] args) {
		try {
			ParamGenerator generator = new ParamGenerator();
			generator.parseParams(args);
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}

}
