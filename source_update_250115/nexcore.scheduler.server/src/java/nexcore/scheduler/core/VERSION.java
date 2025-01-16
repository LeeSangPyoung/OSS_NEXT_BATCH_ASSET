package nexcore.scheduler.core;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;


public class VERSION implements Serializable {
	
	public static final long serialVersionUID = 4603922674624943284L;
	
//	public static String MAJOR  = "4.0";
//	public static String MINOR  = "0";
//	public static String PATCH  = "20160715";
//	
//	public static String desc   = "Scheduler Core"; 
//	
//	
//	private static boolean isEmpty(String s) {
//		return s==null ? true : s.trim().length()==0;
//	}
//	
//	public static String toFullString() {
//		return "NEXCORE Batch Scheduler [" + MAJOR + "." + MINOR + (isEmpty(PATCH) ? "" : "-"+ PATCH) + "]";
//	}
//	
//	public static String toVersionString() {
//		return "[" + MAJOR + "." + MINOR + (isEmpty(PATCH) ? "" : "-"+ PATCH) + "]";
//	}
//
//	public static void main(String[] args) {
//		System.out.println(toFullString());
//	}

	public static String toVersionString() {
		return "[" + getImplementationVersion() + "]";
	}

	private static final Attributes.Name BUILD_TIME = new Attributes.Name("Build-Time");
	
	private static String implementationVersion;
	private static String buildTime;
	
	public static String getImplementationVersion() {
		prepare();
		return implementationVersion;
	}

	public static String getBuildTime(){
		prepare();
		return buildTime;
	}
	
	public static String getBuildYear(){
		String buildTime = getBuildTime();
		if(buildTime != null && buildTime.length() > 4){
			return buildTime.substring(0, 4);
		}
		return null;
	}

	private static void prepare(){
		if(implementationVersion == null || buildTime == null){
			Manifest manifest = getManifest(VERSION.class);
			if(manifest != null){
				Attributes attrs = manifest.getMainAttributes();
				implementationVersion = attrs.getValue(Attributes.Name.IMPLEMENTATION_VERSION);
				buildTime = attrs.getValue(BUILD_TIME);
            }
		}
	}

    /**
     * 입력된 클래스를 포함하는 jar의 manifest를 조회한다.
     */
    private static Manifest getManifest(Class<?> clazz){
        JarInputStream jis = null;
        try{
            String classNameAsResource = "/" + clazz.getName().replace('.', '/') + ".class";
            String classUrl = clazz.getResource(classNameAsResource).getFile();
            String jarFileUrl = classUrl.substring(0, classUrl.indexOf(classNameAsResource));
            if(jarFileUrl.endsWith("!")) {
            	jarFileUrl = jarFileUrl.substring(0, jarFileUrl.length()-1);
            }
            URL url = new URL(jarFileUrl);
            jis = new JarInputStream(new FileInputStream(url.getFile()), false);
            return jis.getManifest();
        }catch(Exception e){
            return null;
        }finally{
            if(jis != null){
                try {
                    jis.close();
                } catch (IOException e) {
                }
            }
        }
    }
    
	public static void main(String[] args) {
		System.out.println(toVersionString());
	}

}
