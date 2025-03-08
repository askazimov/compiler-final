package myparser;

import java.util.HashMap;
import java.util.Map;

public class MyVisitor implements MyParserVisitor {

    // Maps for functions and procedures, global variables and local variables per function or procedure
    // for functions, funcName is key and return type is value (NULL for proc)
    private Map<String, String> functions = new HashMap<>();
    private Map<String, String> globalVars = new HashMap<>();
    private Map<String, Map<String, String>> symbolsPerFuncOrProc = new HashMap<>();

    // Current scope
    // Will be set to function name when parsing it
    private String currentScope = null;
    
    public MyVisitor() {
        functions.put("getInt", "Integer");
        functions.put("getString", "String");
        functions.put("getBool", "Boolean");
        functions.put("getFloat", "Float");
        functions.put("echo", "NULL");
    }

    @Override
    public Object visit(SimpleNode node, Object data) throws SemanticException {
        node.childrenAccept(this, data);
        return null;
    }

    @Override
    public Object visit(ASTProgram node, Object data) throws SemanticException {

        // Find the MAIN procedure. We start at -1, so it's easy to
        // detect that it's not found.
        // IT'S STILL redundant as parsing fails if main doesn't exist in the first place
        int mainIndex = -1;
        ASTMainProc mainProc = null;
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            if (node.jjtGetChild(i) instanceof ASTMainProc) {
                mainIndex = i;
                break;
            }
        }
        if (mainIndex == -1) {
            throw new SemanticException("Main procedure not found!");
        }
        mainProc = (ASTMainProc) node.jjtGetChild(mainIndex);
        
        System.out.println("Parsing program. Initializing global variables...");

        // In our grammar, we decided that all variables inside main are treated as global
        // hence, we first handle main to init global vars before even starting to parse other functions!
        for (int childIndex = 0; mainProc.jjtGetChild(childIndex) instanceof ASTDeclaration; childIndex++) {
            ASTDeclaration currentDeclaration = (ASTDeclaration) mainProc.jjtGetChild(childIndex);
            String varName = currentDeclaration.data.get("name").toString();
            String varType = currentDeclaration.data.get("type").toString();
            
            if (globalVars.containsKey(varName)) {
                throw new SemanticException("Global variable " + varName + " already defined!");
            }
            globalVars.put(varName, varType);
        }

        System.out.println("Global variables: " + globalVars);
        
        // Now visit the rest of the program.
        node.childrenAccept(this, data);
        return null;
    }

    @Override
    public Object visit(ASTMainProc node, Object data) throws SemanticException {
        currentScope = "main";
        node.childrenAccept(this, data);
        currentScope = null;
        return null;
    }

    @Override
    public Object visit(ASTFunctionDef node, Object data) throws SemanticException {
        String funcName = node.data.get("funcName").toString();
        String funcReturnType = node.data.get("returnType").toString();
        
        if (globalVars.containsKey(funcName)) {
            throw new SemanticException("Function name " + funcName + " conflicts with an already declared global variable!");
        }
        
        if (functions.containsKey(funcName)) {
            throw new SemanticException("Function " + funcName + " already defined elsewhere!");
        }
        
        functions.put(funcName, funcReturnType);
        
        // For storing local vars, will be added to symbol table later
        Map<String, String> localVars = new HashMap<>();
        
        int childIndex = 1;
        for (; node.jjtGetChild(childIndex) instanceof ASTParam; childIndex++) {
            ASTParam currentParam = (ASTParam) node.jjtGetChild(childIndex);
            String paramName = currentParam.data.get("name").toString();
            String paramType = currentParam.data.get("type").toString();
            
            if (globalVars.containsKey(paramName)) {
                throw new SemanticException(funcName + ": parameter " + paramName + " conflicts with a global variable!");
            }
            if (localVars.containsKey(paramName)) {
                throw new SemanticException(funcName + ": duplicate parameter " + paramName);
            }
            localVars.put(paramName, paramType);
        }
        
        // Account for the return type mandatory for functions
        // Index should now point to function's declared variables
        childIndex += 1;

        for (; node.jjtGetChild(childIndex) instanceof ASTDeclaration; childIndex++) {
            ASTDeclaration currentDeclaration = (ASTDeclaration) node.jjtGetChild(childIndex);
            String varName = currentDeclaration.data.get("name").toString();
            String varType = currentDeclaration.data.get("type").toString();
            
            if (globalVars.containsKey(varName)) {
                throw new SemanticException(funcName + ": local variable " + varName + " conflicts with a global variable!");
            }
            if (localVars.containsKey(varName)) {
                throw new SemanticException(funcName + ": variable " + varName + " already defined!");
            }
            localVars.put(varName, varType);
        }
        
        symbolsPerFuncOrProc.put(funcName, localVars);
        System.out.println("Symbols for function " + funcName + ": " + localVars);
        
        currentScope = funcName;
        
        node.childrenAccept(this, data);
        currentScope = null;
        
        return null;
    }

    @Override
    public Object visit(ASTProcedureDef node, Object data) throws SemanticException {
        String procName = node.data.get("procName").toString();
        
        if (globalVars.containsKey(procName)) {
            throw new SemanticException("Procedure name " + procName + " conflicts with an already declared global variable!");
        }
        
        if (functions.containsKey(procName)) {
            throw new SemanticException("Procedure " + procName + " already defined elsewhere!");
        }
        
        // As proc doesn't return anything, we decided to mark it as
        // returning null to make it easy to detect in the future.
        functions.put(procName, "NULL");
        
        Map<String, String> localVars = new HashMap<>();
        
        int childIndex = 1;
        for (; node.jjtGetChild(childIndex) instanceof ASTParam; childIndex++) {
            ASTParam currentParam = (ASTParam) node.jjtGetChild(childIndex);
            String paramName = currentParam.data.get("name").toString();
            String paramType = currentParam.data.get("type").toString();
            
            if (globalVars.containsKey(paramName)) {
                throw new SemanticException(procName + ": parameter " + paramName + " conflicts with a global variable!");
            }
            if (localVars.containsKey(paramName)) {
                throw new SemanticException(procName + ": duplicate parameter " + paramName);
            }
            localVars.put(paramName, paramType);
        }
        
        for (; node.jjtGetChild(childIndex) instanceof ASTDeclaration; childIndex++) {
            ASTDeclaration currentDeclaration = (ASTDeclaration) node.jjtGetChild(childIndex);
            String varName = currentDeclaration.data.get("name").toString();
            String varType = currentDeclaration.data.get("type").toString();
            
            if (globalVars.containsKey(varName)) {
                throw new SemanticException(procName + ": local variable " + varName + " conflicts with a global variable!");
            }
            if (localVars.containsKey(varName)) {
                throw new SemanticException(procName + ": variable " + varName + " already defined!");
            }
            localVars.put(varName, varType);
        }
        
        symbolsPerFuncOrProc.put(procName, localVars);
        System.out.println("Symbols for procedure " + procName + ": " + localVars);
        
        currentScope = procName;
        node.childrenAccept(this, data);
        currentScope = null;
        return null;
    }

    @Override
    public Object visit(ASTParam node, Object data) throws SemanticException {
        node.childrenAccept(this, data);
        return null;
    }

    @Override
    public Object visit(ASTWhileStmt node, Object data) throws SemanticException {
        node.childrenAccept(this, data);
        return null;
    }

    @Override
    public Object visit(ASTStmtList node, Object data) throws SemanticException {
        node.childrenAccept(this, data);
        return null;
    }

    @Override
    public Object visit(ASTStmt node, Object data) throws SemanticException {
        node.childrenAccept(this, data);
        return null;
    }

    @Override
    public Object visit(ASTDeclaration node, Object data) throws SemanticException {
        node.childrenAccept(this, data);
        return null;
    }

    
    @Override
    public Object visit(ASTAssignment node, Object data) throws SemanticException {
        
        String varName = node.data.get("name").toString();
        String varType = lookupVar(varName);
        System.out.println("CURVAR: " + varName + " of type " + varType);
        
        SimpleNode exprNode = (SimpleNode) node.jjtGetChild(node.jjtGetNumChildren() - 1);
        String exprType = evaluateExpressionType(exprNode);
        
        if (!varType.equals(exprType)) {
            throw new SemanticException("Type mismatch in assignment to variable " + varName +
                                        ": expected " + varType + " but got " + exprType);
        }
        
        node.childrenAccept(this, data);
        return null;
    }

    @Override
    public Object visit(ASTIfStmt node, Object data) throws SemanticException {
        node.childrenAccept(this, data);
        return null;
    }

    @Override
    public Object visit(ASTExpr node, Object data) throws SemanticException {
        node.childrenAccept(this, data);
        return null;
    }

    @Override
    public Object visit(ASTArithmExp node, Object data) throws SemanticException {
        node.childrenAccept(this, data);
        return null;
    }

    @Override
    public Object visit(ASTArithmExpTail node, Object data) throws SemanticException {
        node.childrenAccept(this, data);
        return null;
    }

    @Override
    public Object visit(ASTTerm node, Object data) throws SemanticException {
        node.childrenAccept(this, data);
        return null;
    }

    @Override
    public Object visit(ASTTermTail node, Object data) throws SemanticException {
        node.childrenAccept(this, data);
        return null;
    }

    @Override
    public Object visit(ASTUnary node, Object data) throws SemanticException {
        node.childrenAccept(this, data); 
        return null;
    }

    @Override
    public Object visit(ASTElem node, Object data) throws SemanticException {
        node.childrenAccept(this, data);
        return null;
    }

    @Override
    public Object visit(ASTOperand node, Object data) throws SemanticException {
        node.childrenAccept(this, data);
        return null;
    }

    @Override
    public Object visit(ASTBoolExp node, Object data) throws SemanticException {
        node.childrenAccept(this, data);
        return null;
    }

    @Override
    public Object visit(ASTBoolExpTail node, Object data) throws SemanticException {
        node.childrenAccept(this, data);
        return null;
    }

    @Override
    public Object visit(ASTBoolTerm node, Object data) throws SemanticException {
        node.childrenAccept(this, data);
        return null;
    }

    @Override
    public Object visit(ASTBoolTermTail node, Object data) throws SemanticException {
        node.childrenAccept(this, data);
        return null;
    }

    @Override
    public Object visit(ASTNegation node, Object data) throws SemanticException {
        node.childrenAccept(this, data);
        return null;
    }

    @Override
    public Object visit(ASTBoolElem node, Object data) throws SemanticException {
        node.childrenAccept(this, data);
        return null;
    }

    @Override
    public Object visit(ASTComparison node, Object data) throws SemanticException {
        node.childrenAccept(this, data);
        return null;
    }

    @Override
    public Object visit(ASTComparisonOperator node, Object data) throws SemanticException {
        node.childrenAccept(this, data);
        return null;
    }

    
    @Override
    public Object visit(ASTFunctionCall node, Object data) throws SemanticException {
        ASTid idNode = (ASTid) node.jjtGetChild(0);
        String funcName = idNode.data.get("name").toString();
        System.out.println("Calling function " + funcName);
        if (!functions.containsKey(funcName)) {
            throw new SemanticException("Function or procedure " + funcName + " is not defined.");
        }
        node.childrenAccept(this, data);
        return null;
    }

    @Override
    public Object visit(ASTArrayElement node, Object data) throws SemanticException {
        node.childrenAccept(this, data);
        return null;
    }

    @Override
    public Object visit(ASTVartype node, Object data) throws SemanticException {
        return null;
    }

    @Override
    public Object visit(ASTid node, Object data) throws SemanticException {
        node.childrenAccept(this, data);
        return null;
    }
    
    private String lookupVar(String varName) throws SemanticException {
        System.out.println("Getting var " + varName);
        if (currentScope != null && symbolsPerFuncOrProc.containsKey(currentScope)) {
            Map<String, String> localVars = symbolsPerFuncOrProc.get(currentScope);
            System.out.println(localVars);
            if (localVars.containsKey(varName)) {
                return localVars.get(varName);
            }
        }
        if (globalVars.containsKey(varName)) {
            return globalVars.get(varName);
        }
        throw new SemanticException("Variable " + varName + " is not declared in the current scope.");
    }
    
    
    private String evaluateExpressionType(SimpleNode node) throws SemanticException {
        System.out.println("Evaluating " + node.data.get("argType").toString());
        String argType = node.data.get("argType").toString();

        switch (argType) {
            case "Integer":
                return "Integer";
            case "String":
                return "String";
            case "Boolean":
                return "Boolean";
            case "Variable":
                // System.out.println("BBBB " + node.jjtGetChild(0));
                // return lookupVar(((ASTid) node).jjtGetChild(0).data.get("name").toString());
                // return lookupVar(((ASTid) node).jjtGetChild(0).data.get("name").toString());
                return "Variable";
            case "FunctionCall":
                ASTFunctionCall fc = (ASTFunctionCall) node.jjtGetChild(0);
                // System.out.println("AAA " + fc.data);
                // String funcName = idNode.data.get("name").toString();
                String funcName = fc.data.get("funcName").toString();
                if (functions.get(funcName) == null) {
                    throw new SemanticException("Procedure/Function " + funcName + " is not defined!");
                }
                String retType = functions.get(funcName);
                if (retType.equals("NULL")) {
                    throw new SemanticException("Procedure " + funcName + " used in expression context!");
                }
                return retType;
            default:
                return "Unknown";
        }

   }
}
