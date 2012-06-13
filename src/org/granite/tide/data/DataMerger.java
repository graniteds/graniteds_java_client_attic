package org.granite.tide.data;

import org.granite.tide.Expression;
import org.granite.tide.data.spi.MergeContext;


public interface DataMerger {

    public boolean accepts(Object obj);
    
    public Object merge(MergeContext mergeContext, Object obj, Object previous, Expression expr, Object parent, String propertyName);
}
