package tech.chengw.www.task;

/**
 * description 任务body，保存延时任务需要的数据
 *
 * @author chengwj
 * @version 1.0
 * @date 2021/2/2
 **/
public class TaskBody {
    private Object body;

    public Object getBody() {
        return body;
    }

    public TaskBody setBody(Object body) {
        this.body = body;
        return this;
    }
}
