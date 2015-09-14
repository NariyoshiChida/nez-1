package nez.peg.regex;

import java.io.IOException;

import nez.NezException;
import nez.NezOption;
import nez.SourceContext;
import nez.ast.CommonTree;
import nez.lang.Grammar;
import nez.lang.GrammarFile;
import nez.util.ConsoleUtils;

public class Executor {
	private static Grammar grammar;
	
	static GrammarFile regexGrammar = null;
	
	/*
	 * java -jar nez.jar match -p test.regex -i in_ac
	 */
	public final static GrammarFile loadGrammar(String filePath,NezOption option) throws IOException {
		
		SourceContext regex = SourceContext.newFileContext(filePath);
		
		try {
			regexGrammar = GrammarFile.loadGrammarFile("regex.nez", NezOption.newSafeOption());
		}
		catch(IOException e) {
			ConsoleUtils.exit(1, "can't load regex.nez");
		}
		
		Grammar p = regexGrammar.newGrammar("File");
		CommonTree node = p.parseCommonTree(regex);
		
		if (node == null) {
			throw new NezException(regex.getSyntaxErrorMessage());
		}
		if (regex.hasUnconsumed()) {
			throw new NezException(regex.getUnconsumedMessage());
		}

		GrammarFile gfile = GrammarFile.newGrammarFile("re", NezOption.newDefaultOption());
		RegexConverter rc = new RegexConverter(gfile,null);
		rc.convert(node);
		return rc.grammar;
	}
	
	public static void main(String args[]) throws IOException {
		//SourceContext.newStringContext(text);
		//SourceContext sc = SourceContext.newFileContext(args[0]);
		//GrammarFile gf = RegexGrammar.loadGrammar(sc,NezOption.newDefaultOption());
		//RegexConverter rc = new RegexConverter(gf,"File");
		SourceContext regex = SourceContext.newFileContext(args[0]);
		try {
			regexGrammar = GrammarFile.loadGrammarFile("regex.nez", NezOption.newSafeOption());
		}
		catch(IOException e) {
			ConsoleUtils.exit(1, "can't load regex.nez");
		}
		
		Grammar p = regexGrammar.newGrammar("File");
		CommonTree node = p.parseCommonTree(regex);
		
		if (node == null) {
			throw new NezException(regex.getSyntaxErrorMessage());
		}
		if (regex.hasUnconsumed()) {
			throw new NezException(regex.getUnconsumedMessage());
		}
		System.out.println("node = " +node);
		GrammarFile gfile = GrammarFile.newGrammarFile("re", NezOption.newDefaultOption());
		RegexConverter rc = new RegexConverter(gfile,null);
		rc.convert(node);
	}
}
