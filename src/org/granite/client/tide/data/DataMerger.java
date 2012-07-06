package org.granite.client.tide.data;

import org.granite.client.tide.data.spi.MergeContext;
import org.granite.tide.Expression;


public interface DataMerger {

    public boolean accepts(Object obj);
    
    public Object merge(MergeContext mergeContext, Object obj, Object previous, Expression expr, Object parent, String propertyName);
}
