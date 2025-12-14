/*****************************************************************************************
 *
 * Copyright 2015-2025 Gregory Brown. All Rights Reserved.
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

package com.gabstudios.collection;

import java.util.List;

/**
 * A Trie interface.
 *
 * @author Gregory Brown (sysdevone)
 */
public abstract interface Trie {
    /**
     * Gets words that are a close match to the prefix. If the word dictionary includes: "he", "hello", "helloworld" and
     * the prefix is "hel" or "hel111" then the return list should include "hello" and "helloworld"
     *
     * @param prefix
     *            A <code>String</code>instance. Must not be null or empty.
     *
     * @return A <code>List</code> instance containing a matching set of words. May be empty if words were not found.
     */
    public abstract List<String> getWords(String prefix);

    /**
     * Gets all words contained in the Trie.
     *
     * @return A <code>List</code> instance containing String words. May be empty if words were not found.
     */
    public abstract List<String> getWords();

    /**
     * Determines if a word is contained in the Trie.
     *
     * @param word
     *            The word to find.
     *
     * @return A boolean value of true if it is found, otherwise it is false.F
     */
    public boolean contains(String word);

    /**
     * Adds a String to the Trie.
     *
     * @param word
     *            The word to add to the Trie. Must not be null or empty.
     */
    public void add(String word);

    /**
     * Used to clear and reset the Trie.
     */
    public void clear();
}
