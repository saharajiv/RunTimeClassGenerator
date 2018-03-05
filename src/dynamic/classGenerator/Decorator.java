package dynamic.classGenerator;

import static com.rsaha.util.UtilityClass.checkForKeywordSplitWithNewLine;

public abstract class Decorator {
	protected static String IMPORT_KEYWORD = "import";
	protected static String PACKAGE_KEYWORD = "package";
	private String DEFAULT_READ_IMPORT = "import com.rsaha.librarires.console.*;";
	private String DEFAULT_UTIL_IMPORT = "import java.util.*;";
	private String DEFAULT_UTIL_CONCURRENT_IMPORT = "import java.util.concurrent.*;";
	private String DEFAULT_UTIL_CONCURRENT_ATOMIC_IMPORT = "import java.util.concurrent.atomic.*;";
	private String DEFAULT_UTIL_CONCURRENT_LOCKS = "import java.util.concurrent.locks.*;";
	private String DEFAULT_UTIL_FUNCTION = "import java.util.function.*;";
	private String DEFAULT_UTIL_STREAM = "import java.util.stream.*;";
	private String DEFAULT_UTIL_REGEX_IMOPRT = "import java.util.regex.*;";
	private String DEFAULT_UTIL_SPI_IMPORT = "import java.util.spi.*;";
	
	protected int totalLengthOfDefaultImport;
	
	protected int decorateWithDefaultImport(StringBuilder source){
    	int posOfSemicolon = 0;
    	if(checkForKeywordSplitWithNewLine(source.substring(0,100),IMPORT_KEYWORD,true)){
			int posOfImport = source.toString().lastIndexOf(IMPORT_KEYWORD);
			posOfSemicolon = source.toString().indexOf(";",posOfImport+1);
			insertDefaultImport(source,posOfSemicolon);
			posOfImport = source.toString().lastIndexOf(IMPORT_KEYWORD);
			//posOfSemicolon = source.toString().indexOf(";",posOfImport+1);
    	}else if(checkForKeywordSplitWithNewLine(source.substring(0,50),PACKAGE_KEYWORD,false)){
			posOfSemicolon = decorateWithDefaultImportWhenNoUserImports(source);
		}
			return posOfSemicolon;
    }
	
	public void insertDefaultImport(StringBuilder source,int posOfLastSemicolon) {
		source.insert(posOfLastSemicolon+1, '\n');
		//console read import
		source.insert(posOfLastSemicolon+1,DEFAULT_READ_IMPORT);
		//util import
		source.insert(posOfLastSemicolon+1,DEFAULT_UTIL_IMPORT+"\n");
		source.insert(posOfLastSemicolon+1,DEFAULT_UTIL_CONCURRENT_IMPORT+"\n");
		source.insert(posOfLastSemicolon+1,DEFAULT_UTIL_CONCURRENT_ATOMIC_IMPORT+"\n");
		source.insert(posOfLastSemicolon+1,DEFAULT_UTIL_CONCURRENT_LOCKS+"\n");
		source.insert(posOfLastSemicolon+1,DEFAULT_UTIL_FUNCTION+"\n");
		source.insert(posOfLastSemicolon+1,DEFAULT_UTIL_STREAM+"\n");
		source.insert(posOfLastSemicolon+1,DEFAULT_UTIL_REGEX_IMOPRT+"\n");
		source.insert(posOfLastSemicolon+1,DEFAULT_UTIL_SPI_IMPORT+"\n");
		//Math import
		source.insert(posOfLastSemicolon+1, '\n');
		totalLengthOfDefaultImport = DEFAULT_READ_IMPORT.length()+DEFAULT_UTIL_IMPORT.length()+
				DEFAULT_UTIL_CONCURRENT_IMPORT.length()+DEFAULT_UTIL_CONCURRENT_ATOMIC_IMPORT.length()+
				DEFAULT_UTIL_CONCURRENT_LOCKS.length()+DEFAULT_UTIL_FUNCTION.length()+
				DEFAULT_UTIL_STREAM.length()+DEFAULT_UTIL_REGEX_IMOPRT.length()+DEFAULT_UTIL_SPI_IMPORT.length()+11;
	}
	
	
	protected void decorateWithDefaultPackage(boolean classDefined, boolean packageDefined, String defaultPath, final StringBuilder source)
			throws Exception {
		if(packageDefined && !classDefined)
			throw new Exception("Cannot define a package in the file if the class is not defined");
		else if(!packageDefined){
			source.insert(0,"package " + defaultPath + ";\n");
		}
	}
	
	protected int decorateWithDefaultImportWhenNoUserImports(final StringBuilder source) {
		int posOfSemicolon;
		int posOfPackage = source.toString().lastIndexOf(PACKAGE_KEYWORD);
		posOfSemicolon = source.toString().indexOf(";",posOfPackage);
		insertDefaultImport(source,posOfSemicolon);
		return posOfSemicolon;
	}
	
}
