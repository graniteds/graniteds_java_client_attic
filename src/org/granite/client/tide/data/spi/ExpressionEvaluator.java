package org.granite.client.tide.data.spi;

import org.granite.tide.Expression;
import org.granite.client.tide.SyncMode;


public interface ExpressionEvaluator {

    public SyncMode getRemoteSync(Object object);

    public Value getInstance(Expression ref, Object object);

    public Value evaluate(Expression ref);
    
    public static class Value {
        
        public String componentName;
        public String componentClassName;
        public Object instance;
        public Object value;
    }

}
