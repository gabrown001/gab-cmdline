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

import java.util.ArrayList;
import java.util.List;

/**
 * This class is a command definition. It is created when the CmdLine.defineCommand() is called.
 *
 * @author Gregory Brown (sysdevone)
 */
public class CommandDefinition {
    protected String _description;
    protected List<String> _names;
    protected String _optionalVariableListName;
    protected List<String> _optionalVariables;
    protected String _regexValue;
    protected String _requiredVariableListName;
    protected List<String> _requiredVariables;

    /*
     * Constructor
     */
    protected CommandDefinition() {
        this._names = new ArrayList<>();
        this._requiredVariables = new ArrayList<>();
        this._optionalVariables = new ArrayList<>();
    }

    protected String getDescription() {
        return (this._description);
    }

    protected String getRegexValue() {
        return (this._regexValue);
    }

    protected void addName(final String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        this._names.add(name);
    }

    protected void addOptionalVariable(final String name) {
        this._optionalVariables.add(name);
    }

    protected void setOptionalVariableList(final String name) {
        this._optionalVariableListName = name;
    }

    protected void addRequiredVariable(final String name) {
        this._requiredVariables.add(name);
    }

    protected void setRequiredVariableList(final String name) {
        this._requiredVariableListName = name;
    }

    protected List<String> getOptionalVariableNames() {
        return (this._optionalVariables);
    }

    protected String getOptionalVariableListName() {
        return (this._optionalVariableListName);
    }

    protected List<String> getRequiredVariableNames() {
        return (this._requiredVariables);
    }

    protected String getRequiredVariableListName() {
        return (this._requiredVariableListName);
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        final CommandDefinition other = (CommandDefinition) obj;
        if (this._names == null) {
            if (other._names != null) {
                return false;
            }
        } else if (!this._names.equals(other._names)) {
            return false;
        }
        return true;
    }

    protected List<String> getNames() {
        return (this._names);
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((this._names == null) ? 0 : this._names.hashCode());
        return result;
    }

    protected boolean hasOptionalVariableLists() {
        return (this._optionalVariableListName != null && this._optionalVariableListName.length() > 0);
    }

    protected boolean hasOptionalVariables() {
        return (!this._optionalVariables.isEmpty());
    }

    protected boolean hasRequiredVariableLists() {
        return (this._requiredVariableListName != null && this._requiredVariableListName.length() > 0);
    }

    protected boolean hasRequiredVariables() {
        return (!this._requiredVariables.isEmpty());
    }

    protected void setDescription(final String description) {
        if (description == null || description.isEmpty()) {
            throw new IllegalArgumentException("Description cannot be null or empty");
        }
        this._description = description;
    }

    protected void setRegexValue(final String regexValue) {
        if (regexValue == null || regexValue.isEmpty()) {
            throw new IllegalArgumentException("Regex value cannot be null or empty");
        }
        this._regexValue = regexValue;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format(
                "CommandDefinition [_description=%s, _names=%s, _optionalVariableListName=%s, _optionalVariables=%s, _regexValue=%s, _requiredVariableListName=%s, _requiredVariables=%s]",
                this._description, this._names, this._optionalVariableListName, this._optionalVariables,
                this._regexValue, this._requiredVariableListName, this._requiredVariables);
    }

}
