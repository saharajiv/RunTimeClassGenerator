package dynamic.classGenerator;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.ast.CompilationUnit;
import com.rsaha.parser.ClassParser;

import static com.rsaha.util.UtilityClass.checkForKeywordSplitWithNewLine;

public class ClassDecorator extends Decorator{
	
	String [] methodNames = new String [100];
	String modifiedSource = null;
	int lengthOfPseudoCodeMethod ;
	int lastPositionOfSemicolon;
	
	public int decorate(String defaultPath, String defaultClassName,
			final StringBuilder source) throws Exception {
		int posOfSemicolon = 0;
		String sourcePartToBeChecked = null;
		if(source.length()<50){
			sourcePartToBeChecked = source.toString();
		}else{
			sourcePartToBeChecked = source.substring(0,50);
		}
		if(checkForKeywordSplitWithNewLine(sourcePartToBeChecked,IMPORT_KEYWORD,true)){
			int posOfImport = source.toString().lastIndexOf(IMPORT_KEYWORD);
			posOfSemicolon = source.toString().indexOf(";",posOfImport+1);
			insertDefaultImport(source,posOfSemicolon);
			posOfImport = source.toString().lastIndexOf(IMPORT_KEYWORD);
			//posOfSemicolon = source.toString().indexOf(";",posOfImport+1);
		}else if(checkForKeywordSplitWithNewLine(sourcePartToBeChecked,PACKAGE_KEYWORD,false)){
			posOfSemicolon = decorateWithDefaultImportWhenNoUserImports(source);
		}
		source.insert(posOfSemicolon+1+totalLengthOfDefaultImport,"public class " + defaultClassName + " {\n");
		source.insert(posOfSemicolon+totalLengthOfDefaultImport+17+defaultClassName.length(),createPseudoCode());
		source.append("}\n");
		lastPositionOfSemicolon = posOfSemicolon+17+totalLengthOfDefaultImport+defaultClassName.length()+lengthOfPseudoCodeMethod;
		System.out.println(source);
		return lastPositionOfSemicolon;
	}

    
	    public String createPseudoCode(){
	    	final StringBuilder pseudoCode = new StringBuilder();
	    	pseudoCode.append("\n");
	    	pseudoCode.append("\t private void pseduoCode(){\n");
	    	pseudoCode.append("\t\t int a = (Integer)new ConsoleReader(\"int\").read();\n");
	    	pseudoCode.append("\t } \n");
	    	lengthOfPseudoCodeMethod = pseudoCode.length();
	    	return pseudoCode.toString();
	    }
	    
	    public String getModifiedSource(){
	    	return modifiedSource;
	    }
	
	    public String [] getMethodNames(){
	    	return methodNames;
	    }
		
	
	
	
	

	

}
