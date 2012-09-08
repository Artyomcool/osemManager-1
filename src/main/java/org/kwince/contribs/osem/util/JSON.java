package org.kwince.contribs.osem.util;

import org.kwince.contribs.osem.annotations.Id;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

public class JSON {

	private static Gson gson; 
	
    static{
    	gson = new GsonBuilder().setExclusionStrategies(new ExclusionStrategy(){

			@Override
			public boolean shouldSkipClass(Class<?> arg) {
				return false;
			}

			@Override
			public boolean shouldSkipField(FieldAttributes arg) {
				return arg.getAnnotation(Id.class)!=null;
			}
    		
    	}).create();
    }
    
    public static final <T> String serialize(T obj) {
		return gson.toJson(obj);
	}
    
    public static final <T> T deserialize(String json, Class<T> classType) {
		return gson.fromJson(json, classType);
	}
    
    public static final JsonElement toTree(Object e){
    	return gson.toJsonTree(e);
    }

}
