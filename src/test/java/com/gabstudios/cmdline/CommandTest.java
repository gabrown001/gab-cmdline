/*****************************************************************************************
 *
 * Copyright 2016-2025 Gregory Brown. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 *****************************************************************************************
 */

package com.gabstudios.cmdline;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the {@link Command} class.
 */
public class CommandTest {

    // -----------------------------------------------------------------------
    // Positive tests
    // -----------------------------------------------------------------------

    @Test
    public void testGetValuesReturnsEmptyListForUnknownVariable() {
        final Command command = new Command("test");
        final List<String> values = command.getValues("nonexistent");
        Assertions.assertNotNull(values);
        Assertions.assertTrue(values.isEmpty());
    }

    @Test
    public void testAddVariableAccumulatesMultipleValuesForSameKey() {
        final Command command = new Command("test");
        command.addVariable("key", "value1");
        command.addVariable("key", "value2");
        final List<String> values = command.getValues("key");
        Assertions.assertEquals(2, values.size());
        Assertions.assertTrue(values.contains("value1"));
        Assertions.assertTrue(values.contains("value2"));
    }

    @Test
    public void testEqualCommandsHaveSameHashCode() {
        final Command cmd1 = new Command("cmd");
        final Command cmd2 = new Command("cmd");
        Assertions.assertEquals(cmd1, cmd2);
        Assertions.assertEquals(cmd1.hashCode(), cmd2.hashCode());
    }

    @Test
    public void testCommandsWithDifferentNamesAreNotEqual() {
        final Command cmd1 = new Command("cmd1");
        final Command cmd2 = new Command("cmd2");
        Assertions.assertNotEquals(cmd1, cmd2);
    }

    @Test
    public void testHasVariablesReturnsFalseWhenNoVariablesAdded() {
        final Command command = new Command("test");
        Assertions.assertFalse(command.hasVariables());
    }

    // -----------------------------------------------------------------------
    // Negative tests
    // -----------------------------------------------------------------------

    @Test
    public void testAddVariableNullNameThrowsIllegalArgumentException() {
        final Command command = new Command("test");
        Assertions.assertThrows(IllegalArgumentException.class, () -> command.addVariable(null, "value"));
    }

    @Test
    public void testAddVariableEmptyNameThrowsIllegalArgumentException() {
        final Command command = new Command("test");
        Assertions.assertThrows(IllegalArgumentException.class, () -> command.addVariable("", "value"));
    }

    @Test
    public void testAddVariableNullValueThrowsIllegalArgumentException() {
        final Command command = new Command("test");
        Assertions.assertThrows(IllegalArgumentException.class, () -> command.addVariable("key", null));
    }

    @Test
    public void testAddVariableEmptyValueThrowsIllegalArgumentException() {
        final Command command = new Command("test");
        Assertions.assertThrows(IllegalArgumentException.class, () -> command.addVariable("key", ""));
    }
}
