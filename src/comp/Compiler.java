
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
				program.addClass(classDec());
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
			ciaClass.setSuperClass(superClass);

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

	// Semântico feito
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
					if (! q.unchanged){
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
		return m;
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

    // Semântico feito
	private Method methodDec() {
		Method m;
		String methodName = null;

		lexer.nextToken();
		if (lexer.token == Token.ID) {
			// unary method
			methodName = lexer.getStringValue();
			m = new Method(methodName);
			currentMethod = m;
			lexer.nextToken();
		}
		else if ( lexer.token == Token.IDCOLON ) {
			// keyword method. It has parameters
			methodName = lexer.getStringValue();
			m = new Method(methodName);
			if (methodName.equals("run")){
				error("Method run doesn't accepts any parameters");
			}

            m.setParams(formalParamDec());
		}
		else {
			error("An identifier or identifer: was expected after 'func'");
			m = null;
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
		m.setStatementList(statementList());

		if ( lexer.token != Token.RIGHTCURBRACKET ) {
			error("'}' expected");
		}

		symbolTable.putInlocal(methodName, m);
		symbolTable.removeFuncTable();
		next();

		return m;
	}

    private FieldList formalParamDec() {
		FieldList paramList = new FieldList();

	    next();
	    paramList.addField(ParamDec());
	    while (lexer.token == Token.COMMA){
	        next(); // consome a virgula
			paramList.addField(ParamDec());
        }

	    return paramList;
    }

    // Semântico feito
    private Field ParamDec() {
		String fieldName;
		Field f;

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

		f = new Field(fieldName, t);
		symbolTable.putInFunc(fieldName, f);

		return f;
    }

    private StatementList statementList() {
		StatementList sl = new StatementList();
		  // only '}' is necessary in this test
		while ( lexer.token != Token.RIGHTCURBRACKET && lexer.token != Token.END ) {
			sl.addElement(statement());
		}

		return sl;
	}

	private Statement statement() {
		boolean checkSemiColon = true;
		Statement returnStatement;

		switch ( lexer.token ) {
		case IF:
			returnStatement = ifStat();
			checkSemiColon = false;
			break;
		case WHILE:
			returnStatement = whileStat();
			checkSemiColon = false;
			break;
		case RETURN:
			returnStatement = returnStat();
			break;
		case BREAK:
			returnStatement = breakStat();
			break;
		case SEMICOLON:
			return new NullStat();
			break;
		case REPEAT:
			returnStatement = repeatStat();
			break;
		case VAR:
			returnStatement = localDec();
			break;
		case ASSERT:
			returnStatement = assertStat();
			break;
		default:
			if ( lexer.token == Token.ID && lexer.getStringValue().equals("Out") ) {
				returnStatement = writeStat();
			}
			else {
				if ( lexer.token == Token.IDCOLON){
					if (lexer.getStringValue().equals("print:") ||
							lexer.getStringValue().equals("println:")){
						error("Missing 'Out.'");
					}
				}
				returnStatement = assignExpr();
			}

		}
		if ( checkSemiColon ) {
			check(Token.SEMICOLON, "';' expected", true);
			next();
		}
		return returnStatement;
	}

	// semantico feito
	private LocalDecStat localDec() {
		Type t;
		Field f;
		FieldList fieldList = new FieldList();
		String name;
		Expr expr;

		next();
		t = type();
		check(Token.ID, "Identifier expected");
		while ( lexer.token == Token.ID ) {
			name = lexer.getStringValue();
			if (symbolTable.getInFunc(name) != null){
				error("There's already a variable or parameter " + name);
			}
			f = new Field(name, t);
			symbolTable.putInFunc(name, f);
			fieldList.addField(f);

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
			if (fieldList.size() != 1) {
				error("Can only define the value if declaring a single variable");
			}
			expr = expression();
			if (expr.getType() != fieldList.getField(0).getType()) {
				error("Invalid type");
			}
		}

		return new LocalDecStat(fieldList);
	}

	private RepeatStat repeatStat() {
		StatementList sl = new StatementList();
		Expr repeatExpr;

		next();
		loopCounter++;
		while ( lexer.token != Token.UNTIL && lexer.token != Token.RIGHTCURBRACKET && lexer.token != Token.END ) {
			sl.addElement(statement());
		}
		loopCounter--;
		check(Token.UNTIL, "'until' was expected");
		next();
		repeatExpr = expression();

		if (repeatExpr.getType() != Type.booleanType) {
			error("Repeat statements expect boolean expressions");
		}

		return new RepeatStat(sl, repeatExpr);
	}

	private BreakStat breakStat() {
		if (loopCounter == 0) {
			error("Trying to invoke break outside of a loop");
		}
		next();
		return new BreakStat();
	}

	private ReturnStat returnStat() {
		next();
		ReturnStat rs = new ReturnStat(expression());

		if (rs.getType() != currentMethod.getType()) {
			error("Invalid return type.");
		}

		return rs;
	}

	private WhileStat whileStat() {
		WhileStat whileStat;
		Expr whileExpr;
		StatementList sl = new StatementList();
		Statement s;

		next();
		whileExpr = expression();
		if (whileExpr.getType() != Type.booleanType) {
			error("While statements expect boolean expressions");
		}
		whileStat = new WhileStat(whileExpr);

		check(Token.LEFTCURBRACKET, "'{' expected after the 'while' expression");
		next();
		loopCounter++;
		while ( lexer.token != Token.RIGHTCURBRACKET && lexer.token != Token.END ) {
			s = statement();
			sl.addElement(s);
		}
		whileStat.setStatementList(sl);
		loopCounter--;
		check(Token.RIGHTCURBRACKET, "'}' was expected");
		next();

		return whileStat;
	}

	// Semantico feito
	private IfStat ifStat() {
		IfStat ifStat;
		Expr ifExpr;
		StatementList ifList = new StatementList();
		StatementList elseList = new StatementList();

		next();
		ifExpr = expression();
		if (ifExpr.getType() != Type.booleanType) {
			error("If statements expect boolean expressions");
		}

		ifStat = new IfStat(ifExpr);

		check(Token.LEFTCURBRACKET, "'{' expected after the 'if' expression");
		next();
		while ( lexer.token != Token.RIGHTCURBRACKET && lexer.token != Token.END && lexer.token != Token.ELSE ) {
			ifList.addElement(statement());
		}
		ifStat.setIfList(ifList);

		check(Token.RIGHTCURBRACKET, "'}' was expected");
		next();

		if ( lexer.token == Token.ELSE ) {
			next();
			check(Token.LEFTCURBRACKET, "'{' expected after 'else'");
			next();
			while ( lexer.token != Token.RIGHTCURBRACKET ) {
				elseList.addElement(statement());
			}
			ifStat.setElseList(elseList);
			check(Token.RIGHTCURBRACKET, "'}' was expected");
			next();
		}

		return ifStat;
	}

	private void intValue() {
	    next();
	    check(Token.LITERALINT, "Número inteiro esperado");
    }

	/**
		Semantico feito
	 */
	private WriteStat writeStat() {
		Expr expr;
		boolean ln = false;

		next();
		check(Token.DOT, "a '.' was expected after 'Out'");
		next();
		check(Token.IDCOLON, "'print:' or 'println:' was expected after 'Out.'");

		String printName = lexer.getStringValue();
		if (printName.equals("print")) {
			ln = false;
		} else if (printName.equals("println")) {
			ln = true;
		} else {
			error("'print:' or 'println:' was expected after 'Out.'");
		}

		next();

		expr = expression();
		if (! expr.getType().isCompatible(Type.stringType)) {
			error("Write expression must be of type string");
		}

		return new WriteStat(expr, ln);
	}

	//	Semântico feito
	private AssignStat assignExpr(){
		Expr left;
		boolean hasAssign = false;
		Expr right = null;

		left = expression();
        if (lexer.token == Token.ASSIGN){
        	hasAssign = true;
            next();
            right = expression();
        }

        if (! right.getType().isCompatible(left.getType())) {
			error("Invalid types");
		}

        return new AssignStat(left, hasAssign, right);
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

			if (lexer.token == Token.DOT) {
				next();

				switch (lexer.token) {
					case ID:
						Field f = currentMethod.getParam(name);
						if (f == null) {
							f = symbolTable.getInFunc(name);
							if (f == null)
								error("Parameter " + name + "does not exist");
						}

						if (! f.getType().getName().equals("CiaClass")) {
							error("Trying invoke a method or variable of something that is not an object");
						}

						name = lexer.getStringValue();
						if (symbolTable.getInGlobal())

						break;
					case IDCOLON:
						break;
					case NEW:
						break;
					default:
						error("Variable name, method name or new expected");
						break;
				}
			}




			    if (param != null){
                    if (lexer.token == Token.ID){
                        if (! param.getType().getName().equals("CiaClass")){
                            error("");
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

	// Semântica feita
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
			t = null;
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

	// Semantico feito
	private AssertStat assertStat() {
		Expr expr;
		LiteralString string;
		int lineNumber;

		lineNumber = lexer.getLineNumber();
		lexer.nextToken();
		expr = expression();
		if (! expr.getType().isCompatible(Type.stringType)) {
			error("Assert expression must be a String");
		}

		if ( lexer.token != Token.COMMA ) {
			this.error("',' expected after the expression of the 'assert' statement");
		}
		lexer.nextToken();
		if ( lexer.token != Token.LITERALSTRING ) {
			this.error("A literal string expected after the ',' of the 'assert' statement");
		}

		string = new LiteralString(lexer.getLiteralStringValue());
		lexer.nextToken();

		return new AssertStat(expr, string, lineNumber);
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
	private int 			loopCounter = 0;

}
