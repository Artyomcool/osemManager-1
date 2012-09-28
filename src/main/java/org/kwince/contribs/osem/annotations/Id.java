package org.kwince.contribs.osem.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * An annotation to point your Id-field.
 * You need to have exactly one field per entity class.
 * If your class extends another class with field annotated with
 * <b>@Id</b> you can't provide another one Id-field.
 * <br>Note: dependent entities can do not have the Id-field.
 * In that case they will be stored as a nested objects.
 *  
 * @author Artyomcool
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Id {
}
