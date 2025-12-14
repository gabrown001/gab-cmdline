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
 * This interface handles the <code>Command</code> instances that are processed when the parse(...) method is called.
 *
 * @author Gregory Brown (sysdevone)
 */
public interface CommandListener {
    /**
     * Handles the parser callbacks when a Command is created.
     *
     * @param command
     *            A Command instance.
     */
    public void handle(Command command);
}
