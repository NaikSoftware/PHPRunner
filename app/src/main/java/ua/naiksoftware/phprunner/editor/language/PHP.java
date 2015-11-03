package ua.naiksoftware.phprunner.editor.language;

import java.util.regex.*;
import ua.naiksoftware.phprunner.editor.*;

public class PHP extends Language {

	private static final PatternColorPair keywords = new PatternColorPair(Pattern.compile(
																			  "\\b((a(bstract|nd|rray|s))|(c(a(llable|se|tch)|l(ass|one)|on(st|tinue)))|"
																			  + "(d(e(clare|fault)|ie|o))|(e(cho|lse(if)?|mpty|nd(declare|for(each)?|if|switch|while)|val|x"
																			  + "(it|tends)))|(f(inal|or(each)?|unction))|(g(lobal|oto))|(i(f|mplements|n(clude(_once)?|"
																			  + "st(anceof|eadof)|terface)|sset))|(n(amespace|ew))|(p(r(i(nt|vate)|otected)|ublic))|"
																			  + "(re(quire(_once)?|turn))|(s(tatic|witch))|(t(hrow|r(ait|y)))|(u(nset|se))|"
																			  + "(__halt_compiler|break|list|(x)?or|var|while))\\b", Pattern.CASE_INSENSITIVE), SourceEditor.COLOR_KEYWORD);

	private static final PatternColorPair dividers = new PatternColorPair(Pattern.compile("\\[|\\]|\\+|\\-|\\<|\\>|\\|\\!|\\&|\\||=|\\)|\\(|\\}|\\{|\\*|\\?php|\\?\\>|::|\\!"), SourceEditor.COLOR_BUILTIN);

	private static final PatternColorPair builtins = new PatternColorPair(Pattern.compile(
																			  "\\b(md5|query|real_escape_string|strval|intval|strlen|header|trim|abs|define|"
																			  + "session_start|session_name|strpos|print|print\\_r|printf|substr|htmlspecialchars)\\b", Pattern.CASE_INSENSITIVE), SourceEditor.COLOR_BUILTIN);

	private static final PatternColorPair vars = new PatternColorPair(Pattern.compile("\\$[a-zA-Z_\\x7f-\\xff][a-zA-Z0-9_\\x7f-\\xff]*"), SourceEditor.COLOR_VAR);

	private static final PatternColorPair literals = new PatternColorPair(Pattern.compile(
																			  "\\b(\\d*[.]?\\d+)\\b|'(\\\\'|[^'\n])*'|\"(\\\\\"|[^\"\n])*\"|true|false"), SourceEditor.COLOR_LITERAL);
	//"\\b(\\d*[.]?\\d+)\\b|(('(\\\\'|[^'\n])*[^\\\\]')|([^\\\\']+''))|true|false"), SourceEditor.COLOR_LITERAL);
	//"\\b(\\d*[.]?\\d+)\\b|([\"'])(\\\\\\2|.)*?[^\\\\]\\2|true|false"), SourceEditor.COLOR_LITERAL);


	private static final PatternColorPair comments = new PatternColorPair(Pattern.compile(
																			  "/\\*(?:.|[\\n\\r])*?\\*/|//.*|#.*"), SourceEditor.COLOR_COMMENT);

	private static final PatternColorPair[] patterns = new PatternColorPair[] {keywords, dividers, builtins, vars, literals, comments};
	public PatternColorPair[] getPatternColorPairs() {
		return patterns;
	}
}
