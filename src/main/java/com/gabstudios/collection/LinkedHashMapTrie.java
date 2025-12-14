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

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

/**
 * An implementation of a Trie. Create a dictionary of words by using the add(String word ) method. This can be used to
 * get words that are similar prefix by using the getWords( String prefix ) method.
 *
 * @author Gregory Brown (sysdevone)
 */
public class LinkedHashMapTrie extends LinkedHashMapTree<Character> implements Trie {

    /**
     * The node within a Tree that holds a <code>Character</code>.
     *
     * @author Gregory Brown (sysdevone)
     */
    public static class TrieNode extends Node<Character> {
        /*
         * A flag to mark that the node is the last character in a chain of letters that form a word.
         */
        private boolean _isWord;

        /**
         * @param tree
         *            The tree that this node is part of.
         * @param data
         *            A <code>Character</code> instance.
         */
        protected TrieNode(final LinkedHashMapTrie tree, final Character data) {
            super(tree, data);
            this._isWord = false;
        }

        /**
         * A flag to determine if this node is the end of a word.
         *
         * @return A boolean value - true if it is the end of a work. Otherwise it is false.
         */
        public boolean isWord() {
            return (this._isWord);
        }

        /*
         * Used to mark a node as the end of a word.
         */
        void markWord() {
            this._isWord = true;
        }

    }

    /**
     * Constructor. The root is the '*' character.
     */
    public LinkedHashMapTrie() {
        super('*');
    }

    /**
     * Used to clear and reset the Trie.
     */
    @Override
    public void clear() {
        Node<Character> root = this.getRoot();
        root.removeChildren();
    }

    /**
     * Add a word to the container that will be used as suggestions.
     *
     * @param word
     *            A <code>String</code> instance. May not be null or empty.
     */
    @Override
    public void add(final String word) {
        if (word == null || word.isEmpty())
            throw new IllegalArgumentException("word cannot be null or empty");

        // TODO - what is the max length of a word?

        final int count = word.length();
        TrieNode node = (TrieNode) this.getRoot();
        for (int i = 0; i < count; ++i) {
            // break the word into characters and add each character to the
            // tree.
            // A character will be a child of the previous character.
            // The complete word is the final leaf.
            final Character character = word.charAt(i);
            if (node.containsChild(character)) {
                // if the character is contained, get that node so that further
                // characters can be added to it.
                node = (TrieNode) node.getChild(character);
            } else {
                // if the character is not contained, then create a new node so
                // that further characters can be added to it.
                node = (TrieNode) node.addChild(character);
            }
        }
        // mark the last node with terminator.
        node.markWord();
    }

    /*
     * A factory helper method that creates the <code>Node</code> implementation.
     * @return The <code>Node</code> instance that was created.
     */
    @Override
    protected TrieNode createNode(final Character data) {
        assert (data != null) : "Not able to create Node.  The parameter 'data' should not be null.";
        final TrieNode node = new TrieNode(this, data);
        return (node);
    }

    /**
     * Gets all of the words that were added.
     *
     * @return A <code>List</code> instance containing zero to many <code>String</code> instances.
     */
    @Override
    public List<String> getWords() {
        return (this.getWords("*"));
    }

    /**
     * Gets all words that start with the prefix.
     *
     * @param prefix
     *            A <code>String</code> instance. May not be null or empty.
     *
     * @return A <code>List</code> instance containing zero to many <code>String</code> instances.
     */
    @Override
    public List<String> getWords(final String prefix) {
        if (prefix == null || prefix.isEmpty())
            throw new IllegalArgumentException("prefix cannot be null or empty");
        // TODO - add a max.

        // walk prefix to known set of nodes.
        // input helo
        // helloworld
        // hello
        // Tests to see if the character exists in the tree.
        // -------------------
        final LinkedList<String> data = new LinkedList<String>();

        final StringBuilder prefixWord = new StringBuilder();
        final int count = prefix.length();
        TrieNode node = (TrieNode) this.getRoot();
        for (int i = 0; i < count; ++i) {
            final char character = prefix.charAt(i);
            if (node.containsChild(character)) {
                // if the character exists, then get that node.
                // continue walking down the tree character by character.
                node = (TrieNode) node.getChild(character);
                prefixWord.append(character);
            } else {
                // if the character is not found. STOP.
                break;
            }
        }

        final Stack<TrieNode> stack = new Stack<>();
        stack.push(node);

        final Stack<String> prefixStack = new Stack<>();
        prefixStack.push(prefixWord.toString());

        while (!stack.isEmpty()) {
            final TrieNode stackNode = stack.pop();
            String rootPrefix = null;
            if (!stackNode.isEmpty()) {
                rootPrefix = prefixStack.pop();
                if (stackNode.isWord()) {
                    data.add(rootPrefix);
                }

            }

            final List<Node<Character>> children = stackNode.getChildren();
            for (final Node<Character> child : children) {
                stack.push((TrieNode) child);
                prefixStack.push(rootPrefix + child.getData());
            }

        }

        return (data);
    }

    /*
     * (non-Javadoc)
     * @see org.gabsocial.collection.Trie#contains(java.lang.String)
     */
    @Override
    public boolean contains(String word) {
        if (word == null || word.isEmpty())
            throw new IllegalArgumentException("word cannot be null or empty");

        boolean isContained = false;

        final int count = word.length();
        TrieNode node = (TrieNode) this.getRoot();
        for (int i = 0; i < count; ++i) {
            final char character = word.charAt(i);
            if (node.containsChild(character)) {
                // if the character exists, then get that node.
                // continue walking down the tree character by character.
                node = (TrieNode) node.getChild(character);
                isContained = node._isWord;
            } else {
                // if the character is not found. STOP.
                isContained = false;
                break;
            }
        }

        return (isContained);
    }
}
