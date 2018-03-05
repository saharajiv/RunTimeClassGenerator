package dynamic.classGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.ForeachStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public class MethodVisitor extends VoidVisitorAdapter{
    public boolean methodExists = false;
	public List methodNames = new ArrayList<String>();
	public List methodStatements = new ArrayList<String>();
	public String modifiedSource  = null;
	public static Map<String,Node> statementMap = new HashMap();
	
	@Override
	public void visit(final CompilationUnit n, Object arg) {
		super.visit(n, arg);
		int methodCount = 0;
		NodeList<TypeDeclaration<?>> nL = n.getTypes();
		Iterator<TypeDeclaration<?>> it = nL.iterator();
		while(it.hasNext()){
			ClassOrInterfaceDeclaration classDeclaration = (ClassOrInterfaceDeclaration)it.next();
			Iterator iterator = classDeclaration.getMembers().iterator();
			while(iterator.hasNext()){
				MethodDeclaration methodDeclaration = (MethodDeclaration)iterator.next();
				if(!methodDeclaration.getName().toString().equals("defaultMethod"))
					methodExists = true;
				methodNames.add(methodDeclaration.getName().toString());
			}
			
		}
		modifiedSource = n.toString();
		
	}
	
	@SuppressWarnings("unchecked")
	@Override
    public void visit(final MethodDeclaration n, final Object arg) {
		//System.out.println(n.toString());
		String methodName = n.getNameAsString();
		ClassRefactorer refactorer = new ClassRefactorer();
        List<Node> nodes = n.getBody().get().getChildNodes();
        for(Node node:nodes){
        	if(node.getChildNodes().size()>0 && node.getChildNodes().get(0)!=null){
        		if(methodName.equals("pseduoCode")){
        			insertIntoMapForRead(node);
        		}
        		refactorer.javaBlockRefactorer(node);
        	}
        }
	}

	
	@Override
    public void visit(MethodCallExpr methodCall, Object arg)
    {
		System.out.print("Method call: " + methodCall.getName() + "\n");
		if(methodCall.equals("print")){
			//methodCall.replace();
			methodCall.getParentNode();
		}
        /*List<Expression> args = methodCall.getArguments();
        if (args != null)
            handleExpressions(args);*/
        
    }
	
	private void insertIntoMapForRead(Node node){
		statementMap.put("read", node.getChildNodes().get(0).getChildNodes().get(0).getChildNodes().get(2));
	}
	
	public static Map<String,Node> getStatementMap(){
		return statementMap;
	}
	
   
}