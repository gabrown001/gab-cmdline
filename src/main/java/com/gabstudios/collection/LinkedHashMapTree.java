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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * This is a Tree data structure that holds a value. The value may be null. A node can have 1 to many children.
 * Duplicate siblings with the same value are not allowed. The order of when a child is added is maintained. A tree may
 * hold onto multiple nodes with the same data. A Node uses a hashmap to hold its children so the search is O(1).
 *
 * @author Gregory Brown (sysdevone)
 *
 * @param <T>
 *            This defines the class type of the data.
 */
public class LinkedHashMapTree<T> {

    /**
     * The node within a Tree.
     *
     * @author Gregory Brown (sysdevone)
     *
     * @param <T>
     *            This defines the class type of the data.
     */
    public static class Node<T> {
        /*
         * Holds the child nodes of this node. Will not hold a duplicate child.
         */
        private final java.util.LinkedHashMap<T, Node<T>> _children;

        /*
         * The data held in this node. May be null.
         */
        private final T _data;

        /*
         * The parent node to this node. May be null if this node is the root.
         */
        private Node<T> _parent;

        /*
         * The tree that this node belongs too.
         */
        private final LinkedHashMapTree<T> _tree;

        /**
         * Constructor to create a node in the Tree.
         *
         * @param tree
         *            The <code>Tree</code> instance that created the node.
         * @param data
         *            The data bound to the node.
         */
        protected Node(final LinkedHashMapTree<T> tree, final T data) {
            assert (tree != null) : "Not able to create Node.  The parameter 'tree' should not be null.";
            assert (data != null) : "Not able to create Node.  The parameter 'data' should not be null.";

            this._tree = tree;
            this._data = data;
            this._children = new java.util.LinkedHashMap<T, Node<T>>();
        }

        /**
         * Adds a child to the node.
         *
         * @param data
         *            Is an object instance held in the Node.
         *
         * @return A <code>Node</code> instance that was created. If the data is already a child of this node, then null
         *         is returned.
         */
        public Node<T> addChild(final T data) {
            if (data == null)
                throw new IllegalArgumentException("data cannot be null");

            Node<T> node = null;
            if (!this.containsChild(data)) {
                node = this._tree.createNode(data);
                node.setParent(this);
                this._children.put(node.getData(), node);
            }
            return (node);
        }

        /**
         * Determines if data is a child of the node.
         *
         * @param data
         *            Is an object instance held in the Node.
         *
         * @return A boolean value that is true if the data is a child. Otherwise, false is returned.
         */
        public boolean containsChild(final T data) {
            if (data == null)
                throw new IllegalArgumentException("data cannot be null");
            return (this._children.containsKey(data));
        }

        /**
         * Gets the node associated with the data.
         *
         * @param data
         *            Is an object instance held in the Node.
         *
         * @return A <code>Node</code> instance.
         */
        public Node<T> getChild(final T data) {
            if (data == null)
                throw new IllegalArgumentException("data cannot be null");
            return (this._children.get(data));
        }

        /*
         * Gets the count count from this node down.
         * @return An integer number of 0 to n.
         */
        private int getChildCount() {
            int count = 0;

            if (this.isLeaf()) {
                count = 0;
            } else {
                count = this.getChildren().size();
                final List<Node<T>> childNodes = this.getChildren();
                for (final Node<T> childNode : childNodes) {
                    count += childNode.getChildCount();
                }
            }
            return (count);
        }

        /**
         * Gets the children added to this node.
         *
         * @return A <code>List</code> instance containing 0 to n <code>Node</code> instances.
         */
        public List<Node<T>> getChildren() {
            final Collection<Node<T>> childNodes = this._children.values();
            final List<Node<T>> children = new ArrayList<Node<T>>(childNodes);
            return (children);
        }

        /**
         * Gets the data bound to the node.
         *
         * @return The data held in the node.
         */
        public T getData() {
            return (this._data);
        }

        /*
         * Gets the height of the tree based on this node. Uses recursion.
         */
        private int getHeight() {
            int height = 0;

            if (this.isLeaf()) {
                height = 0;
            } else {
                int maxHeight = 0;
                final List<Node<T>> childNodes = this.getChildren();
                for (final Node<T> childNode : childNodes) {
                    height = 1 + childNode.getHeight();
                    if (height > maxHeight) {
                        maxHeight = height;
                    }
                }
                height = maxHeight;
            }
            return (height);

        }

        /*
         * A helper method to walk through the nodes finding the leaf data.
         */
        List<T> getLeafData(final List<T> data) {
            assert (data != null) : "Not able to get Leaf Node.  The parameter 'data' should not be null.";
            if (this.isLeaf()) {
                data.add(this._data);
            } else {
                final List<Node<T>> childNodes = this.getChildren();
                for (final Node<T> childNode : childNodes) {
                    childNode.getLeafData(data);
                }
            }
            return (data);
        }

        /**
         * Gets the parent of this node.
         *
         * @return A <code>Node</code> instance that is the parent of this node. May be null if this node is the root of
         *         the tree.
         */
        public Node<T> getParent() {
            return (this._parent);
        }

        /**
         * A method to determine if the node has data. A node is empty if it does not have any data. The data is null.
         *
         * @return A boolean value. Will return true if the data is empty, otherwise it will be false.
         */
        public boolean isEmpty() {
            return (this._data == null);
        }

        /**
         * A method to determine if the node is a leaf. A node is a leaf if it does not have any children.
         *
         * @return A boolean value. Will return true if the node is a leaf, otherwise it will be false.
         */
        public boolean isLeaf() {
            return (this._children.isEmpty());
        }

        /**
         * A method to determine if the node is the root. A node is the root if it does not have a parent. A Tree can
         * have only one root node.
         *
         * @return A boolean value. Will return true if the node is the root., otherwise it will be false.
         */
        public boolean isRoot() {
            final Node<T> root = this._tree.getRoot();
            return (root.equals(this));
        }

        /**
         * Removes a child from the node. The complete subtree is removed. The node that was removed is returned so that
         * additional processing can be performed.
         *
         * @param data
         *            The data to remove.
         *
         * @return The <code>Node</code> instance that was removed. May be null if not found.
         */
        public Node<T> removeChild(final T data) {

            if (data == null)
                throw new IllegalArgumentException("data cannot be null");

            final Node<T> removedNode = this._children.remove(data);

            // clear parent on node that is removed.
            if (removedNode != null) {
                removedNode.setParent(null);
            }

            return (removedNode);

        }

        /**
         * Removes all children from the current node. Child nodes will be lost.
         */
        public void removeChildren() {
            this._children.clear();
        }

        /*
         * Sets the parent of the node. This is called when the addChild method is called.
         */
        private void setParent(final Node<T> parent) {
            assert (parent != null) : "The parameter 'parent' should not be null.";
            assert (parent.getData() != null) : "The parameter parent's data should not be null.";
            if (this._parent != null) {
                this._parent.removeChild(this._data);
            }
            this._parent = parent;
        }
    }

    /*
     * The root of the tree. Should not be null.
     */
    private final Node<T> _root;

    /**
     * Constructor of the Tree. A Tree must have one <code>Node</code> instance that is the root.
     *
     * @param data
     *            The data of the root <code>Node</code> instance. May be null.
     */
    public LinkedHashMapTree(final T data) {
        if (data == null)
            throw new IllegalArgumentException("data cannot be null");
        final Node<T> root = this.createNode(data);
        this._root = root;
    }

    /**
     * Adds a child to the root <code>Node</code> instance. This is a helper method that performs the same action as
     * getting the root node and adding a child.
     *
     * @param data
     *            The data of the new <code>Node</code> instance. May be null.
     *
     * @return The <code>Node</code> instance that was created.
     */
    public Node<T> addChild(final T data) {
        if (data == null)
            throw new IllegalArgumentException("data cannot be null");
        final Node<T> node = this._root.addChild(data);
        return (node);
    }

    /*
     * A factory helper method that creates the <code>Node</code> implementation.
     * @return The <code>Node</code> instance that was created.
     */
    protected Node<T> createNode(final T data) {
        assert (data != null) : "Not able to create Node.  The parameter 'data' should not be null.";
        final Node<T> node = new Node<T>(this, data);
        return (node);
    }

    /**
     * Gets the height of the Tree.
     *
     * @return An integer of 0 to n.
     */
    public int getHeight() {
        return (this._root.getHeight());
    }

    /**
     * Gets all of the data within the leaf nodes. A leaf is a Node that does not have children.
     *
     * @return A <code>List</code> instance containing the data.
     */
    public List<T> getLeafData() {
        final LinkedList<T> data = new LinkedList<T>();
        this._root.getLeafData(data);
        return (data);
    }

    /**
     * Gets the number of nodes contained in this tree. A Tree instance will always have one node that is the root;
     *
     * @return An integer value from 1 to n.
     */
    public int getNodeCount() {
        return (1 + this._root.getChildCount());
    }

    /**
     * Gets the root node of the Tree. A Tree can only have one root.
     *
     * @return The <code>Node</code> instance that is the root.
     */
    public Node<T> getRoot() {
        return (this._root);
    }

    /**
     * Removes a child from the root <code>Node</code> instance. This is a helper method that performs the same action
     * as getting the root node and remove a child.
     *
     * @param data
     *            The data of the <code>Node</code> instance to remove.
     *
     * @return The <code>Node</code> instance that was removed.
     */
    public Node<T> removeChild(final T data) {
        if (data == null)
            throw new IllegalArgumentException("data cannot be null");
        final Node<T> node = this._root.removeChild(data);
        return (node);
    }

}
