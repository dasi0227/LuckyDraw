package com.dasi.infrastructure.persistent.dao;

import cn.bugstack.middleware.db.router.annotation.DBRouter;
import com.dasi.infrastructure.persistent.po.Task;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ITaskDao {
    void saveTask(Task task);

    @DBRouter
    void updateTaskState(Task task);

    List<Task> queryUnsolvedTask();

}
