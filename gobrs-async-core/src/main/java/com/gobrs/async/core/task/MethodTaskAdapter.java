package com.gobrs.async.core.task;

import com.gobrs.async.core.TaskSupport;
import com.gobrs.async.core.anno.MethodTask;
import com.gobrs.async.core.common.def.DefaultConfig;
import com.gobrs.async.core.common.domain.MethodTaskMatch;
import com.gobrs.async.core.common.exception.AsyncTaskNotFoundException;
import com.gobrs.async.core.common.exception.MethodTaskArgumentException;
import com.gobrs.async.core.common.util.ProxyUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.Parameter;
import java.util.*;

import static com.gobrs.async.core.common.domain.GobrsTaskMethodEnum.*;

/**
 * The type Method task adaptation.
 *
 * @program: gobrs -async
 * @ClassName MethodTaskAdaptation
 * @description:
 * @author: sizegang
 * @create: 2023 -01-04
 */
@Slf4j
@Data
public class MethodTaskAdapter extends AsyncTask<Object, Object> {

    private Map<String, MethodTaskMatch> PARAMETERS_CACHE;

    private Object proxy;

    private static final String MTASKCONTEXT = MTaskContext.class.getName();
    /**
     * The Method task.
     */
    MethodTask methodTask;

    @Override
    public Object task(Object parameter, TaskSupport support) {
        MethodTaskMatch match = PARAMETERS_CACHE.get(TASK.getMethod());
        if (Objects.isNull(match)) {
            throw new AsyncTaskNotFoundException(String.format(" MethodTask not found %s", getName()));
        }
        Object[] param = createParam(parameter, support, match);
        return ProxyUtil.invokeMethod(match.getMethod(), proxy, param);
    }


    @Override
    public boolean necessary(Object parameter, TaskSupport support) {
        MethodTaskMatch match = PARAMETERS_CACHE.get(NECESSARY.getMethod());
        if (Objects.isNull(match)) {
            return DefaultConfig.TASK_NECESSARY;
        }
        Object[] param = createParam(support.getParam(), support, match);
        Optional<Object> o = Optional.ofNullable(doProxy(match, param));
        if (o.isPresent()) {
            return (Boolean) o.get();
        }
        return true;

    }

    @Override
    public void onFail(TaskSupport support, Exception exception) {
        MethodTaskMatch match = PARAMETERS_CACHE.get(ONFAIL.getMethod());
        if (Objects.isNull(match)) {
            super.onFail(support, exception);
            return;
        }
        Object[] param = createParam(support.getParam(), support, match);
        doProxy(match, param);
    }

    private Object doProxy(MethodTaskMatch match, Object[] parameter) {
        return ProxyUtil.invokeMethod(match.getMethod(), proxy, parameter);
    }

    @Override
    public void onSuccess(TaskSupport support) {
        MethodTaskMatch match = PARAMETERS_CACHE.get(ONSUCCESS.getMethod());
        if (Objects.isNull(match)) {
            super.onSuccess(support);
            return;
        }
        Object[] param = createParam(support.getParam(), support, match);
        doProxy(match, param);
    }

    @Override
    public void prepare(Object parameter) {
        MethodTaskMatch match = PARAMETERS_CACHE.get(PREPARE.getMethod());
        if (Objects.isNull(match)) {
            super.prepare(parameter);
            return;
        }
        Object[] param = createParam(parameter, null, match);
        doProxy(match, param);
    }

    @Override
    public void rollback(Object parameter) {
        MethodTaskMatch match = PARAMETERS_CACHE.get(ROLLBACK.getMethod());
        if (Objects.isNull(match)) {
            super.rollback(parameter);
            return;
        }
        Object[] param = createParam(parameter, null, match);
        doProxy(match, param);
    }


    /**
     * 创建请求参数
     * @param parameter
     * @param support
     * @param match
     * @return
     */
    private Object[] createParam(Object parameter, TaskSupport support, MethodTaskMatch match) {

        Object[] params = match.getParams();

        if (params == null || params.length == 0) {
            throwCustomException();
        }

        MTaskContext<Object> context = MTaskContext.builder().param(parameter).support(support).build();

        List<Object> newParams = new ArrayList<>(Arrays.asList(params));

        Object lastParam = newParams.get(params.length - 1);

        argumentCheck((Parameter) lastParam);

        for (int i = newParams.size() - 2; i >= 0; i--) {
            newParams.set(i, null);
        }

        newParams.set(params.length - 1, context);

        return newParams.toArray();
    }

    /**
     * 参数验证
     * @param lastParam
     */
    private void argumentCheck(Parameter lastParam) {
        try {
            String classReference = lastParam.getParameterizedType().getTypeName();
            if (StringUtils.isNotBlank(classReference) && !classReference.contains(MTASKCONTEXT)) {
                throwCustomException();
            }
        } catch (Exception exception) {
            throwCustomException();
        }
    }

    private void throwCustomException() {
        throw new MethodTaskArgumentException(String.format("%s  The last argument to a method annotated with @MethodTask must be of type MTaskContext", getName()));
    }

}
