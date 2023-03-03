/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.web.bind;

import org.junit.Test;

import org.springframework.tests.sample.beans.TestBean;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import static org.junit.Assert.*;

/**
 * @author Juergen Hoeller
 * @since 02.05.2003
 */
public class EscapedErrorsTests {

	@Test
	public void testEscapedErrors() {
		TestBean tb = new TestBean();
		tb.setName("empty &");

		Errors errors = new EscapedErrors(new BindException(tb, "tb"));
		errors.rejectValue("name", "NAME_EMPTY &", null, "message: &");
		errors.rejectValue("age", "AGE_NOT_SET <tag>", null, "message: <tag>");
		errors.rejectValue("age", "AGE_NOT_32 <tag>", null, "message: <tag>");
		errors.reject("GENERAL_ERROR \" '", null, "message: \" '");

		assertTrue("Correct errors flag", errors.hasErrors());
		assertEquals("Correct number of errors", 4, errors.getErrorCount());
		assertEquals("Correct object name", "tb", errors.getObjectName());

		assertTrue("Correct global errors flag", errors.hasGlobalErrors());
		assertEquals("Correct number of global errors", 1, errors.getGlobalErrorCount());
		ObjectError globalError = errors.getGlobalError();
		String defaultMessage = globalError.getDefaultMessage();
		assertEquals("Global error message escaped", "message: &quot; &#39;", defaultMessage);
		assertEquals("Global error code not escaped", "GENERAL_ERROR \" '", globalError.getCode());
		ObjectError globalErrorInList = errors.getGlobalErrors().get(0);
		assertEquals("Same global error in list", defaultMessage, globalErrorInList.getDefaultMessage());
		ObjectError globalErrorInAllList = errors.getAllErrors().get(3);
		assertEquals("Same global error in list", defaultMessage, globalErrorInAllList.getDefaultMessage());

		assertTrue("Correct field errors flag", errors.hasFieldErrors());
		assertEquals("Correct number of field errors", 3, errors.getFieldErrorCount());
		assertEquals("Correct number of field errors in list", 3, errors.getFieldErrors().size());
		FieldError fieldError = errors.getFieldError();
		assertEquals("Field error code not escaped", "NAME_EMPTY &", fieldError.getCode());
		assertEquals("Field value escaped", "empty &amp;", errors.getFieldValue("name"));
		FieldError fieldErrorInList = errors.getFieldErrors().get(0);
		assertEquals("Same field error in list", fieldError.getDefaultMessage(), fieldErrorInList.getDefaultMessage());

		assertTrue("Correct name errors flag", errors.hasFieldErrors("name"));
		assertEquals("Correct number of name errors", 1, errors.getFieldErrorCount("name"));
        assertEquals("Correct number of name errors in list", 1, errors.getFieldErrors("name").size());
		FieldError nameError = errors.getFieldError("name");
        assertEquals("Name error message escaped", "message: &amp;", nameError.getDefaultMessage());
        assertEquals("Name error code not escaped", "NAME_EMPTY &", nameError.getCode());
        assertEquals("Name value escaped", "empty &amp;", errors.getFieldValue("name"));
		FieldError nameErrorInList = errors.getFieldErrors("name").get(0);
        assertEquals("Same name error in list", nameError.getDefaultMessage(), nameErrorInList.getDefaultMessage());

		assertTrue("Correct age errors flag", errors.hasFieldErrors("age"));
        assertEquals("Correct number of age errors", 2, errors.getFieldErrorCount("age"));
        assertEquals("Correct number of age errors in list", 2, errors.getFieldErrors("age").size());
		FieldError ageError = errors.getFieldError("age");
        assertEquals("Age error message escaped", "message: &lt;tag&gt;", ageError.getDefaultMessage());
        assertEquals("Age error code not escaped", "AGE_NOT_SET <tag>", ageError.getCode());
        assertEquals("Age value not escaped", (new Integer(0)), errors.getFieldValue("age"));
		FieldError ageErrorInList = errors.getFieldErrors("age").get(0);
        assertEquals("Same name error in list", ageError.getDefaultMessage(), ageErrorInList.getDefaultMessage());
		FieldError ageError2 = errors.getFieldErrors("age").get(1);
        assertEquals("Age error 2 message escaped", "message: &lt;tag&gt;", ageError2.getDefaultMessage());
        assertEquals("Age error 2 code not escaped", "AGE_NOT_32 <tag>", ageError2.getCode());
	}

}
