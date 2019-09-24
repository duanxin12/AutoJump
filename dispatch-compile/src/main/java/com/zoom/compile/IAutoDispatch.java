package com.zoom.compile;

import com.zoom.annotation.DispatchMeta;

import java.util.Map;

public interface IAutoDispatch {
    public void loadIntoDispatchs(Map<String, DispatchMeta> dispatchs);
}
