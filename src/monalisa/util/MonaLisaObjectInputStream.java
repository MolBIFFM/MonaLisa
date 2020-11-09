/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */
package monalisa.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * A modified version of the {@link java.io.ObjectInputStream} to avoid
 * ClassNotFoundExeptions (of not longer existing classes) during
 * de-serialization. Found:
 * http://stackoverflow.com/questions/20543100/how-to-bypass-classnotfoundexception-while-objectinputstream-readobject
 *
 * @author Jens Einloft
 */
public class MonaLisaObjectInputStream extends ObjectInputStream {

    public MonaLisaObjectInputStream(InputStream in) throws IOException {
        super(in);

        try {
            // activating override on readObject thanks to http://stackoverflow.com/a/3301720/535203
            Field enableOverrideField = ObjectInputStream.class.getDeclaredField("enableOverride");

            enableOverrideField.setAccessible(true);

            Field fieldModifiersField = Field.class.getDeclaredField("modifiers");
            fieldModifiersField.setAccessible(true);
            fieldModifiersField.setInt(enableOverrideField, enableOverrideField.getModifiers() & ~Modifier.FINAL);

            enableOverrideField.set(this, true);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            warnCantOverride(e);
        }
    }

    private void warnCantOverride(Exception e) {
        System.err.println("Couldn't enable readObject override, won't be able to avoid ClassNotFoundException while reading InputStream");
        System.err.println(e);
    }

    @Override
    public void defaultReadObject() throws IOException, ClassNotFoundException {
        try {
            super.defaultReadObject();
        } catch (ClassNotFoundException e) {
            System.err.println("Potentially Fatal Deserialization Operation.");
            System.err.println(e);
        }
    }

    @Override
    protected Object readObjectOverride() throws IOException, ClassNotFoundException {
        try {
            int outerHandle = getObjectInputStreamFieldValue("passHandle");
            long depth = getObjectInputStreamFieldValue("depth");
            try {
                Object obj = callObjectInputStreamMethod("readObject0", new Class<?>[]{boolean.class}, false);
                Object handles = getObjectInputStreamFieldValue("handles");
                Object passHandle = getObjectInputStreamFieldValue("passHandle");
                callMethod(handles, "markDependency", new Class<?>[]{int.class, int.class}, outerHandle, passHandle);

                ClassNotFoundException ex = callMethod(handles, "lookupException", new Class<?>[]{int.class}, passHandle);

                if (ex != null) {
                    System.err.println("Avoiding exception");
                    System.err.println(ex);
                }
                if (depth == 0) {
                    callMethod(getObjectInputStreamFieldValue("vlist"), "doCallbacks", new Class<?>[]{});
                }
                return obj;
            } finally {
                getObjectInputStreamField("passHandle").setInt(this, outerHandle);
                boolean closed = getObjectInputStreamFieldValue("closed");
                if (closed && depth == 0) {
                    callObjectInputStreamMethod("clear", new Class<?>[]{});
                }
            }
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw createCantMimicReadObject(e);
        } catch (Throwable t) {
            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            }
            if (t instanceof IOException) {
                throw (IOException) t;
            }
            throw createCantMimicReadObject(t);
        }
    }

    private IllegalStateException createCantMimicReadObject(Throwable t) {
        return new IllegalStateException("Can't mimic JDK readObject method", t);
    }

    @SuppressWarnings("unchecked")
    private <T> T getObjectInputStreamFieldValue(String fieldName) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Field declaredField = getObjectInputStreamField(fieldName);
        return (T) declaredField.get(this);
    }

    private Field getObjectInputStreamField(String fieldName) throws NoSuchFieldException {
        Field declaredField = ObjectInputStream.class.getDeclaredField(fieldName);
        declaredField.setAccessible(true);
        return declaredField;
    }

    @SuppressWarnings("unchecked")
    private <T> T callObjectInputStreamMethod(String methodName, Class<?>[] parameterTypes, Object... args) throws Throwable {
        Method declaredMethod = ObjectInputStream.class.getDeclaredMethod(methodName, parameterTypes);
        declaredMethod.setAccessible(true);
        try {
            return (T) declaredMethod.invoke(this, args);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T callMethod(Object object, String methodName, Class<?>[] parameterTypes, Object... args) throws Throwable {
        Method declaredMethod = object.getClass().getDeclaredMethod(methodName, parameterTypes);
        declaredMethod.setAccessible(true);
        try {
            return (T) declaredMethod.invoke(object, args);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

}
