package nez.main;

import java.io.IOException;
import java.util.Scanner;

import nez.NezOption;
import nez.SourceContext;
import nez.ast.CommonTree;
import nez.dfa.DFAConverter;
import nez.lang.Grammar;
import nez.lang.GrammarFile;
import nez.peg.regex.RegexConverter;
import nez.util.ConsoleUtils;

public class LCdfa extends Command {
	@Override
	public String getDesc() {
		return "dfa converter";
	}

	static GrammarFile regexGrammar      = null;
	static DFAConverter dfaConverter     = null;
	static RegexConverter regexConverter = null;
	static GrammarFile convertedRegexGrammarFile = null;
	@Override
	public void exec(CommandContext config) {
		try {
			regexGrammar = GrammarFile.loadGrammarFile("regex.nez",NezOption.newSafeOption());
		} catch( IOException e ) {
			ConsoleUtils.exit(1,"can't load regex.nez");
		}
		Grammar g = regexGrammar.newGrammar("File");
		
		// -i
		String filePath = null;
		if( config.hasInputSource() ) {
			try {
				convertedRegexGrammarFile = RegexConverter.loadGrammar(config.nextInputSource(), NezOption.newSafeOption());
			} catch( IOException e ) {
				ConsoleUtils.exit(1,"can't load grammar file");
			}
		}
		
		if( convertedRegexGrammarFile == null ) {
			ConsoleUtils.exit(1,"can't load grammar file");
		}
		
		dfaConverter = new DFAConverter(convertedRegexGrammarFile,null);
		
		boolean printTime = false;
		Scanner in = new Scanner(System.in);
		ConsoleUtils.print(">>>");
		while( in.hasNext() ) {
			String query = in.next();
			if( query.length() >= 2 && query.charAt(0) == ';' && query.charAt(1) == ';' ) {
				if( query.equals(";;toDOT") ) {
					dfaConverter.convertBFAtoDOT();
					ConsoleUtils.println("|- fin -|");
				} else if( query.equals(";;prDOT") ) {
					dfaConverter.printBFA();
					ConsoleUtils.println("|- fin -|");
				} else if( query.equals(";;switchBE") ) {
					dfaConverter.switchShowBooleanExpression();
				} else if( query.equals(";;execTime") ) {
					printTime = true;
				} else {
					ConsoleUtils.println("|- wrong query -|");
				}
			} else {
				long st = System.currentTimeMillis();
				ConsoleUtils.println(dfaConverter.exec(query)?"accepted":"rejected");
				long ed = System.currentTimeMillis();
				if( printTime ) {
					System.out.println((ed - st)  + "ms");
					printTime = false;
				}
			}
			ConsoleUtils.print(">>>");
		}
	}
}
