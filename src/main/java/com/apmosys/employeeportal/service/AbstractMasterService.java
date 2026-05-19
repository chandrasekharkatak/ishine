package com.apmosys.employeeportal.service;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Abstract base class for master data services
 * Provides common CRUD operations for master entities
 * 
 * @param <T> Entity type
 * @param <R> Repository type extending JpaRepository
 */
public abstract class AbstractMasterService<T, R extends JpaRepository<T, Integer>>
        implements MasterService<T> {

    protected abstract R getRepository();

    /**
     * Override this method to add custom validation before creating an entity
     */
    protected void validateForCreate(T entity) {}

    /**
     * Override this method to add custom validation before updating an entity
     */
    protected void validateForUpdate(T entity) {}

    /**
     * Get current user ID from security context
     * Override this method to implement proper security context retrieval
     */
    protected Long getCurrentUserId() {
        // TODO: Get from SecurityContextHolder or similar
        // return SecurityContextHolder.getContext().getAuthentication().getPrincipal().getUserId();
        return null;
    }

    @Override
    public List<T> getAllActive() {
        return getRepository().findAll().stream()
                .filter(this::isActive)
                .collect(Collectors.toList());
    }

    @Override
    public T getById(Integer id) {
        return getRepository().findById(id)
                .orElseThrow(() -> new RuntimeException("Entity not found with id: " + id));
    }

    @Override
    public T create(T entity) {
        validateForCreate(entity);
        setCreatedBy(entity);
        setActive(entity, true);
        return getRepository().save(entity);
    }

    @Override
    public T update(Integer id, T entity) {
        T existingEntity = getById(id);
        validateForUpdate(entity);
        
        // Copy fields from entity to existingEntity
        copyFields(entity, existingEntity);
        setUpdatedBy(existingEntity);
        
        return getRepository().save(existingEntity);
    }

    @Override
    public void deactivate(Integer id) {
        T entity = getById(id);
        setActive(entity, false);
        getRepository().save(entity);
    }

    /**
     * Check if entity is active using reflection
     */
    private boolean isActive(T entity) {
        try {
            Method getIsActive = entity.getClass().getMethod("getIsActive");
            Boolean isActive = (Boolean) getIsActive.invoke(entity);
            return isActive != null && isActive;
        } catch (Exception e) {
            // If isActive field doesn't exist, consider it active
            return true;
        }
    }

    /**
     * Set isActive field using reflection
     */
    private void setActive(T entity, boolean active) {
        try {
            Method setIsActive = entity.getClass().getMethod("setIsActive", Boolean.class);
            setIsActive.invoke(entity, active);
        } catch (Exception e) {
            // If setIsActive method doesn't exist, ignore
        }
    }

    /**
     * Set createdBy field using reflection
     */
    private void setCreatedBy(T entity) {
        try {
            Method setCreatedBy = entity.getClass().getMethod("setCreatedBy", Long.class);
            Long currentUserId = getCurrentUserId();
            if (currentUserId != null) {
                setCreatedBy.invoke(entity, currentUserId);
            }
        } catch (Exception e) {
            // If setCreatedBy method doesn't exist, ignore
        }
    }

    /**
     * Set updatedBy field using reflection
     */
    private void setUpdatedBy(T entity) {
        try {
            Method setUpdatedBy = entity.getClass().getMethod("setUpdatedBy", Long.class);
            Long currentUserId = getCurrentUserId();
            if (currentUserId != null) {
                setUpdatedBy.invoke(entity, currentUserId);
            }
        } catch (Exception e) {
            // If setUpdatedBy method doesn't exist, ignore
        }
    }

    /**
     * Copy fields from source to target using reflection
     * Copies all non-null fields from source to target, excluding ID and audit fields
     */
    private void copyFields(T source, T target) {
        try {
            Method[] methods = source.getClass().getMethods();
            for (Method method : methods) {
                if (method.getName().startsWith("get") && method.getParameterCount() == 0) {
                    Object value = method.invoke(source);
                    if (value != null && !method.getName().equals("getClass")) {
                        String setterName = method.getName().replaceFirst("get", "set");
                        try {
                            Method setter = target.getClass().getMethod(setterName, method.getReturnType());
                            // Skip ID fields (any field ending with "Id"), createdOn, and createdBy
                            String fieldName = method.getName().replaceFirst("get", "");
                            if (!fieldName.endsWith("Id") && 
                                !setterName.equals("setCreatedOn") &&
                                !setterName.equals("setCreatedBy")) {
                                setter.invoke(target, value);
                            }
                        } catch (NoSuchMethodException e) {
                            // Setter doesn't exist, skip
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error copying fields", e);
        }
    }
}

