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

/**
 * This is used by the DefinedCommandTokenizer to represent the different types of settings. The types are: COMMAND -
 * The command that will have variables. DESCRIPTION - A description of what the command does. REQUIRED_VALUE = A
 * required variable of the command. OPTIONAL_VALUE = An optional variable of the command. REGEX_VALUE = A regex that
 * will be used to validate the data within a VALUE REQUIRED_LIST_VALUE = A required variable that will have one to many
 * values. OPTIONAL_LIST_VALUE = An optional variable that will have zero to many values.
 *
 * @author Gregory Brown (sysdevone)
 */
public class Token {
    /*
     * The type of token.
     */
    protected enum Type {
        COMMAND, DESCRIPTION, REQUIRED_VALUE, OPTIONAL_VALUE, REGEX_VALUE, REQUIRED_LIST_VALUE, OPTIONAL_LIST_VALUE
    }

    /*
     * The type of token.
     */
    private final Type _type;

    /*
     * The String value of the token.
     */
    private final String _value;

    /*
     * Constructor.
     */
    protected Token(final Type type) {
        assert (type != null) : "The parameter 'type' must not be null";

        this._type = type;
        this._value = null;
    }

    /*
     * Constructor.
     */
    protected Token(final Type type, final String value) {
        assert (type != null) : "The parameter 'type' must not be null";
        assert (value != null && value.length() > 0) : "The parameter 'value' must not be null or empty";

        this._type = type;
        this._value = value;
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
        final Token other = (Token) obj;
        if (this._type != other._type) {
            return false;
        }
        if (this._value == null) {
            if (other._value != null) {
                return false;
            }
        } else if (!this._value.equals(other._value)) {
            return false;
        }
        return true;
    }

    /*
     * Get the type.
     * @return The Type of token.
     */
    protected Type getType() {
        return (this._type);
    }

    /*
     * Get the value of the token.
     * @return A String instance. May not be null or empty.
     */
    protected String getValue() {
        return (this._value);
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((this._type == null) ? 0 : this._type.hashCode());
        result = (prime * result) + ((this._value == null) ? 0 : this._value.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("Token [_value='%s', _type='%s']", this._value, this._type);
    }

}
