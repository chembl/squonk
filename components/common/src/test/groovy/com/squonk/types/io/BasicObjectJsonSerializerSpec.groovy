package com.squonk.types.io

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.cfg.ContextAttributes
import com.fasterxml.jackson.databind.module.SimpleModule
import com.im.lac.types.BasicObject
import com.squonk.types.io.JsonHandler
import spock.lang.Specification

/**
 *
 * @author timbo
 */
class BasicObjectJsonSerializerSpec extends Specification {
	
    void "test simple marshal"() {
        
        BasicObject bo = new BasicObject([one:1,two:2])
        
        ObjectMapper mapper = new ObjectMapper()
        SimpleModule module = new SimpleModule()
        module.addSerializer(BasicObject.class, new BasicObjectJsonSerializer())
        mapper.registerModule(module)
        
        when:
        String json = mapper.writer().writeValueAsString(bo)
        
        then:
        json != null
        println json
    }
    
    void "test generate mappings"() {
        
        BasicObject bo = new BasicObject([one:1,two:2.2])
        Map mappings = [:]
        
        ObjectMapper mapper = new ObjectMapper()
        SimpleModule module = new SimpleModule()
        module.addSerializer(BasicObject.class, new BasicObjectJsonSerializer())
        mapper.registerModule(module)
        
        when:
        ContextAttributes attrs = ContextAttributes.getEmpty().withSharedAttribute(JsonHandler.ATTR_VALUE_MAPPINGS, mappings)
        String json = mapper.writer().with(attrs).writeValueAsString(bo)
        
        then:
        json != null
        println json
        mappings['one'] == Integer.class
        mappings['two'] == BigDecimal.class

    }
    
    
}

