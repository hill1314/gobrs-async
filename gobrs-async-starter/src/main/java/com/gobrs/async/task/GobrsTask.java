package com.gobrs.async.task;

import com.gobrs.async.TaskSupport;

/**
 * The interface Gobrs task.
 *
 * @param <Param>  the type parameter
 * @param <Result> the type parameter
 * @program: gobrs -async-core
 * @description:
 * @author: sizegang
 * @create: 2022 -03-23 23:31
 */
public interface GobrsTask<Param, Result> extends Task {
    /**
     * Before the mission begins
     *
     * @param param the param
     */
    void prepare(Param param);

    /**
     * Tasks to be performed
     *
     * @param param   the param
     * @param support the support
     * @return result result
     */
    Result task(Param param, TaskSupport support);

    /**
     * Whether a task needs to be executed
     *
     * @param param   the param
     * @param support the support
     * @return boolean boolean
     */
    boolean nessary(Param param, TaskSupport support);

    /**
     * Task Executed Successfully
     *
     * @param support the support
     */
    void onSuccess(TaskSupport support);

    /**
     * Task execution failure
     *
     * @param support the support
     */
    void onFail(TaskSupport support);

    /**
     * rollback
     *
     * @param param the param
     */
    default void rollback(Param param) {

    }
}
