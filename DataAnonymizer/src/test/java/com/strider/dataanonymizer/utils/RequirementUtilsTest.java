/*
 *
 * Copyright 2014, Armenak Grigoryan, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 */
package com.strider.dataanonymizer.utils;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import com.strider.dataanonymizer.requirement.Column;
import com.strider.dataanonymizer.requirement.Key;
import com.strider.dataanonymizer.requirement.Parameter;
import com.strider.dataanonymizer.requirement.Requirement;
import com.strider.dataanonymizer.requirement.Table;

/**
 * Unit test to test requirement related utility methods
 *
 * @author Matthew Eaton
 */
public class RequirementUtilsTest extends TestCase {
    private Requirement requirement = null;

    // Setup load-related tests
    private void load() throws Exception {
        load(getClass().getResource("/Requirement.xml").getFile());
    }
    private void load(String fname) throws Exception {
        requirement = RequirementUtils.load(fname);
        assertNotNull(requirement);
    }
    
    // Doesn't tests a reading a writing a simple requirement
    public void testSimpleWriteRead() throws Exception {
        // create
        Requirement req = new Requirement();
        req.setClient("Unit test client.");
        req.setVersion("X.X");
        
        Table table = new Table();
        table.setName("Table name");
        table.setPkey("pKey");
        
        Column col = new Column();
        col.setName("cName");
        col.setFunction("some.function()");
        
        Parameter param = new Parameter();
        param.setName("pName");
        param.setValue("pvalue/file.txt");
        param.setType("String");
        
        col.setParamters(Arrays.asList(param));
        table.setColumns(Arrays.asList(col));
        req.setTables(Arrays.asList(table));
        
        String fName = "target/test-classes/utest-req-write.xml";
        // write
        RequirementUtils.write(req, fName);
        
        // read and test
        load(fName);
        assertEquals("Unit test client.", requirement.getClient());
        assertEquals("X.X", requirement.getVersion());
        assertEquals(1, requirement.getTables().size());
        Table t1 = requirement.getTables().get(0);
        assertEquals("Table name", t1.getName());
        assertEquals("pKey", t1.getPKey());
        assertEquals(1, t1.getColumns().size());
        Column c1 = t1.getColumns().get(0);
        assertEquals("cName", c1.getName());
        assertEquals("some.function()", c1.getFunction());
        assertEquals(1, c1.getParameters().size());
        Parameter p1 = c1.getParameters().get(0);
        assertEquals("pName", p1.getName());
        assertEquals("pvalue/file.txt", p1.getValue());
        assertEquals("String", p1.getType());
    }

    /**
     * Test loaded requirements from file
     * @throws Exception
     */
    public void testLoad() throws Exception {
        load();
        assertEquals("Test Client", requirement.getClient());
        assertEquals("1.0", requirement.getVersion());
        assertEquals(3, requirement.getTables().size());
        assertEquals("test_table", requirement.getTables().get(0).getName());

        int columnNo = 0;
        for (Column column : requirement.getTables().get(0).getColumns()) {
            assertEquals(("column" + ++columnNo), column.getName());
        }
        
        List<Key> pKeys = requirement.getTables().get(1).getPrimaryKeys();
        assertNotNull(pKeys);
        assertEquals(2, pKeys.size());
        assertEquals("id1", pKeys.get(0).getName());
        assertEquals("id2", pKeys.get(1).getName());

        assertEquals(5, columnNo);
    }

    /**
     * Test getting parameters with name "file"
     * @author Matthew Eaton
     * @throws Exception
     */
    public void testGetFileParameter() throws Exception {
        load();

        int columnNo = 0;
        for (Column column : requirement.getTables().get(0).getColumns()) {
            if ("column1".equals(column.getName()) || "column2".equals(column.getName())) {
                Parameter parameter = RequirementUtils.getFileParameter(column.getParameters());
                assertNotNull(parameter);
                assertEquals(RequirementUtils.PARAM_NAME_FILE, parameter.getName());
            } else {
                Parameter parameter = RequirementUtils.getFileParameter(column.getParameters());
                assertNull(parameter);
            }
            assertEquals(("column" + ++columnNo), column.getName());
        }
    }
    
    public void testGetPrimitiveParameterValues() throws Exception {
        load();

        List<Parameter> params = requirement.getTables().get(2).getColumns().get(0).getParameters();
        assertNotNull(params);
        
        assertEquals(true, (boolean) params.get(0).getTypeValue());
        assertEquals(Boolean.class, params.get(0).getTypeValue().getClass());
        assertEquals(1, (byte) params.get(1).getTypeValue());
        assertEquals(Byte.class, params.get(1).getTypeValue().getClass());
        assertEquals(2, (short) params.get(2).getTypeValue());
        assertEquals(Short.class, params.get(2).getTypeValue().getClass());
        assertEquals('s', (char) params.get(3).getTypeValue());
        assertEquals(Character.class, params.get(3).getTypeValue().getClass());
        assertEquals(-3, (int) params.get(4).getTypeValue());
        assertEquals(Integer.class, params.get(4).getTypeValue().getClass());
        assertEquals(4, (long) params.get(5).getTypeValue());
        assertEquals(Long.class, params.get(5).getTypeValue().getClass());
        assertEquals(0.25f, (float) params.get(6).getTypeValue());
        assertEquals(Float.class, params.get(6).getTypeValue().getClass());
        assertEquals(0.5, (double) params.get(7).getTypeValue());
        assertEquals(Double.class, params.get(7).getTypeValue().getClass());
    }
    
    public void testGetStringArrayParameterValue() throws Exception {
        load();

        List<Parameter> params = requirement.getTables().get(2).getColumns().get(1).getParameters();
        assertNotNull(params);
        
        Object ret = params.get(0).getTypeValue();
        assertNotNull(ret);
        
        String[] arr = (String[]) ret;
        assertEquals(3, arr.length);
        
        assertEquals("column1", arr[0]);
        assertEquals("column2", arr[1]);
        assertEquals("column3", arr[2]);
    }
    
    public void testGetPrimitiveArrayParameterValue() throws Exception {
        load();

        List<Parameter> params = requirement.getTables().get(2).getColumns().get(2).getParameters();
        assertNotNull(params);
        
        Object ret = params.get(0).getTypeValue();
        assertNotNull(ret);
        
        int[] arr = (int[]) ret;
        assertEquals(3, arr.length);
        
        assertEquals(1, arr[0]);
        assertEquals(10, arr[1]);
        assertEquals(-20, arr[2]);
        
        params = requirement.getTables().get(2).getColumns().get(2).getParameters();
        assertNotNull(params);
        
        ret = params.get(1).getTypeValue();
        assertNotNull(ret);
        
        double[] dArr = (double[]) ret;
        assertEquals(3, dArr.length);
        
        assertEquals(1.2, dArr[0]);
        assertEquals(10.5, dArr[1]);
        assertEquals(-20.1, dArr[2]);
    }
}