package org.apache.olingo.odata2.jpa.processor.core.access.data;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

public class ReflectionUtil {

  public static Method getMethod(Object o, String name) throws NoSuchMethodException {
    if (o != null) {
      Class clazz = o instanceof Class ? (Class) o : o.getClass();

      for (Method m: clazz.getMethods()) {
        if (m.getName().equalsIgnoreCase(name)) {
          return m;
        }
      }

      for (Method m: clazz.getDeclaredMethods()) {
        if (m.getName().equalsIgnoreCase(name)) {
          return m;
        }
      }
    }

    throw new NoSuchMethodException("No such Method");
  }

  public static Field getField(Object o, String name) throws NoSuchFieldException {
    if (o != null) {
      Class clazz = o instanceof Class ? (Class) o : o.getClass();

      for (Field f: clazz.getFields()) {
        if (f.getName().equalsIgnoreCase(name)) {
          return f;
        }
      }

      for (Field f: clazz.getDeclaredFields()) {
        if (f.getName().equalsIgnoreCase(name)) {
          return f;
        }
      }
    }

    throw new NoSuchFieldException("No such Field");
  }

  public static Object getter(Object o, String name) throws NoSuchFieldException {
    if (o instanceof Map) {
      return ((Map) o).get(name);
    }

    if (o instanceof VirtualClassInterface) {
      return ((VirtualClassInterface) o).get(name);
    }
    try {
      Method method = getMethod(o, "get"+name);
      if (method != null) {
        return method.invoke(o);
      }
    } catch (Exception e) {
       //NoCommand
    }

    try {
      Field field = getField(o, name);
      if (field != null) {
        field.setAccessible(true);
        return field.get(o);
      }
    } catch (Exception e) {
      //NoCommand
    }


    throw new NoSuchFieldException("No such Field");

  }

}
