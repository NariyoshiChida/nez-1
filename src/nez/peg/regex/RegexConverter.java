package nez.peg.regex;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;

import nez.NezException;
import nez.NezOption;
import nez.SourceContext;
import nez.ast.AbstractTree;
import nez.ast.CommonTree;
import nez.ast.Tag;
import nez.lang.Expression;
import nez.lang.Grammar;
import nez.lang.GrammarFactory;
import nez.lang.GrammarFile;
import nez.lang.Production;
import nez.util.ConsoleUtils;
import nez.util.StringUtils;
import nez.util.UList;

public class RegexConverter extends GrammarConverter{
	HashMap<Integer, Method> methodMap = new HashMap<Integer, Method>();
	int NonTerminalCount = 0;
	public RegexConverter(GrammarFile grammar, String name) {
		super(grammar, name);
	}
	
	
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

	public final static GrammarFile loadGrammar(SourceContext regex,NezOption option) throws IOException {
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
		
	
	public final Expression pi(AbstractTree<?> expr, Expression k) {
		Tag tag = expr.getTag();
		Method m = lookupPiMethod("pi", tag.tagId);
		if(m != null) {
			try {
				return (Expression)m.invoke(this, expr, k);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				System.err.println(expr);
				e.printStackTrace();
			}
		}
		return null;
	}
	
	protected Method lookupPiMethod(String method, int tagId) {
		Integer key = tagId;
		Method m = this.methodMap.get(key);
		if(m == null) {
			String name = method + Tag.tag(tagId).getName();
			try {
				m = this.getClass().getMethod(name, CommonTree.class, Expression.class);
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
				return null;
			} catch (SecurityException e) {
				return null;
			}
			this.methodMap.put(key, m);
		}
		return m;
	}
	
	@Override
	public void convert(CommonTree e) {
		grammar.defineProduction(e, "File", pi(e, null));
		//System.out.println("\nConverted Rule: " + grammar.getResourceName());
		//grammar.dump();
		makeFile(e);
	}
	
	public void makeFile(CommonTree e) {
		file.write("// This file is generated by src.nez.x.RegexConverter.java");
		file.writeIndent("// Generate Date: " + new Date().toString());
		file.writeIndent("// Input regex :  " + e.toText());
		file.writeIndent("\n");
		String prev = new String();
		for(Production r : grammar.getAllProductionList()) {
			//if( prev.equals(r.toString()) ) continue;
			prev = r.toString();
			file.write(r.toString()); 
			file.writeIndent("\n");
		}
		file.flush();
	}
	
	public Expression piPattern(CommonTree e, Expression k) {
		return this.pi(e.get(0), k);
	}

	// pi(e, k) e: regular expression, k: continuation
	// pi(e1|e2, k) = pi(e1, k) / pi(e2, k)
	public Expression piOr(CommonTree e, Expression k) {
		return toChoice(e, pi(e.get(0), k), pi(e.get(1), k));
	}

	// pi(e1e2, k) = pi(e1, pi(e2, k))
	public Expression piConcatenation(CommonTree e, Expression k) {
		return pi(e.get(0), pi(e.get(1), k));
	}

	// pi((?>e), k) = pi(e, "") k
	public Expression piIndependentExpr(CommonTree e, Expression k) {
		return toSeq(e, pi(e.get(0), toEmpty(e)), k);
	}

	// pi((?=e), k) = &pi(e, "") k
	public Expression piAnd(CommonTree e, Expression k) {
		return toAnd(e, k);
	}

	// pi((?!e), k) = !pi(e, "") k
	public Expression piNot(CommonTree e, Expression k) {
		return toNot(e, k);
	}

	// pi(e*+, k) = pi(e*, "") k
	public Expression piPossessiveRepetition(CommonTree e, Expression k) {
		return toSeq(e, piRepetition(e, toEmpty(e)), k);
	}

	// pi(e*?, k) = A, A <- k / pi(e, A)
	public Expression piLazyQuantifiers(CommonTree e, Expression k) {
		String ruleName = "Repetition" + NonTerminalCount++;
		Expression ne = GrammarFactory.newNonTerminal(e, this.grammar, ruleName);
		if( k == null ) { 
			k = GrammarFactory.newEmpty(null);
		}
		grammar.defineProduction(e, ruleName, toChoice(e, k, pi(e.get(0), ne)));
		return ne;
	}

	// pi(e*, k) = A, A <- pi(e, A) / k
	public Expression piRepetition(CommonTree e, Expression k) {
		String ruleName = "Repetition" + NonTerminalCount++;
		Expression ne = GrammarFactory.newNonTerminal(e, this.grammar, ruleName);
		grammar.defineProduction(e, ruleName, toChoice(e, pi(e.get(0), ne), k));
		return ne;
	}
	 		
	// pi(e?, k) = pi(e, k) / k
	public Expression piOption(CommonTree e, Expression k) {
		return toChoice(e, pi(e.get(0), k), k);
	}

	public Expression piOneMoreRepetition(CommonTree e, Expression k) {
		return pi(e.get(0), piRepetition(e, k));
	}

	public Expression piAny(CommonTree e, Expression k) {
		return toSeq(e,k);
	}

	public Expression piNegativeCharacterSet(CommonTree e, Expression k) {
		Expression nce = toSeq(e, GrammarFactory.newNot(e, toCharacterSet(e)), toAny(e));
		return toSeq(e, nce, k);
	}

	public Expression piCharacterSet(CommonTree e, Expression k) {
		return toSeq(e, k);
	}

	public Expression piCharacterRange(CommonTree e, Expression k) {
		return toSeq(e, k);
	}
	
	public Expression piCharacterSetItem(CommonTree e, Expression k) {
		return toSeq(e, k);
	}

	// pi(c, k) = c k
	// c: single character
	public Expression piCharacter(CommonTree c, Expression k) {
		return toSeq(c, k);
	}
	
	// grouping
	// piGrouping
	public Expression piCapture(CommonTree e,Expression k) {
		return pi(e.get(0),k);
	}
	
	private Expression toExpression(AbstractTree<?> e) {
		return (Expression)this.visit("to", e);
	}
	
	public Expression toCharacter(AbstractTree<?> c) {
		String text = c.toText();
		byte[] utf8 = StringUtils.toUtf8(text);
		if (utf8.length !=1) {
			ConsoleUtils.exit(1, "Error: not Character Literal");
		}
		return GrammarFactory.newByteChar(null, false, utf8[0]);
	}
	
	boolean byteMap[];
	boolean useByteMap = true;
	public Expression toCharacterSet(AbstractTree<?> e) {
		UList<Expression> l = new UList<Expression>(new Expression[e.size()]);
		byteMap = new boolean[257];
		for(AbstractTree<?> subnode: e) {
			GrammarFactory.addChoice(l, toExpression(subnode));
		}
		if (useByteMap) {
			return GrammarFactory.newByteMap(null, false, byteMap);
		}
		else {
			return GrammarFactory.newChoice(null, l);
		}
	}
	
	public Expression toCharacterRange(AbstractTree<?> e) {
		byte[] begin = StringUtils.toUtf8(e.get(0).toText());
		byte[] end = StringUtils.toUtf8(e.get(1).toText());
		byteMap = new boolean[257];
		for(byte i = begin[0]; i <= end[0]; i++) {
			byteMap[i] = true;
		}
		return GrammarFactory.newCharSet(null, e.get(0).toText(), e.get(1).toText());
	}
	
	public Expression toCharacterSetItem(AbstractTree<?> c) {
		byte[] utf8 = StringUtils.toUtf8(c.toText());
		byteMap[utf8[0]] = true;
		return GrammarFactory.newByteChar(null, false, utf8[0]);
	}
	
	public Expression toEmpty(AbstractTree<?> node) {
		return GrammarFactory.newEmpty(null);
	}

	public Expression toAny(AbstractTree<?> e) {
		return GrammarFactory.newAnyChar(null, false);
	}
	
	public Expression toAnd(AbstractTree<?> e, Expression k) {
		return toSeq(e, GrammarFactory.newAnd(null, pi(e.get(0), toEmpty(e))), k);
	}
	
	public Expression toNot(AbstractTree<?> e, Expression k) {
		return toSeq(e, GrammarFactory.newNot(null, pi(e.get(0), toEmpty(e))), k);
	}

	public Expression toChoice(AbstractTree<?> node, Expression e, Expression k) {
		UList<Expression> l = new UList<Expression>(new Expression[2]);
		GrammarFactory.addChoice(l, e);
		if (k != null) {
			GrammarFactory.addChoice(l, k);
		}
		else {
			GrammarFactory.addChoice(l, toEmpty(node));
		}
		return GrammarFactory.newChoice(null, l);
	}

	public Expression toSeq(AbstractTree<?> e, Expression k) {
		UList<Expression> l = new UList<Expression>(new Expression[2]);
		GrammarFactory.addSequence(l, toExpression(e));
		if(k != null) {
			GrammarFactory.addSequence(l, k);
		}
		return GrammarFactory.newSequence(null, l);
	}
	
	public Expression toSeq(AbstractTree<?> node, Expression e, Expression k) {
		UList<Expression> l = new UList<Expression>(new Expression[2]);
		GrammarFactory.addSequence(l, e);
		if (k != null) {
			GrammarFactory.addSequence(l, k);
		}
		return GrammarFactory.newSequence(null, l);
	}

	@Override
	public String getDesc() {
		return "regex";
	}
}
