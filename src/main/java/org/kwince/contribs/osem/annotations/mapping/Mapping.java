package org.kwince.contribs.osem.annotations.mapping;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.kwince.contribs.osem.annotations.Synthetic;

/**
 * An annotation that allows to point the ES-mapping for entity.
 * Could be applied to entity fields or {@link Synthetic} methods.
 * Otherwise - ignored.
 * The mappings must be grouped by parameter type.
 * <br>
 * An example of mappings:
 * <pre>
 * public class TestA{
 * 
 *    @Mapping(
 *       stringParams={
 *           @StringParam(name="store",value="yes")
 *           @StringParam(name="index",value="no")
 *       }
 *       doubleParams={
 *           @StringParam(name="boost",value=1.0)
 *       }
 *    )
 *    private String data;
 *		
 *    @Synthetic("myField")
 *    @Mapping(
 *       stringParams={
 *           @StringParam(name="store",value="yes")
 *       }
 *    )
 *    public int getSynteticField(){
 *        return (int) (data*100);
 *    }
 *		
 * }
 * </pre>
 * The basic information about ES-mappings can be found <a href="http://www.elasticsearch.org/guide/reference/mapping/core-types.html">here</a>.
 * @author Artyomcool
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD,ElementType.METHOD})
public @interface Mapping {

	StringParam[] stringParams() default {};
	
	BooleanParam[] booleanParams() default {};
	
	IntParam[] intParams() default {};
	
	DoubleParam[] doubleParams() default {};
	
}
