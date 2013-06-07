/**************************************************************************************************
 *   JAVALI TREE GRAMMAR
 *   Compiler Construction I
 *
 *   ABOUT:
 *   -------
 *   
 *   A tree grammar takes as input the ANTLR AST generated by the rewriting rules of the parser grammar
 *   and generates a visitor that visits the ANTLR AST and issues the action code associated with the tree
 *   grammar rule. We use the tree grammar for generating the Javali AST (i.e., AST whose nodes are
 *   instances of cd.ir.Ast.java). Thus, we invoke the appropriate Javali AST constructors in the action
 *   code associated with the tree grammar rules.
 *
 *   The rewriting rules of the parser grammar are a good starting point for generating the tree grammar.
 *   The easiest way to generate a tree grammar is to start with the parser grammar (copy & paste) and to
 *   remove the grammar productions (left side of arrow), leaving only the rewriting rules (right side of arrow).
 *   Note, however, that the rewriting rules may have to be changed. For instance, imaginary tokens need no
 *   longer to be created, but can simply be referenced (i.e., brackets with token binding must be omitted).
 *   Also, variables must be replaced with the actual node that is visited. Most likely, the tree grammar
 *   will also become more condensed than the parser grammar since it can expect the parser grammar
 *   to pass on valid ANTLR ASTs only. For instance, the tree grammar does not need to encode
 *   operator precedences.
 *
**************************************************************************************************/

tree grammar JavaliWalker; // tree grammar, parses streams of nodes

// (1) OPTIONS
options {
	tokenVocab=Javali; // uses tokens defined in Javali.g
	ASTLabelType=CommonTree;
}

// (2) ACTIONS
@header {
package cd.parser; // include any import statements that are required by your action code

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

import cd.ir.ast.*;
import cd.ir.ast.UnaryOp.UOp;
import cd.ir.ast.BinaryOp.BOp;
import cd.ir.symbols.BottomTypeSymbol;
import cd.util.Pair;
import cd.exceptions.ParseFailure;
}

@members { // incude any members that are required by your action code

	public <T> List<T> emptyList() {
		return java.util.Collections.emptyList();
	}
    
    @SuppressWarnings({"unchecked", "varargs"})
	public Seq seq(List<? extends Ast>... members) {
		Seq res = new Seq(null);
		if (members != null) {
			for (List<? extends Ast> lst : members)
				res.rwChildren().addAll(lst);
		}
		return res;
	}

	public Integer intValue(String s, int radix, int line) {
		try {
			return Integer.valueOf(s, radix);
		} catch (NumberFormatException e) {
			throw new ParseFailure(line, "Illegal integer: " + s);
		}
	}
	
	public Float floatValue(String s, int line) {
		try {
			return Float.valueOf(s);
		} catch (NumberFormatException e) {
			throw new ParseFailure(line, "Illegal float: " + s);
		}
	}
	
}

// (3) TREE GRAMMAR RULES
unit returns [List<ClassDecl> classDeclList]
@init {
	classDeclList = new ArrayList<ClassDecl>();
}
	:	( cl=classDecl { classDeclList.add($cl.node); } )+
	;
	
classDecl returns [ClassDecl node]
@init{
	ArrayList<Decl> memberDeclList = new ArrayList<Decl>();
}
	:	^( ClassDecl clName=Identifier superName=Identifier body=cDeclList[memberDeclList]? )
		{ $node = new ClassDecl($clName.text, $superName.text, memberDeclList); }
	;

cDeclList[List<Decl> members]
	:	( v=varDecl[$members] | m=methodDecl { $members.add($m.mth); } )+
	;
	
mDeclList[List<Decl> members]
	:	( v=varDecl[$members] )+
	;

varDecl[List<Decl> members]
	:	^( VarDecl t=type n=Identifier )
		{ $members.add(new VarDecl($t.typeName, $n.text)); }
	|	^( VarDeclList t=type ( n=Identifier { $members.add(new VarDecl($t.typeName, $n.text)); } )+ )
	;

methodDecl returns [MethodDecl mth]
   :	^( MethodDecl head=methodHeading body=methodBody )
      { $mth = new MethodDecl($head.returnType, $head.mthName, $head.formalParams, $body.decls, $body.stmts); }
   ;

methodHeading returns [String returnType, String mthName, List<Pair<String>> formalParams]
   :	r=type? n=Identifier { $returnType = ($r.typeName == null ? BottomTypeSymbol.INSTANCE.getName() : $r.typeName) ; $mthName = $n.text; $formalParams = emptyList(); }
   |	r=type? n=Identifier { $returnType = ($r.typeName == null ? BottomTypeSymbol.INSTANCE.getName() : $r.typeName) ; $mthName = $n.text; $formalParams = new ArrayList<Pair<String>>(); } formalParamList[$formalParams]
   ;

formalParam[List<Pair<String>> formalParams]
	:  ^( ParamType t=type n=Identifier ) { $formalParams.add(new Pair<String>($t.typeName, $n.text)); }
	|  ^( ParamNoType n=Identifier ) { $formalParams.add(new Pair<String>(BottomTypeSymbol.INSTANCE.getName(), $n.text)); }
	;

formalParamList[List<Pair<String>> formalParams]
	:	(p=formalParam[$formalParams])+
	;

methodBody returns [Seq decls, Seq stmts]
@init{
	$decls = seq(); $stmts = seq();
}
	:	^( MethodBody declL=methodBodyWithDeclList { $decls = seq($declL.vars); } ^( Seq (stmtL=stmtList { $stmts = seq($stmtL.stmtList); } )? ) )
	;

methodBodyWithDeclList returns [List<Decl> vars]
@init{
	$vars = new ArrayList<Decl>();
}
	:	^( Seq mDeclList[$vars]? )
	;

// STATEMENTS

stmtList returns [List<Stmt> stmtList]
@init {
	stmtList = new ArrayList<Stmt>();
}
	:	( s=stmt { stmtList.add($s.stmt); } )+
	;

stmt returns [Stmt stmt]
	:	a=assignmentOrMethodCall
		{ $stmt = $a.assigOrMthCall; }
	|	^( BuiltInWrite e=expr )
		{ $stmt = new BuiltInWrite($e.expr); }
   |	^( BuiltInWriteFloat e=expr )
      { $stmt = new BuiltInWriteFloat($e.expr); }	
	|	BuiltInWriteln
		{ $stmt = new BuiltInWriteln(); }
	|	BuiltInTick
		{ $stmt = new BuiltInTick(); }
	|	BuiltInTock
		{ $stmt = new BuiltInTock(); }
	|	^( IfElse cond=expr thenB=stmtBlock elseB=ifStmtTail )
		{ $stmt = new IfElse($cond.expr, $thenB.stmtSeq, $elseB.elseBlock); }
	|	^( WhileLoop cond=expr body=stmtBlock )
		{ $stmt = new WhileLoop($cond.expr, $body.stmtSeq); }
   |	^( ReturnStmt e=expr? )
      { $stmt = new ReturnStmt($e.expr); }	
	;

assignmentOrMethodCall returns [Stmt assigOrMthCall]
	:	a=assignmentTail
		{ $assigOrMthCall = $a.assign; }
	|	i=identAccess
		{ 
		   
		   if ($i.expr instanceof MethodCallExpr) {
		      $assigOrMthCall = new MethodCall((MethodCallExpr) $i.expr);    
		   } else {
		      throw new ParseFailure(-1, "Illegal expression as statement: " + $i.expr);
		   }
		   
		}
   ;
      
assignmentRHS returns [Expr expr]
	:	e=expr
		{ $expr = $e.expr; }
	|	n=newExpr
		{ $expr = $n.expr; }
	|	r=readExpr
		{ $expr = $r.readExpr; }
	|	f=readExprFloat
      { $expr = $f.readExprFloat; }	
	;
	
assignmentTail returns [Assign assign]
	:	^( Assign left=identAccess right=assignmentRHS )
		{ $assign = new Assign($left.expr, $right.expr); }
	;

actualParamList returns [List<Expr> paramList]
@init {
	paramList = new ArrayList<Expr>();
}
	:	( e=expr { paramList.add($e.expr); } )+
	;

ifStmtTail returns [Ast elseBlock]
	:	Nop
		{ $elseBlock = new Nop(); }
	|	block=stmtBlock
		{ $elseBlock = $block.stmtSeq; }
	;

stmtBlock returns [Seq stmtSeq]
	:	Seq
		{ $stmtSeq = seq(); }
	|	^( Seq sl=stmtList )
		{ $stmtSeq = seq($sl.stmtList); }
	;
	

// EXPRESSIONS

newExpr returns [Expr expr]
	:	^( NewObject id=Identifier )
		{ $expr = new NewObject($id.text); }
	|	^( NewArray id=Identifier e=expr )
		{ $expr = new NewArray($id.text, $e.expr); }
	;

readExpr returns [BuiltInRead readExpr]
	:	BuiltInRead
		{ $readExpr = new BuiltInRead(); }
	;

readExprFloat returns [BuiltInReadFloat readExprFloat]
   :	BuiltInReadFloat
      { $readExprFloat = new BuiltInReadFloat(); }
   ;

expr returns [Expr expr]
	:	^( BinaryOp left=expr op=binaryOp right=expr )
		{ $expr = new BinaryOp($left.expr, $op.op, $right.expr); }
	|	^( UnaryOp U_PLUS e=expr )
		{ $expr = new UnaryOp(UOp.U_PLUS, $e.expr); }
	|	^( UnaryOp U_MINUS e=expr )
		{ $expr = new UnaryOp(UOp.U_MINUS, $e.expr); }
	|	^( UnaryOp U_BOOL_NOT e=expr )
		{ $expr = new UnaryOp(UOp.U_BOOL_NOT, $e.expr); }
	|	n=DecimalIntConst
		{ $expr = new IntConst(intValue($n.text, 10, $n.line)); }
	|	n=HexIntConst
		{ $expr = new IntConst(intValue($n.text.substring(2), 16, $n.line)); }
	|	n=FloatConst
      { $expr = new FloatConst(floatValue($n.text, $n.line)); }
	|	b=BooleanConst
		{ $expr = new BooleanConst(Boolean.valueOf($b.text)); }
	|	NullConst
		{ $expr = new NullConst(); }
	|	^( Cast e=expr t=type )
		{ $expr = new Cast($e.expr, $t.typeName); }
	|	idA=identAccess
		{ $expr = $idA.expr; }
	;

binaryOp returns [BOp op]
	:	B_EQUAL
		{ $op = BOp.B_EQUAL; }
	|	B_NOT_EQUAL
		{ $op = BOp.B_NOT_EQUAL; }
	|	B_LESS_THAN
		{ $op = BOp.B_LESS_THAN; }
	|	B_LESS_OR_EQUAL
		{ $op = BOp.B_LESS_OR_EQUAL; }
	|	B_GREATER_THAN
		{ $op = BOp.B_GREATER_THAN; }
	|	B_GREATER_OR_EQUAL
		{ $op = BOp.B_GREATER_OR_EQUAL; }
	|	B_PLUS
		{ $op = BOp.B_PLUS; }
	|	B_MINUS
		{ $op = BOp.B_MINUS; }
	|	B_OR
		{ $op = BOp.B_OR; }
	|	B_TIMES
		{ $op = BOp.B_TIMES; }
	|	B_DIV
		{ $op = BOp.B_DIV; }
	|	B_MOD
		{ $op = BOp.B_MOD; }
	|	B_AND
		{ $op = BOp.B_AND; }
	;

identAccess returns [Expr expr]
	:	^( Var id=Identifier )
		{ $expr = new Var($id.text); }
	|	ThisRef
		{ $expr = new ThisRef(); }
	|	^( Field t=identAccess n=Identifier )
		{ $expr = new Field($t.expr, $n.text); }
	|	^( Index t=identAccess e=expr )
		{ $expr = new Index($t.expr, $e.expr); }
   |	^( MethodCall i=identAccess? name=Identifier aP=actualParamList? )
      { $expr = new MethodCallExpr($i.expr, $name.text, ($aP.paramList != null ? $aP.paramList : new ArrayList<Expr>())); }
   ;
   	
// TYPES

type returns [String typeName]
	:	id=Identifier
		{ $typeName = $id.text; }
	|	^( ArrayType id=Identifier )
		{ $typeName = $id.text + "[]"; }
	;	
