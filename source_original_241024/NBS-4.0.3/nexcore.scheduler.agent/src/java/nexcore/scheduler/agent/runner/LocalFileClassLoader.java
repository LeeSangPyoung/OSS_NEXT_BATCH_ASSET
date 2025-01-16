package nexcore.scheduler.agent.runner;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 공통모듈 - 유틸리티</li>
 * <li>설  명 : custom 클래스로더. 지정된 디렉토리 밑에서 *.class 파일을 찾아 로드함.</li>
 * <li>작성일 : 2009. 11. 25</li>
 * <li>작성자 : 정호철</li>
 * 
 * </ul>
 */ // 온라인 core 에서 복사하여 배치는 따로 관리함.
public class LocalFileClassLoader extends ClassLoader {
    class ClassFileInfo {
        private String  className;
        private File    classFile;
        private int     classFileSize;
        private long    loadedTime;
        
        ClassFileInfo(String className, File classFile, int classFileSize) {
            this.className      = className;
            this.classFile      = classFile;
            this.classFileSize  = classFileSize;
            this.loadedTime     = System.currentTimeMillis();
        }

        public String getClassName() {
            return this.className;
        }

        public File getClassFile() {
            return this.classFile;
        }

        public int getClassFileSize() {
            return this.classFileSize;
        }
        
        public long getClassFileTime() {
        	return this.classFile.lastModified();
        }

        public long getLoadedTime() {
            return this.loadedTime;
        }
    }
    
    private HashMap/*<String:name, ClassFileInfo:entry> */ loadedClassFiles = new HashMap();
    
    /*
     * HashMap의 keySet().iterator()를 하지 않기 위해 class name 리스트를 별도 List 로 관리한다.
     * 왜냐면, keySet().iterator() 를 하려면 synchronized(HashMap) 를 걸어야하므로 비효율적일 수 있다.
     */
    private List   /*<String:name> */ loadedClassnames = new ArrayList();
    
	private File[] classDirs;  // 여기 지정되는 디렉토리는 CLASSPATH 에 설정되지 않은 디렉토리이어야 함.

	
	// 사용법
	/*
	 *  LocalFileClassLoader mycl = new LocalFileClassLoader("/home/nexcore/bizunits/");
	 *  Class bizUnitClass = Class.forName("test.general.sample.biz.AAAUnit", false, mycl);
	 *  bizUnitClass.newInstance();
	 *  
	 *  ...
	 *  
	 *  위와 같이 하면 AAAUnit 이라는 클래스를 /home/nexcore/bizunits/test/general/sample/biz/AAAUnit.class 파일로 부터 로드함.
	 *  Class.forName() 을 할때마다 파일에서 다시 로드하기 때문에 클래스 파일 변경분을 별도 배포 과정없이 즉시 즉시 로드할 수 있음 
	 */
	
	// ----------------------------------------------------
	public LocalFileClassLoader(File dir) {
		super(LocalFileClassLoader.class.getClassLoader()); // 부모 클래스 로더 지정.
		classDirs = new File[]{dir};
	}

	public LocalFileClassLoader(String dir) {
		super(LocalFileClassLoader.class.getClassLoader());
		classDirs = new File[]{new File(dir)};
	}

    public LocalFileClassLoader(String[] dirs) {
        super(LocalFileClassLoader.class.getClassLoader());
        if (dirs.length == 0 || dirs == null) {
            throw new RuntimeException("Class base directories is not set.");
        }
        classDirs = new File[dirs.length];
        for (int i=0; i<dirs.length; i++) {
            classDirs[i] = new File(dirs[i]);
        }
    }

	public void finalize() throws Throwable {
		super.finalize();
		destroy();
	}
	
	public void destroy() {
	    if (loadedClassFiles != null) loadedClassFiles.clear();
	    if (loadedClassnames != null) loadedClassnames.clear();
	    classDirs = null;
	}
	
	public Class findClass(String name) throws ClassNotFoundException {
		String classFileName = name.replace('.', '/');
		for (int i=0; i<classDirs.length; i++) {
    		File classFile = new File(classDirs[i], classFileName+".class");
    		if (!classFile.exists()) {
    		    continue;
    		}
    		int classFileSize = (int)classFile.length();
    		byte[] classbytes = new byte[classFileSize];
    		DataInputStream in = null;
    		try {
    			in = new DataInputStream(new FileInputStream(classFile));
    			in.readFully(classbytes);
    		}catch(Exception e) {
    			throw new ClassNotFoundException(classDirs[i].getAbsolutePath()+"/"+classFileName, e);
    		}finally {
    			try {in.close();}catch(Exception ignore) {}
    		}
    		synchronized(loadedClassFiles) {
    		    loadedClassFiles.put(name, new ClassFileInfo(name, classFile, classFileSize));
    		    loadedClassnames.add(name);
    		}
    		
    		Class c = defineClass(name, classbytes, 0, classbytes.length);
    		return c;
		}
		// 모든디렉토리에 해당 클래스가 없는 경우
		throw new ClassNotFoundException(Arrays.toString(classDirs)+"/"+classFileName);
	}
	
	public File[] getDirectories() {
		return classDirs;
	}
	
	public File getLoadedClassFile(String name) {
	    return ((ClassFileInfo)loadedClassFiles.get(name)).getClassFile();
	}
    public int getLoadedClassFileSize(String name) {
        return ((ClassFileInfo)loadedClassFiles.get(name)).getClassFileSize();
    }
    public long getLoadedClassFileTime(String name) {
    	return ((ClassFileInfo)loadedClassFiles.get(name)).getClassFileTime();
    }
    
    public long getLoadedTime(String name) {
        return ((ClassFileInfo)loadedClassFiles.get(name)).getLoadedTime();
    }
    // 로드한 클래스 중에 변경된 클래스가 하나라도 있는지?
    public boolean constainsModifiedClassFile() {
        // loadedClassFiles map 객체를 iterate 하면서 할 수도 있지만, 그렇게 하기 위해서는 map 에 synchronized 를 걸어야한다. 대안으로 classname을 별도의 list 로 따로 관리한다. 
        for (int i=0; i<loadedClassnames.size(); i++) {
            try {
                String className = (String)loadedClassnames.get(i);
                ClassFileInfo classFileInfo = (ClassFileInfo)loadedClassFiles.get(className);
                if ( !classFileInfo.getClassFile().exists() ) {
                    return true; // 파일 삭제된 경우도 reload 대상이다.
                }else if ( classFileInfo.getClassFileTime() > classFileInfo.getLoadedTime()) {
                    return true;
                }
            }catch (IndexOutOfBoundsException e) {
                // ignore
            }
        }
        return false;
    }
    
    // -- 배치에서 xsql 파일 로드시 사용됨.
    public URL findResource(String name) {
        for (int i=0; i<classDirs.length; i++) {
            File resFile = new File(classDirs[i], name);
            if (resFile.exists()) {
                try {
                    return resFile.toURI().toURL();
                } catch (MalformedURLException e) {
                    return null;
                }
            }
        }
        // 모든디렉토리에 해당 리소드가 없는 경우
        return null;
    }

}
