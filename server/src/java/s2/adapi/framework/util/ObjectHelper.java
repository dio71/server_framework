package s2.adapi.framework.util;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import s2.adapi.framework.vo.ValueObject;
import s2.adapi.framework.vo.ValueObjectAssembler;

/**
 * Object 관련 Helper Class
 */
public class ObjectHelper {

	private static final Logger log = LoggerFactory.getLogger(ObjectHelper.class);
	
    /**
     * <p>
     * Primitive type을 wrapping 하는 Java class 객체를 담아 놓는다.
     * </p>
     */
    private static Map<Object,Class<?>> primitiveMap = null;
    
    /**
     * ValueObjectAssembler를 파라메터로 받는 메소드를 찾기 위하여 미리 만들어 놓음.
     */
    private static ValueObjectAssembler[] arrayTypeForVOA = 
    		new ValueObjectAssembler[]{new ValueObjectAssembler()};
    
    /**
     * <p>
     * static 초기화 부분
     * </p>
     *
     */
    static {
        configure();
    }

    /**
     * <p>
     * static 초기화 함수
     * </p>
     */
    protected static void configure() {
    	primitiveMap = new HashMap<Object,Class<?>>();
    	primitiveMap.put(boolean.class, Boolean.class);
    	primitiveMap.put(byte.class, Byte.class);
    	primitiveMap.put(char.class, Character.class);
    	primitiveMap.put(double.class, Double.class);
    	primitiveMap.put(float.class, Float.class);
    	primitiveMap.put(long.class, Long.class);
    	primitiveMap.put(int.class, Integer.class);
    	
    	primitiveMap.put("boolean", boolean.class);
    	primitiveMap.put("byte", byte.class);
    	primitiveMap.put("char", char.class);
    	primitiveMap.put("double", double.class);
    	primitiveMap.put("float", float.class);
    	primitiveMap.put("long", long.class);
    	primitiveMap.put("int", int.class);
    }
    
    /**
     * <p>
     * <strong>ObjectHelper</strong>의 default 컨스트럭터(Constructor).
     * </p>
     */
    protected ObjectHelper() {
    }

    /**
     * primitive type의 클래스를 지정하면 그 Wrapper 클래스 객체를 리턴한다.
     * 해당 Wrapper가 없으면 null 을 리턴한다.
     * <pre>
     * Class wrapper = ObjectHelper.getWrapperType(int.class);
     * </pre>
     * @param primitiveClass
     * @return primitiveClass의 Wrapper 클래스
     */
    public static Class<?> getWrapperType(Class<?> primitiveClass) {
    	return primitiveMap.get(primitiveClass);
    }
    
    /**
     * primitive type 명을 지정하면 그 Wrapper 클래스 객체를 리턴한다.
     * 해당 Wrapper가 없으면 null 을 리턴한다.
     * <pre>
     * Class wrapper = ObjectHelper.getWrapperType("int");
     * </pre>
     * @param primitiveClassName
     * @return primitiveClass의 Wrapper 클래스
     */
    public static Class<?> getWrapperType(String primitiveName) {
    	return getWrapperType(primitiveMap.get(primitiveName));
    }
    
    /**
     * primitive type 명을 지정하면 해당 primitve type의 클래스 객체를 리턴한다.
     * primitive type이 아니라면 null 을 리턴한다.
     * @param primitiveName
     * @return
     */
    
    public static Class<?> getPrimitiveClass(String primitiveName) {
    	return primitiveMap.get(primitiveName);
    }
    
    /**
     * <p>
     * 오브젝트의 <code>null</code> 여부
     * </p>
     *
     * @param source 체크하고자 하는 오브젝트
     * @return <code>null</code> 여부
     */
    public static boolean isNull(Object source) {
        return (source == null) ? true : false;
    }

    /**
     * <p>
     * 오브젝트 Array의 길이
     * </p>
     *
     * <pre>
     * Object[] source = null;
     * ...
     * if(isNull(source)){
     *     System.out.println("NULL 이다!");
     * }
     * </pre>
     *
     * @param source 길이를 알고 싶은 오브젝트 Array
     * @return 해당 오브젝트 Array의 길이
     */
    public static int getLength(Object[] source) {
        if (source == null) {
            return 0;
        }
        return source.length;
    }

    /**
     * <p>
     * Class의 String Name만을 이용하여 {@link s2.adapi.framework.util.SystemHelper}의 getClassLoader를 이용하여
     * <code>ClassLoader</code>에서 load한다. 그래서 해당 Class는 Classpath에 적용되어 있어야 한다.
     * </p>
     *
     * <pre>
     * String className = "s2.adapi.framework.XXXXX";
     *
     * Class clazz = ObjectHelper.classForName(className);
     * </pre>
     *
     * @param className 로드할 클래스 이름
     * @return 로드된 <code>Class</code> 오브젝트
     * @throws ClassNotFoundException 로드할 Class를 <strong>Classpath</strong>로 적용되어 있지 않아 찾을 수 없는 경우
     */
    public static Class<?> classForName(String className) throws ClassNotFoundException {
        Class<?> clazz = null;
        try {
            clazz = SystemHelper.getClassLoader().loadClass(className);
        }
        catch (Exception e) {
            // Ignore.  Failsafe below.
        }
        
        if (clazz == null) {
            clazz = Class.forName(className);
        }
        
        return clazz;
    }

    /**
     * <p>
     * 임의로 로드된 <code>Class</code>의 <strong>이름</strong>만을 이용하여 인스턴스로 만든다.
     * </p>
     *
     * <pre>
     * String className = "s2.adapi.framework.XXXXX";
     * Object obj = ObjectHelper.instantiate(className);
     * </pre>
     *
     * @param className - 인스턴스화 하기 위한 class의 이름
     * @return class의 인스턴스
     * @throws ClassNotFoundException 해당 class를 찾지 못할 경우
     * @throws InstantiationException 해당 class를 인스턴스화 하지 못할 경우
     * @throws IllegalAccessException 해당 class의 컨스트럭쳐가 <code>public</code>으로 선언되어 있지 않거나, 접근이 불가 할 경우
     * @throws SecurityException 
     * @throws NoSuchMethodException 
     * @throws InvocationTargetException 
     * @throws IllegalArgumentException 
     */
    public static Object instantiate(String className)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
        return instantiate(classForName(className));
    }

    /**
     * <p>
     * 임의로 로드된 <strong>Class</strong>을 이용하여 인스턴스로 만든다.
     * </p>
     *
     * <pre>
     * String className = "s2.adapi.framework.XXXXX";
     * Class clazz = ObjectHelper.instantiate(className);
     * Object obj = ObjectHelper.instantiate(clazz);
     * </pre>
     *
     * @param clazz - 인스턴스화 하기 위한 class
     * @return class의 인스턴스
     * @throws InstantiationException 해당 class를 인스턴스화 하지 못할 경우
     * @throws IllegalAccessException 해당 class의 컨스트럭쳐가 <code>public</code>으로 선언되어 있지 않거나, 접근이 불가 할 경우
     * @throws SecurityException 
     * @throws NoSuchMethodException 
     * @throws InvocationTargetException 
     * @throws IllegalArgumentException 
     */
    public static Object instantiate(Class<?> clazz)
            throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
    	return clazz.getDeclaredConstructor().newInstance();
    }
 
    /**
     * <p>
     * 임의로 로드된 <code>Class</code>의 <strong>이름</strong>을 이용하여 인스턴스로 만든다.
     * 인스턴스 생성시 <code>argTypes</code>에 해당되는 파라메터를 같는 생성자를 찾아서 
     * 여기에 <code>argValues</code>를 파라메터로 하여 인스턴스를 생성한다.
     *
     * <code>argTypes</code> 인자가 null 인 경우에는 <code>argValues</code> 인자의 <code>getClass()</code>
     * 메소드를 호출하여 얻은 <code>Class</code> 오브젝트가 사용된다. 
     * </p>
     *
     * <pre>
     * String className = "s2.adapi.framework.XXXXX";
     * Object obj = ObjectHelper.instantiate(className,new Class[]{String.class}, new Object[] {"aaa"} );
     * </pre>
     *
     * @param className - 인스턴스화 하기 위한 class의 이름
     * @param argTypes - 생성자의 파라메터 타입
     * @param argValues - 생성자로 인스턴스를 생성할 때 사용할 넘겨줄 파라메터
     * @return class의 인스턴스
     * @throws ClassNotFoundException 해당 class를 찾지 못할 경우
     * @throws InstantiationException 해당 class를 인스턴스화 하지 못할 경우
     * @throws IllegalAccessException 해당 class의 컨스트럭쳐가 <code>public</code>으로 선언되어 있지 않거나, 접근이 불가 할 경우
     * @throws NoSuchMethodException  파라메터 타입에 해당되는 생성자가 존재하지 않는 경우
     * @throws IllegalArgumentException 파라메터 타입과 파라메터 값의 개수나 타입이 상이할 경우
     */      
    public static Object instantiate(String className, Class<?>[] argTypes, Object[] argValues) 
            throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    	Class<?> clazz = classForName(className);
    	return instantiate(clazz,argTypes,argValues);
    }
    
    /**
     * <p>
     * 임의로 로드된 <code>Class</code>를 이용하여 인스턴스로 만든다.
     * 인스턴스 생성시 <code>argTypes</code>에 해당되는 파라메터를 같는 생성자를 찾아서 
     * 여기에 <code>argValues</code>를 파라메터로 하여 인스턴스를 생성한다.
     *
     * <code>argTypes</code> 인자가 null 인 경우에는 <code>argValues</code> 인자의 <code>getClass()</code>
     * 메소드를 호출하여 얻은 <code>Class</code> 오브젝트가 사용된다. 
     * </p>
     *
     * <pre>
     * String className = "s2.adapi.framework.XXXXX";
     * Class clazz = ObjectHelper.classForName(className);
     * Object obj = ObjectHelper.instantiate(clazz,new Class[]{String.class}, new Object[] {"aaa"} );
     * </pre>
     *
     * @param clazz - 인스턴스화 하기 위한 class 객체
     * @param argTypes - 생성자의 파라메터 타입
     * @param argValues - 생성자로 인스턴스를 생성할 때 사용할 넘겨줄 파라메터
     * @return class의 인스턴스
     * @throws ClassNotFoundException 해당 class를 찾지 못할 경우
     * @throws InstantiationException 해당 class를 인스턴스화 하지 못할 경우
     * @throws IllegalAccessException 해당 class의 컨스트럭쳐가 <code>public</code>으로 선언되어 있지 않거나, 접근이 불가 할 경우
     * @throws NoSuchMethodException  파라메터 타입에 해당되는 생성자가 존재하지 않는 경우
     * @throws IllegalArgumentException 파라메터 타입과 파라메터 값의 개수나 타입이 상이할 경우
     */  
    public static Object instantiate(Class<?> clazz, Class<?>[] argTypes, Object[] argValues) 
           throws NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
	    
	    if ( argTypes == null && argValues == null ) {
		    return instantiate(clazz); // clazz.newInstance();
	    }
	    
	    if (argTypes == null && argValues != null) {

            argTypes = new Class[argValues.length];

            for (int i = 0; i < argValues.length; ++i) {
                if (argValues[i] == null) {
                    argTypes[i] = java.lang.Object.class;
                } else {
                    argTypes[i] = argValues[i].getClass();
                }
            }
        }
        
	    Constructor<?> constructor = clazz.getConstructor(argTypes);
	    
	    return constructor.newInstance(argValues);
    }
    
    /**
     * <p>
     * 임의로 로드된 <code>Class</code>의 <strong>이름</strong>을 이용하여 인스턴스로 만든다.
     * 이때 <code>argValues</code> 인자의 클래스를 사용하여 생성자를 찾아서 인스턴스를 생성한다.
     * </p>
     *
     * <pre>
     * String className = "s2.adapi.framework.XXXXX";
     * Object obj = ObjectHelper.instantiate(className, new Object[] {"aaa"} );
     * </pre>
     *
     * @param className - 인스턴스화 하기 위한 class의 이름
     * @param argValues - 생성자로 인스턴스를 생성할 때 사용할 넘겨줄 파라메터
     * @return class의 인스턴스
     * @throws ClassNotFoundException 해당 class를 찾지 못할 경우
     * @throws InstantiationException 해당 class를 인스턴스화 하지 못할 경우
     * @throws IllegalAccessException 해당 class의 컨스트럭쳐가 <code>public</code>으로 선언되어 있지 않거나, 접근이 불가 할 경우
     * @throws NoSuchMethodException  파라메터 타입에 해당되는 생성자가 존재하지 않는 경우
     * @throws IllegalArgumentException 파라메터 타입과 파라메터 값의 개수나 타입이 상이할 경우
     */  
    public static Object instantiate(String className, Object[] argValues) 
            throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    	return instantiate(classForName(className), null, argValues);
    }
    
    /**
     * <p>
     * 임의로 로드된 <code>Class</code>를 이용하여 인스턴스로 만든다.
     * 이때 <code>argValues</code> 인자의 클래스를 사용하여 생성자를 찾아서 인스턴스를 생성한다.
     * </p>
     *
     * <pre>
     * String className = "s2.adapi.framework.XXXXX";
     * Class clazz = ObjectHelper.classForName(className);
     * Object obj = ObjectHelper.instantiate(clazz, new Object[] {"aaa"} );
     * </pre>
     *
     * @param clazz - 인스턴스화 하기 위한 class 객체
     * @param argValues - 생성자로 인스턴스를 생성할 때 사용할 넘겨줄 파라메터
     * @return class의 인스턴스
     * @throws ClassNotFoundException 해당 class를 찾지 못할 경우
     * @throws InstantiationException 해당 class를 인스턴스화 하지 못할 경우
     * @throws IllegalAccessException 해당 class의 컨스트럭쳐가 <code>public</code>으로 선언되어 있지 않거나, 접근이 불가 할 경우
     * @throws NoSuchMethodException  파라메터 타입에 해당되는 생성자가 존재하지 않는 경우
     * @throws IllegalArgumentException 파라메터 타입과 파라메터 값의 개수나 타입이 상이할 경우
     */    
    public static Object instantiate(Class<?> clazz, Object[] argValues) 
    		throws NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    	return instantiate(clazz, null, argValues);
    }
    
    /**
     * 해당 클래스가 구현하는 모든 Interface들을 찾아서 배열로 반환한다.
     * @param clazz
     * @return
     */
    public static Class<?>[] getAllInterfacesOf(Class<?> clazz) {

		if (clazz.isInterface()) {
			return new Class[] {clazz};
		}
		
		Set<Class<?>> ret = new HashSet<Class<?>>();
		while (clazz != null) {
			Class<?>[] infs = clazz.getInterfaces();
			for (int i=0; i<infs.length; i++) {
				ret.add(infs[i]);
			}
			clazz = clazz.getSuperclass();
		}
		
		return (Class<?>[]) ret.toArray(new Class[ret.size()]);
	}
    
    /**
     * reflection을 사용하여 Object의 메소드 호출을 수행한다.
     * @param className
     * @param obj
     * @param methodName
     * @param argTypes
     * @param argValues
     * @return
     * @throws Throwable
     */
    public static Object invoke(String className, Object obj, String methodName,
            Class<?>[] argTypes, Object[] argValues) throws Throwable {
    	Class<?> clazz = null;
    	if (className == null) {
    		clazz = obj.getClass();
    	} else {
    		clazz = SystemHelper.getClassLoader().loadClass(className);
    	}
    	return invoke(clazz,obj,methodName,argTypes,argValues);
    }
    
    /**
     * reflection을 사용하여 Object의 메소드 호출을 수행한다.
     * @param obj
     * @param methodName
     * @param argTypes
     * @param argValues
     * @return
     * @throws Throwable
     */
    public static Object invoke(Object obj, String methodName,
            Class<?>[] argTypes, Object[] argValues) throws Throwable {
    	return invoke((Class<?>)null,obj,methodName,argTypes,argValues);
    }
    
    /**
     * reflection을 사용하여 Object의 메소드 호출을 수행한다.
     * @param clazz
     * @param obj
     * @param methodName
     * @param argTypes
     * @param argValues
     * @return
     * @throws Throwable
     */
    public static Object invoke(Class<?> clazz, Object obj, String methodName,
            Class<?>[] argTypes, Object[] argValues) throws Throwable {
    	return invoke(clazz,obj,methodName,argTypes,argValues,false);
    }
    
    /**
     * reflection을 사용하여 Object의 메소드 호출을 수행한다.
     * force가 true 인 경우 파라메터 타입이 다르더라도 강제적인 변환을 시도하여
     * 변환이 가능한 경우 그 메소드를 호출한다.
     * @param clazz
     * @param obj
     * @param methodName
     * @param argTypes
     * @param argValues
     * @param force
     * @return
     * @throws Throwable
     */
    public static Object invoke(Class<?> clazz, Object obj, String methodName,
            Class<?>[] argTypes, Object[] argValues, boolean force) throws Throwable {

    	Method method = null;

    	if (clazz == null) {
			clazz = obj.getClass();
    	}
		
		if (argTypes == null && argValues != null) {

			argTypes = new Class[argValues.length];

			for (int i = 0; i < argValues.length; ++i) {
				if (argValues[i] == null) {
					argTypes[i] = java.lang.Object.class;
				} else {
					argTypes[i] = argValues[i].getClass();
				}
			}
		}
		
		try {
			long stime = System.currentTimeMillis();

			if (force) {
				method = getMethod(clazz,methodName,argValues,true);
				if (method != null) {
					// 해당 메소드 존재함.
					Class<?>[] toTypes = method.getParameterTypes();
					for(int i=0;i<toTypes.length;i++) {
						argValues[i] = convertType(argValues[i],toTypes[i]);
					}
				}
			} else {
				method = getMethod(clazz,methodName,argValues,false);
			}
			
			if (method == null) {
				
				// 해당 메소드가 존재하지 않음
				// 입력 파라메터가 VO 배열인 경우에는  ValueObjectAssembler를 파라메터로 받는 메소드가 있는지 다시 확인
				if (argValues instanceof ValueObject[]) {
					//method = getMethod(clazz, methodName, new Class<?>[]{ValueObjectAssembler.class},false);
					method = getMethod(clazz, methodName, arrayTypeForVOA, false);
					if (method != null) {
						//System.out.println("assign ValueObject[] to ValueObjectAssembler..OK");
						ValueObject[] voList = (ValueObject[])argValues;
						ValueObjectAssembler reqVOs = new ValueObjectAssembler();
						for(int i=0;i<voList.length;i++) {
							reqVOs.set(StringHelper.null2string(voList[i].getName(),String.valueOf(i)), voList[i]);
						}
						argValues = new Object[]{reqVOs};
					}
				}
			}
			
			if (method == null) {
				throw new NoSuchMethodException(unwrapProxyName(clazz)+"."+methodName+argumentTypesToString(argTypes));
			}
			
			if (log.isDebugEnabled()) {
				long etime = System.currentTimeMillis();
				log.debug("getMethod() takes "+(etime-stime)+" msec.");
			}
			
			return method.invoke(obj, argValues);
			
		} catch (InvocationTargetException ex) {
			Throwable tr = ex.getCause();
			throw tr;
		} catch (Exception ex) {
			throw ex;
		}

    }	
    
    /**
     * Class.getMethod()를 사용하면 입력 파라메터 타입이 정확히 일치되는 메소드만 찾아준다.
     * 파라메터가 다르더라도 casting이 가능한 타입이라면 reflection으로 메소드 호출하는데 문제가 없으므로
     * 파라메터간의 casting 가능여부를 고려하여 메소드를 찾아주는 기능을 제공한다.
     * @param clazz
     * @param methodName
     * @param argTypes
     * @param force 강제적인 타입변환 여부까지 확인할 지 여부
     * @return
     */
    public static Method getMethod(Class<?> clazz, String methodName, Object[] argValues, boolean force) {
		
		Method targetMethod = null;
		
		// 파라메터 개수 확인용
		int numParam = 0;
		if (argValues != null) {
			numParam = argValues.length;
		}

		Method[] methods = clazz.getMethods();
		
		for(int i=0;i<methods.length;i++) {
			// check method name
			if (!methods[i].getName().equals(methodName)) {
				continue;
			}
			
			Class<?>[] toTypes = methods[i].getParameterTypes();
			if ( toTypes.length != numParam) {
				// 입력 파라메터와 메소드의 파라메터 수가 다른 경우
				continue;
			} else if (numParam == 0) {
				// 파라메터 수가 양쪽 모두 0으로 일치하므로 파라메터 타입 확인할 필요없이 메소드 리턴
				targetMethod = methods[i];
				break;
			} else {
				// 파라메터 수가 서로 같으므로 순서대로 파라메터 확인
				// check parameter is assignable
				boolean ok = true;
				for(int j=0;j<toTypes.length;j++) {
					//System.out.println("check assignable from "+argValues[j].getClass().getName()+" to "+toTypes[j]);
					if ( !isAssignable(argValues[j],toTypes[j],force)) {
						ok = false;
						//System.out.println("assignable Fail");
						break;
					}
					//System.out.println("assignable OK");
				}
				
				if (ok) {
					targetMethod = methods[i];
					break;
				}
			}
		}

		return targetMethod;
    }
    
    /**
     * from 객체를 to 클래스로 변환하여 반환한다.
     * @param from
     * @param to
     * @return ClassCastException 변환이 불가능한 경우
     */
    public static Object convertType(Object from, Class<?> to) {
    	if (to == ValueObjectAssembler.class) {
    		if (from instanceof ValueObjectAssembler) {
    			return (ValueObjectAssembler)from;
    		} else {
    			if (from instanceof Map<?,?>) {
					Map<?,?> m = (Map<?,?>)from;
					if (m.size() == 0) {
						return new ValueObjectAssembler(); // return emtpy VOA
					} else {
						ValueObjectAssembler voa = new ValueObjectAssembler();
						Set<?> keySet = m.keySet();
						Iterator<?> keyItor = keySet.iterator();
						while(keyItor.hasNext()) {
							Object key = keyItor.next();
							Object value = m.get(key);
							voa.put(String.valueOf(key), ValueObjectUtil.build(value));
						}
						return voa;
					}
				} else {
					// 무조건 casting 하여 반환
					return (ValueObjectAssembler)from;
				}
    		}
    	} else if (to == ValueObject.class) {
    		return ValueObjectUtil.build(from);
    	} else {
    		//System.out.println("from:"+from.getClass().getName()+",to:"+to.getName());
    		Class<?> fromClass = from.getClass();
    		if ((to == Integer.TYPE && Integer.class.isAssignableFrom(fromClass)) ||
		         (to == Double.TYPE && Double.class.isAssignableFrom(fromClass)) ||
		         (to == Long.TYPE && Long.class.isAssignableFrom(fromClass)) ||
		         (to == Boolean.TYPE && Boolean.class.isAssignableFrom(fromClass)) ||
		         (to == Character.TYPE && Character.class.isAssignableFrom(fromClass)) ||
		         (to == Float.TYPE && Float.class.isAssignableFrom(fromClass)) ||
		         (to == Short.TYPE && Short.class.isAssignableFrom(fromClass)) ||
		         (to == Byte.TYPE && Byte.class.isAssignableFrom(fromClass))) {
    			return from; // 그대로 리턴
    		} else {
    			return to.cast(from);
    		}
    	}
    }
    
    /**
     * from 클래스가 to 클래스로 변환이 가능한지 여부를 반환한다.
     * force 파라메터가 false이면 implicity conversion 가능여부만 판단하며,
     * force 파라메터가 true이면 impllicity conversion이 불가능해도 몇가지 변환을 시도하여
     * 그 가능 여부를 반환한다.
     * @param from 변환하고자할 객체
     * @param to 변환하고자할 타입
     * @param force 강제 변환 여부
     * @return
     */
    public static boolean isAssignable(Object from, Class<?> to, boolean force) {
    	//System.out.println("check assignable "+(from!=null?from.getClass().getName():"null")+" to " + to.getName());
    	
    	if (from == null) {
    		return true;
    	}
    	
    	Class<?> fromClass = from.getClass();
		if ((to.isAssignableFrom(fromClass)) ||
	         (to == Integer.TYPE && Integer.class.isAssignableFrom(fromClass)) ||
	         (to == Double.TYPE && Double.class.isAssignableFrom(fromClass)) ||
	         (to == Long.TYPE && Long.class.isAssignableFrom(fromClass)) ||
	         (to == Boolean.TYPE && Boolean.class.isAssignableFrom(fromClass)) ||
	         (to == Character.TYPE && Character.class.isAssignableFrom(fromClass)) ||
	         (to == Float.TYPE && Float.class.isAssignableFrom(fromClass)) ||
	         (to == Short.TYPE && Short.class.isAssignableFrom(fromClass)) ||
	         (to == Byte.TYPE && Byte.class.isAssignableFrom(fromClass))) {
			return true;
		}
		
		if (force) {
			// ValueObjectAssembler 부터 확인한다.
			if (to == ValueObjectAssembler.class) {
				// Object --> ValueObjectAssembler 변환 가능 여부 확인
				if (from instanceof Map<?,?>) {
					Map<?,?> m = (Map<?,?>)from;
					if (m.size() == 0) {
						return true;
					} else {
						// Map에 저장된 객체들이 ValueObject로 변환 가능한지 확인
						Collection<?> c = m.values();
						Iterator<?> itor = c.iterator();
						while(itor.hasNext()) {
							Object o = itor.next();
							if (!ValueObjectUtil.isAssignable(o)) {
								return false;
							}
						}
						return true;
					}
				} else {
					return false;
				}
			} else if (to == ValueObject.class) {
				// Object --> ValueObject 변환 가능 여부 확인
				return ValueObjectUtil.isAssignable(from);
			}
		}
			
		return false;
	}
    
    /**
     * obj 가 담고 있는 데이터의 건수를 n 단계까지 탐색하여 반환한다.
     * @param obj
     * @return
     */
    public static int getCount(Object obj, int n) {
    	int count = 0;
    	
    	if (obj == null) {
    		return 0;
    	}
    	
		if (obj instanceof Object[]) {
			Object[] oa = (Object[])obj;
			if (n > 1) {
				for(int i=0;i<oa.length;i++) {
					count += getCount(oa[i],n-1);
				}
			} else {
				count = oa.length;
			}
		} else if (obj.getClass().isArray()) {
			// primitive array
			count = Array.getLength(obj);
		} else if (obj instanceof List<?>) {
			List<?> ol = (List<?>)obj;
			if (n > 1) {
				for(int i=0;i<ol.size();i++) {
					count += getCount(ol.get(i),n-1);
				}
			} else {
				count = ol.size();
			}
		} else if (obj instanceof Map<?,?>) {
			Map<?,?> om = (Map<?,?>)obj;
			if (n > 1) {
				Iterator<?> itor = om.values().iterator();
				while(itor.hasNext()) {
					Object o = itor.next();
					count += getCount(o,n-1);
				}
			} else {
				count = 1;
			}
		} else {
			count = 1;
		}
		
		return count;
    }
    
    /**
     * 자신을 호출한 클래스명을 반환한다.
     * 
     * @return
     */
    public static String getCallerClassName() {
    	StackTraceElement ste[] = new Exception().getStackTrace();
    	
    	if (ste != null && ste.length > 2) {
    		return ste[2].getClassName();
    	}
    	else {
    		return null;
    	}
    }
    
    private static String argumentTypesToString(Class<?>[] argTypes) {
        StringBuilder buf = new StringBuilder();
        buf.append("(");
        if (argTypes != null) {
            for (int i = 0; i < argTypes.length; i++) {
                if (i > 0) {
                    buf.append(", ");
                }
                Class<?> c = argTypes[i];
                buf.append((c == null) ? "null" : c.getName());
            }
        }
        buf.append(")");
        return buf.toString();
    }
    
    /**
     * Proxy 클래스인 경우 그 클래스가 구현한 인터페이스 클래스 명칭을 반환한다.
     * 구현한 인터페이스가 2개 이상인 경우에는 Proxy 클래스 명을 반환한다.
     * @param clazz
     * @return
     */
    private static String unwrapProxyName(Class<?> clazz) {
    	if (Proxy.isProxyClass(clazz)) {
    		Class<?>[] infs = clazz.getInterfaces();
    		if (infs.length == 1) {
    			return infs[0].getName();
    		} else {
    			return clazz.getName();
    		}
    	} else {
    		return clazz.getName();
    	}
    }
}
