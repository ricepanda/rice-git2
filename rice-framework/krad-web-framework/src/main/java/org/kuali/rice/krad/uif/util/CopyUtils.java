/**
 * Copyright 2005-2014 The Kuali Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl2.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.rice.krad.uif.util;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.WeakHashMap;

import org.kuali.rice.core.api.config.property.Config;
import org.kuali.rice.core.api.config.property.ConfigContext;
import org.kuali.rice.krad.datadictionary.Copyable;
import org.kuali.rice.krad.uif.component.DelayedCopy;
import org.kuali.rice.krad.uif.component.ReferenceCopy;
import org.kuali.rice.krad.uif.lifecycle.ViewLifecycle;
import org.kuali.rice.krad.uif.lifecycle.ViewLifecyclePhase;
import org.kuali.rice.krad.uif.lifecycle.ViewLifecycleTask;
import org.kuali.rice.krad.util.KRADConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a lightweight "hands-free" copy implementation to replace the need for copyProperties()
 * in building {@link LifecycleElement} implementations.
 * 
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public final class CopyUtils {

    private static Logger LOG = LoggerFactory.getLogger(CopyUtils.class);

    private static Boolean delay;

    /**
     * Determine whether or not to use a delayed copy proxy.
     * 
     * <p>
     * When true, and {@link #isUseClone()} is also true, then deep copy operations will be
     * truncated where a copyable represented by an interfaces is specified by the field, array,
     * list or map involved indicated. Rather than copy the object directly, a proxy wrapping the
     * original will be placed, which when used will invoke the copy operation.
     * </p>
     * 
     * <p>
     * This value is controlled by the parameter &quot;krad.uif.copyable.delay&quot;. By default,
     * full deep copy will be used.
     * </p>
     * 
     * @return True if deep copy will be truncated with a delayed copy proxy, false for full deep
     *         copy.
     */
    public static boolean isDelay() {
        if (delay == null) {
            boolean defaultDelay = false;
            Config config = ConfigContext.getCurrentContextConfig();
            delay = config == null ? defaultDelay : config.getBooleanProperty(
                    KRADConstants.ConfigParameters.KRAD_COPY_DELAY, defaultDelay);
        }

        return delay;
    }

    /**
     * Mix-in copy implementation for objects that implement the {@link Copyable} interface}
     * 
     * @param <T> copyable type
     * @param obj The object to copy.
     * @return A deep copy of the object.
     */
    @SuppressWarnings("unchecked")
    public static <T> T copy(Copyable obj) {
        if (obj == null) {
            return null;
        }

        String cid = null;
        if (ViewLifecycle.isTrace()) {
            StackTraceElement[] trace = Thread.currentThread().getStackTrace();
            int i = 3;
            while (ComponentUtils.class.getName().equals(trace[i].getClassName()))
                i++;
            StackTraceElement caller = trace[i];
            cid = obj.getClass().getSimpleName() + ":" + caller.getClassName()
                    + ":" + caller.getMethodName() + ":" + caller.getLineNumber();
            ProcessLogger.ntrace("deep-copy:", ":" + cid, 1000);
        }

        return (T) getDeepCopy(obj);
    }

    /**
     * Determine if shallow copying is available on an object.
     * 
     * @param <T> copyable type
     * @param obj The object to check.
     * @return True if {@link #getShallowCopy(Object)} may be expected to return a shallow copy of
     *         the object. False if a null return value is expected.
     */
    public static <T> boolean isShallowCopyAvailable(T obj) {
        return obj != null &&
                (isDeepCopyAvailable(obj.getClass()) ||
                getMetadata(obj.getClass()).cloneMethod != null);
    }

    /**
     * Determine if deep copying is available for a type.
     * 
     * @param type The type to check.
     * @return True if {@link #getDeepCopy(Object)} may be expected to follow references to this
     *         type. False if the type should not be deeply copied.
     */
    public static boolean isDeepCopyAvailable(Class<?> type) {
        return type != null
                && (Copyable.class.isAssignableFrom(type)
                        || List.class.isAssignableFrom(type)
                        || Map.class.isAssignableFrom(type)
                        || (type.isArray() && isDeepCopyAvailable(type.getComponentType())));
    }

    /**
     * Get a shallow copy (clone) of an object.
     * 
     * <p>
     * This method simplifies access to the clone() method.
     * </p>
     * 
     * @param <T> copyable type
     * @param obj The object to clone.
     * @return A shallow copy of obj, or null if obj is null.
     * @throws CloneNotSupportedException If copying is not available on the object, or if thrown by
     *         clone() itself. When isShallowCopyAvailable() returns true, then this exception is
     *         not expected and may be considered an internal error.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getShallowCopy(T obj) throws CloneNotSupportedException {
        if (obj == null) {
            return null;
        }
        
        if (ViewLifecycle.isTrace()) {
            ProcessLogger.ntrace("clone:",":"+obj.getClass().getSimpleName(), 1000);
        }

        synchronized (obj) {
            // Create new mutable, cloneable instances of List/Map to replace wrappers
            if (!(obj instanceof Cloneable)) {
                if (obj instanceof List) {
                    return (T) new ArrayList<Object>((List<Object>) obj);
                } else if (obj instanceof Map) {
                    return (T) new HashMap<Object, Object>((Map<Object, Object>) obj);
                } else {
                    throw new UnsupportedOperationException(
                            "Not cloneable, and not a supported collection.  This condition should not be reached.");
                }
            }

            // Bypass reflection overhead for commonly used types.
            // There is not need for these checks to be exhaustive - any Cloneable types
            // that define a public clone method and aren't specifically noted below
            // will be cloned by reflection.
            if (obj instanceof Copyable) {
                return (T) ((Copyable) obj).clone();
            }

            if (obj instanceof Object[]) {
                return (T) ((Object[]) obj).clone();
            }

            // synchronized on collections/maps below here is to avoid
            // concurrent modification
            if (obj instanceof ArrayList) {
                synchronized (obj) {
                    return (T) ((ArrayList<?>) obj).clone();
                }
            }

            if (obj instanceof LinkedList) {
                synchronized (obj) {
                    return (T) ((LinkedList<?>) obj).clone();
                }
            }

            if (obj instanceof HashSet) {
                synchronized (obj) {
                    return (T) ((HashSet<?>) obj).clone();
                }
            }

            if (obj instanceof HashMap) {
                synchronized (obj) {
                    return (T) ((HashMap<?, ?>) obj).clone();
                }
            }

            // Use reflection to invoke a public clone() method on the object, if available.
            Method cloneMethod = getMetadata(obj.getClass()).cloneMethod;
            if (cloneMethod == null) {
                throw new CloneNotSupportedException(obj.getClass() + " does not define a public clone() method");
            } else {
                try {

                    return (T) cloneMethod.invoke(obj);

                } catch (IllegalAccessException e) {
                    throw new IllegalStateException("Access error invoking clone()", e);
                } catch (InvocationTargetException e) {
                    Throwable cause = e.getCause();
                    if (cause instanceof RuntimeException) {
                        throw (RuntimeException) cause;
                    } else if (cause instanceof Error) {
                        throw (Error) cause;
                    } else if (cause instanceof CloneNotSupportedException) {
                        throw (CloneNotSupportedException) cause;
                    } else {
                        throw new IllegalStateException("Unexpected error invoking clone()", e);
                    }
                }
            }
        }
    }

    /**
     * Helper for {@link #preventModification(Copyable)} and {@link #getDeepCopy(Object)} for
     * detecting whether or not to queue deep references from the current node.
     */
    private static boolean isDeep(CopyReference<?> ref, Object source) {
        if (!(ref instanceof FieldReference)) {
            return true;
        }

        FieldReference<?> fieldRef = (FieldReference<?>) ref;
        Field field = fieldRef.field;
        
        if (field.isAnnotationPresent(ReferenceCopy.class)) {
            return false;
        }
 
        if (!(source instanceof Copyable) &&
                ((source instanceof Map) || (source instanceof List))) {
            Class<?> collectionType = getMetadata(fieldRef.source.getClass())
                    .collectionTypeByField.get(field);
            
            if (!Object.class.equals(collectionType)
                    && !isDeepCopyAvailable(collectionType)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Helper for {@link #getDeepCopy(Object)} to 
     * detect whether or not to copy the current node or to keep the cloned reference.
     */
    private static boolean isCopy(CopyReference<?> ref) {
        if (!(ref instanceof FieldReference)) {
            return true;
        }

        FieldReference<?> fieldRef = (FieldReference<?>) ref;
        Field field = fieldRef.field;
        ReferenceCopy refCopy = (ReferenceCopy) field.getAnnotation(ReferenceCopy.class);

        return refCopy == null || refCopy.newCollectionInstance();
    }

    /**
     * Prepare a copyable object for caching by calling {@link Copyable#preventModification()} on
     * all copyable instances located by a deep traversal of the object.
     * 
     * @param obj The object to prepare for caching.
     */
    public static void preventModification(Copyable obj) {
        CopyState copyState = RecycleUtils.getRecycledInstance(CopyState.class);
        if (copyState == null) {
            copyState = new CopyState();
        }

        SimpleReference<?> topReference = getSimpleReference(obj);
        try {
            copyState.queue.offer(topReference);

            while (!copyState.queue.isEmpty()) {
                CopyReference<?> toCache = copyState.queue.poll();
                Object source = toCache.get();

                if (!isShallowCopyAvailable(source)) {
                    continue;
                }

                if (source instanceof Copyable) {
                    ((Copyable) source).preventModification();
                }

                if (isDeep(toCache, source)) {
                    copyState.queueDeepCopyReferences(source, null, toCache);
                }

                if (toCache != topReference) {
                    recycle(toCache);
                }
            }

        } finally {
            recycle(topReference);
            copyState.recycle();
        }
        
    }

    /**
     * Prepare a copyable object for caching by calling {@link Copyable#preventModification()} on
     * all copyable instances located by a deep traversal of the object.
     * 
     * @param <T> copyable type
     * @param obj The object to prepare for caching.
     * @return A deep copy of the object.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Copyable> T unwrapDeep(Copyable obj) {
        CopyState copyState = RecycleUtils.getRecycledInstance(CopyState.class);
        if (copyState == null) {
            copyState = new CopyState();
        }

        Copyable unwrappedObj = obj.unwrap();
        SimpleReference<?> topReference = getSimpleReference(unwrappedObj);
        try {
            copyState.queue.offer(topReference);

            while (!copyState.queue.isEmpty()) {
                CopyReference<?> toUnwrap = copyState.queue.poll();
                Object source = toUnwrap.get();

                if (!isShallowCopyAvailable(source)) {
                    continue;
                }

                Object unwrapped = source;
                if (source instanceof Copyable) {
                    unwrapped = ((Copyable) source).unwrap();
                    if (source != unwrapped) {
                        toUnwrap.set(unwrapped);
                    }
                }

                if (isDeep(toUnwrap, unwrapped)) {
                    copyState.queueDeepCopyReferences(unwrapped, unwrapped, toUnwrap);
                    copyState.cache.put(unwrapped, unwrapped);
                }

                if (toUnwrap != topReference) {
                    recycle(toUnwrap);
                }
            }

        } finally {
            recycle(topReference);
            copyState.recycle();
        }
        
        return (T) unwrappedObj;
    }

    /**
     * Get a deep copy of an object using cloning.
     * 
     * @param <T> copyable type
     * @param obj The object to get a deep copy of.
     * @return A deep copy of the object.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getDeepCopy(T obj) {
        CopyState copyState = RecycleUtils.getRecycledInstance(CopyState.class);
        if (copyState == null) {
            copyState = new CopyState();
        }

        SimpleReference<?> topReference = getSimpleReference(obj);
        try {
            copyState.queue.offer(topReference);

            while (!copyState.queue.isEmpty()) {
                CopyReference<?> toCopy = copyState.queue.poll();
                Object source = toCopy.get();

                if (!isShallowCopyAvailable(source) || !isCopy(toCopy)) {
                    continue;
                }
                
                if (source instanceof Copyable) {
                    source = ((Copyable) source).unwrap();
                }

                toCopy.set(copyState.getTarget(source, isDeep(toCopy, source), toCopy));

                if (toCopy != topReference) {
                    recycle(toCopy);
                }
            }

            return (T) topReference.get();

        } finally {
            recycle(topReference);
            copyState.recycle();
        }
    }

    /**
     * Holds copy state for use with {@link #getDeepCopy(Object)}.
     */
    private static class CopyState {

        private final Queue<CopyReference<?>> queue = new LinkedList<CopyReference<?>>();
        private final Map<Object, Object> cache = new IdentityHashMap<Object, Object>();

        /**
         * Get a shallow copy of the source object appropriate for the current copy operation.
         * 
         * @param source The original source object to copy.
         * @return A shallow copy of the source object.
         */
        private Object getTarget(Object source, boolean queueDeepReferences, CopyReference<?> ref) {
            boolean useCache = source != Collections.EMPTY_LIST && source != Collections.EMPTY_MAP;

            Object target = useCache ? cache.get(source) : null;

            if (target == null) {
                Class<?> targetClass = ref.getTargetClass();

                if (Copyable.class.isAssignableFrom(targetClass) && targetClass.isInterface()
                        && ref.isDelayAvailable() && isDelay()) {
                    target = DelayedCopyableHandler.getDelayedCopy((Copyable) source);
                    
                } else {
                    
                    try {
                        target = getShallowCopy(source);
                    } catch (CloneNotSupportedException e) {
                        throw new IllegalStateException("Unexpected cloning error during shallow copy", e);
                    }

                    if (queueDeepReferences) {
                        queueDeepCopyReferences(source, target, ref);
                    }
                }
                
                if (useCache) {
                    cache.put(source, target);
                }
            }

            return target;
        }

        /**
         * Queues references for deep copy after performing the shallow copy.
         * 
         * @param source The original source object at the current node.
         * @param target A shallow copy of the source object, to be analyzed for deep copy.
         */
        private void queueDeepCopyReferences(Object source, Object target, CopyReference<?> ref) {
            Class<?> type = source.getClass();
            Class<?> targetClass = ref.getTargetClass();
            if (!isDeepCopyAvailable(type)) {
                return;
            }

            // Don't queue references if the source has already been seen
            if (cache.containsKey(source)) {
                return;
            } else if (target == null) {
                cache.put(source, source);
            }

            if (Copyable.class.isAssignableFrom(type)) {
                for (Field field : getMetadata(type).cloneFields) {
                    queue.offer(getFieldReference(source, target, field, ref));
                }

                // Used fields for deep copying, even if List or Map is implemented.
                // The wrapped list/map should be picked up as a field during deep copy.
                return;
            }

            if (List.class.isAssignableFrom(targetClass)) {
                List<?> sourceList = (List<?>) source;
                List<?> targetList = (List<?>) target;
                Type componentType = ObjectPropertyUtils.getComponentType(ref.getType());
                
                if (componentType instanceof TypeVariable<?>) {
                    TypeVariable<?> tvar = (TypeVariable<?>) componentType;
                    if (ref.getTypeVariables().containsKey(tvar.getName())) {
                        componentType = ref.getTypeVariables().get(tvar.getName());
                    }
                }
                
                Class<?> componentClass = ObjectPropertyUtils.getUpperBound(componentType);

                for (int i = 0; i < sourceList.size(); i++) {
                    queue.offer(getListReference(sourceList, targetList,
                            i, componentClass, componentType, ref));
                }
            }

            if (Map.class.isAssignableFrom(targetClass)) {
                Map<?, ?> sourceMap = (Map<?, ?>) source;
                Map<?, ?> targetMap = (Map<?, ?>) target;
                Type componentType = ObjectPropertyUtils.getComponentType(ref.getType());
                Class<?> componentClass = ObjectPropertyUtils.getUpperBound(componentType);

                for (Map.Entry<?, ?> sourceEntry : sourceMap.entrySet()) {
                    queue.offer(getMapReference(sourceEntry, targetMap,
                            componentClass, componentType, ref));
                }
            }

            if (targetClass.isArray()) {
                for (int i = 0; i < Array.getLength(source); i++) {
                    queue.offer(getArrayReference(source, target, i, ref));
                }
            }
        }

        /**
         * Clear queue and cache, and recycle this state object.
         */
        private void recycle() {
            queue.clear();
            cache.clear();
            RecycleUtils.recycle(this);
        }
    }

    /**
     * Represents a abstract reference to a targeted value for use during deep copying.
     */
    private interface CopyReference<T> {
        
        /**
         * Gets the type this reference refers to.
         * 
         * @return the class referred to
         */
        Class<T> getTargetClass();
        
        /**
         * Determines whether or not a delayed copy proxy should be considered on this reference.
         * 
         * @return True if a delayed copy proxy may be used with this reference, false to always
         *         perform deep copy.
         */
        boolean isDelayAvailable();

        /**
         * Gets the generic type this reference refers to.
         * 
         * @return the generic type referred to
         */
        Type getType();

        /**
         * Gets the type variable mapping.
         * 
         * @return the type variable mapping.
         */
        Map<String, Type> getTypeVariables();

        /**
         * Retrieve the targeted value for populating the reference.
         * 
         * <p>
         * This value returned by this method will typically come from a source object, then after
         * copy operations have been performed {@link #set(Object)} will be called to populate the
         * target value on the destination object.
         * </p>
         * 
         * @return The targeted value for populating the reference.
         */
        T get();

        /**
         * Modify the value targeted by the reference.
         * 
         * <p>
         * This value passed to this method will have typically come {@link #get()}. After copy
         * operations have been performed, this method will be called to populate the target value
         * on the destination object.
         * </p>
         * 
         * @param value The value to modify the reference as.
         */
        void set(Object value);

        /**
         * Clean the reference for recycling.
         */
        void clean();

    }

    /**
     * Recycle a copy reference for later use, once the copy has been performed.
     */
    private static <T> void recycle(CopyReference<T> ref) {
        ref.clean();
        RecycleUtils.recycle(ref);
    }

    /**
     * Simple copy reference for holding top-level value to be later inspected and returned. Values
     * held by this class will be modified in place.
     */
    private static class SimpleReference<T> implements CopyReference<T> {

        private T value;
        private Class<T> targetClass;

        /**
         * Gets the target class.
         * 
         * @return target class
         */
        public Class<T> getTargetClass() {
            return this.targetClass;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isDelayAvailable() {
            return false;
        }

        /**
         * Gets the target class.
         * 
         * @return target class
         */
        @Override
        public Type getType() {
            return this.targetClass;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Map<String, Type> getTypeVariables() {
            return Collections.emptyMap();
        }

        /**
         * Gets the value.
         * 
         * @return value
         */
        @Override
        public T get() {
            return value;
        }

        /**
         * Sets the a value.
         * 
         * @param value The value to set.
         */
        @Override
        public void set(Object value) {
            this.value = targetClass.cast(value);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void clean() {
            this.value = null;
            this.targetClass = null;
        }
    }

    /**
     * Get a simple reference for temporary use while deep cloning.
     * 
     * <p>
     * Call {@link #recycle(CopyReference)} when done working with the reference.
     * </p>
     * 
     * @param value The initial object to refer to.
     * 
     * @return A simple reference for temporary use while deep cloning.
     */
    @SuppressWarnings("unchecked")
    private static SimpleReference<?> getSimpleReference(Object value) {
        SimpleReference<Object> ref = RecycleUtils.getRecycledInstance(SimpleReference.class);

        if (ref == null) {
            ref = new SimpleReference<Object>();
        }

        ref.targetClass = (Class<Object>) value.getClass();
        ref.value = value;

        return ref;
    }

    /**
     * Reference implementation for a field on an object.
     */
    private static class FieldReference<T> implements CopyReference<T> {

        private Object source;
        private Object target;
        private Field field;
        private boolean delayAvailable;
        private Map<String, Type> typeVariables = new HashMap<String, Type>();

        /**
         * Gets the type of the field.
         * 
         * {@inheritDoc}
         */
        @SuppressWarnings("unchecked")
        @Override
        public Class<T> getTargetClass() {
            return (Class<T>) field.getType();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isDelayAvailable() {
            return delayAvailable;
        }

        /**
         * Gets the generic type of this field.
         * 
         * {@inheritDoc}
         */
        @Override
        public Type getType() {
            return field.getGenericType();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Map<String, Type> getTypeVariables() {
            return typeVariables;
        }

        /**
         * Get a value from the field on the source object.
         * 
         * @return The value referred to by the field on the source object.
         */
        @SuppressWarnings("unchecked")
        @Override
        public T get() {
            try {
                ReferenceCopy ref = field.getAnnotation(ReferenceCopy.class);
                if (ref != null && ref.referenceTransient()) {
                    return null;
                }

                return (T) field.get(source);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Access error attempting to get from " + field, e);
            }
        }

        /**
         * Set a value for the field on the target object.
         * 
         * @param value The value to set for the field on the target object.
         */
        @Override
        public void set(Object value) {
            try {
                field.set(target, value);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Access error attempting to set " + field, e);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void clean() {
            source = null;
            target = null;
            field = null;
            delayAvailable = false;
            typeVariables.clear();
        }

    }

    /**
     * Get a field reference for temporary use while deep cloning.
     * 
     * <p>
     * Call {@link #recycle(CopyReference)} when done working with the reference.
     * </p>
     * 
     * @param source The source object.
     * @param target The target object.
     * @param field The field to use as the reference target.
     * 
     * @return A field reference for temporary use while deep cloning.
     */
    private static <T> FieldReference<T> getFieldReference(Object source, Object target, Field field,
            CopyReference<T> pref) {
        @SuppressWarnings("unchecked")
        FieldReference<T> ref = RecycleUtils.getRecycledInstance(FieldReference.class);

        if (ref == null) {
            ref = new FieldReference<T>();
        }

        ref.source = source;
        ref.target = target;
        ref.field = field;

        DelayedCopy delayedCopy = field.getAnnotation(DelayedCopy.class);
        ref.delayAvailable = delayedCopy != null &&
                (!delayedCopy.inherit() || pref.isDelayAvailable());

        Map<String, Type> pTypeVars = pref.getTypeVariables();
        
        if (pTypeVars != null && source != null) {
            Class<?> sourceType = source.getClass();
            Class<?> targetClass = pref.getTargetClass();
            Type targetType = ObjectPropertyUtils.findGenericType(sourceType, targetClass);
            if (targetType instanceof ParameterizedType) {
                ParameterizedType parameterizedTargetType = (ParameterizedType) targetType;
                Type[] params = parameterizedTargetType.getActualTypeArguments();
                for (int j = 0; j < params.length; j++) {
                    if (params[j] instanceof TypeVariable<?>) {
                        Type pType = pTypeVars.get(targetClass.getTypeParameters()[j].getName());
                        ref.typeVariables.put(((TypeVariable<?>) params[j]).getName(), pType);
                    }
                }
            }
        }

        Class<?> rawType = field.getType();
        Type genericType = field.getGenericType();
        if (genericType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) genericType;
            TypeVariable<?>[] typeParams = rawType.getTypeParameters();
            Type[] params = parameterizedType.getActualTypeArguments();
            assert params.length == typeParams.length;
            for (int i=0; i < params.length; i++) {
                Type paramType = params[i];
                if (paramType instanceof TypeVariable<?>) {
                    Type fType = ref.typeVariables.get(((TypeVariable<?>) paramType).getName());
                    if (fType != null) {
                        paramType = fType;
                    }
                }
                ref.typeVariables.put(typeParams[i].getName(), paramType);
            }
        }
        
        return ref;
    }

    /**
     * Reference implementation for an entry in an array.
     */
    private static class ArrayReference<T> implements CopyReference<T> {

        private Object source;
        private Object target;
        private int index = -1;
        private boolean delayAvailable;
        private Map<String, Type> typeVariables = new HashMap<String, Type>();

        /**
         * Gets the component type of the array.
         * 
         * @return component type
         */
        @SuppressWarnings("unchecked")
        @Override
        public Class<T> getTargetClass() {
            return (Class<T>) source.getClass().getComponentType();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isDelayAvailable() {
            return delayAvailable;
        }

        /**
         * Gets the component type of the array.
         * 
         * @return component type
         */
        @Override
        public Type getType() {
            return source.getClass().getComponentType();
        }

        /**
         * {@inheritDoc}
         */
        public Map<String, Type> getTypeVariables() {
            return this.typeVariables;
        }

        /**
         * Get the value of the indicated entry in the source array.
         * 
         * @return The value of the indicated entry in the source array.
         */
        @SuppressWarnings("unchecked")
        @Override
        public T get() {
            return (T) Array.get(source, index);
        }

        /**
         * Modify the value of the indicated entry in the target array.
         * 
         * @param value The value to set on the indicated entry in the target array.
         */
        @Override
        public void set(Object value) {
            Array.set(target, index, value);
        }

        @Override
        public void clean() {
            source = null;
            target = null;
            index = -1;
            delayAvailable = false;
            typeVariables.clear();
        }
    }

    /**
     * Get an array reference for temporary use while deep cloning.
     * 
     * <p>
     * Call {@link #recycle(CopyReference)} when done working with the reference.
     * </p>
     * 
     * @param source The source array.
     * @param target The target array.
     * @param index The array index.
     * 
     * @return An array reference for temporary use while deep cloning.
     */
    private static <T> ArrayReference<T> getArrayReference(
            Object source, Object target, int index, CopyReference<?> pref) {
        @SuppressWarnings("unchecked")
        ArrayReference<T> ref = RecycleUtils.getRecycledInstance(ArrayReference.class);

        if (ref == null) {
            ref = new ArrayReference<T>();
        }

        ref.source = source;
        ref.target = target;
        ref.index = index;
        ref.delayAvailable = pref.isDelayAvailable();
        ref.typeVariables.putAll(pref.getTypeVariables());
        
        return ref;
    }

    /**
     * Reference implementation for an item in a list.
     */
    private static class ListReference<T> implements CopyReference<T> {

        private Class<T> targetClass;
        private Type type;
        private List<T> source;
        private List<T> target;
        private int index = -1;
        private boolean delayAvailable;
        private Map<String, Type> typeVariables = new HashMap<String, Type>();

        /**
         * Gets the item class for the list.
         * 
         * @return item class
         */
        @Override
        public Class<T> getTargetClass() {
            return targetClass;
        }

        /**
         * {@inheritDoc}
         */
        public boolean isDelayAvailable() {
            return this.delayAvailable;
        }


        /**
         * Gets the generic item type for the list.
         * 
         * @return generic item type
         */
        @Override
        public Type getType() {
            return type;
        }

        /**
         * {@inheritDoc}
         */
        public Map<String, Type> getTypeVariables() {
            return this.typeVariables;
        }

        /**
         * Get the value of the indicated item in the source array.
         * 
         * @return The value of the indicated item in the source array.
         */
        @Override
        public T get() {
            return targetClass.cast(source.get(index));
        }

        /**
         * Modify the list item.
         * 
         * @param value The value to modify the list item as.
         */
        @Override
        public void set(Object value) {
            target.set(index, targetClass.cast(value));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void clean() {
            targetClass = null;
            type = null;
            source = null;
            target = null;
            index = -1;
            delayAvailable = false;
            typeVariables.clear();
        }
    }

    /**
     * Get a list reference for temporary use while deep cloning.
     * 
     * <p>
     * Call {@link #recycle(CopyReference)} when done working with the reference.
     * </p>
     * 
     * @param source The source list.
     * @param target The target list.
     * @param index The index of the list item.
     * 
     * @return A list reference for temporary use while deep cloning.
     */
    @SuppressWarnings("unchecked")
    private static ListReference<?> getListReference(List<?> source, List<?> target, int index,
            Class<?> targetClass, Type type, CopyReference<?> pref) {
        ListReference<Object> ref = RecycleUtils.getRecycledInstance(ListReference.class);

        if (ref == null) {
            ref = new ListReference<Object>();
        }

        ref.source = (List<Object>) source;
        ref.target = (List<Object>) target;
        ref.index = index;
        ref.targetClass = (Class<Object>) targetClass;
        ref.type = type;
        ref.delayAvailable = pref.isDelayAvailable();
        ref.typeVariables.putAll(pref.getTypeVariables());

        return ref;
    }

    /**
     * Reference implementation for an entry in a map.
     */
    private static class MapReference<T> implements CopyReference<T> {

        private Class<T> targetClass;
        private Type type;
        private Map.Entry<Object, T> sourceEntry;
        private Map<Object, T> target;
        private boolean delayAvailable;
        private Map<String, Type> typeVariables = new HashMap<String, Type>();

        /**
         * Gets the value class for the map.
         * 
         * @return value class
         */
        @Override
        public Class<T> getTargetClass() {
            return targetClass;
        }

        /**
         * @return the delayAvailable
         */
        public boolean isDelayAvailable() {
            return this.delayAvailable;
        }

        /**
         * Gets the generic value type for the map.
         * 
         * @return generic value type
         */
        @Override
        public Type getType() {
            return type;
        }

        /**
         * {@inheritDoc}
         */
        public Map<String, Type> getTypeVariables() {
            return this.typeVariables;
        }

        /**
         * Get the value of the map entry.
         * 
         * @return The value of the map entry.
         */
        @Override
        public T get() {
            return sourceEntry.getValue();
        }

        /**
         * Modify the map entry.
         * 
         * @param value The value to modify the map entry with.
         */
        @Override
        public void set(Object value) {
            target.put(sourceEntry.getKey(), targetClass.cast(value));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void clean() {
            targetClass = null;
            type = null;
            sourceEntry = null;
            target = null;
            delayAvailable = false;
            typeVariables.clear();
        }
    }

    /**
     * Get a map reference for temporary use while deep cloning.
     * 
     * <p>
     * Call {@link #recycle(CopyReference)} when done working with the reference.
     * </p>
     * 
     * @param sourceEntry The source entry.
     * @param target The target map.
     * 
     * @return A map reference for temporary use while deep cloning.
     */
    @SuppressWarnings("unchecked")
    private static MapReference<?> getMapReference(Map.Entry<?, ?> sourceEntry, Map<?, ?> target,
            Class<?> targetClass, Type type, CopyReference<?> pref) {
        MapReference<Object> ref = RecycleUtils.getRecycledInstance(MapReference.class);

        if (ref == null) {
            ref = new MapReference<Object>();
        }

        ref.sourceEntry = (Map.Entry<Object, Object>) sourceEntry;
        ref.target = (Map<Object, Object>) target;
        ref.targetClass = (Class<Object>) targetClass;
        ref.type = type;
        ref.delayAvailable = pref.isDelayAvailable();
        ref.typeVariables.putAll(pref.getTypeVariables());

        return ref;
    }
    
    /**
     * Internal field cache meta-data node, for reducing field lookup overhead.
     * 
     * @author Kuali Rice Team (rice.collab@kuali.org)
     */
    private static class ClassMetadata {

        /**
         * The public clone method, if defined for this type.
         */
        private final Method cloneMethod;

        /**
         * All fields on the class that should have a shallow copy performed during a deep copy
         * operation.
         */
        private final List<Field> cloneFields;

        /**
         * Mapping from field to generic collection type, for Map and List fields that should be
         * deep copied.
         */
        private final Map<Field, Class<?>> collectionTypeByField;

        /**
         * Create a new field reference for a target class.
         * 
         * @param targetClass The class to inspect for meta-data.
         */
        private ClassMetadata(Class<?> targetClass) {

            Method targetCloneMethod = null;
            if (Cloneable.class.isAssignableFrom(targetClass) && !targetClass.isArray()) {
                try {
                    targetCloneMethod = targetClass.getMethod("clone");
                } catch (NoSuchMethodException e) {
                    LOG.warn("Target " + targetClass + " is cloneable, but does not define a public clone() method", e);
                } catch (SecurityException e) {
                    LOG.warn("Target " + targetClass + " is cloneable, but the public clone() method is restricted", e);
                }
            }
            cloneMethod = targetCloneMethod;

            // Create mutable collections for building meta-data indexes.
            List<Field> cloneList = new ArrayList<Field>();
            Map<Field, Class<?>> collectionTypeMap = new HashMap<Field, Class<?>>();

            Class<?> currentClass = targetClass;
            while (currentClass != Object.class && currentClass != null) {

                for (Field currentField : currentClass.getDeclaredFields()) {
                    if ((currentField.getModifiers() & Modifier.STATIC) == Modifier.STATIC) {
                        continue;
                    }

                    Class<?> type = currentField.getType();

                    if (type.isArray()
                            || isDeepCopyAvailable(type)
                            || Cloneable.class.isAssignableFrom(type)) {
                        currentField.setAccessible(true);
                        cloneList.add(currentField);
                    }

                    boolean isList = List.class.isAssignableFrom(type);
                    boolean isMap = Map.class.isAssignableFrom(type);
                    if (!isList && !isMap) {
                        continue;
                    }
                    
                    Class<?> collectionType = ObjectPropertyUtils
                            .getUpperBound(ObjectPropertyUtils
                                    .getComponentType(currentField.getGenericType()));

                    if (collectionType.equals(Object.class) || isDeepCopyAvailable(collectionType)) {
                        collectionTypeMap.put(currentField, collectionType);
                    }
                }

                currentClass = currentClass.getSuperclass();
            }

            // Seal index collections to prevent external modification.
            cloneFields = Collections.unmodifiableList(cloneList);
            collectionTypeByField = Collections.unmodifiableMap(collectionTypeMap);
        }
    }

    /**
     * Static cache for reducing annotated field lookup overhead.
     */
    private static final Map<Class<?>, ClassMetadata> CLASS_META_CACHE =
            Collections.synchronizedMap(new WeakHashMap<Class<?>, ClassMetadata>());

    /**
     * Get copy metadata for a class.
     * @param targetClass The class.
     * @return Copy metadata for the class.
     */
    private static final ClassMetadata getMetadata(Class<?> targetClass) {
        ClassMetadata metadata = CLASS_META_CACHE.get(targetClass);

        if (metadata == null) {
            CLASS_META_CACHE.put(targetClass, metadata = new ClassMetadata(targetClass));
        }

        return metadata;
    }

}
