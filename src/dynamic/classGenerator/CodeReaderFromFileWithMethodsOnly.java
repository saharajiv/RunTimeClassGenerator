package dynamic.classGenerator;

import sun.misc.Unsafe;

import javax.tools.*;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;

import static java.util.Collections.singletonList;
import static javax.tools.JavaFileObject.Kind.SOURCE;

/**
 * by Rajib Saha
 */
public class CodeReaderFromFileWithMethodsOnly {

    public void dynamicClassCreation() throws ClassNotFoundException, IllegalAccessException, InstantiationException, URISyntaxException, NoSuchFieldException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException, IOException {
    	Class noparams[] = {};
    	String path = "com.bounded.buffer";
    	String className = "HelloWorld";
    	final StringBuilder source = new StringBuilder();
        final String fullClassName = path.replace('.', '/') + "/" + className;
    	source.append("package " + path + ";");
        source.append("public class " + className + " {\n");
    	String sCurrentLine;
    	BufferedReader br = new BufferedReader(new FileReader("resource/src/DynamicJavaFileWithMethodsOnly"));
    	while((sCurrentLine = br.readLine())!=null){
    		source.append(sCurrentLine);
    	}
    	br.close();
    	source.append("}\n");
        System.out.println(source);	
        // A byte array output stream containing the bytes that would be written to the .class file
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        final SimpleJavaFileObject simpleJavaFileObject
                = new SimpleJavaFileObject(URI.create(fullClassName + ".java"), SOURCE) {

            @Override
            public CharSequence getCharContent(boolean ignoreEncodingErrors) {
                return source;
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

        final Object o = aClass.newInstance();
        Method method = aClass.getDeclaredMethod("printIt", noparams);
        method.invoke(o, null);
        //System.out.println(o);

    }

    public static final void main(String... args) throws ClassNotFoundException, URISyntaxException, NoSuchFieldException, InstantiationException, IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException, IOException {
        new CodeReaderFromFileWithMethodsOnly().dynamicClassCreation();
    }

}