package com.im.lac.wicket.inmethod;

import java.io.Serializable;

/**
 * @author simetrias
 */
public interface CssProviderHeaderModel extends Serializable {

    String getHeaderCssClassForColumn(String columnId);

}
