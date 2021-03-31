package org.digitalcampus.oppia.task.result;

public class EntityResult<T> extends BasicResult {

    private T entity;

    public EntityResult(boolean success) {
        super(success);
    }

    public EntityResult(boolean success, String resultMessage) {
        super(success, resultMessage);
    }

    public EntityResult() {   }


    public T getEntity() {
        return entity;
    }

    public void setEntity(T entity) {
        this.entity = entity;
    }
}
