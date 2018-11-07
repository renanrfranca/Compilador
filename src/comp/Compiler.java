
package comp;

import java.io.PrintWriter;
import java.util.ArrayList;
import ast.CianetoClass;
import ast.LiteralInt;
import ast.MetaobjectAnnotation;
import ast.Program;
import ast.Statement;
import lexer.Lexer;
import lexer.Token;

public class Compiler {

	// compile must receive an input with an character less than
	// p_input.lenght
	public Program compile(char[] input, PrintWriter outError) {

		ArrayList<CompilationError> compilationErrorList = new ArrayList<>();
		signalError = new ErrorSignaller(outError, compilationErrorList);
		symbolTable = new SymbolTable();
		lexer = new Lexer(input, signalError);
		signalError.setLexer(lexer);

		Program program = null;
		lexer.nextToken();
		program = program(compilationErrorList);
		return program;
	}

	private Program program(ArrayList<CompilationError> compilationErrorList) {
		// Program ::= CianetoClass { CianetoClass }
		ArrayList<MetaobjectAnnotation> metaobjectCallList = new ArrayList<>();
		ArrayList<CianetoClass> CianetoClassList = new ArrayList<>();
		Program program = new Program(CianetoClassList, metaobjectCallList, compilationErrorList);
		boolean thereWasAnError = false;
		while ( lexer.token == Token.CLASS ||
				(lexer.token == Token.ID && lexer.getStringValue().equals("open") ) ||
				lexer.token == Token.ANNOT ) {
			try {
				while ( lexer.token == Token.ANNOT ) {
					metaobjectAnnotation(metaobjectCallList);
				}
				classDec();
			}
			catch( CompilerError e) {
				// if there was an exception, there is a compilation error
				thereWasAnError = true;
				while ( lexer.token != Token.CLASS && lexer.token != Token.EOF ) {
					try {
						next();
					}
					catch ( RuntimeException ee ) {
						e.printStackTrace();
						return program;
					}
				}
			}
			catch ( RuntimeException e ) {
				e.printStackTrace();
				thereWasAnError = true;
			}

		}
		if ( !thereWasAnError && lexer.token != Token.EOF ) {
			try {
				error("End of file expected");
			}
			catch( CompilerError e) {
			}
		}
		return program;
	}

	/**  parses a metaobject annotation as <code>{@literal @}cep(...)</code> in <br>
     * <code>
     * @cep(5, "'class' expected") <br>
     * class Program <br>
     *     func run { } <br>
     * end <br>
     * </code>
     *

	 */
	@SuppressWarnings("incomplete-switch")
	private void metaobjectAnnotation(ArrayList<MetaobjectAnnotation> metaobjectAnnotationList) {
		String name = lexer.getMetaobjectName();
		int lineNumber = lexer.getLineNumber();
		lexer.nextToken();
		ArrayList<Object> metaobjectParamList = new ArrayList<>();
		boolean getNextToken = false;
		if ( lexer.token == Token.LEFTPAR ) {
			// metaobject call with parameters
			lexer.nextToken();
			while ( lexer.token == Token.LITERALINT || lexer.token == Token.LITERALSTRING ||
					lexer.token == Token.ID ) {
				switch ( lexer.token ) {
				case LITERALINT:
					metaobjectParamList.add(lexer.getNumberValue());
					break;
				case LITERALSTRING:
					metaobjectParamList.add(lexer.getLiteralStringValue());
					break;
				case ID:
					metaobjectParamList.add(lexer.getStringValue());
				}
				lexer.nextToken();
				if ( lexer.token == Token.COMMA )
					lexer.nextToken();
				else
					break;
			}
			if ( lexer.token != Token.RIGHTPAR )
				error("')' expected after metaobject call with parameters");
			else {
				getNextToken = true;
			}
		}
		if ( name.equals("nce") ) {
			if ( metaobjectParamList.size() != 0 )
				error("Metaobject 'nce' does not take parameters");
		}
		else if ( name.equals("cep") ) {
			if ( metaobjectParamList.size() != 3 && metaobjectParamList.size() != 4 )
				error("Metaobject 'cep' take three or four parameters");
			if ( !( metaobjectParamList.get(0) instanceof Integer)  ) {
				error("The first parameter of metaobject 'cep' should be an integer number");
			}
			else {
				int ln = (Integer ) metaobjectParamList.get(0);
				metaobjectParamList.set(0, ln + lineNumber);
			}
			if ( !( metaobjectParamList.get(1) instanceof String) ||  !( metaobjectParamList.get(2) instanceof String) )
				error("The second and third parameters of metaobject 'cep' should be literal strings");
			if ( metaobjectParamList.size() >= 4 && !( metaobjectParamList.get(3) instanceof String) )
				error("The fourth parameter of metaobject 'cep' should be a literal string");

		}
		metaobjectAnnotationList.add(new MetaobjectAnnotation(name, metaobjectParamList));
		if ( getNextToken ) lexer.nextToken();
	}

	private void classDec() {
		if ( lexer.token == Token.ID && lexer.getStringValue().equals("open") ) {
			// open class
            next();
		}
		if ( lexer.token != Token.CLASS ) error("'class' expected");
		lexer.nextToken();
		if ( lexer.token != Token.ID )
			error("Identifier expected");
		String className = lexer.getStringValue();
		lexer.nextToken();
		if ( lexer.token == Token.EXTENDS ) {
			lexer.nextToken();
			if ( lexer.token != Token.ID )
				error("Identifier expected");
			String superclassName = lexer.getStringValue();

			lexer.nextToken();
		}

		memberList();
		if ( lexer.token != Token.END)
			error("'end' expected");
		lexer.nextToken();

	}

	private void memberList() {
		while ( true ) {
			qualifier();
			if ( lexer.token == Token.VAR ) {
				fieldDec();
			}
			else if ( lexer.token == Token.FUNC ) {
				methodDec();
			}
			else {
				break;
			}
		}
	}

	private void error(String msg) {
		this.signalError.showError(msg);
	}

	private void next() {
		lexer.nextToken();
	}

	private void check(Token shouldBe, String msg) {
		if ( lexer.token != shouldBe ) {
			error(msg);
		}
	}

	private void methodDec() {
		lexer.nextToken();
		if ( lexer.token == Token.ID ) {
			// unary method
			lexer.nextToken();

		}
		else if ( lexer.token == Token.IDCOLON ) {
			// keyword method. It has parameters
            formalParamDec();
		}
		else {
			error("An identifier or identifer: was expected after 'func'");
		}
		if ( lexer.token == Token.MINUS_GT ) {
			// method declared a return type
			lexer.nextToken();
			type();
		}
		if ( lexer.token != Token.LEFTCURBRACKET ) {
			error("'{' expected");
		}
		next();
		statementList();
		if ( lexer.token != Token.RIGHTCURBRACKET ) {
			error("'{' expected");
		}
		next();

	}

    private void formalParamDec() {
	    next();
	    ParamDec();
	    while (lexer.token == Token.COMMA){
	        next(); // consome a virgula
	        ParamDec();
        }
        next();
    }

    private void ParamDec() {
	    type();
        if ( lexer.token == Token.ID ) {
            next();
        } else {
            error("An identifier or was expected after the type");
        }
    }

    private void statementList() {
		  // only '}' is necessary in this test
		while ( lexer.token != Token.RIGHTCURBRACKET && lexer.token != Token.END ) {
			statement();
		}
	}

	private void statement() {
		boolean checkSemiColon = true;
		switch ( lexer.token ) {
		case IF:
			ifStat();
			checkSemiColon = false;
			break;
		case WHILE:
			whileStat();
			checkSemiColon = false;
			break;
		case RETURN:
			returnStat();
			break;
		case BREAK:
			breakStat();
			break;
		case SEMICOLON:
			next();
			break;
		case REPEAT:
			repeatStat();
			break;
		case VAR:
			localDec();
			break;
		case ASSERT:
			assertStat();
			break;
		default:
			if ( lexer.token == Token.ID && lexer.getStringValue().equals("Out") ) {
				writeStat();
			}
			else {
				assignExpr();
			}

		}
		if ( checkSemiColon ) {
			check(Token.SEMICOLON, "';' expected");
		}
	}

	private void localDec() {
		next();
		type();
		check(Token.ID, "A variable name was expected");
		while ( lexer.token == Token.ID ) {
			next();
			if ( lexer.token == Token.COMMA ) {
				next();
			}
			else {
				break;
			}
		}
		if ( lexer.token == Token.ASSIGN ) {
			next();
			// check if there is just one variable
			expression();
		}

	}

	private void repeatStat() {
		next();
		while ( lexer.token != Token.UNTIL && lexer.token != Token.RIGHTCURBRACKET && lexer.token != Token.END ) {
			statement();
		}
		check(Token.UNTIL, "'until' was expected");
	}

	private void breakStat() {
		next();

	}

	private void returnStat() {
		next();
		expression();
	}

	private void whileStat() {
		next();
		expression();
		check(Token.LEFTCURBRACKET, "'{' expected after the 'while' expression");
		next();
		while ( lexer.token != Token.RIGHTCURBRACKET && lexer.token != Token.END ) {
			statement();
		}
		check(Token.RIGHTCURBRACKET, "'}' was expected");
	}

	private void ifStat() {
		next();
		expression();
		check(Token.LEFTCURBRACKET, "'{' expected after the 'if' expression");
		next();
		while ( lexer.token != Token.RIGHTCURBRACKET && lexer.token != Token.END && lexer.token != Token.ELSE ) {
			statement();
		}
		check(Token.RIGHTCURBRACKET, "'}' was expected");
		if ( lexer.token == Token.ELSE ) {
			next();
			check(Token.LEFTCURBRACKET, "'{' expected after 'else'");
			next();
			while ( lexer.token != Token.RIGHTCURBRACKET ) {
				statement();
			}
			check(Token.RIGHTCURBRACKET, "'}' was expected");
		}
	}

	/**

	 */
	private void writeStat() {
		next();
		check(Token.DOT, "a '.' was expected after 'Out'");
		next();
		check(Token.IDCOLON, "'print:' or 'println:' was expected after 'Out.'");
		String printName = lexer.getStringValue();
		expression();
	}

	private void assignExpr(){
	    expression();
        if (lexer.token == Token.EQ){
            next();
            expression();
        }
        // next(); (expression vai dar next?)
    }

	private void expression() {
        simpleExpression();
        if (isRelation(lexer.token)){
            next();
            simpleExpression();
        }
        // next dentro das funções
	}

	private void expressionList() {
		expression(); // Next
		while (lexer.token == Token.COMMA) {
			expression();
		}
	}

    private void simpleExpression() {
        SumSubExpression();
        while (lexer.token == Token.PLUSPLUS){
            next();
            SumSubExpression();
        }
    }

    private void SumSubExpression() {
	    term();
	    while (isLowOperator(lexer.token)) { // “+” | “−” | “||”
	        term();
        }
    }

    private void term() {
	    signalFactor();
        while (isHighOperator(lexer.token)) { // “∗” | “/” | “&&”
            signalFactor();
        }
    }

    private void signalFactor() {
	    if (isSignal(lexer.token)){
	        next();
        }
	    factor();
    }

	private void factor() {
		if (isBasicValue(lexer.token)) {
			next();
			return;
		}

		if (lexer.token == Token.LEFTPAR){
			next();
			expression();
			if (lexer.token != Token.RIGHTPAR)
				error("A right parenthesis was expected");
			next();
			return;
		}

		if (lexer.token == Token.NOT){
			next();
			factor(); // Já faz next;
			return;
		}

		if (lexer.token == Token.NULL){
			next();
			return;
		}

		// ObjectCreation foi mesclado com o primaryExpression!
		if (lexer.token == Token.ID ||
			lexer.token == Token.SUPER ||
			lexer.token == Token.SELF ||
			lexer.token == Token.IN)
		{
			primaryExpression();
			return;
		}
	}

	// readExpression e objectCreation mesclados
	private void primaryExpression() {
		if (lexer.token == Token.SUPER){
			next();
			if (lexer.token != Token.DOT)
				error("Dot expected.");
			next();
			if (lexer.token == Token.IDCOLON){
				next();
				expressionList();
			} else if (lexer.token == Token.ID) {
				next();
			} else {
				error("An identifier was expected after the dot");
			}
			return;
		}

		if (lexer.token == Token.ID) {
			next();
			if (lexer.token == Token.DOT){
				if (lexer.token == Token.ID){
					next();
					return;
				}

				if (lexer.token == Token.IDCOLON){
					next();
					expressionList();
					return;
				}

				// objectCreation mesclado
				if (lexer.token == Token.NEW){
					next();
					return;
				}
			}
			return;
		}

		if (lexer.token == Token.SELF){
			next();
			if (lexer.token == Token.DOT){
				next();
				if (lexer.token == Token.ID){
					next();

					if (lexer.token == Token.DOT){
						next();
						if (lexer.token == Token.ID){
							next();
							return;
						}
						if (lexer.token == Token.IDCOLON){
							next();
							expressionList();
							return;
						}
						error("Id expected");
					}

					return;
				}

				if (lexer.token == Token.IDCOLON){
					next();
					expressionList();
					return;
				}
			}
			return;
		}

		if (lexer.token == Token.IN){
			next();
			check(Token.DOT, "Dot expected");
			next();
			if (lexer.token != Token.READINT && lexer.token != Token.READSTRING)
				error("readInt or readString expected");
			next();
			return;
		}
	}

	private void fieldDec() {
		lexer.nextToken();
		type();
		if ( lexer.token != Token.ID ) {
			this.error("A variable name was expected");
		}
		else {
			while ( lexer.token == Token.ID  ) {
				lexer.nextToken();
				if ( lexer.token == Token.COMMA ) {
					lexer.nextToken();
				}
				else {
					break;
				}
			}
		}

	}

	private void basicValue(){
		if (lexer.token == Token.LITERALINT || lexer.token == Token.TRUE || lexer.token == Token.FALSE || lexer.token == Token.LITERALSTRING ) {
			next();
		} else {
			this.error("An integer, string or boolean value was expected");
		}
	}

	private void type() {
		if ( lexer.token == Token.INT || lexer.token == Token.BOOLEAN || lexer.token == Token.STRING ) {
			next();
		}
		else if ( lexer.token == Token.ID ) {
			next();
		}
		else {
			this.error("A type was expected");
		}

	}


	private void qualifier() {
		if ( lexer.token == Token.PRIVATE ) {
			next();
		}
		else if ( lexer.token == Token.PUBLIC ) {
			next();
		}
		else if ( lexer.token == Token.OVERRIDE ) {
			next();
			if ( lexer.token == Token.PUBLIC ) {
				next();
			}
		}
		else if ( lexer.token == Token.FINAL ) {
			next();
			if ( lexer.token == Token.PUBLIC ) {
				next();
			}
			else if ( lexer.token == Token.OVERRIDE ) {
				next();
				if ( lexer.token == Token.PUBLIC ) {
					next();
				}
			}
		}
	}
	/**
	 * change this method to 'private'.
	 * uncomment it
	 * implement the methods it calls
	 */
	public Statement assertStat() {

		lexer.nextToken();
		int lineNumber = lexer.getLineNumber();
		expression();
		if ( lexer.token != Token.COMMA ) {
			this.error("',' expected after the expression of the 'assert' statement");
		}
		lexer.nextToken();
		if ( lexer.token != Token.LITERALSTRING ) {
			this.error("A literal string expected after the ',' of the 'assert' statement");
		}
		String message = lexer.getLiteralStringValue();
		lexer.nextToken();
		if ( lexer.token == Token.SEMICOLON )
			lexer.nextToken();

		return null;
	}




	private LiteralInt literalInt() {

		LiteralInt e = null;

		// the number value is stored in lexer.getToken().value as an object of
		// Integer.
		// Method intValue returns that value as an value of type int.
		int value = lexer.getNumberValue();
		lexer.nextToken();
		return new LiteralInt(value);
	}

	private static boolean startExpr(Token token) {

		return token == Token.FALSE || token == Token.TRUE
				|| token == Token.NOT || token == Token.SELF
				|| token == Token.LITERALINT || token == Token.SUPER
				|| token == Token.LEFTPAR || token == Token.NULL
				|| token == Token.ID || token == Token.LITERALSTRING;

	}


    // ----------------------- Checagem ------------------------

    private boolean isRelation(Token token) {
        return  (token == Token.EQ) ||
                (token == Token.GE) ||
                (token == Token.GT) ||
                (token == Token.LT) ||
                (token == Token.LE) ||
                (token == Token.NEQ);
    }

    private boolean isLowOperator(Token token) {
	    return (token == Token.PLUS) ||
                (token == Token.MINUS) ||
                (token == Token.OR);
    }

    private boolean isHighOperator(Token token) {
	    return (token == Token.MULT) ||
                (token == Token.DIV) ||
                (token == Token.AND);
    }

    private boolean isSignal(Token token) {
        return (token == Token.PLUS) ||
                (token == Token.MINUS);
    }

    private boolean isBasicValue(Token token){
		return (lexer.token == Token.LITERALINT || lexer.token == Token.TRUE || lexer.token == Token.FALSE || lexer.token == Token.LITERALSTRING );
	}


	private SymbolTable		symbolTable;
	private Lexer			lexer;
	private ErrorSignaller	signalError;

}
