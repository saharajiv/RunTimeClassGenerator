package dynamic.classGenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.AbstractMap;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.ForeachStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.ast.type.PrimitiveType;

public class ClassRefactorer {
	private Node node;
	private static Map<String, String> primitiveMarkerMap = Collections.unmodifiableMap(
			Stream.of(
                new AbstractMap.SimpleEntry<>("int", "Integer"),
                new AbstractMap.SimpleEntry<>("float", "Float"),
                new AbstractMap.SimpleEntry<>("long", "Long"),
                new AbstractMap.SimpleEntry<>("double", "Double"),
                new AbstractMap.SimpleEntry<>("short", "Short"))
                .collect(Collectors.toMap((e) -> e.getKey(), (e) -> e.getValue())));
	
	public ClassRefactorer(){
	}
	
	public ClassRefactorer(Node node){
		this.node = node;
	}
	
	private void checkForPrintStmt(Node node) {
		String expr = null;
		if(node instanceof MethodCallExpr){
			expr = node.getChildNodes().get(0).toString();
			if(expr.equals("print") || expr.equals("println")){
				List<Node> nodeOperated = node.getChildNodes();
				List<Node> clonedNode = new ArrayList<Node>();
				clonedNode.add(nodeOperated.get(0));
				node.getChildNodes().get(0).remove();
				SimpleName alteredNode = (SimpleName)clonedNode.get(0);
				alteredNode.setIdentifier("System.out."+alteredNode.getIdentifier());
				return;
			}
		}else if(node.getChildNodes().get(0).getChildNodes() == null || node.getChildNodes().get(0).getChildNodes().size()<1){
			return;
		}else{
			expr = node.getChildNodes().get(0).getChildNodes().get(0).toString();
		}
		if(expr.equals("print") || expr.equals("println")){
			//SimpleName name = ((MethodCallExpr)(node.getChildNodes().get(0))).getName();
			List<Node> nodeOperated = node.getChildNodes().get(0).getChildNodes();
			List<Node> clonedNode = new ArrayList<Node>();
			clonedNode.addAll(nodeOperated);
			node.getChildNodes().get(0).remove();
			SimpleName alteredNode = (SimpleName)clonedNode.get(0);
			alteredNode.setIdentifier("System.out."+alteredNode.getIdentifier());
			((MethodCallExpr)node.getChildNodes().get(0)).setName(alteredNode);
			((MethodCallExpr)node.getChildNodes().get(0)).setArguments(((MethodCallExpr)node.getChildNodes().get(0)).getArguments());
			node.getChildNodes().get(0).getChildNodes();
		}
	}
	
	
	private void checkForInputStmt(Node node) {
		
		String expr = null;
		int position = checkForPosition(node);
		if(position==0){
			return ;
		}else if(position == 3){
			 	Node replacingNode = getReplacingNode(node);
				List<Node> nodeOperated = node.getChildNodes();
				node.getChildNodes().get(0).getChildNodes().get(0).getChildNodes();
				((MethodCallExpr)(node.getChildNodes().get(0).getChildNodes().get(0).getChildNodes().get(2))).replace(replacingNode);
				return;
			
		}else{
			throw new RuntimeException("Data type not declared in the expression");
		}
		
		/*else if(node.getChildNodes().get(0).getChildNodes() == null || node.getChildNodes().get(0).getChildNodes().size()<1){
			return;
		}else{
			expr = node.getChildNodes().get(0).getChildNodes().get(0).toString();
		}*/
		/*if(expr.equals("input")){
			//SimpleName name = ((MethodCallExpr)(node.getChildNodes().get(0))).getName();
			List<Node> nodeOperated = node.getChildNodes().get(0).getChildNodes();
			List<Node> clonedNode = new ArrayList<Node>();
			clonedNode.addAll(nodeOperated);
			node.getChildNodes().get(0).remove();
			SimpleName alteredNode = (SimpleName)clonedNode.get(0);
			alteredNode.setIdentifier("System.out."+alteredNode.getIdentifier());
			((MethodCallExpr)node.getChildNodes().get(0)).setName(alteredNode);
			((MethodCallExpr)node.getChildNodes().get(0)).setArguments(((MethodCallExpr)node.getChildNodes().get(0)).getArguments());
			node.getChildNodes().get(0).getChildNodes();
		}*/
	}
	

	private Node getReplacingNode(Node node) {
		String primitiveDataType = ((PrimitiveType)(node.getChildNodes().get(0).getChildNodes().get(0).getChildNodes().get(0))).getType().asString();
		String markerDataType = primitiveMarkerMap.get(primitiveDataType);
		Node replacingNode = MethodVisitor.statementMap.get("read").clone();
		replacingNode.getChildNodes().get(0);
		//replacingNode.getChildNodes().get(0).remove();
		((SimpleName)(replacingNode.getChildNodes().get(0).getChildNodes().get(0))).setIdentifier(markerDataType);
		((StringLiteralExpr)(replacingNode.getChildNodes().get(1).getChildNodes().get(0).getChildNodes().get(1))).setValue(primitiveDataType);
		return replacingNode;
	}

	private int checkForPosition(Node node) {
		int index = 0;
		if(node.getChildNodes().get(0)!=null && node.getChildNodes().get(0).getChildNodes().size()>0 &&node.getChildNodes().get(0).getChildNodes().get(0)!=null){
			if(node instanceof ExpressionStmt && node.getChildNodes().get(0) instanceof VariableDeclarationExpr && node.getChildNodes().get(0).getChildNodes().get(0) instanceof VariableDeclarator){
				if(node.getChildNodes().get(0).getChildNodes().get(0).getChildNodes().size()==3){
					if(node.getChildNodes().get(0).getChildNodes().get(0).getChildNodes().get(2).toString().equals("read()"))
						index = 3;
				}else if(node.getChildNodes().get(0).getChildNodes().get(0).getChildNodes().size()==2){
					if(node.getChildNodes().get(0).getChildNodes().get(0).getChildNodes().get(1).toString().equals("read()"))
						index = 2;
				}
				
			}else if(node instanceof ExpressionStmt && node.getChildNodes().get(0) instanceof AssignExpr && node.getChildNodes().get(0).getChildNodes().get(1) instanceof MethodCallExpr){
				if(node.getChildNodes().get(0).getChildNodes().get(1).getChildNodes().size()==1){
					if(node.getChildNodes().get(0).getChildNodes().get(1).getChildNodes().get(0).toString().equals("read"))
						index = 1;
				}
			}
		}
		return index;
	}

	public void javaBlockRefactorer(Node node) {
		if(node instanceof IfStmt){
			ifStmtBlockExecutor(node);
		}else if(node instanceof WhileStmt){
			whileStmtBlockExecutor(node);
		}else if (node instanceof ForeachStmt){
			forEachStmtBlockExecutor(node);
		}else if(node instanceof ForStmt){
			forStmtBlockExecutor(node);
		}else if(node instanceof ExpressionStmt || node instanceof MethodCallExpr){
			checkForPrintStmt(node);
			checkForInputStmt(node);
		}
	}

	private void ifStmtBlockExecutor(Node node) {
		Statement statement = ((IfStmt) node).getThenStmt();
		for(Node ifNode : statement.getChildNodes()){
			System.out.println(ifNode);
			javaBlockRefactorer(ifNode);
		}
		((IfStmt) node).getElseStmt().ifPresent(new Consumer<Statement>() {
			public void accept(Statement t) {
				for(Node elseNode:t.getChildNodes()){
					javaBlockRefactorer(elseNode);
				}
				
			}
		});
		
	}
	
	private void whileStmtBlockExecutor(Node node) {
		Statement statement = ((WhileStmt) node).getBody();
		for(Node whileNode : statement.getChildNodes()){
			System.out.println(whileNode);
			javaBlockRefactorer(whileNode);
		}
	}
	
	
	private void forStmtBlockExecutor(Node node) {
		for(Node forNode : node.getChildNodes()){
			if(forNode instanceof VariableDeclarationExpr || forNode instanceof BinaryExpr){
				continue;
			}else{
				javaBlockRefactorer(forNode);
			}
		}
		Statement statement = ((ForStmt) node).getBody();
		for(Node forNode : statement.getChildNodes()){
			System.out.println(forNode);
			javaBlockRefactorer(forNode);
		}
	}
	
	
	private void forEachStmtBlockExecutor(Node node) {
		Statement statement = ((ForeachStmt) node).getBody();
		for(Node forNode : statement.getChildNodes()){
			System.out.println(forNode);
			javaBlockRefactorer(forNode);
		}
	}
	
	
}
