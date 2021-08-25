package de.yard.threed.java2cs;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Very basic Java to CS syntax converter.
 * Based on Pat Niemeyers Java to Swift converter (https://github.com/patniemeyer/j2swift)
 *
 */
public class J2CSListener extends Java8BaseListener {
    Logger logger = Logger.getLogger(J2CSListener.class);
    CommonTokenStream tokens;
    TokenStreamRewriter rewriter;
    boolean cs = true;
    boolean nasal = false;
    boolean swift = false;
    boolean inConstructor;
    int formalParameterPosition;
    boolean foundextends = false;
    boolean isextending = false;
    boolean isfunctionalinterface = false;
    String functionalinterfacename;
    // Liste der von der aktuellen Klasse implementierten Interfaces
    public List<String> implementing = new ArrayList<String>();

    // Some basic type mappings
    Map<String, String> typeMap = new HashMap<String, String>();
    // Das mit den Methoden kann natürlich heikel sein. Das sollte möglichst vermieden werden.
    Map<String, String> methodMap = new HashMap<String, String>();
    // Some basic modifier mappings (others in context)
    Map<String, String> modifierMap = new HashMap<String, String>();
    boolean foundthis = false;
    boolean foundsuper = false;
    public List<String> errors = new ArrayList<String>();
    private List<String> usednamespaces = new ArrayList<String>();
    public List<String> reservedwords = new ArrayList<String>();
    private List<String> nasalfieldeclaration = new ArrayList<String>();
    private KnownInterfaces knowninterfaces;
    private KnownGenericMethods knwonGenericMethods;
    private int classindex = 0;

    public J2CSListener(CommonTokenStream tokens, KnownInterfaces knownInterfaces, KnownGenericMethods knownGenericMethods, boolean nasal) {
        if (nasal) {
            cs = false;
            this.nasal = true;
        }
        if (cs) {
            //typeMap.put("float", "float");
            // Ja, Single ist ein float in CS. 20.7.16: Aber auch als Nullable
            typeMap.put("Float", "Nullable<Single>");
            // System.Double um Konflikte mit nachgebautem zu vermeiden.
            // Der nachgebaute ist ja eh nur für ein paar statics. 23.11.18: Jetzt nullable.
            typeMap.put("Double", "Nullable<System.Double>");
            //Int32 gibts aber auch, ist aber nicht nullable
            typeMap.put("Integer", "Nullable<Int32>"/*"Int32"*/);
            typeMap.put("Long", "Nullable<Int64>"/*"In64"*/);
            typeMap.put("boolean", "bool");
            typeMap.put("Boolean", "Nullable<Boolean>");
            typeMap.put("String", "string");
            typeMap.put("Character", "Char");
            typeMap.put("Class", "System.Type");
            //typeMap.put("Map", "Dictionary");
            //typeMap.put("HashSet", "Set");
            //typeMap.put("HashMap", "Dictionary");
            //typeMap.put("List", "List");
            //typeMap.put("ArrayList", "ArrayList");
            //modofiermap ist nur für Klassen und MEthoden
            modifierMap.put("final", "");
            //modifierMap.put("volatile", "/*volatile*/");
            reservedwords.add("ref");
            reservedwords.add("object");
            reservedwords.add("params");
            reservedwords.add("base");
            //out ist zwar reserverd, wird aber nicht getestet, weil dann auch immer System.out gemeldet wird, was den Test stoert.
            //reservedwords.add("out");
            reservedwords.add("in");
            reservedwords.add("is");
            reservedwords.add("as");
            reservedwords.add("string");
            reservedwords.add("event");
            reservedwords.add("delegate");
            reservedwords.add("lock");
            methodMap.put("replace", "Replace");
            //6.3.17 jetzt in StringUtils methodMap.put("startsWith", "StartsWith");
            methodMap.put("compareTo", "CompareTo");
            // split koennte anders arbeiten
            //methodMap.put("split", "Split");
            //methodMap.put("substring", "Substring");
            // methodMap.put("trim", "Trim");
            methodMap.put("equals", "Equals");
            methodMap.put("abs", "Abs");
            methodMap.put("cos", "Cos");
            methodMap.put("sin", "Sin");
            methodMap.put("tan", "Tan");
            methodMap.put("acos", "Acos");
            methodMap.put("asin", "Asin");
            methodMap.put("atan", "Atan");
            methodMap.put("atan2", "Atan2");
            methodMap.put("sqrt", "Sqrt");
            methodMap.put("round", "Round");
            methodMap.put("pow", "Pow");
            methodMap.put("min", "Min");
            methodMap.put("max", "Max");
            methodMap.put("floor", "Floor");
            methodMap.put("toString", "ToString");
            methodMap.put("printStackTrace", "ToString");

            // Das waere wuest, weil Length eine Property ist. Das mit length ist
            // ein Unding. Vector3 und viele andere haben auch so eine Methode.
            // Fuer Strings muss StringUtils.length verwendet werden.
            //methodMap.put("length", "Length+new int");
            //jetzt eigene Klasse,ging eh noch nicht methodMap.put("Float.parseFloat","float.parse");
            //methodMap.put("charAt", "gehtnuuebernicht");

        }
        if (swift) {
            typeMap.put("float", "Float");
            typeMap.put("Float", "Float");
            typeMap.put("int", "Int");
            typeMap.put("Integer", "Int");
            typeMap.put("long", "Int64");
            typeMap.put("Long", "Int64");
            typeMap.put("boolean", "Bool");
            typeMap.put("Boolean", "Bool");
            typeMap.put("Map", "Dictionary");
            typeMap.put("HashSet", "Set");
            typeMap.put("HashMap", "Dictionary");
            typeMap.put("List", "Array");
            typeMap.put("ArrayList", "Array");
            modifierMap.put("protected", "internal");
            modifierMap.put("volatile", "/*volatile*/");
        }
        if (nasal) {
            typeMap.put("ArrayList", "List");
        }
        this.tokens = tokens;
        this.rewriter = new TokenStreamRewriter(tokens);
        this.knowninterfaces = knownInterfaces;
        this.knwonGenericMethods = knownGenericMethods;
    }

    Java8Parser.UnannTypeContext unannType;

    @Override
    public void exitTypeDeclaration(Java8Parser.TypeDeclarationContext ctx) {
    }

    @Override
    public void enterFieldDeclaration(Java8Parser.FieldDeclarationContext ctx) {
        //:	fieldModifier* unannType variableDeclaratorList ';'
        // Store the unannType for the variableDeclarator (convenience)
        unannType = ctx.unannType();
    }

    @Override
    public void exitFieldDeclaration(Java8Parser.FieldDeclarationContext ctx) {
        // replace on exit because the unannType rules will rewrite it
        //dumpContext("exitFieldDeclaration", ctx);
        handleFieldDeclaration(ctx);
        unannType = null;
    }

    private void handleFieldDeclaration(Java8Parser.FieldDeclarationContext ctx) {
        //dumpFieldDeclaration(ctx);
        String fieldname = "";//getFieldname(ctx);
        boolean isstatic = hasModifier(ctx, "static");
        boolean isprivate = hasModifier(ctx, "private");
        boolean isprotected = hasModifier(ctx, "protected");
        boolean ispublic = hasModifier(ctx, "public");
        boolean isfinal = hasModifier(ctx, "final");
        //System.out.println("fieldname=" + fieldname + ",isfinal=" + isfinal + ", isprotected=" + isprotected + ", static=" + isstatic + ", private=" + isprivate);

        if (cs) {
            if (/*isstatic &&*/ isfinal) {
                // wichtig fuer switch. Kann man eigentlich bei allen finals machen. static darf es bei const aber nicht geben.
                replaceModifier(ctx, "static", "");
                replaceModifier(ctx, "final", "const");
            }
            // Wie Methoden mit default access level bekommt public. C# hat strengere Defaults
            if (!isprivate && !ispublic && !isprotected) {
                rewriter.insertBefore(ctx.start, "public ");
            }
        }
        if (swift) {
            // das stimmt nicht mehr
            replace(ctx.unannType(), "var");
        }
        if (nasal) {
            replace(ctx.unannType(), "var");
            String nasaldeclaration = ctx.getText();
            nasalfieldeclaration.add(nasaldeclaration);
            replace(ctx, "");
        }
    }

    @Override
    public void enterLocalVariableDeclaration(Java8Parser.LocalVariableDeclarationContext ctx) {
        //:	variableModifier* unannType variableDeclaratorList
        unannType = ctx.unannType();
    }

    @Override
    public void exitLocalVariableDeclaration(Java8Parser.LocalVariableDeclarationContext ctx) {
        if (!cs) {
            replace(ctx.unannType(), "var");
        }
        unannType = null;
    }

    @Override
    public void enterConstantDeclaration(Java8Parser.ConstantDeclarationContext ctx) {
        //:	constantModifier* unannType variableDeclaratorList ';'
        unannType = ctx.unannType();
    }

    @Override
    public void exitConstantDeclaration(Java8Parser.ConstantDeclarationContext ctx) {
        if (swift || nasal) {
            replace(ctx.unannType(), "var");
        }
        unannType = null;
    }

    @Override
    public void exitVariableDeclarator(Java8Parser.VariableDeclaratorContext ctx) {
        //:	variableDeclaratorId ('=' variableInitializer)?
        if (swift) {
            // We could search the parent contexts for unannType but since we have to remove it anyway we store it.
            // Use the rewritten text, not the original.
            // todo: not sure what's up here, crashing on lambdas
            try {
                rewriter.insertAfter(ctx.variableDeclaratorId().stop, " : " + getText(unannType));
            } catch (Exception e) {
                // do nothing
            }
        }
    }

    @Override
    public void enterConstructorDeclaration(Java8Parser.ConstructorDeclarationContext ctx) {
        //:	constructorModifier* constructorDeclarator throws_? constructorBody
        // Search children of constructorBody for any explicit constructor invocations
        List<Java8Parser.ExplicitConstructorInvocationContext> eci =
                ctx.constructorBody().getRuleContexts(Java8Parser.ExplicitConstructorInvocationContext.class);
        if (swift) {
            if (!eci.isEmpty()) {
                rewriter.insertBefore(ctx.constructorDeclarator().start, "convenience ");
            }
        }
    }

    @Override
    public void enterConstructorDeclarator(Java8Parser.ConstructorDeclaratorContext ctx) {
        //:	typeParameters? simpleTypeName '(' formalParameterList? ')'
        if (swift) {
            replace(ctx.simpleTypeName(), "init");
        }
        inConstructor = true;
    }

    @Override
    public void exitConstructorDeclaration(Java8Parser.ConstructorDeclarationContext ctx) {
        inConstructor = false;
        //dumpContext("exitConstructorDeclaration", ctx);
        if (cs) {
            if (foundthis) {
                if (!replaceFirst(ctx.constructorBody(), Java8Lexer.LBRACE, ":")) {
                    throw new RuntimeException("no first { found");
                }
                if (!replaceFirst(ctx.constructorBody().explicitConstructorInvocation(), Java8Lexer.SEMI, " {")) {
                    throw new RuntimeException("no first ; found");
                }
            }
            foundthis = false;
            if (foundsuper) {
                if (!replaceFirst(ctx.constructorBody().explicitConstructorInvocation(), Java8Lexer.SUPER, "base")) {
                    throw new RuntimeException("no first super found");
                }
                if (!replaceFirst(ctx.constructorBody(), Java8Lexer.LBRACE, ":")) {
                    throw new RuntimeException("no first { found");
                }
                if (!replaceFirst(ctx.constructorBody().explicitConstructorInvocation(), Java8Lexer.SEMI, " {")) {
                    throw new RuntimeException("no first ; found");
                }
            }
            foundsuper = false;
            if (!hasModifier(ctx, "public") && !hasModifier(ctx, "private") && !hasModifier(ctx, "protected")) {
                // alle nicht private oder protected constructor public machen, weil CS keine Package default rights hat
                rewriter.insertBefore(ctx.getStart(), " public ");
            }
        }
        if (nasal) {
            if (foundthis || foundsuper) {
                throw new RuntimeException("unexpected this or super");
            }
            // class name -> 'new: func'
            rewriter.replace(ctx.getStart(), "new: func");
            for (String s : nasalfieldeclaration) {
                rewriter.insertAfter(ctx.getStop(), s + "\n");
            }
            nasalfieldeclaration.clear();
            foundthis = false;
            foundsuper = false;
        }
    }

    @Override
    public void enterFormalParameterList(Java8Parser.FormalParameterListContext ctx) {
        // called from methodDeclarator
        //:	formalParameters ',' lastFormalParameter
        //    |	lastFormalParameter
        formalParameterPosition = 0;
    }

    @Override
    public void enterFormalParameters(Java8Parser.FormalParametersContext ctx) {
        // called from formalParameterList
        //:	formalParameter (',' formalParameter)*
        //    |	receiverParameter (',' formalParameter)*
        formalParameterPosition = 0;
    }

    @Override
    public void exitFormalParameter(Java8Parser.FormalParameterContext ctx) {
        //dumpContext("exitFormalParameter", ctx);
        if (nasal) {
            replace(ctx.unannType(), "");
        }

        if (swift) {
            rewriter.insertAfter(ctx.variableDeclaratorId().stop, " : " + getText(ctx.unannType()));
        }

        //:	variableModifier* unannType variableDeclaratorId
        if (swift) {
            if (formalParameterPosition++ > 0 || inConstructor) {
                replace(ctx.unannType(), "_");
            } else {
                removeRight(ctx.unannType());
            }
        }
    }

    @Override
    public void exitMethodHeader(Java8Parser.MethodHeaderContext ctx) {
        //:	result methodDeclarator throws_?
        //|	typeParameters annotation* result methodDeclarator throws_?
        //dumpContext("exitMethodHeader", ctx);

        if (swift) {
            if (!ctx.result().getText().equals("void")) {
                rewriter.insertAfter(ctx.methodDeclarator().stop, " -> " + getText(ctx.result()));
            }
            replace(ctx.result(), "func");
        }

    }

    @Override
    public void exitThrows_(Java8Parser.Throws_Context ctx) {
        // es gibt kein throws in C#
        //dumpContext("exitThrows_", ctx);

        if (cs) {
            replace(ctx, "");
        }
    }

    @Override
    public void enterPackageDeclaration(Java8Parser.PackageDeclarationContext ctx) {
        //dumpContext("package", ctx);
        if (cs) {
            // In Systems ist z.B. Exception definiert. Darum den immer einbinden. Und den mit den Javaklassen auch.
            usednamespaces.add("System");
            usednamespaces.add("java.lang");
            replaceFirst(ctx, Java8Lexer.PACKAGE, "using System;\nusing java.lang;\nnamespace");
            replaceLast(ctx, Java8Lexer.SEMI, " {");
        }
        if (swift) {
            rewriter.insertBefore(ctx.start, "// ");
        }
        if (nasal) {
            replace(ctx, "");
        }
    }

    @Override
    public void enterPrimaryNoNewArray_lfno_primary(Java8Parser.PrimaryNoNewArray_lfno_primaryContext ctx) {
        if (ctx.getText().equals("this")) {
            if (cs) {
                //dumpContext("this",ctx);

            } else {
                replace(ctx, "self");
            }
        }
    }

    @Override
    public void enterFieldModifier(Java8Parser.FieldModifierContext ctx) {
        // changed in 1.2
        //if ( ctx.getText().equals( "static" )) { replace( ctx, "class" ); }
       /*ist in exit if (cs) {
            // final gibt es in CS so nicht, aber es ist verzichtbar.Nee, wegen case nicht verzichtbar 
            if (ctx.getText().equals("final")) {
                replace(ctx, "");
            }
        }*/
    }

    /*@Override
    public void exitFieldModifier(Java8Parser.FieldModifierContext ctx) {
        / *zu simple. jetzt in exitfielddeclaration dumpContext("exitFieldModifier",ctx);
        replaceElementModifier(ctx.);
        //replace(ctx, mapModifier(ctx));
    }*/

    @Override
    public void enterMethodModifier(Java8Parser.MethodModifierContext ctx) {
        if (swift) {
            if (ctx.getText().equals("static")) {
                replace(ctx, "class");
            }
        }
    }

    @Override
    public void enterLiteral(Java8Parser.LiteralContext ctx) {
        //IntegerLiteral
        //        |	FloatingPointLiteral
        //        |	BooleanLiteral
        //        |	CharacterLiteral
        //        |	StringLiteral
        //        |	NullLiteral
        if (swift || nasal) {
            if (ctx.getText().equals("null")) {
                replace(ctx, "nil");
            } else if (ctx.FloatingPointLiteral() != null) {
                String text = ctx.getText();
                if (text.toLowerCase().endsWith("f")) {
                    text = text.substring(0, text.length() - 1);
                    replace(ctx, text);
                }
            }
        }
    }


    @Override
    public void exitClassInstanceCreationExpression(Java8Parser.ClassInstanceCreationExpressionContext ctx) {
        //:	'new' typeArguments? annotation* Identifier ('.' annotation* Identifier)* typeArgumentsOrDiamond? '(' argumentList? ')' classBody?
        //|	expressionName '.' 'new' typeArguments? annotation* Identifier typeArgumentsOrDiamond? '(' argumentList? ')' classBody?
        //|	primary '.' 'new' typeArguments? annotation* Identifier typeArgumentsOrDiamond? '(' argumentList? ')' classBody?
        //dumpContext("exitClassInstanceCreationExpression", ctx);
        if (swift) {
            if (ctx.start.getText().equals("new")) {
                replaceFirst(ctx, Java8Lexer.Identifier, mapType(ctx.Identifier().get(0).getText()));
                rewriter.delete(ctx.start);
                rewriter.delete(ctx.start.getTokenIndex() + 1); // space
            }
        }
        if (nasal) {
            if (ctx.start.getText().equals("new")) {
                replaceFirst(ctx, Java8Lexer.Identifier, mapType(ctx.Identifier().get(0).getText()) + ".new");
                replace(ctx.typeArguments(), "");
                replace(ctx.typeArgumentsOrDiamond(), "");
                rewriter.delete(ctx.start);
                rewriter.delete(ctx.start.getTokenIndex() + 1); // space
            }
        }
    }

    @Override
    public void enterClassInstanceCreationExpression_lfno_primary(Java8Parser.ClassInstanceCreationExpression_lfno_primaryContext ctx) {
        //:	'new' typeArguments? annotation* Identifier ('.' annotation* Identifier)* typeArgumentsOrDiamond? '(' argumentList? ')' classBody?
        //|	expressionName '.' 'new' typeArguments? annotation* Identifier typeArgumentsOrDiamond? '(' argumentList? ')' classBody?
        //dumpContext("enterClassInstanceCreationExpression_lfno_primary", ctx);
        if (cs) {
            // z.B. fuer die Wrapper um primitives
            replaceFirst(ctx, Java8Lexer.Identifier, mapType(ctx.Identifier().get(0).getText()));
        }
        if (nasal) {
            if (ctx.start.getText().equals("new")) {
                replaceFirst(ctx, Java8Lexer.Identifier, mapType(ctx.Identifier().get(0).getText()) + ".new");
                replace(ctx.typeArguments(), "");
                replace(ctx.typeArgumentsOrDiamond(), "");
                rewriter.delete(ctx.start);
                rewriter.delete(ctx.start.getTokenIndex() + 1); // space
            }
        }
        if (swift) {
            if (ctx.start.getText().equals("new")) {
                replaceFirst(ctx, Java8Lexer.Identifier, mapType(ctx.Identifier().get(0).getText()));
                rewriter.delete(ctx.start);
                rewriter.delete(ctx.start.getTokenIndex() + 1); // space
            }
        }
    }

    @Override
    public void enterThrowStatement(Java8Parser.ThrowStatementContext ctx) {
        //:	'throw' expression ';'
        if (!cs) {
            rewriter.insertBefore(ctx.start, "throwException() /* ");
            rewriter.insertAfter(ctx.stop, " */");
        }
    }

    @Override
    public void enterCastExpression(Java8Parser.CastExpressionContext ctx) {
        //:	'(' primitiveType ')' unaryExpression
        //    |	'(' referenceType additionalBound* ')' unaryExpressionNotPlusMinus
        //    |	'(' referenceType additionalBound* ')' lambdaExpression
        if (ctx.primitiveType() != null) {
            replace(ctx.primitiveType(), mapType(ctx.primitiveType()));
        }
    }

    @Override
    public void exitUnannType(Java8Parser.UnannTypeContext ctx) {
        // mapping may already have been done by more specific rule but this shouldn't hurt it
        // todo: this needs to be more specific, preventing rewrites on generic type args
        //if ( !ctx.getText().contains( "<" ) && !ctx.getText().contains( "[" )) {
        if (cs || swift) {
            replace(ctx, mapType(getText(ctx)));
        }
        //}
    }

    @Override
    public void exitArrayType(Java8Parser.ArrayTypeContext ctx) {
        //:	primitiveType dims
        //|	classOrInterfaceType dims
        //|	typeVariable dims
        //dumpContext("exitArrayType", ctx);
        if (cs) {
            ParserRuleContext rule;
            if (ctx.primitiveType() != null) {
                rule = ctx.primitiveType();
            } else if (ctx.classOrInterfaceType() != null) {
                rule = ctx.classOrInterfaceType();
            } else {
                rule = ctx.typeVariable();
            }
            replace(ctx, mapType(rule) + getArrayDimension(ctx.dims()));
        }
        if (swift) {
            ParserRuleContext rule;
            if (ctx.primitiveType() != null) {
                rule = ctx.primitiveType();
            } else if (ctx.classOrInterfaceType() != null) {
                rule = ctx.classOrInterfaceType();
            } else {
                rule = ctx.typeVariable();
            }
            replace(ctx, "[" + mapType(rule) + "]");
        }
    }

    @Override
    public void exitUnannArrayType(Java8Parser.UnannArrayTypeContext ctx) {
        //:	unannPrimitiveType dims
        //|	unannClassOrInterfaceType dims
        //|	unannTypeVariable dims
        //dumpContext("exitUnannArrayType", ctx);
        if (cs) {
            ParserRuleContext rule;
            if (ctx.unannPrimitiveType() != null) {
                rule = ctx.unannPrimitiveType();
            } else if (ctx.unannClassOrInterfaceType() != null) {
                rule = ctx.unannClassOrInterfaceType();
            } else {
                rule = ctx.unannTypeVariable();
            }
            replace(ctx, mapType(rule) + getArrayDimension(ctx.dims()));
        }
        if (swift) {
            ParserRuleContext rule;
            if (ctx.unannPrimitiveType() != null) {
                rule = ctx.unannPrimitiveType();
            } else if (ctx.unannClassOrInterfaceType() != null) {
                rule = ctx.unannClassOrInterfaceType();
            } else {
                rule = ctx.unannTypeVariable();
            }
            replace(ctx, "[" + mapType(rule) + "]");
        }
    }

    private String getArrayDimension(Java8Parser.DimsContext dims) {
        int dim = dims.children.size();
        //logger.debug("dim="+dim+":"+dims.getText());
        if (dims.getText().equals("[][]")) {
            // sehr schlicht, aber brauchbar.
            return "[][]";
        }

        return "[]";
    }

    @Override
    public void exitArrayCreationExpression(Java8Parser.ArrayCreationExpressionContext ctx) {
        //dumpContext("exitArrayCreationExpression", ctx);
        Java8Parser.PrimitiveTypeContext primitive = ctx.primitiveType();
        if (primitive != null) {
            replace(primitive, mapType(primitive));
        }
    }

    @Override
    public void enterExplicitConstructorInvocation(Java8Parser.ExplicitConstructorInvocationContext ctx) {
        //:	typeArguments? 'this' '(' argumentList? ')' ';'
        //    |	typeArguments? 'super' '(' argumentList? ')' ';'
        //    |	expressionName '.' typeArguments? 'super' '(' argumentList? ')' ';'
        //    |	primary '.' typeArguments? 'super' '(' argumentList? ')' ';'
        //dumpContext("enterExplicitConstructorInvocation", ctx);

        List<TerminalNode> thisTokens = ctx.getTokens(Java8Lexer.THIS);
        if (thisTokens != null && !thisTokens.isEmpty()) {
            if (cs) {
                //thisclause = ctx;
                foundthis = true;
            }
            if (swift) {
                rewriter.replace(thisTokens.get(0).getSymbol().getTokenIndex(), "self.init");
            }
            if (nasal) {
                throw new RuntimeException("no this constructor allowd");
            }
        }
        thisTokens = ctx.getTokens(Java8Lexer.SUPER);
        if (thisTokens != null && !thisTokens.isEmpty()) {
            if (cs) {
                //thisclause = ctx;
                foundsuper = true;
            }
            if (swift) {
                //??
            }
            if (nasal) {
                throw new RuntimeException("no super constructor allowd");
            }
        }

    }

    @Override
    public void enterImportDeclaration(Java8Parser.ImportDeclarationContext ctx) {
        //dumpContext("enterImportDeclaration", ctx);
        if (cs) {
            // Das raussuchen ist zwar etwas friemlig, aber so richtig einfach elegenat geht das wohl nicht
            String phrase = ctx.getText();
            phrase = StringUtils.substringBeforeLast(phrase, ".");
            phrase = phrase.substring(6);
            //logger.debug("remaining importing namespace: " + phrase);
            if (!usednamespaces.contains(phrase)) {
                rewriter.insertBefore(ctx.start, "using " + phrase + ";// ");
                usednamespaces.add(phrase);
            } else {
                rewriter.insertBefore(ctx.start, "// ");
            }
        }
        if (nasal) {
            replace(ctx, "");
        }
    }

    @Override
    public void enterSuperclass(Java8Parser.SuperclassContext ctx) {
        //:	'extends' classType
        //Swift==C#
        if (swift || cs) {
            if (!replaceFirst(ctx, Java8Lexer.EXTENDS, " : ")) {
                throw new RuntimeException("no first extends found");
            }
        }
        if (nasal) {
            replace(ctx, "");
        }
        foundextends = true;
        isextending = true;
    }

    @Override
    public void enterExtendsInterfaces(Java8Parser.ExtendsInterfacesContext ctx) {
        //:	'extends' interfaceTypeList
        if (cs) {
            if (!replaceFirst(ctx, Java8Lexer.EXTENDS, " : ")) {
                throw new RuntimeException("no first extends found");
            }
        }
        if (nasal) {
            replace(ctx, "");
        }
        foundextends = true;
        isextending = true;
    }

    @Override
    public void enterSuperinterfaces(Java8Parser.SuperinterfacesContext ctx) {
        //:	'implements' interfaceTypeList
        //dumpContext("enterSuperinterfaces", ctx);
        //Swift==C# ? Nein
        if (cs) {
            Java8Parser.InterfaceTypeListContext typelist = ctx.interfaceTypeList();
            for (Java8Parser.InterfaceTypeContext type : typelist.interfaceType()) {
                implementing.add(type.getText());
            }
            if (foundextends) {
                replaceFirst(ctx, Java8Lexer.IMPLEMENTS, " , ");
                foundextends = false;
            } else {
                replaceFirst(ctx, Java8Lexer.IMPLEMENTS, " : ");
            }
        }
    }

    @Override
    public void enterNormalClassDeclaration(Java8Parser.NormalClassDeclarationContext ctx) {
        // dumpContext("enterNormalClassDeclaration", ctx);
        foundextends = false;
        isextending = false;
        implementing.clear();
    }

    @Override
    public void exitNormalClassDeclaration(Java8Parser.NormalClassDeclarationContext ctx) {
        // dumpContext("exitNormalClassDeclaration", ctx);
        if (cs) {
            if (!hasModifier(ctx, "public")) {
                // alle inner classes public machen
                rewriter.insertBefore(ctx.getStart(), " public ");
            }
        }
        if (nasal) {
            replaceFirst(ctx, Java8Lexer.CLASS, "var ");
            rewriter.insertBefore(ctx.classBody().getStart(), "=");
        }
        //classindex++;
    }

    @Override
    public void exitNormalInterfaceDeclaration(Java8Parser.NormalInterfaceDeclarationContext ctx) {
        if (cs) {
            if (!hasModifier(ctx, "public")) {
                // auch alle inner interfaces public machen
                rewriter.insertBefore(ctx.getStart(), " public ");
            }
        }
        //classindex++;
    }

    @Override
    public void exitMethodModifier(Java8Parser.MethodModifierContext ctx) {
        if (cs) {
            replace(ctx, mapModifier(ctx));
        }
    }

    @Override
    public void exitClassModifier(Java8Parser.ClassModifierContext ctx) {
        if (cs) {
            replace(ctx, mapModifier(ctx));
        }
        if (nasal) {
            replace(ctx, "");
        }
    }

    @Override
    public void exitVariableModifier(Java8Parser.VariableModifierContext ctx) {
        //dumpContext("exitVariableModifier", ctx);
        // Nur bei Parametern? Dann kann aus final immer const werden. NeeNee, C# kann kein const in der PAraliste, zumindest nicht so
        replaceFirst(ctx, Java8Lexer.FINAL, "");
        //replace(ctx, mapModifier(ctx));
    }

    @Override
    public void enterNormalInterfaceDeclaration(Java8Parser.NormalInterfaceDeclarationContext ctx) {
        //:	interfaceModifier* 'interface' Identifier typeParameters? extendsInterfaces? interfaceBody
        //dumpContext("enterNormalInterfaceDeclaration", ctx);
        foundextends = false;
        isextending = false;
        isfunctionalinterface = false;

        ctx.interfaceModifier();

        if (cs) {
            if (hasAnnotation(ctx.interfaceModifier(), "FunctionalInterface")) {
                logger.debug("found FunctionalInterface");
                isfunctionalinterface = true;
                //den Interfacename merken und hier verwerfen.
                functionalinterfacename = ctx.Identifier().getText();
                replaceFirst(ctx, Java8Lexer.Identifier, "  ");
                removeAnnotation(ctx.interfaceModifier(), "FunctionalInterface");
                replaceFirst(ctx, Java8Lexer.INTERFACE, " delegate ");
                //22.6.20: auch den generic type "<T>" vewerfen. Passiert aber spaeter bei Methode im process...()? Nee, das hier ist an der Interfacedefinition
                if (ctx.typeParameters() != null) {
                    Java8Parser.TypeParametersContext typeParameters = ctx.typeParameters();
                    rewriter.delete(typeParameters.start, typeParameters.stop);
                }
                replaceFirst(ctx.interfaceBody(), Java8Lexer.LBRACE, " ");
                replaceFirst(ctx.interfaceBody(), Java8Lexer.RBRACE, " ");
                //23.6.20: Das kann ja auch ein generic sein. Das wird aber spaeter behandelt.

            }
        }

        if (swift) {
            List<TerminalNode> intfTokens = ctx.getTokens(Java8Lexer.INTERFACE);
            rewriter.replace(intfTokens.get(0).getSymbol().getTokenIndex(), "protocol");
        }
    }

    @Override
    public void exitBasicForStatement(Java8Parser.BasicForStatementContext ctx) {
        //:	'for' '(' forInit? ';' expression? ';' forUpdate? ')' statement
        if (swift) {
            deleteFirst(ctx, Java8Lexer.RPAREN);
            replaceFirst(ctx, Java8Lexer.LPAREN, " "); // todo: should check spacing here
            if (!ctx.statement().start.getText().equals("{")) {
                rewriter.insertBefore(ctx.statement().start, "{ ");
                rewriter.insertAfter(ctx.statement().stop, " }");
            }
        }
    }

    @Override
    public void exitWhileStatement(Java8Parser.WhileStatementContext ctx) {
        //:	'while' '(' expression ')' statement
        if (swift) {
            deleteFirst(ctx, Java8Lexer.RPAREN);
            deleteFirst(ctx, Java8Lexer.LPAREN);
            if (!ctx.statement().start.getText().equals("{")) {
                rewriter.insertBefore(ctx.statement().start, "{ ");
                rewriter.insertAfter(ctx.statement().stop, " }");
            }
        }
    }

    @Override
    public void exitMethodInvocation(Java8Parser.MethodInvocationContext ctx) {
        //dumpContext("exitMethodInvocation", ctx);

        // todo: make a map for these
        if (ctx.getText().startsWith("System.out.println") || ctx.getText().startsWith("System.err.println")) {
            if (cs) {
                // Das "Debug.Log" wäre Unity spezifisch und erfordert spezielle Usings. 
                // System.Console.WriteLine() ist aber anscheinend in der Unity Console nicht zu sein.
                replace(ctx, "System.Console.WriteLine(" + getText(ctx.argumentList()) + ")");
            } else {
                replace(ctx, "println(" + getText(ctx.argumentList()) + ")");
            }
        } else {
            //21.6.20: mapmethod also added here
            mapmethod(ctx);
            replaceFirst(ctx, Java8Lexer.SUPER, "base");
        }
    }

    @Override
    public void exitMethodInvocation_lf_primary(Java8Parser.MethodInvocation_lf_primaryContext ctx) {
        //dumpContext("exitMethodInvocation_lf_primary", ctx);
        if (cs) {
            mapmethod(ctx);
            replaceFirst(ctx, Java8Lexer.SUPER, "base");
        }
    }

    @Override
    public void exitMethodInvocation_lfno_primary(Java8Parser.MethodInvocation_lfno_primaryContext ctx) {
        //dumpContext("exitMethodInvocation_lfno_primary", ctx);
        if (cs) {

            Java8Parser.TypeArgumentsContext typeArguments = ctx.typeArguments();

            if (typeArguments != null) {
                //method itself has <T> declaration?
                //logger.debug("typeArguments=" + typeArguments.getText());
                rewriter.delete(typeArguments.start, typeArguments.stop);
                replaceFirst(ctx, Java8Lexer.LPAREN, typeArguments.getText() + "(");
            }

            mapmethod(ctx);
            replaceFirst(ctx, Java8Lexer.SUPER, "base");
        }
    }

    @Override
    public void exitCatchClause(Java8Parser.CatchClauseContext ctx) {
        //dumpContext("exitCatchClause", ctx);
        String phrase = ctx.getText();
        if (phrase.contains("catch(Exceptionse")) {
            replaceFirst(ctx, Java8Lexer.LPAREN, "(System.");
        }
    }

    /**
     * 21.6.20: Now also for FunctionalInterfaces?
     *
     * @param ctx
     */
    private void mapmethod(ParserRuleContext ctx) {
        List<TerminalNode> identTokens = ctx.getTokens(Java8Lexer.Identifier);
        if (identTokens.size() > 0) {
            Token token = identTokens.get(0).getSymbol();
            //logger.debug("mapmethod(ParserRuleContext): "+token.getText());
            if (knowninterfaces.isFunctionalInterface(token.getText())) {
                int start = ctx.start.getStartIndex();
                int stop = ctx.stop.getStopIndex();
                //logger.debug(String.format("isFunctionalInterface start %d stop %d tokenindex %d",start,stop,token.getTokenIndex()));
                dumpContext("isFunctionalInterface", ctx);
                rewriter.delete(token.getTokenIndex() - 1, token.getTokenIndex());
                //rewriter.replace(token.getTokenIndex(),"xx");
            } else {
                rewriter.replace(token.getTokenIndex(), mapMethod(identTokens.get(0)));
            }
        }
    }

    @Override
    public void exitEnhancedForStatement(Java8Parser.EnhancedForStatementContext ctx) {
        //:	'for' '(' variableModifier* unannType variableDeclaratorId ':' expression ')' statement
        if (cs) {
            if (!replaceFirst(ctx, Java8Lexer.FOR, " foreach ")) {
                throw new RuntimeException("no first for found");
            }
            if (!replaceFirst(ctx, Java8Lexer.COLON, " in ")) {
                throw new RuntimeException("no first for found");
            }
        } else {
            if (!ctx.statement().start.getText().equals("{")) {
                rewriter.insertBefore(ctx.statement().start, "{ ");
                rewriter.insertAfter(ctx.statement().stop, " }");
            }
            String st = getText(ctx.statement());

            String out = "for " + getText(ctx.variableDeclaratorId()) + " : " + getText(ctx.unannType())
                    + " in " + getText(ctx.expression()) + " " + st;

            replace(ctx, out);
        }
    }

    @Override
    public void exitUnannClassType_lfno_unannClassOrInterfaceType(Java8Parser.UnannClassType_lfno_unannClassOrInterfaceTypeContext ctx) {
        //unannClassType_lfno_unannClassOrInterfaceType
        //:	Identifier typeArguments?
        // dumpContext("exitUnannClassType_lfno_unannClassOrInterfaceType", ctx);
        replaceFirst(ctx, ctx.Identifier().getSymbol().getType(), mapType(ctx.Identifier().getText()));
    }

    @Override
    public void exitRelationalExpression(Java8Parser.RelationalExpressionContext ctx) {
        //:	shiftExpression
        //    |	relationalExpression '<' shiftExpression
        //    |	relationalExpression '>' shiftExpression
        //    |	relationalExpression '<=' shiftExpression
        //    |	relationalExpression '>=' shiftExpression
        //    |	relationalExpression 'instanceof' referenceType

        //C# = Swift
        replaceFirst(ctx, Java8Lexer.INSTANCEOF, "is");
        // maptype wegen z.B. String? 1.4.16: String wird aber schon gemapped. Dann wohl auch die anderen Basistypen. Ein Risiko bleibt aber.
        //TODO besser eine whitelist verwenden
        Java8Parser.ReferenceTypeContext reftype = ctx.referenceType();
        if (reftype != null) {
            //System.out.println("instanceof reftype = " + reftype.getText());
        }
    }

    @Override
    public void exitExpression(Java8Parser.ExpressionContext ctx) {
       /* String starttext = ctx.start.getText();

        List<TerminalNode> identTokens = ctx.getTokens(Java8Lexer.Identifier);

        logger.debug("starttext=" + starttext + " text=" + ctx.getText()+" identToken="+toString(identTokens));

     /*   if (identTokens.size() > 1) {
            logger.debug("identToken[1]=" + identTokens.get(1).getText());
            replaceLast(ctx, Java8Lexer.Identifier, " GetType() ");
        }*/
    }

    @Override
    public void exitPrimary(Java8Parser.PrimaryContext ctx) {
        String stoptext = ctx.stop.getText();

        List<TerminalNode> identTokens = ctx.getTokens(Java8Lexer.Identifier);

       /* logger.debug("exitPrimary:stoptext=" + stoptext + " text=" + ctx.getText()+" identToken="+toString(identTokens));

        if (stoptext.equals("class")) {
          //  logger.debug("identToken[1]=" + identTokens.get(1).getText());
            replaceLast(ctx, Java8Lexer.CLASS, " GetType() ");
        }*/
    }

    @Override
    public void exitPrimaryNoNewArray_lfno_primary(Java8Parser.PrimaryNoNewArray_lfno_primaryContext ctx) {
        //dumpContext("exitPrimaryNoNewArray_lfno_primary", ctx);

        String stoptext = ctx.stop.getText();

        List<TerminalNode> identTokens = ctx.getTokens(Java8Lexer.Identifier);

        if (cs) {
            if (stoptext.equals("class")) {
                // GetType() geht hier nicht, weil das wohl nur auf Objekten geht. Wenn das hier ein Objekt ist, wird das scheitern?
                //replaceLast(ctx, Java8Lexer.CLASS, "typeof(");
                rewriter.insertBefore(ctx.start, "typeof(");
                replaceLast(ctx, Java8Lexer.CLASS, "");
                replaceLast(ctx, Java8Lexer.DOT, ")");
            }
        } else {
            if (stoptext.equals("class")) {
                replaceLast(ctx, Java8Lexer.CLASS, "GetType()");
            }
        }
    }

    @Override
    public void exitPostfixExpression(Java8Parser.PostfixExpressionContext ctx) {
        String starttext = ctx.start.getText();

        List<TerminalNode> identTokens = ctx.getTokens(Java8Lexer.Identifier);

        //logger.debug("exitPostfixExpression:starttext=" + starttext + " text=" + ctx.getText()+" identToken="+toString(identTokens));

    }

    @Override
    public void exitExpressionName(Java8Parser.ExpressionNameContext ctx) {
        //dumpContext("exitExpressionName", ctx);
        replaceArrayLength(ctx);

    }

    @Override
    public void exitAnnotation(Java8Parser.AnnotationContext ctx) {
        String starttext = ctx.start.getText();
        String stoptext = ctx.stop.getText();

        // dumpContext("exitAnnotation", ctx);

        if (ctx.start.getType() == Java8Lexer.AT && stoptext.equals("Deprecated")) {
            //  logger.debug("identToken[1]=" + identTokens.get(1).getText());
            replace(ctx, "");
        }
    }

    @Override
    public void exitLambdaExpression(Java8Parser.LambdaExpressionContext ctx) {
        replaceFirst(ctx, Java8Lexer.ARROW, "=>");

    }


    @Override
    public void enterInterfaceMethodDeclaration(Java8Parser.InterfaceMethodDeclarationContext ctx) {
        //dumpContext("enterInterfaceMethodDeclaration", ctx);

    }

    /**
     * CS hat keine Modifier an interface Methoden.
     *
     * @param ctx
     */
    @Override
    public void exitInterfaceMethodModifier(Java8Parser.InterfaceMethodModifierContext ctx) {
        //dumpContext("exitInterfaceMethodModifier", ctx);
        if (ctx.start.getType() == Java8Lexer.PUBLIC) {
            replace(ctx, "");
        }

    }

    @Override
    public void exitInterfaceMethodDeclaration(Java8Parser.InterfaceMethodDeclarationContext ctx) {
        //dumpContext("exitInterfaceMethodDeclaration", ctx);
        String methodname = getMethodname(ctx.methodHeader());
        if (isfunctionalinterface) {
            // dann kann/muss der Methodenname durch den Interfacenamen ersetzt werden
            replaceFirst(ctx.methodHeader().methodDeclarator(), Java8Lexer.Identifier, functionalinterfacename);

            processGenericMethod(methodname, ctx.methodHeader());
        }
    }

    /**
     * Fuer implementierte Methoden (ausserhalb interfaces). Erst hier, wenn auch der Methodennamen bekannt ist, kann entschieden werden, was mit
     * einer override Annotation passiert.
     *
     * @param ctx
     */
    @Override
    public void exitMethodDeclaration(Java8Parser.MethodDeclarationContext ctx) {
        //methodDeclaration
        //:	methodModifier* methodHeader methodBody
        //dumpContext("exitMethodDeclaration", ctx);
        dumpMethodDeclaration(ctx);
        String methodname = getMethodname(ctx.methodHeader());
        boolean override = hasAnnotation(ctx, "Override");
        boolean depr = hasAnnotation(ctx, "Deprecated");
        boolean isstatic = hasModifier(ctx, "static");
        boolean isprivate = hasModifier(ctx, "private");
        boolean isprotected = hasModifier(ctx, "protected");
        boolean ispublic = hasModifier(ctx, "public");
        boolean isabstract = hasModifier(ctx, "abstract");
        // System.out.println("methodname=" + methodname + ",override=" + override + ", deprecated=" + depr + ", static=" + isstatic + ", private=" + isprivate);
        if (methodname.equals("parseJsonToModel")) {
            int h = 9;
        }
        if (cs) {
            boolean gotoverride = false;
            if (override) {
                //  logger.debug("identToken[1]=" + identTokens.get(1).getText());
                // Das ist bei C# keine Annotation, aber ein modifier. Allerdings nicht für interfaces. Und das ich hier nur mit Riesenaufwand unterscheiden.
                // Mal mitner kruecke. // dann kommt der override dran. Aber nicht, wenn die Methode eine bekannte Interfacemethode ist.

                if ((isextending || methodname.equals("toString") || methodname.equals("equals")) && !isInterfaceMethod(methodname)) {
                    //replace(ctx, "override");
                    replaceAnnotation(ctx, "Override", "override");
                    gotoverride = true;
                } else {
                    //replace(ctx, "");//"override");
                    replaceAnnotation(ctx, "Override", "");
                }
            }
            if (!isstatic && (isprotected || ispublic) && !isabstract && !gotoverride) {
                // dann muss "virtual" dazu, damit eine ableitende Klasse ueberschreiben kann. Aber nicht wenn sie selber ueberschreibt
                rewriter.insertBefore(ctx.start, "virtual ");
            }
            // Eine Methode mit default access level bekommt public. C# hat strengere Defaults
            if (!isprivate && !ispublic && !isprotected) {
                rewriter.insertBefore(ctx.start, "public ");

            }
            // und dann noch die vom Test entfernen
            replaceAnnotation(ctx, "Test", "");
            replaceAnnotation(ctx, "BeforeClass", "");

            if (methodname.equals("toString") && gotoverride) {
                Java8Parser.MethodHeaderContext header = ctx.methodHeader();
                Java8Parser.MethodDeclaratorContext declarator = header.methodDeclarator();
                replaceFirst(declarator, Java8Lexer.Identifier, "ToString");
            }
            if (methodname.equals("equals") && gotoverride) {
                Java8Parser.MethodHeaderContext header = ctx.methodHeader();
                Java8Parser.MethodDeclaratorContext declarator = header.methodDeclarator();
                replaceFirst(declarator, Java8Lexer.Identifier, "Equals");
            }
            processGenericMethod(methodname, ctx.methodHeader());

        }
        if (nasal) {
            rewriter.replace(ctx.start, "func");
            Java8Parser.MethodHeaderContext header = ctx.methodHeader();
            // return type raus
            //rewriter.replace(header.result().start, "");
            replace(header.result(), "");

        }
    }


    @Override
    public void enterAssertStatement(Java8Parser.AssertStatementContext ctx) {
        //dumpContext("enterAssertStatement", ctx);
        addError("assert not allowed");
    }

    @Override
    public void exitReferenceType(Java8Parser.ReferenceTypeContext ctx) {
        //dumpContext("exitReferenceType", ctx);
        //replaceFirst(ctx, ctx.Identifier().getSymbol().getType(), mapType(ctx.Identifier().getText()));
        if (cs) {
            replace(ctx, mapType(getText(ctx)));
        }
    }

    @Override
    public void exitFieldAccess(Java8Parser.FieldAccessContext ctx) {
        //dumpContext("exitFieldAccess", ctx);
    }

    @Override
    public void exitFieldAccess_lf_primary(Java8Parser.FieldAccess_lf_primaryContext ctx) {
        //dumpContext("exitFieldAccess_lf_primary", ctx);
        replaceArrayLength(ctx);
    }

    private void replaceArrayLength(ParserRuleContext ctx) {
        List<TerminalNode> identTokens = ctx.getTokens(Java8Lexer.Identifier);
        if (identTokens.size() > 0 && identTokens.get(identTokens.size() - 1).getText().equals("length")) {
            // Das ist speziell fuer den Array.length. Weil das risky ist, pruefen dass es keine sonstigen Fields mit Namen length gibt TODO.
            replaceLast(ctx, Java8Lexer.Identifier, "Length");
        }
    }

    @Override
    public void exitFieldAccess_lfno_primary(Java8Parser.FieldAccess_lfno_primaryContext ctx) {
        //dumpContext("exitFieldAccess_lfno_primary", ctx);
    }

    @Override
    public void exitSynchronizedStatement(Java8Parser.SynchronizedStatementContext ctx) {
        //dumpContext("exitSynchronizedStatement", ctx);
        replace(ctx.start, "lock");

    }

    //
    // util
    //
    private boolean isInterfaceMethod(String method) {
        for (String impl : implementing) {
            KnownInterfaces.KnownInterface knownInterface = knowninterfaces.knowninterfaces.get(impl);
            if (knownInterface != null) {
                List<String> methods = knownInterface.methods;
                if (methods != null && methods.contains(method)) {
                    return true;
                }
            }
        }
        // Keine erkannte Interfacemethode
        return false;
    }

    private List<String> isGenericMethod(String method) {
        return knwonGenericMethods.knowngenericmethods.get(method);
    }

    private void deleteFirst(ParserRuleContext ctx, int token) {
        List<TerminalNode> tokens = ctx.getTokens(token);
        if (tokens.size() > 0) {
            rewriter.delete(tokens.get(0).getSymbol().getTokenIndex());
        }
    }

    private boolean replaceFirst(ParserRuleContext ctx, int token, String str) {
        List<TerminalNode> tokens = ctx.getTokens(token);
        if (tokens == null || tokens.isEmpty()) {
            return false;
        }
        rewriter.replace(tokens.get(0).getSymbol().getTokenIndex(), str);
        return true;
    }

    private void replaceLast(ParserRuleContext ctx, int token, String str) {
        List<TerminalNode> tokens = ctx.getTokens(token);
        if (tokens == null || tokens.isEmpty()) {
            return;
        }
        rewriter.replace(tokens.get(tokens.size() - 1).getSymbol().getTokenIndex(), str);
    }

    // Get possibly rewritten text
    private String getText(ParserRuleContext ctx) {
        if (ctx == null) {
            return "";
        }
        return rewriter.getText(new Interval(ctx.start.getTokenIndex(), ctx.stop.getTokenIndex()));
    }

    private void replace(ParserRuleContext ctx, String s) {
        if (ctx != null) {
            rewriter.replace(ctx.start, ctx.stop, s);
        }
    }

    // remove context and hidden tokens to right
    private void removeRight(ParserRuleContext ctx) {
        rewriter.delete(ctx.start, ctx.stop);
        List<Token> htr = tokens.getHiddenTokensToRight(ctx.stop.getTokenIndex());
        for (Token token : htr) {
            rewriter.delete(token);
        }
    }

    public String mapType(ParserRuleContext ctx) {
        //if ( ctx instanceof Java8Parser.UnannArrayTypeContext ) { }
        //String text = ctx.getText();
        String text = getText(ctx);
        return mapType(text);
    }

    public String mapMethod(TerminalNode node) {
        //if ( ctx instanceof Java8Parser.UnannArrayTypeContext ) { }
        //String text = ctx.getText();
        String text = node.getText();
        return mapMethod(text);
    }

    public String mapType(String text) {
        String mapText = typeMap.get(text);
        return mapText == null ? text : mapText;
    }

    public String mapMethod(String text) {
        String mapText = methodMap.get(text);
        //logger.debug("mapMethod(String): "+text);
        return mapText == null ? text : mapText;
    }

    public String mapModifier(ParserRuleContext ctx) {
        //if ( ctx instanceof Java8Parser.UnannArrayTypeContext ) { }
        //String text = ctx.getText();
        String text = getText(ctx);
        return mapModifier(text);
    }

    private void addError(String msg) {
        logger.error(msg);
        errors.add(msg);
    }

    public String mapModifier(String text) {
        String mapText = modifierMap.get(text);
        return mapText == null ? text : mapText;
    }

    private void dumpContext(String label, ParserRuleContext ctx) {
        //System.out.println(label + ": ctx.text=" + ctx.getText() + ", starttoken=" + ctx.start);
        //TokenStream ts = ctx.get

        String starttext = ctx.start.getText();
        String stoptext = ctx.stop.getText();
        List<TerminalNode> identTokens = ctx.getTokens(Java8Lexer.Identifier);
        logger.debug(label + ":starttext=" + starttext + " ,stoptext=" + stoptext + " ,text=" + ctx.getText() + " ,identToken=" + toString(identTokens));

    }

    private void dumpTokenStream(TokenStream ts) {
        for (int i = 0; i < ts.size(); i++) {
            Token token = ts.get(i);
            System.out.println("token " + i + ": " + token.getText());
        }
    }

    private void dumpMethodDeclaration(Java8Parser.MethodDeclarationContext ctx) {
        Java8Parser.MethodHeaderContext header = ctx.methodHeader();
        Java8Parser.MethodDeclaratorContext declarator = header.methodDeclarator();
        //System.out.print("  MethodDeclaration:type = method=" + declarator.Identifier().getText());

        List<Java8Parser.MethodModifierContext> modifiers = ctx.methodModifier();
        for (Java8Parser.MethodModifierContext modifier : modifiers) {
            //System.out.print(", modifier=" + modifier.getText());
        }

        List<Java8Parser.AnnotationContext> annotations = header.annotation();
        for (Java8Parser.AnnotationContext annotation : annotations) {
            //System.out.print(", annotation=" + annotation.getText());
        }

        if (header.methodDeclarator().formalParameterList() != null && header.methodDeclarator().formalParameterList().formalParameters() != null) {
            List<Java8Parser.FormalParameterContext> parameters = header.methodDeclarator().formalParameterList().formalParameters().formalParameter();
            for (Java8Parser.FormalParameterContext parameter : parameters) {
                //System.out.print(", parameter type=" + parameter.getStart().getText());
            }
        }
        //System.out.println();
    }

    private void dumpFieldDeclaration(Java8Parser.FieldDeclarationContext ctx) {
        // Java8Parser.MethodHeaderContext header = ctx.methodHeader();
        Java8Parser.VariableDeclaratorListContext declaratorlist = ctx.variableDeclaratorList();
        Java8Parser.UnannTypeContext typectx = ctx.unannType();
        System.out.print("  FieldDeclaration:type=" + typectx.getText());

        for (Java8Parser.VariableDeclaratorContext declctx : declaratorlist.variableDeclarator()) {
            System.out.print(", field=" + declctx.getText());
        }

        List<Java8Parser.FieldModifierContext> modifiers = ctx.fieldModifier();
        for (Java8Parser.FieldModifierContext modifier : modifiers) {
            System.out.print(", modifier=" + modifier.getText());
        }
        System.out.println();

    }

    private String getMethodname(Java8Parser.MethodHeaderContext header) {
        Java8Parser.MethodDeclaratorContext declarator = header.methodDeclarator();
        return declarator.Identifier().getText();
    }

    /*not yet private String getFieldname(Java8Parser.FieldDeclarationContext ctx) {
        Java8Parser.MethodHeaderContext header = ctx.methodHeader();
        Java8Parser.MethodDeclaratorContext declarator = header.methodDeclarator();
        return declarator.Identifier().getText();
    }*/

    private boolean hasAnnotation(Java8Parser.MethodDeclarationContext ctx, String annotation) {
        List<Java8Parser.MethodModifierContext> modifiers = ctx.methodModifier();
        for (Java8Parser.MethodModifierContext modifier : modifiers) {
            if (modifier.start.getType() == Java8Lexer.AT && modifier.stop.getText().equals(annotation)) {
                // if (modifier.getText().equals("@Override")){
                return true;
            }
        }
        return false;
    }

    private boolean hasAnnotation(List<Java8Parser.InterfaceModifierContext> ctx, String annotation) {
        //Java8Parser.NormalAnnotationContext annotations = ctx.annotation().normalAnnotation();
        for (Java8Parser.InterfaceModifierContext modifier : ctx) {
            if (modifier.start.getType() == Java8Lexer.AT && modifier.stop.getText().equals(annotation)) {
                // if (modifier.getText().equals("@Override")){
                return true;
            }
        }
        return false;
    }

    private boolean hasModifier(Java8Parser.MethodDeclarationContext ctx, String modifier) {
        List<Java8Parser.MethodModifierContext> modifiers = ctx.methodModifier();
        for (Java8Parser.MethodModifierContext modi : modifiers) {
            if (modi.start.getText().equals(modifier)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasModifier(Java8Parser.FieldDeclarationContext ctx, String modifier) {
        List<Java8Parser.FieldModifierContext> modifiers = ctx.fieldModifier();
        for (Java8Parser.FieldModifierContext modi : modifiers) {
            if (modi.start.getText().equals(modifier)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasModifier(Java8Parser.ConstructorDeclarationContext ctx, String modifier) {
        List<Java8Parser.ConstructorModifierContext> modifiers = ctx.constructorModifier();
        for (Java8Parser.ConstructorModifierContext modi : modifiers) {
            if (modi.start.getText().equals(modifier)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasModifier(Java8Parser.NormalClassDeclarationContext ctx, String modifier) {
        List<Java8Parser.ClassModifierContext> modifiers = ctx.classModifier();
        for (Java8Parser.ClassModifierContext modi : modifiers) {
            if (modi.start.getText().equals(modifier)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasModifier(Java8Parser.NormalInterfaceDeclarationContext ctx, String modifier) {
        List<Java8Parser.InterfaceModifierContext> modifiers = ctx.interfaceModifier();
        for (Java8Parser.InterfaceModifierContext modi : modifiers) {
            if (modi.start.getText().equals(modifier)) {
                return true;
            }
        }
        return false;
    }

    private void replaceAnnotation(Java8Parser.MethodDeclarationContext ctx, String annotation, String replacement) {
        List<Java8Parser.MethodModifierContext> modifiers = ctx.methodModifier();
        for (Java8Parser.MethodModifierContext modifier : modifiers) {
            if (modifier.start.getType() == Java8Lexer.AT && modifier.stop.getText().equals(annotation)) {
                replace(modifier, replacement);
            }
        }
    }

    private void replaceModifier(Java8Parser.FieldDeclarationContext ctx, String mod, String replacement) {
        List<Java8Parser.FieldModifierContext> modifiers = ctx.fieldModifier();
        for (Java8Parser.FieldModifierContext modifier : modifiers) {
            if (modifier.start.getText().equals(mod)) {
                replace(modifier, replacement);
            }
        }
    }

    /**
     * Bei Feldern/Vars muss aus einem static final ein const werden (wegen switch)
     *
     * @param ctx
     * @param annotation
     * @param replacement
     */
    private void replaceElementModifier(Java8Parser.MethodDeclarationContext ctx, String annotation, String replacement) {
        List<Java8Parser.MethodModifierContext> modifiers = ctx.methodModifier();
        for (Java8Parser.MethodModifierContext modifier : modifiers) {
            if (modifier.start.getType() == Java8Lexer.AT && modifier.stop.getText().equals(annotation)) {
                replace(modifier, replacement);
            }
        }
    }

    private void removeAnnotation(List<Java8Parser.InterfaceModifierContext> ctx, String annotation) {
        //List<Java8Parser.MethodModifierContext> modifiers = ctx.methodModifier();
        for (Java8Parser.InterfaceModifierContext modifier : ctx) {
            if (modifier.start.getType() == Java8Lexer.AT && modifier.stop.getText().equals(annotation)) {
                replace(modifier, "");
                // Das @ muss offenbar nicht mit entfernt werden (??)
            }
        }
    }

    private void replace(Token token, String s) {
        rewriter.replace(token, s);
    }

    private String toString(List<TerminalNode> tokens) {
        String s = "";
        for (TerminalNode token : tokens) {
            s += token.getText() + ", ";
        }
        return s;
    }

    protected void checkforReservedword(ParseTree pt) {
        if (pt.getPayload() instanceof ParserRuleContext) {
            checkforReservedword((ParserRuleContext) pt.getPayload());
        }
        for (int i = 0; i < pt.getChildCount(); i++) {
            checkforReservedword(pt.getChild(i));
        }
    }

    private void checkforReservedword(ParserRuleContext ctx) {
        List<TerminalNode> identTokens = ctx.getTokens(Java8Lexer.Identifier);
        if (identTokens.size() > 0) {
            for (String rw : reservedwords) {
                if (identTokens.get(0).getText().equals(rw)) {
                    addError("'" + rw + "' is a keyword in CS");
                }
            }
        }
    }

    /**
     * Fuer normale und in (Functional)Interfaces
     */
    private void processGenericMethod(String methodname, Java8Parser.MethodHeaderContext methodHeader) {

        List<String> typelist;
        //Die Erkennung der generics geht ueber "known...". Ginge wohl auch generisch über TypeParametersContext.
        if ((typelist = isGenericMethod(methodname)) != null) {
            // Bei C# muss bei der ganzen Methoden ein Generc (<T>?)sein, wenn einzelne Parameter Generics sind.
            // Man muss auch unterscheiden, ob die ganze Klasse generic ist oder nur die Methode.
            // Muss evtl. noch weiter ausgebaut werden.
            Java8Parser.MethodHeaderContext header = methodHeader;
            Java8Parser.TypeParametersContext typeParameter = header.typeParameters();
            Java8Parser.MethodDeclaratorContext declarator = header.methodDeclarator();

            if (typeParameter != null) {
                //method itself has <T> declaration?
                rewriter.delete(typeParameter.start, typeParameter.stop);
            }
            rewriter.insertAfter(declarator.getStart(), "<T>");

            if (header.methodDeclarator().formalParameterList() != null && header.methodDeclarator().formalParameterList().formalParameters() != null) {
                List<Java8Parser.FormalParameterContext> parameters = header.methodDeclarator().formalParameterList().formalParameters().formalParameter();
                for (Java8Parser.FormalParameterContext parameter : parameters) {
                    for (String type : typelist) {
                        if (type.equals(parameter.getStart().getText())) {
                            rewriter.insertAfter(parameter.getStart(), "<T>");
                        }
                    }
                }
            }
        }
    }
}

