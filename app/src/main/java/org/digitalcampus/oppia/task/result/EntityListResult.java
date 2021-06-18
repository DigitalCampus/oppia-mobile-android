package org.digitalcampus.oppia.task.result;

import java.util.ArrayList;
import java.util.List;

public class EntityListResult<T> extends BasicResult {

    private List<T> entityList = new ArrayList<>();

    public EntityListResult(boolean success) {
        super(success);
    }

    public EntityListResult(boolean success, String resultMessage) {
        super(success, resultMessage);
    }

    public EntityListResult() {   }

    public List<T> getEntityList() {
        return entityList;
    }

    public void setEntityList(List<T> entityList) {
        this.entityList = entityList;
    }

    public boolean hasItems() {
        return entityList != null && !entityList.isEmpty();
    }
}
