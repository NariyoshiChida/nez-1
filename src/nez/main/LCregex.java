package nez.main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import nez.NezException;
import nez.NezOption;
import nez.SourceContext;
import nez.ast.CommonTree;
import nez.lang.Grammar;
import nez.lang.GrammarFile;
import nez.peg.regex.RegexConverter;
import nez.util.ConsoleUtils;
import nez.util.UList;

public class LCregex extends Command {
	@Override
	public String getDesc() {
		return "regex converter";
	}

	static GrammarFile regexGrammar = null;
	@Override
	public void exec(CommandContext config) {
		try {
			regexGrammar = GrammarFile.loadGrammarFile("regex.nez",NezOption.newSafeOption());
		} catch( IOException e ) {
			ConsoleUtils.exit(1,"can't load regex.nez");
		}
		Grammar g = regexGrammar.newGrammar("File");

		// -i  
		while( config.hasInputSource() ) {
			SourceContext source = config.nextInputSource();
			CommonTree node = g.parseCommonTree(source);
			if( node == null ) {
				ConsoleUtils.println(source.getSyntaxErrorMessage());
				continue;
			}
			if( source.hasUnconsumed() ) {
				ConsoleUtils.println(source.getUnconsumedMessage());
			}
			//ConsoleUtils.println(node);
			GrammarFile gfile = GrammarFile.newGrammarFile("re",NezOption.newDefaultOption());
			RegexConverter rc = new RegexConverter(gfile,null);
			rc.convert(node);
		}
	}
}
