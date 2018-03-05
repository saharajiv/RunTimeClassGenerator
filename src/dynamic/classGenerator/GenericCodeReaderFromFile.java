package dynamic.classGenerator;

import static java.util.Collections.singletonList;
import static javax.tools.JavaFileObject.Kind.SOURCE;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Properties;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.ast.CompilationUnit;
import com.rsaha.parser.ClassParser;

import sun.misc.Unsafe;

import static com.rsaha.util.UtilityClass.checkForKeyword;
import static com.rsaha.util.UtilityClass.checkForKeywordSplitWithNewLine;

/**
 * by Rajib Saha
 */
public class GenericCodeReaderFromFile extends Decorator{
	private static final String ENTRY_CLASS = "entry-class";
	private static final String SRC = "src";
	private static final String CLASS_KEYWORD = "class";
	private ClassDecorator classDecorator = new ClassDecorator();
	private ClassParser parser = new ClassParser();
	String defaultPath = "com.repl";
	String defaultClassName = "DefaultClass";
	String fullClassName = defaultPath.replace('.', '/') + "/" + defaultClassName;
	String className = null;
	String path = null;
	boolean packageDefined = false;
	boolean classDefined = false;
	boolean methodDefined = false;
	String filename = "config.properties";
	private String srcPath;
	/*private String entryClassOfApp;*/
	private String fullPathOfEntryclass;
	
    public void dynamicClassCreation() throws Exception {
    	Class noparams[] = {};
    	final StringBuilder source = new StringBuilder();
    	String sCurrentLine;
    	loadConfigFile();
    	//BufferedReader br = new BufferedReader(new FileReader("src/main/java/com/rsaha/dynamic/classes/HelloWorld.java"));
    	File [] listOfFiles = listFilesRecursively(srcPath);
    	//BufferedReader br = new BufferedReader(new  FileReader(fullPathOfEntryclass));
    	BufferedReader br = new BufferedReader(new  FileReader(fullPathOfEntryclass));
    	readSourceFile(source, br);
    	decorateWithDefaultPackage(classDefined,packageDefined, defaultPath, source);
    	if(!classDefined){
    		int lastPositionOfSemicolon= classDecorator.decorate(defaultPath, defaultClassName, source);
    		parser.setLastPositionOfSemicolon(lastPositionOfSemicolon);
    		methodDefined = parser.parse(source);
    	}else{
    		fullClassName = setFullClassName(packageDefined, defaultPath, className, path);
    		decorateWithDefaultImport(source);
    		methodDefined = parser.parse(source);
    	}
    	/*if(!methodDefined){
    		source.insert(27," public void defaultMethod() {\n");
    	}*/
    	br.close();
    	String modifiedSource = parser.getModifiedSource();
    	System.out.println(modifiedSource);
    	// A byte array output stream containing the bytes that would be written to the .class file
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        final SimpleJavaFileObject simpleJavaFileObject
                = new SimpleJavaFileObject(URI.create(fullClassName + ".java"), SOURCE) {

            @Override
            public CharSequence getCharContent(boolean ignoreEncodingErrors) {
                return modifiedSource;
            }

            @Override
            public OutputStream openOutputStream() throws IOException {
                return byteArrayOutputStream;
            }
        };

        final JavaFileManager javaFileManager = new ForwardingJavaFileManager(
                ToolProvider.getSystemJavaCompiler().getStandardFileManager(null, null, null)) {

            @Override
            public JavaFileObject getJavaFileForOutput(Location location,
                                                       String className,
                                                       JavaFileObject.Kind kind,
                                                       FileObject sibling) throws IOException {
                return simpleJavaFileObject;
            }
        };

        ToolProvider.getSystemJavaCompiler().getTask(
                null, javaFileManager, null, null, null, singletonList(simpleJavaFileObject)).call();

        final byte[] bytes = byteArrayOutputStream.toByteArray();

        // use the unsafe class to load in the class bytes
        
        final Field f = Unsafe.class.getDeclaredField("theUnsafe");
        f.setAccessible(true);
        final Unsafe unsafe = (Unsafe) f.get(null);
        final Class aClass = unsafe.defineClass(fullClassName, bytes, 0, bytes.length,this.getClass().getClassLoader(),this.getClass().getProtectionDomain());

        executeClass(noparams, aClass);
    }



	private void executeClass(Class[] noparams, final Class aClass)
			throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		final Object o = aClass.newInstance();
        Method method = null;
        String [] methodNames = parser.getMethodNames();
        if(!methodDefined){
        	 method = aClass.getDeclaredMethod("defaultMethod", noparams);
        }else if(methodDefined){
        	method = aClass.getDeclaredMethod(methodNames[1], noparams);
        }
        Object returned = method.invoke(o, null);
	}



	private void loadConfigFile() {
		Properties prop = new Properties();
    	InputStream input = null;
    	try {
    		input = this.getClass().getClassLoader().getResourceAsStream(filename);
    		if(input==null){
    	            System.out.println("Sorry, unable to load the config file " + filename);
    		    return;
    		}
    		prop.load(input);
    		srcPath = prop.getProperty(SRC);
    		String entryClassOfApp= prop.getProperty(ENTRY_CLASS);
    		fullPathOfEntryclass = srcPath+"/"+entryClassOfApp;
    	    System.out.println(srcPath);
	        System.out.println(fullPathOfEntryclass);
    	} catch (IOException ex) {
    		ex.printStackTrace();
        } finally{
        	if(input!=null){
        		try {
        			input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
        	}
        }
	}


	//not reading recursively now
	private File[] listFilesRecursively(String srcPath) {
		File folder = new File(srcPath);
		File[] listOfFiles = folder.listFiles();
		    for (int i = 0; i < listOfFiles.length; i++) {
		      if (listOfFiles[i].isFile()) {
		        System.out.println("File :" + srcPath+listOfFiles[i].getName());
		      } else if (listOfFiles[i].isDirectory()) {
		        System.out.println("Directory " + listOfFiles[i].getName());
		      }
		    }
		return listOfFiles;
	}



	private void readSourceFile(final StringBuilder source, BufferedReader br) throws IOException {
		String sCurrentLine;
		while((sCurrentLine = br.readLine())!=null){
    		if(packageDefined == false || classDefined == false){
	    		if(sCurrentLine.trim().startsWith(PACKAGE_KEYWORD)){
	    			packageDefined = true;
	    			path = sCurrentLine.substring(8,sCurrentLine.length()-1);
	    		}else if(checkForKeyword(sCurrentLine,CLASS_KEYWORD)){
	    			classDefined = true;
	    			int classIndex = sCurrentLine.indexOf(CLASS_KEYWORD);
	    			className = sCurrentLine.substring(classIndex+6,sCurrentLine.length()-1);
	    			sCurrentLine = sCurrentLine+classDecorator.createPseudoCode();
	    		}
    		}
    		
    		source.append(sCurrentLine+"\n");
    	}
	}

	

	private String setFullClassName(boolean packageDefined, String defaultPath, String className, String path) {
		String fullClassName;
		if(packageDefined)
			fullClassName = path.replace('.', '/') + "/" + className;
		else
			fullClassName = defaultPath.replace('.', '/') + "/" + className;
		return fullClassName;
	}
	
	
	private static void handleException(){
		
	}
	
	private static void listFiles(String path){
        File folder = new File(path);
        File[] files = folder.listFiles();
        for (File file : files) {
            if (file.isFile()){
                System.out.println(file.getName());
            }
            else if (file.isDirectory()){
                listFiles(file.getAbsolutePath());
            }
        }
    }
	
	
	public static final void main(String... args) throws Exception {
       try{
		new GenericCodeReaderFromFile().dynamicClassCreation();
       }catch(IllegalAccessException iae){
    	   throw new RuntimeException("The class in the file is not accessible. Please define the Entry level class and method as public");
       }
	}

}