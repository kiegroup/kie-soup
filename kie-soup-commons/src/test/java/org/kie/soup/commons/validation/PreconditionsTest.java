/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.soup.commons.validation;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.kie.soup.commons.validation.PortablePreconditions.checkGreaterOrEqualTo;
import static org.kie.soup.commons.validation.PortablePreconditions.checkGreaterThan;
import static org.kie.soup.commons.validation.Preconditions.checkCondition;
import static org.kie.soup.commons.validation.Preconditions.checkEachParameterNotNull;
import static org.kie.soup.commons.validation.Preconditions.checkNotEmpty;
import static org.kie.soup.commons.validation.Preconditions.checkNotNull;
import static org.kie.soup.commons.validation.Preconditions.checkNullMandatory;

/**
 * Test class for {@link Preconditions}
 */
public class PreconditionsTest {

    private static final String GREATER_THAN_NOT_MET_ERROR = "Parameter named '%s' must be greater than '%s'!";
    private static final String GREATER_OR_EQUAL_TO_NOT_MET_ERROR = "Parameter named '%s' must be greater or equal to '%s'!";
    private static final String PARAMETER_SHOULD_NOT_BE_NULL_ERROR = "Parameter named '%s' should be not null!";

    private static final String NOT_NULLABLE = "notNullable";
    private static final String NON_NULL_VALUE = "nonNullValue";

    @Test
    public void shouldDoNotThrowExceptionWhenGettingNotEmptyArray() {
        checkNotEmpty("notEmpty",
                      new Object[]{1, 2, 3});
    }

    @Test
    public void shouldDoNotThrowExceptionWhenGettingNotEmptyParameter() {
        checkNotEmpty("notEmpty",
                      "notEmpty");
    }

    @Test
    public void shouldDoNotThrowExceptionWhenGettingNotNullParameter() {
        checkNotNull("notNullable",
                     "notNullValue");
    }

    @Test
    public void shouldDoNotThrowExceptionWhenGettingNullParameter() {
        checkNullMandatory("nullable",
                           null);
    }

    @Test
    public void shouldDoNotThrowExceptionWhenGettingValidConditionParameter() {
        checkCondition("valid",
                       true);
    }

    @Test
    public void shouldDoNotThrowExceptionWhenGettinOnlyNonNullParameters() {
        checkEachParameterNotNull("notNullable",
                                  "nonNull");
        checkEachParameterNotNull("notNullable",
                                  "nonNull",
                                  "anotherNonNull");
    }

    @Test
    public void shouldGetCorrectErrorNessage() {
        try {
            checkNotNull("notNullable",
                         null);
        } catch (final IllegalArgumentException e) {
            assertThat(e.getMessage(),
                       is("Parameter named 'notNullable' should be not null!"));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionOnCheckEmptyWhenGettingNullParameter() {
        checkNotEmpty("notEmpty",
                      (String) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenGettinAllNullParameter() {
        checkEachParameterNotNull("notNullable",
                                  (Object) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenGettingEmptyArray() {
        checkNotEmpty("empty",
                      new Object[]{});
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenGettingEmptyParameter() {
        checkNotEmpty("notEmpty",
                      "");
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionWhenGettingInvalidConditionParameter() {
        checkCondition("valid",
                       false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenGettingNonNullParameter() {
        checkNullMandatory("nullable",
                           "non null");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenGettingNullArray() {
        checkNotEmpty("empty",
                      (Object[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenGettingNullParameter() {
        checkNotNull("notNullable",
                     null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenGettingSpacedParameter() {
        checkNotEmpty("notEmpty",
                      "    ");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenGettinOneNullParameter() {
        checkEachParameterNotNull("notNullable",
                                  "nonNull",
                                  null);
    }

    @Test
    public void checkGreaterThanSuccessful() {
        checkGreaterThan(NOT_NULLABLE, 1, 0);
    }

    @Test
    public void checkGreaterThanNotMetWhenEqual() {
        checkGreaterThanNotMet(1, 1);
    }

    @Test
    public void checkGreaterThanNotMetWhenLower() {
        checkGreaterThanNotMet(1, 2);
    }

    private <T> void checkGreaterThanNotMet(Comparable<T> param, T nonNullValue) {
        assertThatThrownBy(() -> checkGreaterThan(NOT_NULLABLE,
                                                  param,
                                                  nonNullValue))
                .hasMessageStartingWith(String.format(GREATER_THAN_NOT_MET_ERROR, NOT_NULLABLE, nonNullValue));
    }

    @Test
    public void checkGreaterThanWhenNullParam() {
        assertThatThrownBy(() -> checkGreaterThan(NOT_NULLABLE,
                                                  null,
                                                  1))
                .hasMessageStartingWith(String.format(PARAMETER_SHOULD_NOT_BE_NULL_ERROR, NOT_NULLABLE));
    }

    @Test
    public void checkGreaterThanWhenNullValueParam() {
        assertThatThrownBy(() -> checkGreaterThan(NOT_NULLABLE,
                                                  1,
                                                  null))
                .hasMessageStartingWith(String.format(PARAMETER_SHOULD_NOT_BE_NULL_ERROR, NON_NULL_VALUE));
    }

    @Test
    public void checkGreaterOrEqualToWhenEqualSuccessful() {
        checkGreaterOrEqualTo(NOT_NULLABLE, 1, 1);
    }

    @Test
    public void checkGreaterOrEqualToWhenGreaterSuccessful() {
        checkGreaterOrEqualTo(NOT_NULLABLE, 1, 0);
    }

    @Test
    public void checkGreaterOrEqualToNotMet() {
        assertThatThrownBy(() -> checkGreaterOrEqualTo(NOT_NULLABLE,
                                                       0,
                                                       1))
                .hasMessageStartingWith(String.format(GREATER_OR_EQUAL_TO_NOT_MET_ERROR, NOT_NULLABLE, 1));
    }

    @Test
    public void checkGreaterOrEqualToWhenNullParam() {
        assertThatThrownBy(() -> checkGreaterOrEqualTo(NOT_NULLABLE,
                                                       null,
                                                       1))
                .hasMessageStartingWith(String.format(PARAMETER_SHOULD_NOT_BE_NULL_ERROR, NOT_NULLABLE));
    }

    @Test
    public void checkGreaterOrEqualToWhenNullValueParam() {
        assertThatThrownBy(() -> checkGreaterOrEqualTo(NOT_NULLABLE,
                                                       1,
                                                       null))
                .hasMessageStartingWith(String.format(PARAMETER_SHOULD_NOT_BE_NULL_ERROR, NON_NULL_VALUE));
    }
}