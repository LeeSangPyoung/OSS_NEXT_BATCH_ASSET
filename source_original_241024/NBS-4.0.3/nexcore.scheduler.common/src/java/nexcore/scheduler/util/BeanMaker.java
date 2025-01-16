package nexcore.scheduler.util;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;


public class BeanMaker {

	public static Object getValue(Object array) {
		if (array.getClass().isArray()) {
			if (Array.getLength(array) > 0) {
				return Array.get(array, 0);
			}else {
				return null;
			}
		}else {
			return array;
		}
	}
	
	public static void makeFromHttpParameter(Map httpParameterMap, Object beanObject) {
		
		Class beanClass = beanObject.getClass();
		
		Iterator iter = httpParameterMap.entrySet().iterator();
		while(iter.hasNext()) {
			Map.Entry entry = (Map.Entry)iter.next();
			
			String param = (String)entry.getKey();
			if (Util.isBlank(param)) continue;
			String methodName = "set"+param.substring(0,1).toUpperCase()+param.substring(1);
			
			Method m = null;
			Object setValue = null;
			
			// String 타입인경우
			try {
				m = beanClass.getMethod(methodName, new Class[]{String.class});
				setValue = Util.trimIfNotNull(getValue(entry.getValue()));
				m.invoke(beanObject, new Object[]{setValue});
				continue;
			}catch (NoSuchMethodException e) {
				// ignore. 다음 타입 체크
			}catch (Exception e) {
				throw Util.toRuntimeException(e);
			}

			// int 타입인 경우
			try {
				m = beanClass.getMethod(methodName, new Class[]{Integer.TYPE});
				setValue = Integer.valueOf((String)getValue(entry.getValue()));
				m.invoke(beanObject, new Object[]{setValue});
				continue;
			}catch (NoSuchMethodException e) {
				// ignore. 다음 타입 체크
			}catch (NumberFormatException e) {
				// 숫자 타입에 값이 null 이 오면 그냥 0으로 둔다.
			}catch (Exception e) {
				throw Util.toRuntimeException(e);
			}

			// long 타입인 경우
			try {
				m = beanClass.getMethod(methodName, new Class[]{Long.TYPE});
				setValue = Long.valueOf((String)getValue(entry.getValue()));
				m.invoke(beanObject, new Object[]{setValue});
				continue;
			}catch (NoSuchMethodException e) {
				// ignore. 다음 타입 체크
			}catch (NumberFormatException e) {
				// 숫자 타입에 값이 null 이 오면 그냥 0으로 둔다.
			}catch (Exception e) {
				throw Util.toRuntimeException(e);
			}

			// boolean 타입인 경우
			try {
				m = beanClass.getMethod(methodName, new Class[]{Boolean.TYPE});
				setValue = Boolean.valueOf((String)getValue(entry.getValue()));
				m.invoke(beanObject, new Object[]{setValue});
				continue;
			}catch (NoSuchMethodException e) {
				// ignore. 다음 타입 체크
			}catch (Exception e) {
				throw Util.toRuntimeException(e);
			}

			// double 타입인 경우
			try {
				m = beanClass.getMethod(methodName, new Class[]{Double.TYPE});
				setValue = Double.valueOf((String)getValue(entry.getValue()));
				m.invoke(beanObject, new Object[]{setValue});
				continue;
			}catch (NoSuchMethodException e) {
				// ignore. 다음 타입 체크
			}catch (NumberFormatException e) {
				// 숫자 타입에 값이 null 이 오면 그냥 0으로 둔다.
			}catch (Exception e) {
				throw Util.toRuntimeException(e);
			}

			// float 타입인 경우
			try {
				m = beanClass.getMethod(methodName, new Class[]{Float.TYPE});
				setValue = Float.valueOf((String)getValue(entry.getValue()));
				m.invoke(beanObject, new Object[]{setValue});
				continue;
			}catch (NoSuchMethodException e) {
				// ignore. 다음 타입 체크
			}catch (NumberFormatException e) {
				// 숫자 타입에 값이 null 이 오면 그냥 0으로 둔다.
			}catch (Exception e) {
				throw Util.toRuntimeException(e);
			}

		}
	}
}
