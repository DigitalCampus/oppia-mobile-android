package org.digitalcampus.oppia.task.result;

import java.util.List;

public class EntityListResult<T> extends BasicResult {

    private List<T> entityList;

    public EntityListResult(boolean success) {
        super(success);
    }

    public EntityListResult(boolean success, String resultMessage) {
        super(success, resultMessage);
    }

    public List<T> getEntityList() {
        return entityList;
    }

    public void setEntityList(List<T> entityList) {
        this.entityList = entityList;
    }
}
