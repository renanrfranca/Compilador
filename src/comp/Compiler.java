
package comp;

import java.io.PrintWriter;
import java.util.ArrayList;

import ast.*;
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

	// Semântico feito
	private CianetoClass classDec() {
		CianetoClass ciaClass;
		boolean open = false;
		CianetoClass superClass;
		MemberList mList;

		if ( lexer.token == Token.ID && lexer.getStringValue().equals("open") ) {
			// open class
			open = true;
            next();
		}

		if ( lexer.token != Token.CLASS ) error("'class' expected");
		lexer.nextToken();

		if ( lexer.token != Token.ID )
			error("Identifier expected");
		String className = lexer.getStringValue();

		if (symbolTable.getInGlobal(className) != null){
			error("There's already a class named " + className);
		}

        ciaClass = new CianetoClass(className);
        ciaClass.setOpen(open);
		lexer.nextToken();

		if ( lexer.token == Token.EXTENDS ) {
			lexer.nextToken();
			if ( lexer.token != Token.ID )
				error("Identifier expected");

			String superclassName = lexer.getStringValue();
			superClass = symbolTable.getInGlobal(superclassName);

			if (superClass == null){
				error("Superclass" + superclassName + " doesn't exists.");
			}
			if (superclassName.equals(className)){
				error("A class can't inherit itself.");
			}
			if (!superClass.isOpen()){
				error("Superclass" + superclassName + " isn't open.");
			}

			lexer.nextToken();
		}

		ciaClass.setMemberList(memberList());

		if ( lexer.token != Token.END)
			error("'end' expected");
		lexer.nextToken();


		if (className.equals("Program")){
			if (symbolTable.getInLocal("run") == null){
				error("Missing run method from Program class");
			}
		}

		symbolTable.removeLocalTable();
		return ciaClass;
	}

	private MemberList memberList() {
		MemberList m = new MemberList();
		Field f;
		Method method;
		Qualifiers q;

		while ( true ) {
			q = qualifier();
			if ( lexer.token == Token.VAR ) {
				if (!q.unchanged){
					error("Class variables don't accept qualifiers");
				}
				q.setPrivate();
				f = fieldDec();
				f.setQualifiers(q);
				m.addMember(f);
			}
			else if ( lexer.token == Token.FUNC ) {
				method = methodDec();
				if (method.getName().equals("run")){
					if (q.isPrivate() || q.isFinal() || q.isOverride()){
						error("Method run has to be public and accepts no other qualifiers");
					}
				}
				method.setQualifiers(q);

				m.addMember(method);
			}
			else {
				if (lexer.token == Token.SEMICOLON){
					next();
				}
				break;
			}
		}
	}

	private void error(String msg) {
		this.signalError.showError(msg);
	}

	private void errorLastToken(String msg) {
		this.signalError.showError(msg, true);
	}

	private void next() {
		lexer.nextToken();
	}

	private void check(Token shouldBe, String msg) {
		check(shouldBe, msg, false);
	}

	private void check(Token shouldBe, String msg, boolean goPreviousToken) {
		if ( lexer.token != shouldBe ) {
			if (goPreviousToken){
				errorLastToken(msg);
			} else {
				error(msg);
			}
		}
	}

	private void idList(){
        next();
        idList();
        while (lexer.token == Token.COMMA){
            next(); // consome a virgula
            idList();
        }
        next();
    }

	private Method methodDec() {
		Method m;
		String methodName;

		lexer.nextToken();
		if (lexer.token == Token.ID) {
			// unary method
			methodName = lexer.getStringValue();
			m = new Method(methodName);
			lexer.nextToken();
		}
		else if ( lexer.token == Token.IDCOLON ) {
			// keyword method. It has parameters
			methodName = lexer.getStringValue();
			m = new Method(methodName);
			if (methodName.equals("run")){
				error("Method run doesn't accepts any parameters");
			}

            formalParamDec();
		}
		else {
			error("An identifier or identifer: was expected after 'func'");
		}

		if (symbolTable.getInLocal(methodName) != null){
			error("There's already a method named " + methodName);
		}


		if ( lexer.token == Token.MINUS_GT ) {
			// method declared a return type
			lexer.nextToken();
			m.setReturnType(type());
		}
		if ( lexer.token != Token.LEFTCURBRACKET ) {
			error("'{' expected");
		}
		next();
		statementList();
		if ( lexer.token != Token.RIGHTCURBRACKET ) {
			error("'}' expected");
		}

		symbolTable.putInlocal(methodName, m);
		symbolTable.removeFuncTable();
		next();

		return m;
	}

    private void formalParamDec() {

	    next();
	    ParamDec();
	    while (lexer.token == Token.COMMA){
	        next(); // consome a virgula
	        ParamDec();
        }
    }

    private void ParamDec() {
		String fieldName;
		Type t;
	    t = type();
        if ( lexer.token != Token.ID ) {
			error("An identifier or was expected after the type");

        }

		fieldName = lexer.getStringValue();
		next();

		if (symbolTable.getInFunc(fieldName) != null){
			error("Redeclared parameter");
		}

		Field f = new Field(fieldName, t);
		symbolTable.putInFunc(fieldName, f);
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
//			next();
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
				if ( lexer.token == Token.IDCOLON){
					if (lexer.getStringValue().equals("print:") ||
							lexer.getStringValue().equals("println:")){
						error("Missing 'Out.'");
					}
				}
				assignExpr();
			}

		}
		if ( checkSemiColon ) {
			check(Token.SEMICOLON, "';' expected", true);
			next();
		}
	}

	private void localDec() {
		next();
		type();
		check(Token.ID, "Identifier expected");
		while ( lexer.token == Token.ID ) {
			next();
			if ( lexer.token == Token.COMMA ) {
				next();
				check(Token.ID, "Missing identifier");
			}
			else {
				check(Token.SEMICOLON, "Missing ';'", true);
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
		next();
		expression();
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
		next();
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
		next();
		if ( lexer.token == Token.ELSE ) {
			next();
			check(Token.LEFTCURBRACKET, "'{' expected after 'else'");
			next();
			while ( lexer.token != Token.RIGHTCURBRACKET ) {
				statement();
			}
			check(Token.RIGHTCURBRACKET, "'}' was expected");
			next();
		}
	}

	private void intValue() {
	    next();
	    check(Token.LITERALINT, "Número inteiro esperado");
    }

	/**

	 */
	private void writeStat() {
		next();
		check(Token.DOT, "a '.' was expected after 'Out'");
		next();
		check(Token.IDCOLON, "'print:' or 'println:' was expected after 'Out.'");
		String printName = lexer.getStringValue();
		next();

		expression();
	}

	private void assignExpr(){
	    expression();
        if (lexer.token == Token.ASSIGN){
            next();
            expression();
        }
        // next(); (expression vai dar next?)
    }

	private Expr expression() {
		Expr left;
		Token op;
		Expr right;

        left = simpleExpression();

        if (isRelation(lexer.token)){
			op = relation();
            right = simpleExpression();

            if (! left.getType().isCompatible(right.getType())){
            	error("Incompatible types");
			}
            CompositeExpr ce = new CompositeExpr(left, op, right);
            return ce;
        }

        return left;
	}

	private ExpressionList expressionList() {
		ExpressionList exprList = new ExpressionList();

		exprList.addElement(expression()); // Next
		while (lexer.token == Token.COMMA) {
			next();
            exprList.addElement(expression());
		}

		return exprList;
	}

    private Expr simpleExpression() {
		Expr left = SumSubExpression();

		while (lexer.token == Token.PLUS){
			next();
			check(Token.PLUS, "\"++\" expected!");
			next();
			if (left.getType() != Type.stringType && left.getType() != Type.intType){
				error("Type must be String or Int");
			}
			Expr right = SumSubExpression();
			if (right.getType() != Type.stringType && right.getType() != Type.intType){
				error("Type must be String or Int");
			}

			left = new LiteralString("Expression");
		}
		return left;
    }

    private Expr SumSubExpression() {
	    term();
	    while (isLowOperator(lexer.token)) { // “+” | “−” | “||”
	    	next();
	        term();
        }
    }

    private void term() {
	    signalFactor();
        while (isHighOperator(lexer.token)) { // “∗” | “/” | “&&”
        	next();
            signalFactor();
        }
    }

    private void signalFactor() {
	    if (isSignal(lexer.token)){
	        next();
        }
	    factor();
    }

	private Type factor() {
		Type t;

		if (isBasicValue(lexer.token)) {
			if (lexer.token == Token.TRUE || lexer.token == Token.FALSE){
				next();
				return Type.booleanType;
			} else if (lexer.token == Token.LITERALINT){
				next();
				return Type.intType;
			} else {
				next();
				return Type.stringType;
			}
		}

		if (lexer.token == Token.LEFTPAR){
			next();
			t = expression();
			if (lexer.token != Token.RIGHTPAR)
				error("')' expected");
			next();
			return t;
		}

		if (lexer.token == Token.NOT){
			next();
			return factor(); // Já fif (lexer.token != Token.READINT && lexer.token != Token.az next;
		}

		if (lexer.token == Token.NULL){
			next();
			return Type.nullType;
		}

		// ObjectCreation foi mesclado com o primaryExpression!
		if (lexer.token == Token.ID ||
			lexer.token == Token.SUPER ||
			lexer.token == Token.SELF ||
			lexer.token == Token.IN)
		{
			return primaryExpression();
		}

		// Se não caiu em nenhum dos casos, não é uma expressão
		error("Expression expected");
	}

	// objectCreation mesclado
	private Expr primaryExpression() {
        String name;
        Method m;

	    if (lexer.token == Token.SUPER){
            CianetoClass superClass = currentClass.getSuperClass();
		    if (superClass == null){
		        error("Class " + currentClass.getName() + " has no super class");
            }
			next();
			if (lexer.token != Token.DOT)
				error("Dot expected.");
			next();
			if (lexer.token == Token.IDCOLON){
                name = lexer.getStringValue();
                m = superClass.getPublicMethod(name);

                if (m == null){
                    error("Method not found in super class");
                }
				next();
                ExpressionList exprList = expressionList();

                int valid = m.checkParams(exprList);

                if (valid == -1)
                    error("Method " + m.getName() + " requires exactly" + m.getParamList().size() + " parameters");

                if (valid > 0)
                    error("Param " + valid + " of incompatible type. Expected: " + m.getParamList().getField(valid).getType().getName());

                return m;

			} else if (lexer.token == Token.ID) {
			    name = lexer.getStringValue();
			    m = superClass.getMethod(name);

				next();
			    return m;
			} else {
				error("An identifier was expected after the dot");
			}
		}

		if (lexer.token == Token.ID) {
			if (lexer.getStringValue().equals("readInt")){
				error("'In.' expected before 'read' command");
			}
			name = lexer.getStringValue();

			next();

			if (lexer.token == Token.DOT){
			    Field param = currentMethod.getParam(name);

			    if (param != null){
                    if (lexer.token == Token.ID){
                        if (! param.getType().equals("CiaClass")){
                            error("Trying invoke a method or variable of something that is not a class");
                        }
                        name = lexer.getStringValue();
                        Method m =
                        next();
                        return;
                    }

                    if (lexer.token == Token.IDCOLON){
                        next();
                        expressionList();
                        return;
                    }
                } else {
                    CianetoClass cianetoClass = symbolTable.getInGlobal(name);
			        if (cianetoClass == null){
			            error(name + " is not a parameter or a class.");
                    }
			        next();
                    // objectCreation mesclado
                    if (lexer.token == Token.NEW){
                        next();
                        ObjectCreation oc = new ObjectCreation(cianetoClass);
                        return oc;
                    } else {
                        error("'new' expected");
                        return null;
                    }
                }

			    next();



			} else {
                Field variable = currentClass.getField(name);
                if (variable == null)
                    error("Variable " + name + "does not exist.");
                return variable;
            }
		}

		if (lexer.token == Token.IN){
			readExpr();
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
	}

	private ReadExpr readExpr() {
		String methodName;
		next();
		check(Token.DOT, "A dot was expected");
		next();

		check(Token.ID, "Command 'In.' without arguments");
		methodName = lexer.getStringValue();
		next();

		if (methodName.equals("readInt")){
			return new ReadExpr(Type.intType);
		}

		if (methodName.equals("readString")){
			return new ReadExpr(Type.stringType);
		} else {
			error("Method" + methodName + "does not exist");
			return null;
		}
	}

	private Field fieldDec() {
		String fieldName;
		Type t;
		Field f = null;

		lexer.nextToken();
		t = type();

		if ( lexer.token != Token.ID ) {
			this.error("A variable name was expected");
		}

		while ( lexer.token == Token.ID  ) {
			fieldName = lexer.getStringValue();
			if (symbolTable.getInLocal(fieldName) != null){
				error("Variable " + fieldName + " already exists");
			}

			f = new Field(fieldName, t);
			symbolTable.putInlocal(fieldName, f);

			lexer.nextToken();
			if ( lexer.token == Token.COMMA ) {
				lexer.nextToken();
			}
			else {
				if (lexer.token == Token.SEMICOLON){
					next();
				}
				break;
			}
		}

		return f;
	}

	private void basicType(){
	    if (lexer.token == Token.INT ||
            lexer.token == Token.BOOLEAN ||
            lexer.token == Token.STRING
        ){
            next();
        } else {
            this.error("Keywords \"Int\", \"Boolean\" or \"String\" expected");
        }
    }

	private Expr basicValue(){
	    LiteralInt i;
	    LiteralString s;

	    if (lexer.token == Token.LITERALINT){
	        i = new LiteralInt(lexer.getNumberValue());
	        next();
	        return i;
        }

		if (lexer.token == Token.TRUE || lexer.token == Token.FALSE){
		    if (lexer.token == Token.TRUE) {
                next();
                return LiteralBoolean.True;
            } else {
		        next();
		        return LiteralBoolean.False;
            }
        }

		if (lexer.token == Token.LITERALSTRING) {
		    s = new LiteralString(lexer.getLiteralStringValue());
			next();
			return s;
		}

        this.error("An integer, string or boolean value was expected");
		return null;
	}

	private void booleanValue(){
	    if (lexer.token == Token.TRUE || lexer.token == Token.FALSE){
	        next();
        } else {
	        error("True or False expected");
        }
    }

    private void CompStatement(){
	    if (lexer.token != Token.LEFTCURBRACKET){
	        error("LEft bracket expected");
        }
        next();
        statementList();
        if (lexer.token != Token.EOF){
            error("closing bracket expected");
        }
        next();
    }

    // Semântica feita
	private Type type() {
		Type t;
		String className;

		if (lexer.token == Token.INT){
			t = new TypeInt();
			next();
		} else if (lexer.token == Token.BOOLEAN) {
			t = new TypeBoolean();
			next();
		} else if (lexer.token == Token.STRING ) {
			t = new TypeString();
			next();
		} else if ( lexer.token == Token.ID ) {
			className = lexer.getStringValue();
			CianetoClass c = symbolTable.getInGlobal(className);
			if (c == null) {
				error("Variable of inexistent type/class");
			}

			t = c;
			next();
		}
		else {
			this.error("A type was expected");
		}

		return t;
	}


	// Semântico feito
	private Qualifiers qualifier() {
		Qualifiers q = new Qualifiers();

		if ( lexer.token == Token.PRIVATE ) {
			q.setPrivate();
			next();
		} else if ( lexer.token == Token.PUBLIC ) {
			next();
		} else if ( lexer.token == Token.OVERRIDE ) {
			q.setOverride();
			next();
			if ( lexer.token == Token.PUBLIC ) {
				next();
			}
		} else if ( lexer.token == Token.FINAL ) {
			q.setFinal();
			next();
			if ( lexer.token == Token.PUBLIC ) {
				next();
			} else if ( lexer.token == Token.OVERRIDE ) {
				q.setOverride();
				next();
				if ( lexer.token == Token.PUBLIC ) {
					next();
				}
			}
		} else {
			q.unchanged = true;
		}

		return q;
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

	private Token relation(){
		Token token = lexer.token;
		next();
		return token;
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
	private CianetoClass    currentClass;
	private Method          currentMethod;

}
